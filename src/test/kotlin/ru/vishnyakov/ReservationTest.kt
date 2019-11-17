package ru.vishnyakov

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import java.util.stream.Stream

class ReservationTest {

    @Test
    fun reserve() {
        val r = reservation()
        val tenant = tenant()
        r.reserve(
            tenant = tenant,
            flat = flat(r.flatId)
        )
        assertEquals(ReservationStatus.RESERVED, r.status)
        assertEquals(tenant.id, r.tenantId)
    }

    @Test
    fun reserveLess24Hours() {
        assertThrows(ValidationException::class.java) {
            val r = reservation(time = Instant.now())
            val tenant = tenant()
            r.reserve(
                tenant = tenant,
                flat = flat(r.flatId)
            )
        }
    }

    @Test
    fun reserveWithBadStatuses() {
        Stream.of(ReservationStatus.RESERVED, ReservationStatus.APPROVED, ReservationStatus.REJECTED).forEach {
            assertThrows(ValidationException::class.java) {
                val r = reservation(status = it)
                r.reserve(
                    tenant = tenant(),
                    flat = flat(r.flatId)
                )
            }
        }
    }

    @Test
    fun reserveWithCurrentTenant() {
        val r = reservation()
        val tenantId = UUID.randomUUID()
        assertThrows(IllegalAccessException::class.java) {
            r.reserve(
                tenant = tenant(id = tenantId),
                flat = flat(id = r.flatId, tenantId = tenantId)
            )
        }
    }

    @Test
    fun reserveWithAnotherFlat() {
        assertThrows(IllegalAccessException::class.java) {
            reservation().reserve(
                tenant = tenant(),
                flat = flat()
            )
        }
    }

    @Test
    fun cancelWithAnotherTenant() {
        assertThrows(IllegalAccessException::class.java) {
            reservation().cancel(
                tenant = tenant()
            )
        }
    }

    @Test
    fun cancel() {
        Stream.of(ReservationStatus.RESERVED, ReservationStatus.APPROVED).forEach {
            val r = reservation(status = it)
            r.cancel(
                tenant = tenant(id = r.tenantId!!)
            )
            assertEquals(ReservationStatus.FREE, r.status)
            assertNull(r.tenantId)
        }
    }

    @Test
    fun cancelBadStatus() {
        Stream.of(ReservationStatus.FREE, ReservationStatus.REJECTED).forEach {
            assertThrows(ValidationException::class.java) {
                val r = reservation(status = it)
                r.cancel(
                    tenant = tenant(id = r.tenantId!!)
                )
                assertEquals(ReservationStatus.FREE, r.status)
                assertNull(r.tenantId)
            }
        }
    }

    @Test
    fun approve() {
        val r = reservation(status = ReservationStatus.RESERVED)
        val tenant = tenant()
        r.approve(
            tenant = tenant,
            flat = flat(id = r.flatId, tenantId = tenant.id)
        )
        assertEquals(ReservationStatus.APPROVED, r.status)
    }

    @Test
    fun approveWithAnotherTenant() {
        val r = reservation(status = ReservationStatus.RESERVED)
        val tenant = tenant()
        assertThrows(IllegalAccessException::class.java) {
            r.approve(
                tenant = tenant,
                flat = flat(id = r.flatId)
            )
        }
    }

    @Test
    fun approveWithAnotherFlat() {
        val r = reservation(status = ReservationStatus.RESERVED)
        val tenant = tenant()
        assertThrows(IllegalAccessException::class.java) {
            r.approve(
                tenant = tenant,
                flat = flat(tenantId = tenant.id)
            )
        }
    }

    @Test
    fun approveWithBadStatuses() {
        Stream.of(ReservationStatus.FREE, ReservationStatus.APPROVED, ReservationStatus.REJECTED).forEach {
            assertThrows(ValidationException::class.java) {
                val r = reservation(status = it)
                val tenant = tenant()
                r.approve(
                    tenant = tenant,
                    flat = flat(id = r.flatId, tenantId = tenant.id)
                )
            }
        }
    }

    @Test
    fun reject() {
        val r = reservation(status = ReservationStatus.RESERVED)
        val tenant = tenant()
        r.reject(
            tenant = tenant,
            flat = flat(id = r.flatId, tenantId = tenant.id)
        )
        assertEquals(ReservationStatus.REJECTED, r.status)
    }

    @Test
    fun rejectWithAnotherTenant() {
        val r = reservation(status = ReservationStatus.RESERVED)
        val tenant = tenant()
        assertThrows(IllegalAccessException::class.java) {
            r.reject(
                tenant = tenant,
                flat = flat(id = r.flatId)
            )
        }
    }

    @Test
    fun rejectWithAnotherFlat() {
        val r = reservation(status = ReservationStatus.RESERVED)
        val tenant = tenant()
        assertThrows(IllegalAccessException::class.java) {
            r.reject(
                tenant = tenant,
                flat = flat(tenantId = tenant.id)
            )
        }
    }

    @Test
    fun rejectWithBadStatuses() {
        Stream.of(ReservationStatus.FREE, ReservationStatus.APPROVED, ReservationStatus.REJECTED).forEach {
            assertThrows(ValidationException::class.java) {
                val r = reservation(status = it)
                val tenant = tenant()
                r.reject(
                    tenant = tenant,
                    flat = flat(id = r.flatId, tenantId = tenant.id)
                )
            }
        }
    }

    private fun tenant(id: UUID = UUID.randomUUID(), email: String = "a@a.aa") = Tenant(
        id = id,
        email = email
    )

    private fun flat(id: UUID = UUID.randomUUID(), address: String = "address", tenantId:UUID = UUID.randomUUID()) = Flat(
        id = id,
        address = address,
        tenantId = tenantId
    )

    private fun reservation(
        id: UUID = UUID.randomUUID(),
        status: ReservationStatus = ReservationStatus.FREE,
        time: Instant = Instant.now().plusSeconds(2*86400),
        flatId: UUID = UUID.randomUUID(),
        tenantId : UUID = UUID.randomUUID())= Reservation(
        id = id,
        time = time,
        flatId = flatId,
        status = status,
        tenantId = tenantId
    )
}