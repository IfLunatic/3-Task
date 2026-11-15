package task1

// ComputeChannels обчислює суму масиву, розподіляючи частини між горутинами
// та передаючи локальні суми назад через канал (chan int64).
//
// Принцип роботи:
//  1. Масив ділиться на workers частин з урахуванням залишку (remainder).
//  2. Кожна горутина обчислює суму своєї частини та записує результат у буферизований канал ch.
//  3. Головна горутина зчитує рівно workers результатів із каналу та накопичує загальну суму.
func ComputeChannels(array []int64, workers int) int64 {
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

	// Буферизований канал розміром workers
	ch := make(chan int64, workers)
	baseChunk := n / workers
	remainder := n % workers

	start := 0
	for i := 0; i < workers; i++ {
		chunkSize := baseChunk
		if i < remainder {
			chunkSize++
		}
		end := start + chunkSize

		// Запуск горутини для обчислення локального зрізу
		go func(s, e int) {
			ch <- ComputeRange(array, s, e)
		}(start, end)

		start = end
	}

	// Збір результатів із каналу
	var totalSum int64
	for i := 0; i < workers; i++ {
		totalSum += <-ch
	}

	return totalSum
}
