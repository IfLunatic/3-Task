package ua.kpi.hpc.task2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Паралельний хвильовий алгоритм здвоювання з використанням ExecutorService.
 *
 * Принцип роботи:
 *   1. Створюється фіксований пул потоків.
 *   2. На кожній хвилі пари розподіляються між задачами (Callable).
 *   3. Задачі відправляються в пул через invokeAll — це блокує головний потік
 *      до завершення ВСІХ задач поточної хвилі (гарантує синхронізацію між хвилями).
 *   4. Після завершення хвилі — зменшується довжина, і цикл повторюється.
 *
 * Оптимізація: якщо кількість пар мала (< actualThreads * 4),
 * хвиля виконується послідовно в головному потоці, щоб уникнути
 * накладних витрат на створення задач.
 */
public final class ExecutorServiceWaveSum {

    private ExecutorServiceWaveSum() {}

    /**
     * Обчислює суму масиву хвильовим методом через ExecutorService.
     *
     * @param array       вхідний масив (буде модифіковано на місці!)
     * @param threadCount кількість потоків у пулі
     * @return загальна сума (array[0])
     * @throws InterruptedException якщо потік був перерваний
     * @throws ExecutionException   якщо задача завершилася з помилкою
     */
    public static long compute(long[] array, int threadCount) throws InterruptedException, ExecutionException {
        if (array == null || array.length == 0) {
            return 0L;
        }
        if (threadCount <= 1 || array.length <= 1) {
            return SequentialWaveSum.compute(array);
        }

        int actualThreads = Math.min(threadCount, array.length / 2);
        if (actualThreads <= 1) {
            return SequentialWaveSum.compute(array);
        }

        // Створюємо пул потоків (один раз для всіх хвиль)
        ExecutorService executor = Executors.newFixedThreadPool(actualThreads);
        try {
            int currentLength = array.length;

            // Цикл хвиль: продовжуємо, поки не залишиться один елемент
            while (currentLength > 1) {
                int pairs = currentLength / 2;

                // Оптимізація: при малій кількості пар — обчислюємо послідовно
                if (pairs < actualThreads * 4) {
                    for (int i = 0; i < pairs; i++) {
                        int partnerIndex = currentLength - 1 - i;
                        array[i] += array[partnerIndex];
                    }
                } else {
                    // Розподіляємо пари між задачами
                    int tasksCount = actualThreads;
                    int baseChunk = pairs / tasksCount;
                    int remainder = pairs % tasksCount;

                    List<Callable<Void>> tasks = new ArrayList<>(tasksCount);
                    int currentStart = 0;
                    final int waveLen = currentLength; // фіксуємо довжину для лямбди

                    for (int t = 0; t < tasksCount; t++) {
                        final int start = currentStart;
                        final int chunkSize = baseChunk + (t < remainder ? 1 : 0);
                        final int end = start + chunkSize;

                        // Задача: обробити пари з індексами [start, end)
                        tasks.add(() -> {
                            for (int i = start; i < end; i++) {
                                int partnerIndex = waveLen - 1 - i;
                                array[i] += array[partnerIndex];
                            }
                            return null;
                        });
                        currentStart = end;
                    }

                    // invokeAll блокує, поки ВСІ задачі не завершаться —
                    // це і є синхронізація між хвилями
                    List<Future<Void>> futures = executor.invokeAll(tasks);
                    // Перевіряємо, чи не було помилок
                    for (Future<Void> future : futures) {
                        future.get();
                    }
                }

                // Зменшуємо довжину для наступної хвилі
                currentLength = (currentLength + 1) / 2;
            }

            return array[0];
        } finally {
            executor.shutdown();
        }
    }
}
