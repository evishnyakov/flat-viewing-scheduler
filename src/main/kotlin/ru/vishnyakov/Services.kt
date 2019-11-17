package ru.vishnyakov

import java.lang.IllegalArgumentException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.stream.Stream

class TenantsService(
    private val tenants: TenantsRepository
) {
    fun create(email: String) = tenants.save(Tenant(UUID.randomUUID(), email))

    fun find(id: UUID) = tenants.find(id)
}

class ReservationService(
    private val reservations: ReservationsRepository,
    private val tenants: TenantsRepository,
    private val flats: FlatsRepository,
    private val notificationService: NotificationService
) {
    fun fillNextWeek(flat: Flat, startHour: Long = 10, endHour: Long = 20, windowMinutes: Long = 20) {
        if(endHour < startHour) {
            throw IllegalArgumentException("End hour have to be great than start hour")
        }
        if(windowMinutes <= 0) {
            throw IllegalArgumentException("Window have to be more than zero")
        }
        val nextWeek = LocalDate.now(ZoneId.of("UTC")).plusDays(7)
        Arrays.stream(DayOfWeek.values()).map {
            nextWeek.with(it).atStartOfDay().plusHours(startHour)
        }.map {
            Stream.iterate(it, { it.plusMinutes(windowMinutes) }).limit((endHour-startHour)*60/windowMinutes )
        }.flatMap { it }.map {
            it.toInstant(ZoneOffset.UTC)
        }.map {
            Reservation(
                UUID.randomUUID(),
                it,
                flat.id,
                ReservationStatus.FREE,
                null
            )
        }.forEach { reservations.save(it) }
    }

    private fun doAction(id: UUID, action: (Reservation) -> Unit): Boolean {
        val pair = reservations.find(id)
        val lock = pair.second
        val reservation = pair.first
        return if(lock.tryLock()) {
            try {
                action(reservation)
                true
            } finally {
                lock.unlock()
            }
        } else {
            false
        }
    }

    fun reserve(reservationId: UUID, tenantId: UUID) = doAction(reservationId) {
        val flat = flats.find(it.flatId)
        val tenant = tenants.find(tenantId)
        it.reserve(tenant, flat)
        reservations.save(it)
        notificationService.reservationCreated(flat.tenantId, it.flatId)
    }

    fun cancel(reservationId: UUID, tenantId: UUID) = doAction(reservationId) {
        val tenant = tenants.find(tenantId)
        it.cancel(tenant)
        reservations.save(it)
        val flat = flats.find(it.flatId)
        notificationService.reservationCanceled(flat.tenantId, flat.id)
    }

    fun approve(reservationId: UUID, tenantId: UUID) = doAction(reservationId) {
        val flat = flats.find(it.flatId)
        val tenant = tenants.find(tenantId)
        it.approve(tenant, flat)
        reservations.save(it)
        notificationService.reservationApproved(it.tenantId!!, flat.id)
    }

    fun reject(reservationId: UUID, tenantId: UUID) = doAction(reservationId) {
        val flat = flats.find(it.flatId)
        val tenant = tenants.find(tenantId)
        it.reject(tenant, flat)
        reservations.save(it)
        notificationService.reservationRejected(it.tenantId!!, flat.id)
    }

    fun findByFlat(flatId: UUID) = reservations.findByFlat(flatId)

}

class FlatsService(
    private val flats: FlatsRepository,
    private val tenants: TenantsRepository,
    private val reservationService: ReservationService
) {

    fun create(address: String, tenantId: UUID): Flat {
        val tenant = tenants.find(tenantId)
        val flat = Flat(UUID.randomUUID(), address, tenant.id)
        flats.save(flat)
        reservationService.fillNextWeek(flat)
        return flat
    }

    fun find(id: UUID) = flats.find(id)
}

class NotificationService() {
    fun reservationCreated(tenantId: UUID, flatId: UUID) {
        println("Tenant $tenantId reservation has created for a flat $flatId")
    }
    fun reservationCanceled(tenantId: UUID, flatId: UUID) {
        println("Tenant $tenantId reservation has canceled for a flat $flatId")
    }
    fun reservationApproved(tenantId: UUID, flatId: UUID) {
        println("Tenant $tenantId reservation has approved for a flat $flatId")
    }
    fun reservationRejected(tenantId: UUID, flatId: UUID) {
        println("Tenant $tenantId reservation has rejected for a flat $flatId")
    }
}