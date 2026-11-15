package ua.kpi.hpc.common;

/**
 * Запис (record) для зберігання результатів одного бенчмарку.
 *
 * Поля:
 *   algorithmName   — назва алгоритму (наприклад, "ForkJoinPool")
 *   parallelism     — кількість потоків/процесів
 *   arraySize       — розмір масиву
 *   computedSum     — обчислена сума
 *   expectedSum     — очікувана правильна сума
 *   isValid         — чи збіглися обчислена та очікувана суми
 *   executionTimeMs — час виконання в мілісекундах
 */
public record BenchmarkResult(
    String algorithmName,
    int parallelism,
    int arraySize,
    long computedSum,
    long expectedSum,
    boolean isValid,
    double executionTimeMs
) {
    /**
     * Форматує результат у зручний рядок для виведення у таблицю.
     *
     * @return відформатований рядок
     */
    public String toFormattedString() {
        return String.format(
            "%-28s | P=%-3d | N=%-11d | Sum=%-14d | Valid=%-5s | Time=%10.3f ms",
            algorithmName, parallelism, arraySize, computedSum, isValid, executionTimeMs
        );
    }
}
