# ==============================================================================
# Скрипт запуску всіх Java-застосунків та тестів (HPC Lab)
# Використання:
#   .\run_java.ps1              - запуск тестів та бенчмарку на 10,000,000 елементів
#   .\run_java.ps1 50000000     - запуск для власного розміру масиву
#   .\run_java.ps1 1B           - запуск на 1 мільярді елементів
# ==============================================================================
param(
    [string]$Size = "10000000"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JavaDir = Join-Path $ScriptDir "java"

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ">>> Запуск лабораторії HPC: JAVA (Java 25 LTS)" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

# 1. Збірка JAR та виконання юніт-тестів
Write-Host "`n[Крок 1/3] Компіляція та запуск юніт-тестів (JUnit 5)..." -ForegroundColor Yellow

$jarPath = Join-Path $JavaDir "target\parallel-computing-lab-1.0.0.jar"

# Перевіряємо наявність mvn або запускаємо через WSL
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Push-Location $JavaDir
    try {
        mvn test
        mvn package -DskipTests
    } finally {
        Pop-Location
    }
} elseif (Get-Command wsl -ErrorAction SilentlyContinue) {
    wsl -e bash -c "cd /mnt/c/uni/'3 Task'/java && mvn test && mvn package -DskipTests"
} else {
    Write-Host "Maven не знайдено в PATH, компілюємо напряму через javac..." -ForegroundColor Gray
    $classesDir = Join-Path $JavaDir "target\classes"
    if (!(Test-Path $classesDir)) { New-Item -ItemType Directory -Force -Path $classesDir | Out-Null }
    $srcFiles = Get-ChildItem -Path (Join-Path $JavaDir "src\main\java") -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
    & javac -d $classesDir $srcFiles
    & jar cfe $jarPath ua.kpi.hpc.Main -C $classesDir .
}

if (!(Test-Path $jarPath)) {
    # Спробуємо знайти альтернативний JAR
    $altJar = Join-Path $JavaDir "target\hpc-lab-1.0.jar"
    if (Test-Path $altJar) {
        $jarPath = $altJar
    }
}

Write-Host "`n[Крок 2/3] Запуск бенчмарку (поділ на частини + хвильовий метод)..." -ForegroundColor Yellow
Write-Host "Розмір масиву: $Size елементів" -ForegroundColor Gray
& java --enable-native-access=ALL-UNNAMED -cp $jarPath ua.kpi.hpc.Main $Size

# 3. Перевірка MPI, якщо встановлено mpiexec
Write-Host "`n[Крок 3/3] Перевірка розподілених MPI-програм..." -ForegroundColor Yellow
if (Get-Command mpiexec -ErrorAction SilentlyContinue) {
    Write-Host "--- MPI Завдання 1 (Chunking + MPI_Reduce) ---" -ForegroundColor Green
    & mpiexec -n 4 java --enable-native-access=ALL-UNNAMED -cp $jarPath ua.kpi.hpc.mpi.MpiTask1Sum $Size

    Write-Host "`n--- MPI Завдання 2 (Wave + MPI_Barrier) ---" -ForegroundColor Green
    & mpiexec -n 4 java --enable-native-access=ALL-UNNAMED -cp $jarPath ua.kpi.hpc.mpi.MpiTask2WaveSum $Size
} else {
    Write-Host "mpiexec не знайдено в системному PATH. Пропуск MPI-кроку." -ForegroundColor DarkGray
}

Write-Host "`n=====================================================================" -ForegroundColor Cyan
Write-Host ">>> Виконання Java-застосунків завершено успішно!" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
