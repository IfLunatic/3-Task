// Завдання 2: Розподілений хвильовий алгоритм здвоювання (Pairwise Doubling) мовою Go з використанням MPI.
// Процес включає локальну хвилю всередині кожного рангу та розподілену міжпроцесну хвилю через MPI_Send / MPI_Recv / MPI_Barrier.
package main

import (
	"fmt"
	"os"
	"strconv"

	"ua.kpi.hpc/parallel-lab/common"
	"ua.kpi.hpc/parallel-lab/mpi"
	"ua.kpi.hpc/parallel-lab/task2"
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

	localSlice := make([]int64, chunkSize)
	for i := 0; i < chunkSize; i++ {
		localSlice[i] = int64(((start + i) % 100) + 1)
	}

	mpi.Barrier()
	startTime := mpi.Wtime()

	// Крок 1: Локальна хвиля на місці всередині поточного процесу
	localSum := task2.ComputeSequential(localSlice)

	// Крок 2: Розподілена хвиля між MPI-рангами кластеру
	activeRanks := size
	wave := 0
	for activeRanks > 1 {
		pairs := activeRanks / 2
		tag := 300 + wave

		if rank < pairs {
			partnerRank := activeRanks - 1 - rank
			partnerVal := mpi.RecvInt64(partnerRank, tag)
			localSum += partnerVal
		} else if rank >= activeRanks-pairs && rank < activeRanks {
			partnerRank := activeRanks - 1 - rank
			mpi.SendInt64(localSum, partnerRank, tag)
		}

		// Обов'язковий бар'єр синхронізації між хвилями
		mpi.Barrier()
		activeRanks = (activeRanks + 1) / 2
		wave++
	}

	mpi.Barrier()
	endTime := mpi.Wtime()
	elapsedMs := (endTime - startTime) * 1000.0

	if rank == 0 {
		expected := common.ExpectedSum(int64(arraySize))
		valid := (localSum == expected)
		fmt.Printf("[Go MPI Task 2 Wave] Size: %d | Ranks: %d | Sum: %d | Valid: %t | Time: %.3f ms\n",
			arraySize, size, localSum, valid, elapsedMs)
	}
}

func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}
