package ua.kpi.hpc.task1;

/**
 * Послідовне (однопотокове) обчислення суми масиву.
 *
 * Використовується як базова лінія (baseline) для розрахунку
 * прискорення (Speedup) та ефективності (Efficiency) паралельних реалізацій.
 *
 * Алгоритм: простий цикл від start до end, що накопичує суму.
 */
public final class SequentialSum {

    // Заборонений конструктор — утилітний клас
    private SequentialSum() {}

    /**
     * Обчислює суму всіх елементів масиву послідовно.
     *
     * @param array вхідний масив типу long
     * @return 64-бітна сума всіх елементів
     */
    public static long compute(long[] array) {
        if (array == null || array.length == 0) {
            return 0L;
        }
        return computeRange(array, 0, array.length);
    }

    /**
     * Обчислює суму елементів у діапазоні [start, end).
     *
     * Цей метод використовується паралельними реалізаціями:
     * кожен потік/горутина обчислює суму своєї частини масиву,
     * викликаючи computeRange зі своїми межами.
     *
     * @param array вхідний масив
     * @param start початковий індекс (включно)
     * @param end   кінцевий індекс (виключно)
     * @return сума елементів зрізу
     */
    public static long computeRange(long[] array, int start, int end) {
        long sum = 0L;
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }
}
