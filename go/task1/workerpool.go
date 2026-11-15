package task1

import (
	"sync"
)

// sumJob представляє діапазон індексів масиву для обробки
type sumJob struct {
	start int
	end   int
}

// ComputeWorkerPool обчислює суму масиву, використовуючи фіксований пул горутин-робітників (Worker Pool).
//
// Принцип роботи:
//  1. Запускається фіксована кількість numWorkers робочих горутин.
//  2. Масив розбивається на дрібніші підзадачі (numChunks = numWorkers * 4) для динамічного
//     балансування навантаження між робітниками.
//  3. Задачі надсилаються у канал jobs. Робітники зчитують задачі та надсилають суми в results.
//  4. Після закриття каналу jobs робітники завершуються, а головна горутина збирає загальну суму.
func ComputeWorkerPool(array []int64, numWorkers int) int64 {
	n := len(array)
	if n == 0 {
		return 0
	}
	if numWorkers <= 1 || n == 1 {
		return ComputeSequential(array)
	}

	if numWorkers > n {
		numWorkers = n
	}

	// Ділимо на 4*numWorkers блоків для динамічного балансування
	numChunks := numWorkers * 4
	if numChunks > n {
		numChunks = numWorkers
	}

	jobs := make(chan sumJob, numChunks)
	results := make(chan int64, numChunks)

	var wg sync.WaitGroup

	// Запуск пулу робітників
	for w := 0; w < numWorkers; w++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for job := range jobs {
				results <- ComputeRange(array, job.start, job.end)
			}
		}()
	}

	// Подача задач у канал
	baseChunk := n / numChunks
	remainder := n % numChunks
	start := 0
	for i := 0; i < numChunks; i++ {
		chunkSize := baseChunk
		if i < remainder {
			chunkSize++
		}
		end := start + chunkSize
		jobs <- sumJob{start: start, end: end}
		start = end
	}
	close(jobs) // сигналізуємо робітникам про завершення задач

	// Очікуємо робітників в окремій горутині і закриваємо канал результатів
	go func() {
		wg.Wait()
		close(results)
	}()

	// Агрегація загальної суми
	var totalSum int64
	for r := range results {
		totalSum += r
	}

	return totalSum
}
