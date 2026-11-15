package task2

import (
	"sync"
)

// ComputeWaitGroup виконує паралельне хвильове здвоювання, синхронізуючи
// завершення кожної хвилі за допомогою sync.WaitGroup.
//
// Жодна наступна хвиля не розпочинається, доки всі операції поточної хвилі не завершені (wg.Wait()).
// Гонки даних виключені, оскільки кожен індекс i та partnerIndex обробляється лише одним потоком.
func ComputeWaitGroup(array []int64, workers int) int64 {
	currentLength := len(array)
	if currentLength == 0 {
		return 0
	}
	if workers <= 1 || currentLength <= 1 {
		return ComputeSequential(array)
	}

	actualWorkers := workers
	if actualWorkers > currentLength/2 {
		actualWorkers = currentLength / 2
	}
	if actualWorkers <= 1 {
		return ComputeSequential(array)
	}

	var wg sync.WaitGroup

	for currentLength > 1 {
		pairs := currentLength / 2

		// При малій кількості пар обчислюємо послідовно
		if pairs < actualWorkers*4 {
			for i := 0; i < pairs; i++ {
				partnerIndex := currentLength - 1 - i
				array[i] += array[partnerIndex]
			}
		} else {
			tasksCount := actualWorkers
			baseChunk := pairs / tasksCount
			remainder := pairs % tasksCount
			start := 0
			waveLen := currentLength

			for t := 0; t < tasksCount; t++ {
				chunkSize := baseChunk
				if t < remainder {
					chunkSize++
				}
				end := start + chunkSize

				wg.Add(1)
				go func(s, e, cLen int) {
					defer wg.Done()
					for i := s; i < e; i++ {
						partnerIndex := cLen - 1 - i
						array[i] += array[partnerIndex]
					}
				}(start, end, waveLen)

				start = end
			}

			// Обов'язкова бар'єрна синхронізація перед переходом до наступної хвилі
			wg.Wait()
		}

		currentLength = (currentLength + 1) / 2
	}

	return array[0]
}
