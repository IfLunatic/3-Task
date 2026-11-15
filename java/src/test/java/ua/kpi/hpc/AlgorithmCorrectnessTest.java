package ua.kpi.hpc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ua.kpi.hpc.common.DataGenerator;
import ua.kpi.hpc.task1.*;
import ua.kpi.hpc.task2.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlgorithmCorrectnessTest {

    private final int[] threadConfigurations = {1, 2, 3, 4, 7, 8, 16};

    @Test
    @DisplayName("Empty Array Test (length = 0) -> Sum must be 0")
    void testEmptyArray() throws Exception {
        long[] empty = new long[0];

        // Task 1
        assertEquals(0L, SequentialSum.compute(empty));
        for (int p : threadConfigurations) {
            assertEquals(0L, ThreadSum.compute(empty, p));
            assertEquals(0L, ExecutorServiceSum.compute(empty, p));
            assertEquals(0L, ForkJoinSum.compute(empty, p));
        }

        // Task 2
        assertEquals(0L, SequentialWaveSum.compute(empty.clone()));
        for (int p : threadConfigurations) {
            assertEquals(0L, ThreadPoolWaveSum.compute(empty.clone(), p));
            assertEquals(0L, ExecutorServiceWaveSum.compute(empty.clone(), p));
            assertEquals(0L, ForkJoinWaveSum.compute(empty.clone(), p));
        }
    }

    @Test
    @DisplayName("Single Element Array Test (length = 1)")
    void testSingleElement() throws Exception {
        long[] single = new long[]{42L};

        // Task 1
        assertEquals(42L, SequentialSum.compute(single));
        for (int p : threadConfigurations) {
            assertEquals(42L, ThreadSum.compute(single, p));
            assertEquals(42L, ExecutorServiceSum.compute(single, p));
            assertEquals(42L, ForkJoinSum.compute(single, p));
        }

        // Task 2
        assertEquals(42L, SequentialWaveSum.compute(single.clone()));
        for (int p : threadConfigurations) {
            assertEquals(42L, ThreadPoolWaveSum.compute(single.clone(), p));
            assertEquals(42L, ExecutorServiceWaveSum.compute(single.clone(), p));
            assertEquals(42L, ForkJoinWaveSum.compute(single.clone(), p));
        }
    }

    @Test
    @DisplayName("Even Length Array Test")
    void testEvenLengthArray() throws Exception {
        long[] even = new long[]{10, 20, 30, 40, 50, 60, 70, 80};
        long expected = 360L;

        // Task 1
        assertEquals(expected, SequentialSum.compute(even));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadSum.compute(even, p));
            assertEquals(expected, ExecutorServiceSum.compute(even, p));
            assertEquals(expected, ForkJoinSum.compute(even, p));
        }

        // Task 2
        assertEquals(expected, SequentialWaveSum.compute(even.clone()));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadPoolWaveSum.compute(even.clone(), p));
            assertEquals(expected, ExecutorServiceWaveSum.compute(even.clone(), p));
            assertEquals(expected, ForkJoinWaveSum.compute(even.clone(), p));
        }
    }

    @Test
    @DisplayName("Odd Length Array Test (Preserves untouched center in wave)")
    void testOddLengthArray() throws Exception {
        long[] odd = new long[]{10, 20, 30, 40, 50, 60, 70};
        long expected = 280L;

        // Task 1
        assertEquals(expected, SequentialSum.compute(odd));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadSum.compute(odd, p));
            assertEquals(expected, ExecutorServiceSum.compute(odd, p));
            assertEquals(expected, ForkJoinSum.compute(odd, p));
        }

        // Task 2
        assertEquals(expected, SequentialWaveSum.compute(odd.clone()));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadPoolWaveSum.compute(odd.clone(), p));
            assertEquals(expected, ExecutorServiceWaveSum.compute(odd.clone(), p));
            assertEquals(expected, ForkJoinWaveSum.compute(odd.clone(), p));
        }
    }

    @Test
    @DisplayName("Array Size Not Divisible by Thread Count (e.g. 1007 items with 7, 8, 16 threads)")
    void testNonDivisibleArray() throws Exception {
        int size = 1007;
        long[] array = DataGenerator.generate(size);
        long expected = DataGenerator.expectedSum(size);

        // Task 1
        assertEquals(expected, SequentialSum.compute(array));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadSum.compute(array, p));
            assertEquals(expected, ExecutorServiceSum.compute(array, p));
            assertEquals(expected, ForkJoinSum.compute(array, p));
        }

        // Task 2
        assertEquals(expected, SequentialWaveSum.compute(array.clone()));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadPoolWaveSum.compute(array.clone(), p));
            assertEquals(expected, ExecutorServiceWaveSum.compute(array.clone(), p));
            assertEquals(expected, ForkJoinWaveSum.compute(array.clone(), p));
        }
    }

    @Test
    @DisplayName("Array With Zeros and Negative Values")
    void testZerosAndNegativeValues() throws Exception {
        long[] values = new long[]{-100, 0, 50, -25, 0, 75, -50, 100, 0, -50};
        long expected = 0L;

        // Task 1
        assertEquals(expected, SequentialSum.compute(values));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadSum.compute(values, p));
            assertEquals(expected, ExecutorServiceSum.compute(values, p));
            assertEquals(expected, ForkJoinSum.compute(values, p));
        }

        // Task 2
        assertEquals(expected, SequentialWaveSum.compute(values.clone()));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadPoolWaveSum.compute(values.clone(), p));
            assertEquals(expected, ExecutorServiceWaveSum.compute(values.clone(), p));
            assertEquals(expected, ForkJoinWaveSum.compute(values.clone(), p));
        }
    }

    @Test
    @DisplayName("Large Boundary Values (Detect 32-bit integer overflow)")
    void testBoundaryOverflowPrevention() throws Exception {
        // Values larger than Integer.MAX_VALUE (2_147_483_647)
        long large = 3_000_000_000L;
        long[] array = new long[]{large, large, large, large};
        long expected = 12_000_000_000L;

        // Task 1
        assertEquals(expected, SequentialSum.compute(array));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadSum.compute(array, p));
            assertEquals(expected, ExecutorServiceSum.compute(array, p));
            assertEquals(expected, ForkJoinSum.compute(array, p));
        }

        // Task 2
        assertEquals(expected, SequentialWaveSum.compute(array.clone()));
        for (int p : threadConfigurations) {
            assertEquals(expected, ThreadPoolWaveSum.compute(array.clone(), p));
            assertEquals(expected, ExecutorServiceWaveSum.compute(array.clone(), p));
            assertEquals(expected, ForkJoinWaveSum.compute(array.clone(), p));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 1000, 50000})
    @DisplayName("Parameterized Test on Variable Array Sizes")
    void testVariableSizes(int size) throws Exception {
        long[] array = DataGenerator.generate(size);
        long expected = DataGenerator.expectedSum(size);

        for (int p : new int[]{1, 2, 4, 8}) {
            assertEquals(expected, ThreadSum.compute(array, p));
            assertEquals(expected, ExecutorServiceSum.compute(array, p));
            assertEquals(expected, ForkJoinSum.compute(array, p));

            long[] waveCopy = array.clone();
            assertEquals(expected, ThreadPoolWaveSum.compute(waveCopy, p));
        }
    }
}
