// Пакет task2 містить реалізації обчислення суми масиву хвильовим методом здвоювання (Pairwise Doubling).
package task2

// ComputeSequential виконує попарне здвоювання послідовно на місці (in-place).
//
// Формули для кожної хвилі:
//   partnerIndex = currentLength - 1 - index
//   array[index] = array[index] + array[partnerIndex]
//   newLength = (currentLength + 1) / 2
//
// При непарній довжині центральний елемент зберігається без змін.
// Результат після всіх хвиль опиняється в array[0].
func ComputeSequential(array []int64) int64 {
	currentLength := len(array)
	if currentLength == 0 {
		return 0
	}

	for currentLength > 1 {
		pairs := currentLength / 2
		for i := 0; i < pairs; i++ {
			partnerIndex := currentLength - 1 - i
			array[i] += array[partnerIndex]
		}
		currentLength = (currentLength + 1) / 2
	}

	return array[0]
}
