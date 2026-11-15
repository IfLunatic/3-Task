package ua.kpi.hpc.task2;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Паралельний хвильовий алгоритм здвоювання з використанням пулу потоків і CyclicBarrier.
 *
 * Принцип роботи:
 *   1. Створюється фіксований набір робочих потоків (workers).
 *   2. Потоки синхронізуються між хвилями за допомогою CyclicBarrier:
 *      — всі потоки виконують свою частину пар поточної хвилі;
 *      — коли всі потоки досягають бар'єру — виконується дія бар'єру
 *        (оновлення currentLength), і потоки переходять до наступної хвилі.
 *   3. Потоки працюють у циклі while(currentLength > 1), що мінімізує
 *      накладні витрати на створення/знищення потоків (вони створюються лише раз).
 *
 * CyclicBarrier — багаторазовий бар'єр: після кожної хвилі він автоматично
 * скидається і може бути використаний знову.
 */
public final class ThreadPoolWaveSum {

    private ThreadPoolWaveSum() {}

    /**
     * Обчислює суму масиву хвильовим методом, використовуючи потоки та CyclicBarrier.
     *
     * @param array       вхідний масив (буде модифіковано на місці!)
     * @param threadCount кількість робочих потоків
     * @return загальна сума (array[0])
     * @throws InterruptedException якщо головний потік був перерваний
     */
    public static long compute(long[] array, int threadCount) throws InterruptedException {
        if (array == null || array.length == 0) {
            return 0L;
        }
        if (threadCount <= 1 || array.length <= 1) {
            return SequentialWaveSum.compute(array);
        }

        // Кількість потоків не може бути більшою за кількість пар
        final int actualThreads = Math.min(threadCount, array.length / 2);
        if (actualThreads <= 1) {
            return SequentialWaveSum.compute(array);
        }

        // Атомарна змінна для поточної довжини масиву (спільна для всіх потоків)
        final AtomicInteger currentLength = new AtomicInteger(array.length);
        final Thread[] workers = new Thread[actualThreads];

        // Дія бар'єру: виконується один раз, коли ВСІ потоки досягли бар'єру.
        // Оновлюємо поточну довжину масиву для наступної хвилі.
        final CyclicBarrier barrier = new CyclicBarrier(actualThreads, () -> {
            int len = currentLength.get();
            currentLength.set((len + 1) / 2);
        });

        // Створюємо та запускаємо робочі потоки
        for (int t = 0; t < actualThreads; t++) {
            final int workerId = t;
            workers[t] = new Thread(() -> {
                try {
                    while (true) {
                        int len = currentLength.get();
                        // Якщо залишився один елемент — робота завершена
                        if (len <= 1) {
                            break;
                        }

                        int pairs = len / 2;

                        // Розподіляємо пари між потоками рівномірно
                        int baseChunk = pairs / actualThreads;
                        int remainder = pairs % actualThreads;

                        int startPair = workerId * baseChunk + Math.min(workerId, remainder);
                        int endPair = startPair + baseChunk + (workerId < remainder ? 1 : 0);

                        // Обробляємо свою частину пар
                        for (int i = startPair; i < endPair; i++) {
                            int partnerIndex = len - 1 - i;
                            array[i] += array[partnerIndex];
                        }

                        // Чекаємо, поки всі потоки завершать поточну хвилю
                        barrier.await();
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
            workers[t].start();
        }

        // Головний потік очікує завершення всіх робочих потоків
        for (Thread worker : workers) {
            worker.join();
        }

        return array[0];
    }
}
