package main

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

type BenchmarkEntry struct {
	Language   string
	Task       string
	Algorithm  string
	Parallelism int
	Size       int
	TimeMs     float64
	Speedup    float64
	Efficiency float64
}

func main() {
	fmt.Println("=========================================================================")
	fmt.Println("  HPC Parallel Computing Lab: Automated Benchmark Suite")
	fmt.Println("=========================================================================")

	cwd, _ := os.Getwd()
	fmt.Printf("Working directory: %s\n", cwd)

	sizes := []int{1_000_000, 10_000_000, 100_000_000}
	if len(os.Args) > 1 && os.Args[1] == "full" {
		sizes = append(sizes, 1_000_000_000)
	}

	results := make([]BenchmarkEntry, 0)

	// Build Go executables
	fmt.Println("\n>>> Compiling Go executables...")
	exec.Command("go", "build", "-o", "bin/bench_go", "main.go").Run()

	// 1. Run Go Benchmarks
	for _, size := range sizes {
		fmt.Printf("\n--- Running Go benchmarks for N = %d ---\n", size)
		cmd := exec.Command("go", "run", "main.go", strconv.Itoa(size))
		cmd.Dir = filepath.Join(cwd, "go")
		var out bytes.Buffer
		cmd.Stdout = &out
		cmd.Stderr = os.Stderr
		if err := cmd.Run(); err != nil {
			fmt.Printf("Go benchmark error: %v\n", err)
			continue
		}
		parseOutput("Go", size, out.String(), &results)
	}

	// 2. Run Java Benchmarks
	for _, size := range sizes {
		fmt.Printf("\n--- Running Java benchmarks for N = %d ---\n", size)
		cmd := exec.Command("java", "--enable-native-access=ALL-UNNAMED", "-Xmx14g",
			"-cp", "target/classes", "ua.kpi.hpc.Main", strconv.Itoa(size))
		cmd.Dir = filepath.Join(cwd, "java")
		var out bytes.Buffer
		cmd.Stdout = &out
		cmd.Stderr = os.Stderr
		if err := cmd.Run(); err != nil {
			fmt.Printf("Java benchmark error: %v\n", err)
			continue
		}
		parseOutput("Java", size, out.String(), &results)
	}

	// 3. Run MPI Benchmarks (WSL OpenMPI or MS-MPI)
	mpiSizes := []int{1_000_000, 10_000_000, 100_000_000}
	mpiRanks := []int{1, 2, 4, 8}

	for _, size := range mpiSizes {
		fmt.Printf("\n--- Running MPI benchmarks for N = %d ---\n", size)
		var seqTimeTask1, seqTimeTask2 float64

		// Baseline sequentially
		for _, r := range results {
			if r.Size == size && r.Algorithm == "Sequential" && r.Language == "Go" {
				seqTimeTask1 = r.TimeMs
				break
			}
		}
		for _, r := range results {
			if r.Size == size && r.Algorithm == "Sequential Wave" && r.Language == "Go" {
				seqTimeTask2 = r.TimeMs
				break
			}
		}

		for _, np := range mpiRanks {
			// Go MPI Task 1
			goTask1Ms := runMpi("wsl", "-e", "bash", "-c",
				fmt.Sprintf("cd /mnt/c/uni/'3 Task'/go && mpirun --allow-run-as-root --oversubscribe -np %d bin/mpi_task1 %d", np, size))
			if goTask1Ms > 0 {
				sp := 1.0
				if seqTimeTask1 > 0 {
					sp = seqTimeTask1 / goTask1Ms
				}
				results = append(results, BenchmarkEntry{
					Language:    "Go",
					Task:        "Task 1 (Partitioning)",
					Algorithm:   "MPI",
					Parallelism: np,
					Size:        size,
					TimeMs:      goTask1Ms,
					Speedup:     sp,
					Efficiency:  sp / float64(np),
				})
			}

			// Go MPI Task 2
			goTask2Ms := runMpi("wsl", "-e", "bash", "-c",
				fmt.Sprintf("cd /mnt/c/uni/'3 Task'/go && mpirun --allow-run-as-root --oversubscribe -np %d bin/mpi_task2 %d", np, size))
			if goTask2Ms > 0 {
				sp := 1.0
				if seqTimeTask2 > 0 {
					sp = seqTimeTask2 / goTask2Ms
				}
				results = append(results, BenchmarkEntry{
					Language:    "Go",
					Task:        "Task 2 (Wave)",
					Algorithm:   "MPI",
					Parallelism: np,
					Size:        size,
					TimeMs:      goTask2Ms,
					Speedup:     sp,
					Efficiency:  sp / float64(np),
				})
			}

			// Java MPI Task 1
			javaTask1Ms := runMpi("wsl", "-e", "bash", "-c",
				fmt.Sprintf("cd /mnt/c/uni/'3 Task'/java && mpirun --allow-run-as-root --oversubscribe -np %d java --enable-native-access=ALL-UNNAMED -cp target/classes ua.kpi.hpc.mpi.MpiTask1Sum %d", np, size))
			if javaTask1Ms > 0 {
				sp := 1.0
				if seqTimeTask1 > 0 {
					sp = seqTimeTask1 / javaTask1Ms
				}
				results = append(results, BenchmarkEntry{
					Language:    "Java",
					Task:        "Task 1 (Partitioning)",
					Algorithm:   "MPI",
					Parallelism: np,
					Size:        size,
					TimeMs:      javaTask1Ms,
					Speedup:     sp,
					Efficiency:  sp / float64(np),
				})
			}

			// Java MPI Task 2
			javaTask2Ms := runMpi("wsl", "-e", "bash", "-c",
				fmt.Sprintf("cd /mnt/c/uni/'3 Task'/java && mpirun --allow-run-as-root --oversubscribe -np %d java --enable-native-access=ALL-UNNAMED -cp target/classes ua.kpi.hpc.mpi.MpiTask2WaveSum %d", np, size))
			if javaTask2Ms > 0 {
				sp := 1.0
				if seqTimeTask2 > 0 {
					sp = seqTimeTask2 / javaTask2Ms
				}
				results = append(results, BenchmarkEntry{
					Language:    "Java",
					Task:        "Task 2 (Wave)",
					Algorithm:   "MPI",
					Parallelism: np,
					Size:        size,
					TimeMs:      javaTask2Ms,
					Speedup:     sp,
					Efficiency:  sp / float64(np),
				})
			}
		}
	}

	// Generate Markdown Reports & Tables
	generateMarkdownTables(results)
}

func parseOutput(lang string, size int, output string, results *[]BenchmarkEntry) {
	lines := strings.Split(output, "\n")
	currentTask := "Task 1 (Partitioning)"
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if strings.Contains(line, "TASK 2") {
			currentTask = "Task 2 (Wave)"
			continue
		}
		if !strings.Contains(line, "|") || strings.Contains(line, "Algorithm") || strings.Contains(line, "---") {
			continue
		}
		parts := strings.Split(line, "|")
		if len(parts) < 5 {
			continue
		}
		algo := strings.TrimSpace(parts[0])
		pStr := strings.TrimSpace(parts[1])
		pStr = strings.TrimPrefix(pStr, "P=")
		p, _ := strconv.Atoi(pStr)

		timeStr := strings.TrimSpace(parts[2])
		timeStr = strings.TrimSuffix(timeStr, " ms")
		timeStr = strings.ReplaceAll(timeStr, ",", ".")
		t, _ := strconv.ParseFloat(timeStr, 64)

		spStr := strings.TrimSpace(parts[3])
		spStr = strings.TrimSuffix(spStr, "x")
		spStr = strings.ReplaceAll(spStr, ",", ".")
		sp, _ := strconv.ParseFloat(spStr, 64)

		effStr := strings.TrimSpace(parts[4])
		effStr = strings.TrimSuffix(effStr, "%")
		effStr = strings.ReplaceAll(effStr, ",", ".")
		eff, _ := strconv.ParseFloat(effStr, 64)

		*results = append(*results, BenchmarkEntry{
			Language:    lang,
			Task:        currentTask,
			Algorithm:   algo,
			Parallelism: p,
			Size:        size,
			TimeMs:      t,
			Speedup:     sp,
			Efficiency:  eff / 100.0,
		})
	}
}

func runMpi(name string, args ...string) float64 {
	cmd := exec.Command(name, args...)
	var out bytes.Buffer
	cmd.Stdout = &out
	if err := cmd.Run(); err != nil {
		return -1
	}
	output := out.String()
	// Parse "Time: X ms"
	idx := strings.Index(output, "Time: ")
	if idx == -1 {
		return -1
	}
	sub := output[idx+6:]
	msIdx := strings.Index(sub, " ms")
	if msIdx == -1 {
		return -1
	}
	val, err := strconv.ParseFloat(strings.TrimSpace(sub[:msIdx]), 64)
	if err != nil {
		return -1
	}
	return val
}

func generateMarkdownTables(results []BenchmarkEntry) {
	f, err := os.Create("benchmarks/tables/benchmark_results.md")
	if err != nil {
		fmt.Printf("Error creating benchmark table file: %v\n", err)
		return
	}
	defer f.Close()

	fmt.Fprintln(f, "# Зведені результати бенчмаркінгу продуктивності (HPC Parallel Computing Lab)")
	fmt.Fprintf(f, "*Дата вимірювань: %s*\n\n", time.Now().Format("2006-01-02 15:04:05"))
	fmt.Fprintln(f, "Апаратна конфігурація: AMD Ryzen 5 5600H (6 фізичних ядер, 12 потоків), 28 ГБ RAM DDR4, WSL2 Ubuntu 26.04 (Open MPI 5.0) / Windows 11 (MS-MPI 10.1).\n")

	// Group by Size
	sizesMap := make(map[int]bool)
	for _, r := range results {
		sizesMap[r.Size] = true
	}

	sizes := []int{1_000_000, 10_000_000, 100_000_000, 1_000_000_000}
	for _, sz := range sizes {
		if !sizesMap[sz] {
			continue
		}
		fmt.Fprintf(f, "## Розмір масиву: %,d елементів (~%.2f МБ)\n\n", sz, float64(sz*8)/(1024*1024))

		// Task 1 Table
		fmt.Fprintln(f, "### Завдання №1: Розпаралелювання методом поділу на частини (Chunking / Partitioning)")
		fmt.Fprintln(f, "| Мова | Алгоритм | Потоки/Процеси (P) | Час виконання (мс) | Прискорення (Speedup) | Ефективність (Efficiency) |")
		fmt.Fprintln(f, "| :--- | :--- | :---: | :---: | :---: | :---: |")
		for _, r := range results {
			if r.Size == sz && r.Task == "Task 1 (Partitioning)" {
				fmt.Fprintf(f, "| %s | %s | %d | %.3f ms | %.2fx | %.2f%% |\n",
					r.Language, r.Algorithm, r.Parallelism, r.TimeMs, r.Speedup, r.Efficiency*100.0)
			}
		}
		fmt.Fprintln(f)

		// Task 2 Table
		fmt.Fprintln(f, "### Завдання №2: Метод здвоювання (Хвилевий алгоритм / Wave Reduction)")
		fmt.Fprintln(f, "| Мова | Алгоритм | Потоки/Процеси (P) | Час виконання (мс) | Прискорення (Speedup) | Ефективність (Efficiency) |")
		fmt.Fprintln(f, "| :--- | :--- | :---: | :---: | :---: | :---: |")
		for _, r := range results {
			if r.Size == sz && r.Task == "Task 2 (Wave)" {
				fmt.Fprintf(f, "| %s | %s | %d | %.3f ms | %.2fx | %.2f%% |\n",
					r.Language, r.Algorithm, r.Parallelism, r.TimeMs, r.Speedup, r.Efficiency*100.0)
			}
		}
		fmt.Fprintln(f)
	}

	fmt.Println("\n>>> Benchmark markdown report written to benchmarks/tables/benchmark_results.md!")
}
