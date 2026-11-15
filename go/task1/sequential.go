// Пакет task1 містить реалізації обчислення суми масиву методом поділу на частини (Chunking).
package task1

// ComputeSequential обчислює суму всіх елементів зрізу послідовно в одному потоці.
// Слугує базовою лінією (baseline) для розрахунку прискорення та ефективності.
func ComputeSequential(array []int64) int64 {
	return ComputeRange(array, 0, len(array))
}

// ComputeRange обчислює суму елементів у діапазоні array[start:end].
// Використовується всіма паралельними реалізаціями для підсумовування своєї частини.
func ComputeRange(array []int64, start, end int) int64 {
	var sum int64
	for i := start; i < end; i++ {
		sum += array[i]
	}
	return sum
}
