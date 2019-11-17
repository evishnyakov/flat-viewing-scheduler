package ru.vishnyakov

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class ReservationsRepositoryTest {

    private lateinit var repo: ReservationsRepository

    @BeforeEach
    fun init() {
        repo = ReservationsRepository()
    }

    private fun reservation(id: UUID = UUID.randomUUID(), status: ReservationStatus = ReservationStatus.FREE)= Reservation(
        id = id,
        time = Instant.now(),
        flatId = UUID.randomUUID(),
        status = status,
        tenantId = null
    )

    @Test
    fun saveAndFind() {
        val r = reservation()
        repo.save(r)
        val find = repo.find(r.id)
        assertEquals(r, find.first)
        assertNotNull(find.second)
    }

    @Test
    fun ignoreLockWhenUpdate() {
        val r1 = reservation()
        repo.save(r1)
        val find1 = repo.find(r1.id)
        val r2 = reservation(r1.id, ReservationStatus.APPROVED)
        repo.save(r2)
        val find2 = repo.find(r1.id)
        assertEquals(r2, find2.first)
        assertNotEquals(r2, r1)
        assertEquals(find1.second, find2.second)
    }

    @Test
    fun findByFlat() {
        val r = reservation()
        repo.save(r)
        val find = repo.findByFlat(r.flatId)
        assertEquals(1, find.size)
        assertEquals(r, find.get(0))
    }
}