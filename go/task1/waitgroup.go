package task1

import (
	"sync"
)

// ComputeWaitGroup обчислює суму масиву, розподіляючи роботу між горутинами,
// і синхронізує їхнє завершення за допомогою sync.WaitGroup.
//
// Принцип роботи:
//  1. Створюється зріз partialSums розміром workers для збереження результатів.
//  2. Кожна горутина записує свою локальну суму у власний слот partialSums[idx]
//     (немає гонки даних, оскільки кожен індекс унікальний).
//  3. wg.Wait() очікує завершення всіх горутин.
//  4. Головна горутина складає результати з partialSums.
func ComputeWaitGroup(array []int64, workers int) int64 {
	n := len(array)
	if n == 0 {
		return 0
	}
	if workers <= 1 || n == 1 {
		return ComputeSequential(array)
	}

	if workers > n {
		workers = n
	}

	partialSums := make([]int64, workers)
	var wg sync.WaitGroup

	baseChunk := n / workers
	remainder := n % workers

	start := 0
	for i := 0; i < workers; i++ {
		chunkSize := baseChunk
		if i < remainder {
			chunkSize++
		}
		end := start + chunkSize

		wg.Add(1)
		go func(idx, s, e int) {
			defer wg.Done()
			partialSums[idx] = ComputeRange(array, s, e)
		}(i, start, end)

		start = end
	}

	// Очікуємо завершення всіх горутин
	wg.Wait()

	// Агрегуємо локальні суми
	var totalSum int64
	for _, p := range partialSums {
		totalSum += p
	}

	return totalSum
}
