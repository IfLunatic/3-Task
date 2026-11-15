package ua.kpi.hpc.mpi;

import ua.kpi.hpc.common.DataGenerator;
import ua.kpi.hpc.task1.SequentialSum;

/**
 * Завдання 1: Розподілене обчислення суми масиву через MPI.
 *
 * Алгоритм:
 *   1. Масив розміром N розподіляється між MPI-процесами (рангами).
 *   2. Кожен процес генерує лише свою частину масиву (localSlice).
 *   3. Кожен процес обчислює локальну суму своєї частини.
 *   4. MPI_Reduce збирає всі локальні суми на root-процесі (rank 0),
 *      складаючи їх (операція MPI_SUM).
 *
 * Запуск:
 *   mpirun -np 4 java -cp target/hpc-lab-1.0.jar ua.kpi.hpc.MpiTask1Sum 10000000
 */
public final class MpiTask1Sum {

    public static void main(String[] args) {
        // Розмір масиву за замовчуванням — 100 мільйонів
        int arraySize = 100_000_000;
        if (args.length > 0) {
            try {
                arraySize = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        // Ініціалізуємо MPI та отримуємо rank і size
        try (JavaMPI mpi = JavaMPI.getInstance()) {
            mpi.init();
            int rank = mpi.getRank(); // номер поточного процесу
            int size = mpi.getSize(); // загальна кількість процесів

            // Розподіляємо масив між процесами рівномірно
            int baseChunk = arraySize / size;     // базовий розмір частини
            int remainder = arraySize % size;     // залишок

            // Обчислюємо початок і розмір частини для поточного процесу
            int start = rank * baseChunk + Math.min(rank, remainder);
            int chunkSize = baseChunk + (rank < remainder ? 1 : 0);
            int end = start + chunkSize;

            // Генеруємо лише свою локальну частину масиву (не весь масив!)
            long[] localSlice = new long[chunkSize];
            for (int i = 0; i < chunkSize; i++) {
                localSlice[i] = ((long) (start + i) % 100) + 1;
            }

            // Синхронізуємо всі процеси перед початком вимірювання
            mpi.barrier();
            double startTime = mpi.wtime();

            // Обчислюємо локальну суму своєї частини
            long localSum = SequentialSum.compute(localSlice);

            // MPI_Reduce: збираємо суми з усіх процесів на root (rank 0)
            long totalSum = mpi.reduceSum(localSum, 0);

            // Синхронізуємо та фіксуємо час
            mpi.barrier();
            double endTime = mpi.wtime();
            double elapsedMs = (endTime - startTime) * 1000.0;

            // Тільки root-процес виводить результат
            if (rank == 0) {
                long expected = DataGenerator.expectedSum(arraySize);
                boolean valid = (totalSum == expected);
                System.out.printf("[MPI Task 1] Size: %d | Ranks: %d | Sum: %d | Valid: %s | Time: %.3f ms%n",
                    arraySize, size, totalSum, valid, elapsedMs);
            }
        }
    }
}
