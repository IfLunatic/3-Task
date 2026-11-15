// Пакет mpi надає CGO-обгортку над MPI C API для Go.
package mpi

/*
#cgo CFLAGS: -I/usr/lib/x86_64-linux-gnu/openmpi/include -I/usr/lib/x86_64-linux-gnu/openmpi/include/openmpi
#cgo LDFLAGS: -L/usr/lib/x86_64-linux-gnu/openmpi/lib -lmpi
#include <mpi.h>
#include <stdlib.h>
*/
import "C"
import (
	"unsafe"
)

// Init ініціалізує MPI-середовище (MPI_Init).
func Init() {
	C.MPI_Init(nil, nil)
}

// Finalize завершує роботу MPI-середовища (MPI_Finalize).
func Finalize() {
	C.MPI_Finalize()
}

// Rank повертає номер поточного процесу в MPI_COMM_WORLD (MPI_Comm_rank).
func Rank() int {
	var rank C.int
	C.MPI_Comm_rank(C.MPI_COMM_WORLD, &rank)
	return int(rank)
}

// Size повертає загальну кількість процесів в MPI_COMM_WORLD (MPI_Comm_size).
func Size() int {
	var size C.int
	C.MPI_Comm_size(C.MPI_COMM_WORLD, &size)
	return int(size)
}

// Wtime повертає поточний астрономічний час у секундах (MPI_Wtime).
func Wtime() float64 {
	return float64(C.MPI_Wtime())
}

// Barrier блокує виконання, доки всі процеси не досягнуть цієї точки (MPI_Barrier).
func Barrier() {
	C.MPI_Barrier(C.MPI_COMM_WORLD)
}

// ReduceSumInt64 агрегує значення localVal з усіх процесів на кореневий процес root за допомогою операції MPI_SUM (MPI_Reduce).
func ReduceSumInt64(localVal int64, root int) int64 {
	var sendVal C.longlong = C.longlong(localVal)
	var recvVal C.longlong

	C.MPI_Reduce(
		unsafe.Pointer(&sendVal),
		unsafe.Pointer(&recvVal),
		1,
		C.MPI_LONG_LONG,
		C.MPI_SUM,
		C.int(root),
		C.MPI_COMM_WORLD,
	)

	return int64(recvVal)
}

// SendInt64 надсилає значення int64 процесу з номером destRank із тегом tag (MPI_Send).
func SendInt64(val int64, destRank, tag int) {
	cVal := C.longlong(val)
	C.MPI_Send(
		unsafe.Pointer(&cVal),
		1,
		C.MPI_LONG_LONG,
		C.int(destRank),
		C.int(tag),
		C.MPI_COMM_WORLD,
	)
}

// RecvInt64 отримує значення int64 від процесу з номером srcRank із тегом tag (MPI_Recv).
func RecvInt64(srcRank, tag int) int64 {
	var recvVal C.longlong
	C.MPI_Recv(
		unsafe.Pointer(&recvVal),
		1,
		C.MPI_LONG_LONG,
		C.int(srcRank),
		C.int(tag),
		C.MPI_COMM_WORLD,
		C.MPI_STATUS_IGNORE,
	)
	return int64(recvVal)
}
