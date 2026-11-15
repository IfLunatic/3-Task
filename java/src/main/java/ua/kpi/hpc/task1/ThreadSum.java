package ua.kpi.hpc.task1;

/**
 * Паралельне обчислення суми масиву за допомогою власних потоків (Thread).
 *
 * Принцип роботи:
 *   1. Масив розбивається на threadCount частин (chunks).
 *   2. Для кожної частини створюється окремий потік WorkerThread.
 *   3. Кожен потік обчислює локальну суму своєї частини.
 *   4. Головний потік очікує завершення всіх робочих потоків (join).
 *   5. Локальні суми складаються в загальний результат.
 *
 * Залишок елементів (якщо масив не ділиться на threadCount без остачі)
 * розподіляється порівну: перші (remainder) потоків отримують на 1 елемент більше.
 */
public final class ThreadSum {

    private ThreadSum() {}

    /**
     * Внутрішній клас робочого потоку.
     * Кожний потік обчислює суму елементів масиву у своєму діапазоні [start, end).
     */
    private static final class WorkerThread extends Thread {
        private final long[] array;    // посилання на спільний масив
        private final int start;       // початковий індекс (включно)
        private final int end;         // кінцевий індекс (виключно)
        private long partialSum = 0L;  // результат обчислення цього потоку

        WorkerThread(long[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            // Обчислюємо суму елементів у своєму діапазоні
            partialSum = SequentialSum.computeRange(array, start, end);
        }

        public long getPartialSum() {
            return partialSum;
        }
    }

    /**
     * Обчислює суму масиву, використовуючи ручне створення потоків.
     *
     * @param array       вхідний масив
     * @param threadCount кількість потоків
     * @return загальна сума
     * @throws InterruptedException якщо головний потік був перерваний під час join
     */
    public static long compute(long[] array, int threadCount) throws InterruptedException {
        if (array == null || array.length == 0) {
            return 0L;
        }
        // Якщо 1 потік або 1 елемент — послідовне обчислення
        if (threadCount <= 1 || array.length == 1) {
            return SequentialSum.compute(array);
        }

        // Не створюємо потоків більше, ніж елементів у масиві
        int actualThreads = Math.min(threadCount, array.length);
        WorkerThread[] threads = new WorkerThread[actualThreads];

        int n = array.length;
        int baseChunk = n / actualThreads;  // базовий розмір частини
        int remainder = n % actualThreads;  // залишок, який потрібно розподілити

        // Розбиваємо масив на частини та запускаємо потоки
        int currentStart = 0;
        for (int i = 0; i < actualThreads; i++) {
            // Перші (remainder) потоків отримують на 1 елемент більше
            int chunkSize = baseChunk + (i < remainder ? 1 : 0);
            int currentEnd = currentStart + chunkSize;
            threads[i] = new WorkerThread(array, currentStart, currentEnd);
            threads[i].start(); // запускаємо потік
            currentStart = currentEnd;
        }

        // Очікуємо завершення всіх потоків і збираємо локальні суми
        long totalSum = 0L;
        for (WorkerThread thread : threads) {
            thread.join();                    // блокуємо, поки потік не завершиться
            totalSum += thread.getPartialSum(); // додаємо його локальну суму
        }

        return totalSum;
    }
}
