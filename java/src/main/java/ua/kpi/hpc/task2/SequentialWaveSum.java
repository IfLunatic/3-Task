package ua.kpi.hpc.task2;

/**
 * Послідовна (однопотокова) реалізація хвильового алгоритму здвоювання.
 *
 * Алгоритм працює на місці (in-place) і на кожній хвилі:
 *   1. Визначає поточну довжину активної частини масиву (currentLength).
 *   2. Знаходить кількість пар: pairs = currentLength / 2.
 *   3. Для кожного індексу i від 0 до pairs-1:
 *        — знаходить «партнера»: partnerIndex = currentLength - 1 - i
 *        — додає: array[i] = array[i] + array[partnerIndex]
 *   4. Зменшує довжину: newLength = (currentLength + 1) / 2.
 *      При непарній довжині центральний елемент зберігається без змін.
 *   5. Повторює, поки не залишиться один елемент.
 *
 * Результат зберігається в array[0].
 */
public final class SequentialWaveSum {

    private SequentialWaveSum() {}

    /**
     * Обчислює суму елементів масиву хвильовим методом здвоювання (послідовно).
     *
     * @param array вхідний масив (буде модифіковано на місці!)
     * @return загальна сума (також зберігається в array[0])
     */
    public static long compute(long[] array) {
        if (array == null || array.length == 0) {
            return 0L;
        }

        int currentLength = array.length; // поточна довжина активної частини

        // Повторюємо хвилі, поки не залишиться один елемент
        while (currentLength > 1) {
            int pairs = currentLength / 2; // кількість пар для обробки

            // Для кожної пари: додаємо елемент-партнер до елементу з меншим індексом
            for (int i = 0; i < pairs; i++) {
                int partnerIndex = currentLength - 1 - i; // симетричний індекс
                array[i] += array[partnerIndex];
            }

            // Зменшуємо довжину (при непарній — центральний елемент лишається)
            currentLength = (currentLength + 1) / 2;
        }

        return array[0]; // результат — у першому елементі
    }
}
