package task2

import (
	"sync"
)

// waveTask описує порцію пар поточної хвилі для Worker Pool
type waveTask struct {
	start         int
	end           int
	currentLength int
}

// ComputeWorkerPool виконує хвильове здвоювання з постійним пулом горутин (Worker Pool).
// Горутини створюються один раз і перевикористовуються на всіх хвилях обчислення,
// що повністю усуває накладні витрати на багаторазове створення горутин.
func ComputeWorkerPool(array []int64, workers int) int64 {
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

	tasks := make(chan waveTask, actualWorkers)
	var waveWg sync.WaitGroup
	var poolWg sync.WaitGroup

	// Запуск фіксованого пулу горутин
	for w := 0; w < actualWorkers; w++ {
		poolWg.Add(1)
		go func() {
			defer poolWg.Done()
			for task := range tasks {
				for i := task.start; i < task.end; i++ {
					partnerIndex := task.currentLength - 1 - i
					array[i] += array[partnerIndex]
				}
				waveWg.Done()
			}
		}()
	}

	for currentLength > 1 {
		pairs := currentLength / 2

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

				waveWg.Add(1)
				tasks <- waveTask{start: start, end: end, currentLength: waveLen}
				start = end
			}

			// Бар'єрна синхронізація завершення поточної хвилі
			waveWg.Wait()
		}

		currentLength = (currentLength + 1) / 2
	}

	// Завершення пулу робітників
	close(tasks)
	poolWg.Wait()

	return array[0]
}
