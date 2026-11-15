package ua.kpi.hpc.task1;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Паралельне обчислення суми масиву за допомогою ForkJoinPool (механізм «крадіжки роботи»).
 *
 * Принцип роботи:
 *   1. Створюється ForkJoinPool із заданим рівнем паралелізму.
 *   2. Задача SumTask рекурсивно ділить масив навпіл:
 *      — якщо розмір ≤ threshold — обчислює суму послідовно;
 *      — інакше — створює дві підзадачі (ліву і праву).
 *   3. Ліва підзадача відправляється в пул (fork), права обчислюється в поточному потоці.
 *   4. Після завершення обох — результати складаються.
 *
 * Механізм work-stealing: якщо якийсь потік звільнився — він «краде»
 * задачу з черги іншого потоку, що забезпечує рівномірне завантаження.
 */
public final class ForkJoinSum {

    private ForkJoinSum() {}

    /**
     * Рекурсивна задача для Fork/Join: обчислює суму діапазону [start, end).
     * Якщо розмір діапазону ≤ threshold — обчислює послідовно.
     * Інакше — ділить навпіл і створює дві підзадачі.
     */
    private static final class SumTask extends RecursiveTask<Long> {
        private final long[] array;
        private final int start;     // початок діапазону (включно)
        private final int end;       // кінець діапазону (виключно)
        private final int threshold; // мінімальний розмір задачі для послідовного обчислення

        SumTask(long[] array, int start, int end, int threshold) {
            this.array = array;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            // Базовий випадок: діапазон достатньо малий — обчислюємо послідовно
            if (length <= threshold) {
                return SequentialSum.computeRange(array, start, end);
            }

            // Рекурсивний поділ навпіл
            int mid = start + (length / 2);
            SumTask leftTask = new SumTask(array, start, mid, threshold);
            SumTask rightTask = new SumTask(array, mid, end, threshold);

            leftTask.fork();               // відправляємо ліву підзадачу в пул
            long rightResult = rightTask.compute(); // праву обчислюємо в поточному потоці
            long leftResult = leftTask.join();      // чекаємо результат лівої

            return leftResult + rightResult; // об'єднуємо результати
        }
    }

    /**
     * Обчислює суму масиву через ForkJoinPool із заданим рівнем паралелізму.
     *
     * @param array       вхідний масив
     * @param parallelism кількість робочих потоків у ForkJoinPool
     * @return загальна сума
     */
    public static long compute(long[] array, int parallelism) {
        if (array == null || array.length == 0) {
            return 0L;
        }
        if (parallelism <= 1 || array.length == 1) {
            return SequentialSum.compute(array);
        }

        ForkJoinPool customPool = new ForkJoinPool(parallelism);
        try {
            // Адаптивний поріг: створюємо принаймні 4*parallelism підзадач,
            // щоб механізм «крадіжки роботи» (work-stealing) працював ефективно
            int threshold = Math.max(50_000, array.length / (parallelism * 4));
            return customPool.invoke(new SumTask(array, 0, array.length, threshold));
        } finally {
            customPool.shutdown(); // завершуємо пул після використання
        }
    }
}
