package ru.vishnyakov

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class TenantsRepositoryTest {

    private lateinit var repo: TenantsRepository

    @BeforeEach
    fun init() {
        repo = TenantsRepository()
    }


    @Test
    fun saveAndFind() {
        val tenant = Tenant(
            id = UUID.randomUUID(),
            email = "a@a.a"
        )
        repo.save(tenant)
        assertEquals(tenant, repo.find(tenant.id))
    }

}