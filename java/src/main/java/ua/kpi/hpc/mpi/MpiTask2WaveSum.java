package ua.kpi.hpc.mpi;

import ua.kpi.hpc.common.DataGenerator;
import ua.kpi.hpc.task2.SequentialWaveSum;

/**
 * Завдання 2: Розподілений хвильовий алгоритм здвоювання через MPI.
 *
 * Алгоритм складається з двох етапів:
 *
 * Етап 1 — Локальна хвиля:
 *   Кожен процес генерує свою частину масиву та обчислює її суму
 *   хвильовим алгоритмом (SequentialWaveSum). Результат — одне число (localSum).
 *
 * Етап 2 — Розподілена хвиля між MPI-рангами:
 *   Процеси працюють як елементи масиву. На кожній хвилі:
 *     — визначається кількість активних рангів (activeRanks);
 *     — пари (rank, partnerRank) обмінюються значеннями через MPI_Send/MPI_Recv;
 *     — процес із меншим rank додає значення партнера до свого;
 *     — MPI_Barrier синхронізує всі процеси перед наступною хвилею;
 *     — activeRanks зменшується: (activeRanks + 1) / 2.
 *   Після всіх хвиль — результат знаходиться на rank 0.
 *
 * Запуск:
 *   mpirun -np 4 java -cp target/hpc-lab-1.0.jar ua.kpi.hpc.MpiTask2WaveSum 10000000
 */
public final class MpiTask2WaveSum {

    public static void main(String[] args) {
        // Розмір масиву за замовчуванням — 100 мільйонів
        int arraySize = 100_000_000;
        if (args.length > 0) {
            try {
                arraySize = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        try (JavaMPI mpi = JavaMPI.getInstance()) {
            mpi.init();
            int rank = mpi.getRank();
            int size = mpi.getSize();

            // Розподіляємо масив між процесами
            int baseChunk = arraySize / size;
            int remainder = arraySize % size;

            int start = rank * baseChunk + Math.min(rank, remainder);
            int chunkSize = baseChunk + (rank < remainder ? 1 : 0);

            // Генеруємо лише свою локальну частину масиву
            long[] localSlice = new long[chunkSize];
            for (int i = 0; i < chunkSize; i++) {
                localSlice[i] = ((long) (start + i) % 100) + 1;
            }

            mpi.barrier();
            double startTime = mpi.wtime();

            // ==== Етап 1: Локальна хвиля — обчислюємо суму своєї частини ====
            long localSum = SequentialWaveSum.compute(localSlice);

            // ==== Етап 2: Розподілена хвиля між MPI-рангами ====
            int activeRanks = size; // кількість активних процесів
            int wave = 0;           // номер хвилі

            while (activeRanks > 1) {
                int pairs = activeRanks / 2;
                int tag = 200 + wave; // унікальний тег для кожної хвилі

                if (rank < pairs) {
                    // Цей процес — «лівий» у парі: отримує значення від партнера
                    int partnerRank = activeRanks - 1 - rank;
                    long partnerVal = mpi.recvLong(partnerRank, tag);
                    localSum += partnerVal;
                } else if (rank >= activeRanks - pairs && rank < activeRanks) {
                    // Цей процес — «правий» у парі: надсилає своє значення партнеру
                    int partnerRank = activeRanks - 1 - rank;
                    mpi.sendLong(localSum, partnerRank, tag);
                }
                // Процеси поза активним діапазоном — не беруть участі

                // Обов'язковий бар'єр між хвилями!
                mpi.barrier();
                activeRanks = (activeRanks + 1) / 2; // зменшуємо кількість активних рангів
                wave++;
            }

            mpi.barrier();
            double endTime = mpi.wtime();
            double elapsedMs = (endTime - startTime) * 1000.0;

            // Тільки root-процес виводить результат
            if (rank == 0) {
                long expected = DataGenerator.expectedSum(arraySize);
                boolean valid = (localSum == expected);
                System.out.printf("[MPI Task 2 Wave] Size: %d | Ranks: %d | Sum: %d | Valid: %s | Time: %.3f ms%n",
                    arraySize, size, localSum, valid, elapsedMs);
            }
        }
    }
}
