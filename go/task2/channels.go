package task2

// channelChunk описує діапазон пар для обробки робітником у поточній хвилі
type channelChunk struct {
	start         int
	end           int
	currentLength int
}

// ComputeChannels виконує хвильове здвоювання з передачею діапазонів пар через канал
// та синхронізацією завершення через канал токенів doneCh.
//
// Принцип роботи:
//  1. Створюється фіксований пул із workers горутин, які слухають канал taskCh.
//  2. На кожній хвилі головна горутина відправляє порції пар у taskCh.
//  3. Кожен робітник обробляє свою порцію пар і надсилає сигнал у doneCh.
//  4. Головна горутина очікує рівно tasksCount сигналів перед тим, як перейти до наступної хвилі.
//  5. Після завершення всіх хвиль quitCh закривається для коректного виходу робітників.
func ComputeChannels(array []int64, workers int) int64 {
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

	taskCh := make(chan channelChunk, actualWorkers)
	doneCh := make(chan struct{}, actualWorkers)
	quitCh := make(chan struct{})

	// Запуск пулу робітників
	for w := 0; w < actualWorkers; w++ {
		go func() {
			for {
				select {
				case <-quitCh:
					return
				case chunk, ok := <-taskCh:
					if !ok {
						return
					}
					for i := chunk.start; i < chunk.end; i++ {
						partnerIndex := chunk.currentLength - 1 - i
						array[i] += array[partnerIndex]
					}
					doneCh <- struct{}{}
				}
			}
		}()
	}

	// Цикл хвиль здвоювання
	for currentLength > 1 {
		pairs := currentLength / 2

		// Якщо пар мало — швидше обробити послідовно без перемикання контексту
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

				taskCh <- channelChunk{start: start, end: end, currentLength: waveLen}
				start = end
			}

			// Очікуємо завершення поточної хвилі всіма робітниками
			for t := 0; t < tasksCount; t++ {
				<-doneCh
			}
		}

		currentLength = (currentLength + 1) / 2
	}

	close(quitCh)
	return array[0]
}
