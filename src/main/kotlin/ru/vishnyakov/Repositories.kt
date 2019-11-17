package ru.vishnyakov

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class TenantsRepository {
    private val storage = ConcurrentHashMap<UUID, Tenant>()

    fun save(tenant: Tenant): Tenant {
        val copy = tenant.copy()
        storage[tenant.id] = copy
        return copy
    }

    fun find(id: UUID) = storage[id]?.copy() ?: throw NotFoundExceptions(id, Tenant::class)
}

class FlatsRepository {
    private val storage = ConcurrentHashMap<UUID, Flat>()

    fun save(flat: Flat): Flat {
        val copy = flat.copy()
        storage[flat.id] = copy
        return copy
    }

    fun find(id: UUID) = storage[id]?.copy() ?: throw NotFoundExceptions(id, Flat::class)
}

class ReservationsRepository {
    private val storage = ConcurrentHashMap<UUID, Pair<Reservation, Lock>>()

    fun save(reservation: Reservation): Reservation {
        val copy = reservation.copy()
        storage.merge(reservation.id, Pair(copy, ReentrantLock())) {
                old, new -> Pair(new.first, old.second)
        }
        return copy
    }

    fun find(id: UUID) = storage[id]?.copy() ?: throw NotFoundExceptions(id, Reservation::class)

    fun findByFlat(flatId: UUID) = storage.values.map { it.first }.filter { it.flatId == flatId }.map { it.copy() }
}
