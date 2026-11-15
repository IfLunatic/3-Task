// Набір модульних тестів для перевірки коректності всіх реалізацій алгоритмів (Task 1 та Task 2).
package main

import (
	"testing"

	"ua.kpi.hpc/parallel-lab/common"
	"ua.kpi.hpc/parallel-lab/task1"
	"ua.kpi.hpc/parallel-lab/task2"
)

// Кількість потоків/горутин для всебічного тестування (включаючи непарні та більші за кількість ядер)
var testWorkers = []int{1, 2, 3, 4, 7, 8, 16}

func cloneSlice(src []int64) []int64 {
	dst := make([]int64, len(src))
	copy(dst, src)
	return dst
}

// Тест: порожній масив (повинен повертати 0)
func TestEmptyArray(t *testing.T) {
	empty := []int64{}

	// Завдання 1
	if s := task1.ComputeSequential(empty); s != 0 {
		t.Fatalf("Sequential: expected 0, got %d", s)
	}
	for _, p := range testWorkers {
		if s := task1.ComputeChannels(empty, p); s != 0 {
			t.Fatalf("Channels(P=%d): expected 0, got %d", p, s)
		}
		if s := task1.ComputeWaitGroup(empty, p); s != 0 {
			t.Fatalf("WaitGroup(P=%d): expected 0, got %d", p, s)
		}
		if s := task1.ComputeWorkerPool(empty, p); s != 0 {
			t.Fatalf("WorkerPool(P=%d): expected 0, got %d", p, s)
		}
	}

	// Завдання 2
	if s := task2.ComputeSequential(cloneSlice(empty)); s != 0 {
		t.Fatalf("Wave Sequential: expected 0, got %d", s)
	}
	for _, p := range testWorkers {
		if s := task2.ComputeWaitGroup(cloneSlice(empty), p); s != 0 {
			t.Fatalf("Wave WaitGroup(P=%d): expected 0, got %d", p, s)
		}
		if s := task2.ComputeWorkerPool(cloneSlice(empty), p); s != 0 {
			t.Fatalf("Wave WorkerPool(P=%d): expected 0, got %d", p, s)
		}
		if s := task2.ComputeChannels(cloneSlice(empty), p); s != 0 {
			t.Fatalf("Wave Channels(P=%d): expected 0, got %d", p, s)
		}
	}
}

// Тест: масив з одного елемента
func TestSingleElement(t *testing.T) {
	single := []int64{42}
	expected := int64(42)

	// Завдання 1
	if s := task1.ComputeSequential(single); s != expected {
		t.Fatalf("Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task1.ComputeChannels(single, p); s != expected {
			t.Fatalf("Channels(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWaitGroup(single, p); s != expected {
			t.Fatalf("WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWorkerPool(single, p); s != expected {
			t.Fatalf("WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
	}

	// Завдання 2
	if s := task2.ComputeSequential(cloneSlice(single)); s != expected {
		t.Fatalf("Wave Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task2.ComputeWaitGroup(cloneSlice(single), p); s != expected {
			t.Fatalf("Wave WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeWorkerPool(cloneSlice(single), p); s != expected {
			t.Fatalf("Wave WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeChannels(cloneSlice(single), p); s != expected {
			t.Fatalf("Wave Channels(P=%d): expected %d, got %d", p, expected, s)
		}
	}
}

// Тест: парна довжина масиву
func TestEvenLengthArray(t *testing.T) {
	even := []int64{10, 20, 30, 40, 50, 60, 70, 80}
	expected := int64(360)

	// Завдання 1
	if s := task1.ComputeSequential(even); s != expected {
		t.Fatalf("Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task1.ComputeChannels(even, p); s != expected {
			t.Fatalf("Channels(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWaitGroup(even, p); s != expected {
			t.Fatalf("WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWorkerPool(even, p); s != expected {
			t.Fatalf("WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
	}

	// Завдання 2
	if s := task2.ComputeSequential(cloneSlice(even)); s != expected {
		t.Fatalf("Wave Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task2.ComputeWaitGroup(cloneSlice(even), p); s != expected {
			t.Fatalf("Wave WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeWorkerPool(cloneSlice(even), p); s != expected {
			t.Fatalf("Wave WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeChannels(cloneSlice(even), p); s != expected {
			t.Fatalf("Wave Channels(P=%d): expected %d, got %d", p, expected, s)
		}
	}
}

// Тест: непарна довжина масиву (перевірка коректного збереження центрального елемента у хвилі)
func TestOddLengthArray(t *testing.T) {
	odd := []int64{10, 20, 30, 40, 50, 60, 70}
	expected := int64(280)

	// Завдання 1
	if s := task1.ComputeSequential(odd); s != expected {
		t.Fatalf("Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task1.ComputeChannels(odd, p); s != expected {
			t.Fatalf("Channels(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWaitGroup(odd, p); s != expected {
			t.Fatalf("WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWorkerPool(odd, p); s != expected {
			t.Fatalf("WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
	}

	// Завдання 2
	if s := task2.ComputeSequential(cloneSlice(odd)); s != expected {
		t.Fatalf("Wave Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task2.ComputeWaitGroup(cloneSlice(odd), p); s != expected {
			t.Fatalf("Wave WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeWorkerPool(cloneSlice(odd), p); s != expected {
			t.Fatalf("Wave WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeChannels(cloneSlice(odd), p); s != expected {
			t.Fatalf("Wave Channels(P=%d): expected %d, got %d", p, expected, s)
		}
	}
}

// Тест: довжина, що не ділиться на кількість потоків без остачі
func TestNonDivisibleArray(t *testing.T) {
	size := 1007
	array := common.Generate(size)
	expected := common.ExpectedSum(int64(size))

	// Завдання 1
	if s := task1.ComputeSequential(array); s != expected {
		t.Fatalf("Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task1.ComputeChannels(array, p); s != expected {
			t.Fatalf("Channels(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWaitGroup(array, p); s != expected {
			t.Fatalf("WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWorkerPool(array, p); s != expected {
			t.Fatalf("WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
	}

	// Завдання 2
	if s := task2.ComputeSequential(cloneSlice(array)); s != expected {
		t.Fatalf("Wave Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task2.ComputeWaitGroup(cloneSlice(array), p); s != expected {
			t.Fatalf("Wave WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeWorkerPool(cloneSlice(array), p); s != expected {
			t.Fatalf("Wave WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeChannels(cloneSlice(array), p); s != expected {
			t.Fatalf("Wave Channels(P=%d): expected %d, got %d", p, expected, s)
		}
	}
}

// Тест: нульові та від'ємні значення
func TestZerosAndNegativeValues(t *testing.T) {
	values := []int64{-100, 0, 50, -25, 0, 75, -50, 100, 0, -50}
	expected := int64(0)

	// Завдання 1
	if s := task1.ComputeSequential(values); s != expected {
		t.Fatalf("Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task1.ComputeChannels(values, p); s != expected {
			t.Fatalf("Channels(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWaitGroup(values, p); s != expected {
			t.Fatalf("WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWorkerPool(values, p); s != expected {
			t.Fatalf("WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
	}

	// Завдання 2
	if s := task2.ComputeSequential(cloneSlice(values)); s != expected {
		t.Fatalf("Wave Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task2.ComputeWaitGroup(cloneSlice(values), p); s != expected {
			t.Fatalf("Wave WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeWorkerPool(cloneSlice(values), p); s != expected {
			t.Fatalf("Wave WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeChannels(cloneSlice(values), p); s != expected {
			t.Fatalf("Wave Channels(P=%d): expected %d, got %d", p, expected, s)
		}
	}
}

// Тест: запобігання 32-бітному переповненню (робота з int64)
func TestBoundaryOverflowPrevention(t *testing.T) {
	large := int64(3_000_000_000)
	array := []int64{large, large, large, large}
	expected := int64(12_000_000_000)

	// Завдання 1
	if s := task1.ComputeSequential(array); s != expected {
		t.Fatalf("Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task1.ComputeChannels(array, p); s != expected {
			t.Fatalf("Channels(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWaitGroup(array, p); s != expected {
			t.Fatalf("WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task1.ComputeWorkerPool(array, p); s != expected {
			t.Fatalf("WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
	}

	// Завдання 2
	if s := task2.ComputeSequential(cloneSlice(array)); s != expected {
		t.Fatalf("Wave Sequential: expected %d, got %d", expected, s)
	}
	for _, p := range testWorkers {
		if s := task2.ComputeWaitGroup(cloneSlice(array), p); s != expected {
			t.Fatalf("Wave WaitGroup(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeWorkerPool(cloneSlice(array), p); s != expected {
			t.Fatalf("Wave WorkerPool(P=%d): expected %d, got %d", p, expected, s)
		}
		if s := task2.ComputeChannels(cloneSlice(array), p); s != expected {
			t.Fatalf("Wave Channels(P=%d): expected %d, got %d", p, expected, s)
		}
	}
}
