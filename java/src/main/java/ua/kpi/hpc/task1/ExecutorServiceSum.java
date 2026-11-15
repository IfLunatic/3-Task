package ua.kpi.hpc.task1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Паралельне обчислення суми масиву за допомогою ExecutorService (пул потоків).
 *
 * Принцип роботи:
 *   1. Створюється фіксований пул потоків (newFixedThreadPool).
 *   2. Масив розбивається на частини (chunks), кожна стає Callable-задачею.
 *   3. Усі задачі відправляються в пул через invokeAll (блокує до завершення всіх).
 *   4. Локальні суми збираються через Future.get() і складаються.
 *
 * Перевага над Thread-based: не треба вручну керувати потоками — пул
 * створює і перевикористовує потоки автоматично.
 */
public final class ExecutorServiceSum {

    private ExecutorServiceSum() {}

    /**
     * Обчислює суму масиву, використовуючи пул потоків ExecutorService.
     *
     * @param array       вхідний масив
     * @param threadCount кількість робочих потоків у пулі
     * @return загальна сума
     * @throws InterruptedException якщо потік був перерваний під час очікування
     * @throws ExecutionException   якщо задача завершилася з помилкою
     */
    public static long compute(long[] array, int threadCount) throws InterruptedException, ExecutionException {
        if (array == null || array.length == 0) {
            return 0L;
        }
        if (threadCount <= 1 || array.length == 1) {
            return SequentialSum.compute(array);
        }

        int actualThreads = Math.min(threadCount, array.length);
        // Створюємо пул з фіксованою кількістю потоків
        ExecutorService executor = Executors.newFixedThreadPool(actualThreads);

        try {
            int n = array.length;
            int baseChunk = n / actualThreads;  // базовий розмір частини
            int remainder = n % actualThreads;  // залишок для рівномірного розподілу

            // Створюємо список Callable-задач — кожна обчислює суму свого діапазону
            List<Callable<Long>> tasks = new ArrayList<>(actualThreads);
            int currentStart = 0;
            for (int i = 0; i < actualThreads; i++) {
                final int start = currentStart;
                final int chunkSize = baseChunk + (i < remainder ? 1 : 0);
                final int end = start + chunkSize;
                // Лямбда-вираз: обчислити суму діапазону [start, end)
                tasks.add(() -> SequentialSum.computeRange(array, start, end));
                currentStart = end;
            }

            // invokeAll відправляє всі задачі в пул і блокує, поки всі не завершаться
            List<Future<Long>> futures = executor.invokeAll(tasks);

            // Збираємо результати: кожен Future містить локальну суму
            long totalSum = 0L;
            for (Future<Long> future : futures) {
                totalSum += future.get();
            }

            return totalSum;
        } finally {
            // Завжди завершуємо пул після використання
            executor.shutdown();
        }
    }
}
