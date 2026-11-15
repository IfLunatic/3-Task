package ua.kpi.hpc.mpi;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * Обгортка (wrapper) для роботи з MPI у Java через Foreign Function & Memory (FFM) API.
 *
 * FFM API дозволяє викликати нативні C-функції MPI напряму з Java,
 * без JNI та без зовнішніх бібліотек.
 *
 * Підтримує:
 *   — Open MPI (Linux/WSL) — завантажує libmpi.so
 *   — Microsoft MPI (Windows) — завантажує msmpi.dll
 *
 * Основні MPI-операції: Init, Finalize, Comm_rank, Comm_size,
 * Barrier, Wtime, Reduce (SUM), Send, Recv.
 *
 * Реалізує AutoCloseable — можна використовувати у try-with-resources.
 */
public final class JavaMPI implements AutoCloseable {

    // Єдиний екземпляр (Singleton)
    private static JavaMPI instance;

    private final Linker linker;       // API для виклику нативних функцій
    private final SymbolLookup mpiLib; // завантажена MPI-бібліотека
    private final boolean isWindows;   // чи працюємо на Windows

    // Хендли (MethodHandle) для кожної MPI-функції
    private final MethodHandle mpiInit;
    private final MethodHandle mpiFinalize;
    private final MethodHandle mpiCommRank;
    private final MethodHandle mpiCommSize;
    private final MethodHandle mpiBarrier;
    private final MethodHandle mpiWtime;
    private final MethodHandle mpiReduce;
    private final MethodHandle mpiSend;
    private final MethodHandle mpiRecv;

    // MPI-константи: комунікатор, тип даних, операція
    private final MemorySegment commWorld;    // MPI_COMM_WORLD
    private final MemorySegment longLongType; // MPI_LONG_LONG
    private final MemorySegment opSum;        // MPI_SUM

    private int rank;                // номер поточного процесу
    private int size;                // загальна кількість процесів
    private boolean initialized = false;

    /**
     * Приватний конструктор: завантажує MPI-бібліотеку та прив'язує функції.
     */
    private JavaMPI() {
        this.linker = Linker.nativeLinker();
        String os = System.getProperty("os.name").toLowerCase();
        this.isWindows = os.contains("win");

        if (isWindows) {
            // Завантажуємо Microsoft MPI (msmpi.dll)
            this.mpiLib = SymbolLookup.libraryLookup("msmpi.dll", Arena.global());
            // MS-MPI використовує числові константи для комунікатора, типу та операції
            this.commWorld = MemorySegment.ofAddress(0x44000000L);
            this.longLongType = MemorySegment.ofAddress(0x4c000809L);
            this.opSum = MemorySegment.ofAddress(0x58000003L);
        } else {
            // Завантажуємо Open MPI (libmpi.so)
            this.mpiLib = SymbolLookup.libraryLookup("libmpi.so", Arena.global());
            // Open MPI експортує константи як глобальні символи
            this.commWorld = mpiLib.find("ompi_mpi_comm_world").orElseThrow();
            this.longLongType = mpiLib.find("ompi_mpi_long_long_int").orElseThrow();
            this.opSum = mpiLib.find("ompi_mpi_op_sum").orElseThrow();
        }

        try {
            // Прив'язуємо кожну MPI-функцію через FFM API
            this.mpiInit = linker.downcallHandle(
                mpiLib.find("MPI_Init").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );
            this.mpiFinalize = linker.downcallHandle(
                mpiLib.find("MPI_Finalize").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
            );
            this.mpiCommRank = linker.downcallHandle(
                mpiLib.find("MPI_Comm_rank").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );
            this.mpiCommSize = linker.downcallHandle(
                mpiLib.find("MPI_Comm_size").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );
            this.mpiBarrier = linker.downcallHandle(
                mpiLib.find("MPI_Barrier").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );
            this.mpiWtime = linker.downcallHandle(
                mpiLib.find("MPI_Wtime").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE)
            );
            this.mpiReduce = linker.downcallHandle(
                mpiLib.find("MPI_Reduce").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );
            this.mpiSend = linker.downcallHandle(
                mpiLib.find("MPI_Send").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );
            this.mpiRecv = linker.downcallHandle(
                mpiLib.find("MPI_Recv").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося прив'язати нативні MPI-функції: " + e.getMessage(), e);
        }
    }

    /**
     * Повертає єдиний екземпляр JavaMPI (Singleton).
     */
    public static synchronized JavaMPI getInstance() {
        if (instance == null) {
            instance = new JavaMPI();
        }
        return instance;
    }

    /**
     * Ініціалізує MPI-середовище (MPI_Init), визначає rank та size.
     */
    public void init() {
        if (initialized) return;
        try {
            // Викликаємо MPI_Init(NULL, NULL) — ініціалізація без аргументів
            mpiInit.invoke(MemorySegment.NULL, MemorySegment.NULL);

            // Визначаємо номер поточного процесу (rank) і загальну кількість (size)
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment rankSeg = arena.allocate(ValueLayout.JAVA_INT);
                MemorySegment sizeSeg = arena.allocate(ValueLayout.JAVA_INT);
                mpiCommRank.invoke(commWorld, rankSeg);
                mpiCommSize.invoke(commWorld, sizeSeg);
                this.rank = rankSeg.get(ValueLayout.JAVA_INT, 0);
                this.size = sizeSeg.get(ValueLayout.JAVA_INT, 0);
            }
            this.initialized = true;
        } catch (Throwable t) {
            throw new RuntimeException("MPI_Init не вдалося: " + t.getMessage(), t);
        }
    }

    /** Повертає номер (rank) поточного процесу. */
    public int getRank() {
        return rank;
    }

    /** Повертає загальну кількість процесів (size). */
    public int getSize() {
        return size;
    }

    /**
     * Повертає поточний час (wall-clock) у секундах (MPI_Wtime).
     * Використовується для вимірювання часу виконання.
     */
    public double wtime() {
        try {
            return (double) mpiWtime.invoke();
        } catch (Throwable t) {
            return System.nanoTime() / 1e9; // запасний варіант
        }
    }

    /**
     * Бар'єр синхронізації: блокує, поки ВСІ процеси не досягнуть цієї точки (MPI_Barrier).
     */
    public void barrier() {
        try {
            mpiBarrier.invoke(commWorld);
        } catch (Throwable t) {
            throw new RuntimeException("MPI_Barrier не вдалося", t);
        }
    }

    /**
     * Колективна операція MPI_Reduce з операцією SUM.
     * Кожен процес передає своє localVal, і процес з номером root
     * отримує загальну суму всіх значень.
     *
     * @param localVal локальне значення поточного процесу
     * @param root     номер процесу, який отримає результат
     * @return загальна сума (тільки на root-процесі; інші отримують 0)
     */
    public long reduceSum(long localVal, int root) {
        try (Arena arena = Arena.ofConfined()) {
            // Виділяємо пам'ять для відправки та отримання
            MemorySegment sendBuf = arena.allocateFrom(ValueLayout.JAVA_LONG, localVal);
            MemorySegment recvBuf = arena.allocate(ValueLayout.JAVA_LONG);
            mpiReduce.invoke(sendBuf, recvBuf, 1, longLongType, opSum, root, commWorld);
            if (rank == root) {
                return recvBuf.get(ValueLayout.JAVA_LONG, 0);
            }
            return 0L;
        } catch (Throwable t) {
            throw new RuntimeException("MPI_Reduce не вдалося", t);
        }
    }

    /**
     * Надсилає одне значення long до іншого процесу (MPI_Send).
     *
     * @param value    значення для відправки
     * @param destRank номер процесу-отримувача
     * @param tag      тег повідомлення (для ідентифікації)
     */
    public void sendLong(long value, int destRank, int tag) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment sendBuf = arena.allocateFrom(ValueLayout.JAVA_LONG, value);
            mpiSend.invoke(sendBuf, 1, longLongType, destRank, tag, commWorld);
        } catch (Throwable t) {
            throw new RuntimeException("MPI_Send не вдалося", t);
        }
    }

    /**
     * Отримує одне значення long від іншого процесу (MPI_Recv).
     *
     * @param srcRank номер процесу-відправника
     * @param tag     тег повідомлення
     * @return отримане значення
     */
    public long recvLong(int srcRank, int tag) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment recvBuf = arena.allocate(ValueLayout.JAVA_LONG);
            MemorySegment statusBuf = arena.allocate(64); // структура MPI_Status
            mpiRecv.invoke(recvBuf, 1, longLongType, srcRank, tag, commWorld, statusBuf);
            return recvBuf.get(ValueLayout.JAVA_LONG, 0);
        } catch (Throwable t) {
            throw new RuntimeException("MPI_Recv не вдалося", t);
        }
    }

    /**
     * Завершує MPI-середовище (MPI_Finalize).
     */
    public void finalizeMPI() {
        if (!initialized) return;
        try {
            mpiFinalize.invoke();
            this.initialized = false;
        } catch (Throwable t) {
            throw new RuntimeException("MPI_Finalize не вдалося", t);
        }
    }

    /** AutoCloseable — автоматичне завершення MPI при виході з try-with-resources. */
    @Override
    public void close() {
        finalizeMPI();
    }
}
