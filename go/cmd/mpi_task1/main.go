// Завдання 1: Розподілене обчислення суми масиву мовою Go з використанням MPI.
// Кожен процес генерує свій зріз даних, обчислює локальну суму,
// після чого результати агрегуються за допомогою MPI_Reduce.
package main

import (
	"fmt"
	"os"
	"strconv"

	"ua.kpi.hpc/parallel-lab/common"
	"ua.kpi.hpc/parallel-lab/mpi"
	"ua.kpi.hpc/parallel-lab/task1"
)

func main() {
	arraySize := 100_000_000
	if len(os.Args) > 1 {
		if val, err := strconv.Atoi(os.Args[1]); err == nil && val > 0 {
			arraySize = val
		}
	}

	mpi.Init()
	defer mpi.Finalize()

	rank := mpi.Rank()
	size := mpi.Size()

	baseChunk := arraySize / size
	remainder := arraySize % size

	start := rank*baseChunk + min(rank, remainder)
	chunkSize := baseChunk
	if rank < remainder {
		chunkSize++
	}

	// Генерація локального зрізу даних (без виділення повного гігантського масиву)
	localSlice := make([]int64, chunkSize)
	for i := 0; i < chunkSize; i++ {
		localSlice[i] = int64(((start + i) % 100) + 1)
	}

	mpi.Barrier()
	startTime := mpi.Wtime()

	// Локальне підсумовування
	localSum := task1.ComputeSequential(localSlice)
	// Редукція суми до кореневого процесу rank 0
	totalSum := mpi.ReduceSumInt64(localSum, 0)

	mpi.Barrier()
	endTime := mpi.Wtime()
	elapsedMs := (endTime - startTime) * 1000.0

	// Вивід результатів із кореневого рангу
	if rank == 0 {
		expected := common.ExpectedSum(int64(arraySize))
		valid := (totalSum == expected)
		fmt.Printf("[Go MPI Task 1] Size: %d | Ranks: %d | Sum: %d | Valid: %t | Time: %.3f ms\n",
			arraySize, size, totalSum, valid, elapsedMs)
	}
}

func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}
