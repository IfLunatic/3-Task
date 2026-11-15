package ua.kpi.hpc.task2;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Паралельний хвильовий алгоритм здвоювання з використанням ForkJoinPool.
 *
 * Принцип роботи:
 *   1. Створюється ForkJoinPool із заданим рівнем паралелізму.
 *   2. На кожній хвилі задача WaveTask рекурсивно ділить діапазон пар навпіл:
 *      — якщо пар ≤ threshold — обробляє послідовно;
 *      — інакше — розбиває на ліву і праву підзадачі (invokeAll).
 *   3. Після завершення всіх підзадач хвилі — зменшується довжина масиву.
 *   4. Повторюється, поки не залишиться один елемент.
 *
 * RecursiveAction (а не RecursiveTask) — бо задача не повертає значення,
 * а модифікує масив на місці.
 */
public final class ForkJoinWaveSum {

    private ForkJoinWaveSum() {}

    /**
     * Рекурсивна задача для обробки однієї хвилі.
     * Обробляє пари в діапазоні [startPair, endPair) для масиву
     * з поточною довжиною currentLength.
     */
    private static final class WaveTask extends RecursiveAction {
        private final long[] array;
        private final int currentLength; // поточна довжина масиву (для обчислення partnerIndex)
        private final int startPair;     // початковий індекс пари (включно)
        private final int endPair;       // кінцевий індекс пари (виключно)
        private final int threshold;     // мінімальний розмір для послідовної обробки

        WaveTask(long[] array, int currentLength, int startPair, int endPair, int threshold) {
            this.array = array;
            this.currentLength = currentLength;
            this.startPair = startPair;
            this.endPair = endPair;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int length = endPair - startPair;

            // Базовий випадок: мало пар — обробляємо послідовно
            if (length <= threshold) {
                for (int i = startPair; i < endPair; i++) {
                    int partnerIndex = currentLength - 1 - i;
                    array[i] += array[partnerIndex];
                }
                return;
            }

            // Рекурсивний поділ: розбиваємо діапазон пар навпіл
            int mid = startPair + (length / 2);
            WaveTask left = new WaveTask(array, currentLength, startPair, mid, threshold);
            WaveTask right = new WaveTask(array, currentLength, mid, endPair, threshold);

            // invokeAll запускає обидві задачі і чекає їх завершення
            invokeAll(left, right);
        }
    }

    /**
     * Обчислює суму масиву хвильовим методом через ForkJoinPool.
     *
     * @param array       вхідний масив (буде модифіковано на місці!)
     * @param parallelism кількість робочих потоків у ForkJoinPool
     * @return загальна сума (array[0])
     */
    public static long compute(long[] array, int parallelism) {
        if (array == null || array.length == 0) {
            return 0L;
        }
        if (parallelism <= 1 || array.length <= 1) {
            return SequentialWaveSum.compute(array);
        }

        ForkJoinPool customPool = new ForkJoinPool(parallelism);
        try {
            int currentLength = array.length;

            // Цикл хвиль
            while (currentLength > 1) {
                int pairs = currentLength / 2;
                // Адаптивний поріг: щоб ForkJoin міг ефективно розподіляти роботу
                int threshold = Math.max(10_000, pairs / (parallelism * 4));

                // Оптимізація: при малій кількості пар — послідовна обробка
                if (pairs <= threshold) {
                    for (int i = 0; i < pairs; i++) {
                        int partnerIndex = currentLength - 1 - i;
                        array[i] += array[partnerIndex];
                    }
                } else {
                    // Запускаємо рекурсивну задачу для обробки всіх пар хвилі
                    customPool.invoke(new WaveTask(array, currentLength, 0, pairs, threshold));
                }

                // Зменшуємо довжину для наступної хвилі
                currentLength = (currentLength + 1) / 2;
            }

            return array[0];
        } finally {
            customPool.shutdown();
        }
    }
}
