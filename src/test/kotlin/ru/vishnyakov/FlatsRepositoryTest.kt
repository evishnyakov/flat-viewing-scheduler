package ru.vishnyakov

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class FlatsRepositoryTest {
    private lateinit var repo: FlatsRepository

    @BeforeEach
    fun init() {
        repo = FlatsRepository()
    }


    @Test
    fun saveAndFind() {
        val flat = Flat(
            id = UUID.randomUUID(),
            address = "address",
            tenantId = UUID.randomUUID()
        )
        repo.save(flat)
        assertEquals(flat, repo.find(flat.id))
    }
}