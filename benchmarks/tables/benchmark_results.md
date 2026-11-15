# Зведені результати бенчмаркінгу продуктивності (HPC Parallel Computing Lab)
*Дата вимірювань: 2026-11-15 19:49:25*

Апаратна конфігурація: AMD Ryzen 5 5600H (6 фізичних ядер, 12 потоків), 28 ГБ RAM DDR4, WSL2 Ubuntu 26.04 (Open MPI 5.0) / Windows 11 (MS-MPI 10.1).

## Розмір масиву: %!,(int=1000000)d елементів (~7.63 МБ)

### Завдання №1: Розпаралелювання методом поділу на частини (Chunking / Partitioning)
| Мова | Алгоритм | Потоки/Процеси (P) | Час виконання (мс) | Прискорення (Speedup) | Ефективність (Efficiency) |
| :--- | :--- | :---: | :---: | :---: | :---: |
| Go | Sequential | 1 | 0.178 ms | 1.00x | 100.00% |
| Go | Channels | 1 | 0.357 ms | 0.50x | 49.90% |
| Go | WaitGroup | 1 | 0.179 ms | 0.99x | 99.39% |
| Go | Worker Pool | 1 | 0.356 ms | 0.50x | 49.99% |
| Go | Channels | 2 | 0.176 ms | 1.01x | 50.53% |
| Go | WaitGroup | 2 | 0.177 ms | 1.00x | 50.23% |
| Go | Worker Pool | 2 | 0.178 ms | 1.00x | 49.92% |
| Go | Channels | 4 | 0.178 ms | 1.00x | 25.07% |
| Go | WaitGroup | 4 | 0.000 ms | +Infx | +Inf% |
| Go | Worker Pool | 4 | 0.180 ms | 0.99x | 24.81% |
| Go | Channels | 8 | 0.176 ms | 1.01x | 12.65% |
| Go | WaitGroup | 8 | 0.000 ms | +Infx | +Inf% |
| Go | Worker Pool | 8 | 0.000 ms | +Infx | +Inf% |
| Go | Channels | 12 | 0.000 ms | +Infx | +Inf% |
| Go | WaitGroup | 12 | 0.177 ms | 1.00x | 8.37% |
| Go | Worker Pool | 12 | 0.181 ms | 0.98x | 8.18% |
| Java | Sequential | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 12 | 0.000 ms | 0.00x | 0.00% |
| Go | MPI | 1 | 0.408 ms | 0.44x | 43.63% |
| Java | MPI | 1 | 11.759 ms | 0.02x | 1.51% |
| Go | MPI | 2 | 0.330 ms | 0.54x | 26.97% |
| Java | MPI | 2 | 10.600 ms | 0.02x | 0.84% |
| Go | MPI | 4 | 0.248 ms | 0.72x | 17.94% |
| Java | MPI | 4 | 10.541 ms | 0.02x | 0.42% |
| Go | MPI | 8 | 0.399 ms | 0.45x | 5.58% |
| Java | MPI | 8 | 17.549 ms | 0.01x | 0.13% |

### Завдання №2: Метод здвоювання (Хвилевий алгоритм / Wave Reduction)
| Мова | Алгоритм | Потоки/Процеси (P) | Час виконання (мс) | Прискорення (Speedup) | Ефективність (Efficiency) |
| :--- | :--- | :---: | :---: | :---: | :---: |
| Go | Sequential Wave | 1 | 0.734 ms | 1.00x | 100.00% |
| Go | Goroutines + WaitGroup | 1 | 1.108 ms | 0.66x | 66.26% |
| Go | Worker Pool | 1 | 0.570 ms | 1.29x | 128.67% |
| Go | Channels | 1 | 0.547 ms | 1.34x | 134.22% |
| Go | Goroutines + WaitGroup | 2 | 0.562 ms | 1.31x | 65.32% |
| Go | Worker Pool | 2 | 0.374 ms | 1.96x | 98.22% |
| Go | Channels | 2 | 0.753 ms | 0.97x | 48.70% |
| Go | Goroutines + WaitGroup | 4 | 0.372 ms | 1.98x | 49.38% |
| Go | Worker Pool | 4 | 0.000 ms | +Infx | +Inf% |
| Go | Channels | 4 | 0.190 ms | 3.87x | 96.74% |
| Go | Goroutines + WaitGroup | 8 | 0.558 ms | 1.32x | 16.44% |
| Go | Worker Pool | 8 | 0.557 ms | 1.32x | 16.47% |
| Go | Channels | 8 | 0.000 ms | +Infx | +Inf% |
| Go | Goroutines + WaitGroup | 12 | 0.576 ms | 1.27x | 10.62% |
| Go | Worker Pool | 12 | 0.533 ms | 1.38x | 11.46% |
| Go | Channels | 12 | 0.000 ms | +Infx | +Inf% |
| Java | Sequential Wave | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 12 | 0.000 ms | 0.00x | 0.00% |
| Go | MPI | 1 | 1.003 ms | 0.73x | 73.18% |
| Java | MPI | 1 | 10.822 ms | 0.07x | 6.78% |
| Go | MPI | 2 | 0.474 ms | 1.55x | 77.43% |
| Java | MPI | 2 | 10.494 ms | 0.07x | 3.50% |
| Go | MPI | 4 | 0.406 ms | 1.81x | 45.20% |
| Java | MPI | 4 | 13.426 ms | 0.05x | 1.37% |
| Go | MPI | 8 | 1.176 ms | 0.62x | 7.80% |
| Java | MPI | 8 | 24.565 ms | 0.03x | 0.37% |

## Розмір масиву: %!,(int=10000000)d елементів (~76.29 МБ)

### Завдання №1: Розпаралелювання методом поділу на частини (Chunking / Partitioning)
| Мова | Алгоритм | Потоки/Процеси (P) | Час виконання (мс) | Прискорення (Speedup) | Ефективність (Efficiency) |
| :--- | :--- | :---: | :---: | :---: | :---: |
| Go | Sequential | 1 | 4.680 ms | 1.00x | 100.00% |
| Go | Channels | 1 | 5.006 ms | 0.93x | 93.49% |
| Go | WaitGroup | 1 | 4.787 ms | 0.98x | 97.76% |
| Go | Worker Pool | 1 | 4.382 ms | 1.07x | 106.80% |
| Go | Channels | 2 | 2.260 ms | 2.07x | 103.52% |
| Go | WaitGroup | 2 | 3.824 ms | 1.22x | 61.18% |
| Go | Worker Pool | 2 | 2.707 ms | 1.73x | 86.42% |
| Go | Channels | 4 | 1.782 ms | 2.63x | 65.67% |
| Go | WaitGroup | 4 | 2.464 ms | 1.90x | 47.47% |
| Go | Worker Pool | 4 | 1.828 ms | 2.56x | 64.00% |
| Go | Channels | 8 | 2.115 ms | 2.21x | 27.65% |
| Go | WaitGroup | 8 | 2.266 ms | 2.07x | 25.82% |
| Go | Worker Pool | 8 | 2.093 ms | 2.24x | 27.94% |
| Go | Channels | 12 | 2.203 ms | 2.12x | 17.70% |
| Go | WaitGroup | 12 | 1.954 ms | 2.39x | 19.95% |
| Go | Worker Pool | 12 | 1.628 ms | 2.87x | 23.95% |
| Java | Sequential | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 12 | 0.000 ms | 0.00x | 0.00% |
| Go | MPI | 1 | 4.231 ms | 1.11x | 110.61% |
| Java | MPI | 1 | 17.457 ms | 0.27x | 26.81% |
| Go | MPI | 2 | 2.681 ms | 1.75x | 87.28% |
| Java | MPI | 2 | 14.548 ms | 0.32x | 16.08% |
| Go | MPI | 4 | 2.711 ms | 1.73x | 43.16% |
| Java | MPI | 4 | 14.834 ms | 0.32x | 7.89% |
| Go | MPI | 8 | 1.985 ms | 2.36x | 29.47% |
| Java | MPI | 8 | 24.389 ms | 0.19x | 2.40% |

### Завдання №2: Метод здвоювання (Хвилевий алгоритм / Wave Reduction)
| Мова | Алгоритм | Потоки/Процеси (P) | Час виконання (мс) | Прискорення (Speedup) | Ефективність (Efficiency) |
| :--- | :--- | :---: | :---: | :---: | :---: |
| Go | Sequential Wave | 1 | 8.802 ms | 1.00x | 100.00% |
| Go | Goroutines + WaitGroup | 1 | 10.120 ms | 0.87x | 86.98% |
| Go | Worker Pool | 1 | 9.764 ms | 0.90x | 90.14% |
| Go | Channels | 1 | 9.632 ms | 0.91x | 91.39% |
| Go | Goroutines + WaitGroup | 2 | 6.739 ms | 1.31x | 65.31% |
| Go | Worker Pool | 2 | 7.687 ms | 1.15x | 57.25% |
| Go | Channels | 2 | 7.228 ms | 1.22x | 60.89% |
| Go | Goroutines + WaitGroup | 4 | 6.680 ms | 1.32x | 32.94% |
| Go | Worker Pool | 4 | 6.255 ms | 1.41x | 35.18% |
| Go | Channels | 4 | 6.963 ms | 1.26x | 31.60% |
| Go | Goroutines + WaitGroup | 8 | 6.711 ms | 1.31x | 16.40% |
| Go | Worker Pool | 8 | 6.883 ms | 1.28x | 15.98% |
| Go | Channels | 8 | 7.610 ms | 1.16x | 14.46% |
| Go | Goroutines + WaitGroup | 12 | 7.218 ms | 1.22x | 10.16% |
| Go | Worker Pool | 12 | 6.909 ms | 1.27x | 10.62% |
| Go | Channels | 12 | 6.515 ms | 1.35x | 11.26% |
| Java | Sequential Wave | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 12 | 0.000 ms | 0.00x | 0.00% |
| Go | MPI | 1 | 9.526 ms | 0.92x | 92.40% |
| Java | MPI | 1 | 24.053 ms | 0.37x | 36.59% |
| Go | MPI | 2 | 7.735 ms | 1.14x | 56.90% |
| Java | MPI | 2 | 21.915 ms | 0.40x | 20.08% |
| Go | MPI | 4 | 7.204 ms | 1.22x | 30.55% |
| Java | MPI | 4 | 22.428 ms | 0.39x | 9.81% |
| Go | MPI | 8 | 3.972 ms | 2.22x | 27.70% |
| Java | MPI | 8 | 35.798 ms | 0.25x | 3.07% |

## Розмір масиву: %!,(int=100000000)d елементів (~762.94 МБ)

### Завдання №1: Розпаралелювання методом поділу на частини (Chunking / Partitioning)
| Мова | Алгоритм | Потоки/Процеси (P) | Час виконання (мс) | Прискорення (Speedup) | Ефективність (Efficiency) |
| :--- | :--- | :---: | :---: | :---: | :---: |
| Go | Sequential | 1 | 45.426 ms | 1.00x | 100.00% |
| Go | Channels | 1 | 42.875 ms | 1.06x | 105.95% |
| Go | WaitGroup | 1 | 43.016 ms | 1.06x | 105.60% |
| Go | Worker Pool | 1 | 41.481 ms | 1.10x | 109.51% |
| Go | Channels | 2 | 28.643 ms | 1.59x | 79.30% |
| Go | WaitGroup | 2 | 29.141 ms | 1.56x | 77.94% |
| Go | Worker Pool | 2 | 26.391 ms | 1.72x | 86.06% |
| Go | Channels | 4 | 20.410 ms | 2.23x | 55.64% |
| Go | WaitGroup | 4 | 20.971 ms | 2.17x | 54.15% |
| Go | Worker Pool | 4 | 21.434 ms | 2.12x | 52.98% |
| Go | Channels | 8 | 21.315 ms | 2.13x | 26.64% |
| Go | WaitGroup | 8 | 21.137 ms | 2.15x | 26.86% |
| Go | Worker Pool | 8 | 21.446 ms | 2.12x | 26.48% |
| Go | Channels | 12 | 21.992 ms | 2.07x | 17.21% |
| Go | WaitGroup | 12 | 22.319 ms | 2.04x | 16.96% |
| Go | Worker Pool | 12 | 21.555 ms | 2.11x | 17.56% |
| Java | Sequential | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread-based | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 12 | 0.000 ms | 0.00x | 0.00% |
| Go | MPI | 1 | 41.157 ms | 1.10x | 110.37% |
| Java | MPI | 1 | 64.968 ms | 0.70x | 69.92% |
| Go | MPI | 2 | 31.038 ms | 1.46x | 73.18% |
| Java | MPI | 2 | 34.550 ms | 1.31x | 65.74% |
| Go | MPI | 4 | 24.722 ms | 1.84x | 45.94% |
| Java | MPI | 4 | 33.362 ms | 1.36x | 34.04% |
| Go | MPI | 8 | 20.620 ms | 2.20x | 27.54% |
| Java | MPI | 8 | 54.150 ms | 0.84x | 10.49% |

### Завдання №2: Метод здвоювання (Хвилевий алгоритм / Wave Reduction)
| Мова | Алгоритм | Потоки/Процеси (P) | Час виконання (мс) | Прискорення (Speedup) | Ефективність (Efficiency) |
| :--- | :--- | :---: | :---: | :---: | :---: |
| Go | Sequential Wave | 1 | 98.250 ms | 1.00x | 100.00% |
| Go | Goroutines + WaitGroup | 1 | 107.678 ms | 0.91x | 91.24% |
| Go | Worker Pool | 1 | 101.175 ms | 0.97x | 97.11% |
| Go | Channels | 1 | 99.659 ms | 0.99x | 98.59% |
| Go | Goroutines + WaitGroup | 2 | 72.902 ms | 1.35x | 67.39% |
| Go | Worker Pool | 2 | 77.623 ms | 1.27x | 63.29% |
| Go | Channels | 2 | 79.558 ms | 1.23x | 61.75% |
| Go | Goroutines + WaitGroup | 4 | 72.575 ms | 1.35x | 33.84% |
| Go | Worker Pool | 4 | 75.359 ms | 1.30x | 32.59% |
| Go | Channels | 4 | 72.617 ms | 1.35x | 33.82% |
| Go | Goroutines + WaitGroup | 8 | 72.431 ms | 1.36x | 16.96% |
| Go | Worker Pool | 8 | 73.745 ms | 1.33x | 16.65% |
| Go | Channels | 8 | 73.459 ms | 1.34x | 16.72% |
| Go | Goroutines + WaitGroup | 12 | 73.684 ms | 1.33x | 11.11% |
| Go | Worker Pool | 12 | 73.669 ms | 1.33x | 11.11% |
| Go | Channels | 12 | 72.200 ms | 1.36x | 11.34% |
| Java | Sequential Wave | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 1 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 2 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 4 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 8 | 0.000 ms | 0.00x | 0.00% |
| Java | Thread Pool (Barrier) | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ExecutorService | 12 | 0.000 ms | 0.00x | 0.00% |
| Java | ForkJoinPool | 12 | 0.000 ms | 0.00x | 0.00% |
| Go | MPI | 1 | 102.611 ms | 0.96x | 95.75% |
| Java | MPI | 1 | 112.304 ms | 0.87x | 87.49% |
| Go | MPI | 2 | 76.323 ms | 1.29x | 64.36% |
| Java | MPI | 2 | 88.569 ms | 1.11x | 55.47% |
| Go | MPI | 4 | 71.610 ms | 1.37x | 34.30% |
| Java | MPI | 4 | 85.634 ms | 1.15x | 28.68% |
| Go | MPI | 8 | 79.715 ms | 1.23x | 15.41% |
| Java | MPI | 8 | 94.210 ms | 1.04x | 13.04% |

