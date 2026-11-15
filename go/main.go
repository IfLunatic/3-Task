// Головний файл програми для запуску бенчмарків мовою Go.
// Вимірює час виконання, прискорення (Speedup) та ефективність (Efficiency)
// для Завдання 1 (поділ на частини) та Завдання 2 (хвильовий метод здвоювання).
//
// Запуск:
//   go run main.go            — стандартні розміри (1M, 10M, 100M)
//   go run main.go 10000000   — конкретний розмір масиву
//   go run main.go 1B         — 1 мільярд елементів
package main

import (
	"fmt"
	"os"
	"runtime"
	"strconv"
	"time"

	"ua.kpi.hpc/parallel-lab/common"
	"ua.kpi.hpc/parallel-lab/task1"
	"ua.kpi.hpc/parallel-lab/task2"
)

func main() {
	fmt.Println("=========================================================================================")
	fmt.Printf("   High Performance Computing: Parallel Array Summation Lab (Go %s)\n", runtime.Version())
	fmt.Println("=========================================================================================")
	fmt.Printf("Available CPUs: %d | GOMAXPROCS: %d\n", runtime.NumCPU(), runtime.GOMAXPROCS(0))
	fmt.Println("=========================================================================================")

	// Розміри масивів для тестування (за замовчуванням 1M, 10M, 100M)
	sizes := []int{1_000_000, 10_000_000, 100_000_000}
	if len(os.Args) > 1 && (os.Args[1] == "1B" || os.Args[1] == "1b") {
		sizes = []int{1_000_000_000}
	} else if len(os.Args) > 1 {
		if val, err := strconv.Atoi(os.Args[1]); err == nil && val > 0 {
			sizes = []int{val}
		}
	}

	// Кількість горутин/робітників для бенчмарків
	threadCounts := []int{1, 2, 4, 8, 12}

	for _, size := range sizes {
		runBenchmarksForSize(size, threadCounts)
	}
}

// runBenchmarksForSize запускає всі тести для заданого розміру масиву
func runBenchmarksForSize(size int, threadCounts []int) {
	memMB := float64(size*8) / (1024.0 * 1024.0)
	fmt.Println("\n-----------------------------------------------------------------------------------------")
	fmt.Printf(">>> BENCHMARKING ARRAY SIZE: %d elements (Memory: ~%.2f MB)\n", size, memMB)
	fmt.Println("-----------------------------------------------------------------------------------------")

	// Аналітичний розрахунок очікуваної суми для валідації
	expected := common.ExpectedSum(int64(size))
	array := common.Generate(size)

	// ======================= ЗАВДАННЯ 1: Поділ на частини =======================
	fmt.Println("\n--- TASK 1: Chunking / Partitioning Sum ---")
	fmt.Printf("%-24s | %-7s | %-12s | %-9s | %-10s\n",
		"Algorithm", "Threads", "Time (ms)", "Speedup", "Efficiency")
	fmt.Println("-----------------------------------------------------------------------------------------")

	// Прогрів та вимірювання послідовного алгоритму як базової лінії
	task1.ComputeSequential(array)
	seqTime := measureTask1("Sequential", 1, array, expected)
	printRow("Sequential", 1, seqTime, 1.0, 1.0)

	for _, p := range threadCounts {
		cTime := measureTask1("Channels", p, array, expected)
		printRow("Channels", p, cTime, seqTime/cTime, (seqTime/cTime)/float64(p))

		wgTime := measureTask1("WaitGroup", p, array, expected)
		printRow("WaitGroup", p, wgTime, seqTime/wgTime, (seqTime/wgTime)/float64(p))

		wpTime := measureTask1("Worker Pool", p, array, expected)
		printRow("Worker Pool", p, wpTime, seqTime/wpTime, (seqTime/wpTime)/float64(p))
	}

	// ======================= ЗАВДАННЯ 2: Хвильовий алгоритм =======================
	fmt.Println("\n--- TASK 2: Pairwise Doubling (Wave Algorithm) ---")
	fmt.Printf("%-24s | %-7s | %-12s | %-9s | %-10s\n",
		"Algorithm", "Threads", "Time (ms)", "Speedup", "Efficiency")
	fmt.Println("-----------------------------------------------------------------------------------------")

	// Відновлюємо масив перед хвильовим алгоритмом
	common.Fill(array)
	seqWaveTime := measureTask2("Sequential Wave", 1, array, expected)
	printRow("Sequential Wave", 1, seqWaveTime, 1.0, 1.0)

	for _, p := range threadCounts {
		wgTime := measureTask2("Goroutines + WaitGroup", p, array, expected)
		printRow("Goroutines + WaitGroup", p, wgTime, seqWaveTime/wgTime, (seqWaveTime/wgTime)/float64(p))

		wpTime := measureTask2("Worker Pool", p, array, expected)
		printRow("Worker Pool", p, wpTime, seqWaveTime/wpTime, (seqWaveTime/wpTime)/float64(p))

		chTime := measureTask2("Channels", p, array, expected)
		printRow("Channels", p, chTime, seqWaveTime/chTime, (seqWaveTime/chTime)/float64(p))
	}
}

// measureTask1 вимірює час виконання для Завдання 1
func measureTask1(algo string, p int, array []int64, expected int64) float64 {
	repeats := 3
	var totalNanos int64
	for r := 0; r < repeats; r++ {
		t0 := time.Now()
		var sum int64
		switch algo {
		case "Sequential":
			sum = task1.ComputeSequential(array)
		case "Channels":
			sum = task1.ComputeChannels(array, p)
		case "WaitGroup":
			sum = task1.ComputeWaitGroup(array, p)
		case "Worker Pool":
			sum = task1.ComputeWorkerPool(array, p)
		default:
			panic("unknown algo: " + algo)
		}
		elapsed := time.Since(t0).Nanoseconds()
		if elapsed <= 0 {
			elapsed = 1000 // щонайменше 1 мкс для уникнення ділення на 0
		}
		if sum != expected {
			panic(fmt.Sprintf("validation failed for %s (P=%d): got %d, expected %d", algo, p, sum, expected))
		}
		totalNanos += elapsed
	}
	avgNanos := float64(totalNanos) / float64(repeats)
	ms := avgNanos / 1_000_000.0
	if ms < 0.001 {
		ms = 0.001
	}
	return ms
}

// measureTask2 вимірює час виконання для Завдання 2 (хвильовий метод)
func measureTask2(algo string, p int, array []int64, expected int64) float64 {
	repeats := 3
	var totalNanos int64
	for r := 0; r < repeats; r++ {
		common.Fill(array) // перезаповнення, бо хвиля модифікує зріз на місці
		t0 := time.Now()
		var sum int64
		switch algo {
		case "Sequential Wave":
			sum = task2.ComputeSequential(array)
		case "Goroutines + WaitGroup":
			sum = task2.ComputeWaitGroup(array, p)
		case "Worker Pool":
			sum = task2.ComputeWorkerPool(array, p)
		case "Channels":
			sum = task2.ComputeChannels(array, p)
		default:
			panic("unknown algo: " + algo)
		}
		elapsed := time.Since(t0).Nanoseconds()
		if elapsed <= 0 {
			elapsed = 1000 // щонайменше 1 мкс
		}
		if sum != expected {
			panic(fmt.Sprintf("validation failed for %s (P=%d): got %d, expected %d", algo, p, sum, expected))
		}
		totalNanos += elapsed
	}
	avgNanos := float64(totalNanos) / float64(repeats)
	ms := avgNanos / 1_000_000.0
	if ms < 0.001 {
		ms = 0.001
	}
	return ms
}

func printRow(algo string, p int, timeMs, speedup, efficiency float64) {
	if timeMs < 0.001 {
		timeMs = 0.001
	}
	fmt.Printf("%-24s | P=%-5d | %9.3f ms | %7.2fx | %9.2f%%\n",
		algo, p, timeMs, speedup, efficiency*100.0)
}
