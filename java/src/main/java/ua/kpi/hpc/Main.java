package ua.kpi.hpc;

import ua.kpi.hpc.common.BenchmarkResult;
import ua.kpi.hpc.common.DataGenerator;
import ua.kpi.hpc.task1.*;
import ua.kpi.hpc.task2.*;

import java.util.*;

/**
 * Головний клас програми.
 *
 * Запускає бенчмарки для двох завдань:
 *   Завдання 1 — обчислення суми масиву методом поділу на частини (Chunking).
 *   Завдання 2 — обчислення суми масиву методом здвоювання (Wave / Pairwise Doubling).
 *
 * Для кожного завдання вимірюється час виконання, прискорення (Speedup) та
 * ефективність (Efficiency) при різній кількості потоків.
 *
 * Запуск:
 *   java -cp target/hpc-lab-1.0.jar ua.kpi.hpc.Main          — розміри за замовчуванням (1M, 10M, 100M)
 *   java -cp target/hpc-lab-1.0.jar ua.kpi.hpc.Main 50000000 — конкретний розмір масиву
 *   java -cp target/hpc-lab-1.0.jar ua.kpi.hpc.Main 1B       — масив на 1 мільярд елементів
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Виводимо заголовок та системну інформацію
        System.out.println("=========================================================================================");
        System.out.println("   High Performance Computing: Parallel Array Summation Lab (Java 25 LTS)");
        System.out.println("=========================================================================================");
        System.out.printf("Available Processors (Cores/Threads): %d%n", Runtime.getRuntime().availableProcessors());
        System.out.printf("Max Heap Memory: %.2f GB%n", Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0 * 1024.0));
        System.out.println("=========================================================================================");

        // Розміри масивів для тестування (за замовчуванням 1M, 10M, 100M)
        int[] sizes = {1_000_000, 10_000_000, 100_000_000};

        // Якщо передано аргумент "1B" — тестуємо на 1 мільярді елементів
        if (args.length > 0 && "1B".equalsIgnoreCase(args[0])) {
            sizes = new int[]{1_000_000_000};
        } else if (args.length > 0) {
            // Якщо передано число — використовуємо його як розмір масиву
            try {
                sizes = new int[]{Integer.parseInt(args[0])};
            } catch (NumberFormatException ignored) {}
        }

        // Кількість потоків, з якими будемо проводити бенчмарки
        int[] threadCounts = {1, 2, 4, 8, 12};

        // Запускаємо бенчмарки для кожного розміру масиву
        for (int size : sizes) {
            runBenchmarksForSize(size, threadCounts);
        }
    }

    /**
     * Запускає повний набір бенчмарків для заданого розміру масиву.
     *
     * @param size         кількість елементів масиву
     * @param threadCounts масив кількостей потоків для паралельних реалізацій
     */
    public static void runBenchmarksForSize(int size, int[] threadCounts) throws Exception {
        System.out.println("\n-----------------------------------------------------------------------------------------");
        System.out.printf(">>> BENCHMARKING ARRAY SIZE: %,d elements (Memory: ~%.2f MB)%n",
            size, (size * 8.0) / (1024.0 * 1024.0));
        System.out.println("-----------------------------------------------------------------------------------------");

        // Обчислюємо очікувану суму аналітично (для перевірки правильності)
        long expected = DataGenerator.expectedSum(size);
        // Генеруємо масив зі стандартним шаблоном
        long[] array = DataGenerator.generate(size);

        // ======================= ЗАВДАННЯ 1: Поділ на частини =======================
        System.out.println("\n--- TASK 1: Chunking / Partitioning Sum ---");
        System.out.printf("%-24s | %-7s | %-12s | %-9s | %-10s%n",
            "Algorithm", "Threads", "Time (ms)", "Speedup", "Efficiency");
        System.out.println("-----------------------------------------------------------------------------------------");

        // Прогрів JVM (warmup) та вимірювання послідовного алгоритму як базової лінії
        SequentialSum.compute(array);
        double seqTime = measureTask1("Sequential", 1, array, expected);
        printRow("Sequential", 1, seqTime, 1.0, 1.0);

        // Вимірюємо паралельні реалізації з різною кількістю потоків
        for (int p : threadCounts) {
            if (p <= 0) continue;

            // Thread-based — ручне створення потоків
            double tTime = measureTask1("Thread-based", p, array, expected);
            printRow("Thread-based", p, tTime, seqTime / tTime, (seqTime / tTime) / p);

            // ExecutorService — пул потоків через java.util.concurrent
            double eTime = measureTask1("ExecutorService", p, array, expected);
            printRow("ExecutorService", p, eTime, seqTime / eTime, (seqTime / eTime) / p);

            // ForkJoinPool — рекурсивний поділ з механізмом «крадіжки роботи»
            double fTime = measureTask1("ForkJoinPool", p, array, expected);
            printRow("ForkJoinPool", p, fTime, seqTime / fTime, (seqTime / fTime) / p);
        }

        // ======================= ЗАВДАННЯ 2: Хвильовий алгоритм =======================
        System.out.println("\n--- TASK 2: Pairwise Doubling (Wave Algorithm) ---");
        System.out.printf("%-24s | %-7s | %-12s | %-9s | %-10s%n",
            "Algorithm", "Threads", "Time (ms)", "Speedup", "Efficiency");
        System.out.println("-----------------------------------------------------------------------------------------");

        // Заповнюємо масив заново (хвильовий алгоритм модифікує його на місці)
        DataGenerator.fill(array);
        double seqWaveTime = measureTask2("Sequential Wave", 1, array, expected);
        printRow("Sequential Wave", 1, seqWaveTime, 1.0, 1.0);

        // Вимірюємо паралельні варіанти хвильового алгоритму
        for (int p : threadCounts) {
            if (p <= 0) continue;

            // Пул потоків із CyclicBarrier для синхронізації хвиль
            double tpTime = measureTask2("Thread Pool (Barrier)", p, array, expected);
            printRow("Thread Pool (Barrier)", p, tpTime, seqWaveTime / tpTime, (seqWaveTime / tpTime) / p);

            // ExecutorService — відправка задач у пул і очікування завершення кожної хвилі
            double esTime = measureTask2("ExecutorService", p, array, expected);
            printRow("ExecutorService", p, esTime, seqWaveTime / esTime, (seqWaveTime / esTime) / p);

            // ForkJoinPool — рекурсивний поділ пар у рамках хвилі
            double fjTime = measureTask2("ForkJoinPool", p, array, expected);
            printRow("ForkJoinPool", p, fjTime, seqWaveTime / fjTime, (seqWaveTime / fjTime) / p);
        }
    }

    /**
     * Вимірює час виконання алгоритму Завдання 1 (поділ на частини).
     * Виконує кілька повторень і повертає середній час у мілісекундах.
     *
     * @param algo     назва алгоритму
     * @param p        кількість потоків
     * @param array    вхідний масив
     * @param expected очікувана правильна сума
     * @return середній час виконання (мс)
     */
    private static double measureTask1(String algo, int p, long[] array, long expected) throws Exception {
        int repeats = 3; // кількість повторень для усереднення
        double totalMs = 0;
        for (int r = 0; r < repeats; r++) {
            long t0 = System.nanoTime(); // засікаємо час початку
            long sum = switch (algo) {
                case "Sequential" -> SequentialSum.compute(array);
                case "Thread-based" -> ThreadSum.compute(array, p);
                case "ExecutorService" -> ExecutorServiceSum.compute(array, p);
                case "ForkJoinPool" -> ForkJoinSum.compute(array, p);
                default -> throw new IllegalArgumentException(algo);
            };
            long t1 = System.nanoTime(); // засікаємо час завершення

            // Перевіряємо правильність результату
            if (sum != expected) {
                throw new IllegalStateException("Validation mismatch: got " + sum + ", expected " + expected);
            }
            totalMs += (t1 - t0) / 1_000_000.0; // переводимо наносекунди у мілісекунди
        }
        return totalMs / repeats; // повертаємо середнє значення
    }

    /**
     * Вимірює час виконання алгоритму Завдання 2 (хвильовий метод здвоювання).
     * Перед кожним повторенням масив заповнюється заново, бо алгоритм
     * модифікує його на місці.
     *
     * @param algo     назва алгоритму
     * @param p        кількість потоків
     * @param array    вхідний масив (буде перезаповнений перед кожним прогоном)
     * @param expected очікувана правильна сума
     * @return середній час виконання (мс)
     */
    private static double measureTask2(String algo, int p, long[] array, long expected) throws Exception {
        int repeats = 3;
        double totalMs = 0;
        for (int r = 0; r < repeats; r++) {
            DataGenerator.fill(array); // перезаповнення, бо хвиля змінює масив на місці
            long t0 = System.nanoTime();
            long sum = switch (algo) {
                case "Sequential Wave" -> SequentialWaveSum.compute(array);
                case "Thread Pool (Barrier)" -> ThreadPoolWaveSum.compute(array, p);
                case "ExecutorService" -> ExecutorServiceWaveSum.compute(array, p);
                case "ForkJoinPool" -> ForkJoinWaveSum.compute(array, p);
                default -> throw new IllegalArgumentException(algo);
            };
            long t1 = System.nanoTime();

            // Перевірка коректності
            if (sum != expected) {
                throw new IllegalStateException("Validation mismatch in " + algo + ": got " + sum + ", expected " + expected);
            }
            totalMs += (t1 - t0) / 1_000_000.0;
        }
        return totalMs / repeats;
    }

    /**
     * Виводить один рядок таблиці результатів бенчмарку.
     *
     * @param algo       назва алгоритму
     * @param p          кількість потоків
     * @param timeMs     час виконання (мс)
     * @param speedup    прискорення відносно послідовної реалізації
     * @param efficiency ефективність = speedup / p
     */
    private static void printRow(String algo, int p, double timeMs, double speedup, double efficiency) {
        System.out.printf(Locale.US, "%-24s | P=%-5d | %9.3f ms | %7.2fx | %9.2f%%%n",
            algo, p, timeMs, speedup, efficiency * 100.0);
    }
}
