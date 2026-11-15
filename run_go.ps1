# ==============================================================================
# Скрипт запуску всіх Go-застосунків та тестів (HPC Lab)
# Використання:
#   .\run_go.ps1              - запуск тестів та бенчмарку на 10,000,000 елементів
#   .\run_go.ps1 50000000     - запуск для власного розміру масиву
#   .\run_go.ps1 1B           - запуск на 1 мільярді елементів
# ==============================================================================
param(
    [string]$Size = "10000000"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$GoDir = Join-Path $ScriptDir "go"

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ">>> Запуск лабораторії HPC: GO (Go 1.27)" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

Push-Location $GoDir
try {
    # 1. Запуск модульних тестів (correctness_test.go)
    Write-Host "`n[Крок 1/3] Запуск юніт-тестів Go..." -ForegroundColor Yellow
    & go test -v .

    # 2. Запуск локального бенчмарку
    Write-Host "`n[Крок 2/3] Запуск бенчмарку (поділ на частини + хвильовий метод)..." -ForegroundColor Yellow
    Write-Host "Розмір масиву: $Size елементів" -ForegroundColor Gray
    & go run main.go $Size

    # 3. Перевірка MPI, якщо доступно
    Write-Host "`n[Крок 3/3] Перевірка розподілених MPI-програм Go..." -ForegroundColor Yellow
    if (Get-Command mpiexec -ErrorAction SilentlyContinue) {
        $binDir = Join-Path $GoDir "bin"
        if (!(Test-Path $binDir)) { New-Item -ItemType Directory -Force -Path $binDir | Out-Null }

        $mpi1 = Join-Path $binDir "mpi_task1.exe"
        $mpi2 = Join-Path $binDir "mpi_task2.exe"

        # Якщо CGO/MPI скомпільовано на Windows або через WSL
        if (Test-Path $mpi1) {
            Write-Host "--- Go MPI Завдання 1 (Chunking + MPI_Reduce) ---" -ForegroundColor Green
            & mpiexec -n 4 $mpi1 $Size

            Write-Host "`n--- Go MPI Завдання 2 (Wave + MPI_Barrier) ---" -ForegroundColor Green
            & mpiexec -n 4 $mpi2 $Size
        } elseif (Get-Command wsl -ErrorAction SilentlyContinue) {
            Write-Host "Запуск Go MPI через WSL (OpenMPI)..." -ForegroundColor Green
            wsl -e bash -c "cd /mnt/c/uni/'3 Task'/go && go build -o bin/mpi_task1 ./cmd/mpi_task1 && go build -o bin/mpi_task2 ./cmd/mpi_task2 && mpirun --allow-run-as-root -np 4 ./bin/mpi_task1 $Size && mpirun --allow-run-as-root -np 4 ./bin/mpi_task2 $Size"
        } else {
            Write-Host "MPI бінарники не знайдено і WSL не встановлено. Пропуск Go MPI." -ForegroundColor DarkGray
        }
    } else {
        Write-Host "mpiexec не знайдено. Пропуск Go MPI." -ForegroundColor DarkGray
    }
} finally {
    Pop-Location
}

Write-Host "`n=====================================================================" -ForegroundColor Cyan
Write-Host ">>> Виконання Go-застосунків завершено успішно!" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
