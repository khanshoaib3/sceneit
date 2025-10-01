package com.github.khanshoaib3.sceneit.repository

import com.github.khanshoaib3.sceneit.PostgresTestcontainersConfiguration
import com.github.khanshoaib3.sceneit.model.UserEntity
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionSystemException
import kotlin.test.Test
import kotlin.test.assertEquals

@AutoConfigureWebTestClient
@Import(PostgresTestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRepositoryTest(
    @param:Autowired val userRepository: UserRepository,
) {

    @AfterEach
    fun afterAll() {
        userRepository.deleteAll()
    }

    @Test
    fun `should register user successfully`() {
        val testEntity = UserEntity(
            username = "testuser",
            password = "easports",
        )
        userRepository.save(testEntity)
        assertNotNull(userRepository.findByUsername("testuser"))
    }

    @Test
    fun `should fail registration when username exist`() {
        val testEntity = UserEntity(
            username = "testuser",
            password = "eassports",
            email = "test@gmail.com"
        )
        userRepository.save(testEntity)

        // Username exists
        val testEntity2 = UserEntity(
            username = "testuser",
            password = "easports"
        )
        assertThrows<DataIntegrityViolationException> {
            userRepository.save(testEntity2)
        }
    }

    @Test
    fun `should fail registration when email exist`() {
        val testEntity = UserEntity(
            username = "testuser",
            password = "eassports",
            email = "test@gmail.com"
        )
        userRepository.save(testEntity)

        // Email exists
        val testEntity3 = UserEntity(
            username = "testuser3",
            password = "easports",
            email = "test@gmail.com"
        )
        assertThrows<DataIntegrityViolationException> {
            userRepository.save(testEntity3)
        }
    }

    @Test
    fun `should fail when invalid email is provided`() {
        val testEntity4 = UserEntity(
            username = "testuser4",
            password = "easports",
            email = "test"
        )
        assertThrows<ConstraintViolationException> {
            userRepository.save(testEntity4)
        }
    }

    @Test
    fun `existsByUsername and existsByEmail should return true if present`() {
        val entity = UserEntity(username = "u1", password = "pw", email = "u1@test.com")
        userRepository.save(entity)

        assertTrue(userRepository.existsByUsername("u1"))
        assertTrue(userRepository.existsByEmail("u1@test.com"))
        assertFalse(userRepository.existsByUsername("unknown"))
    }

    @Test
    fun `createdAt and lastModifiedAt should be set`() {
        val entity = userRepository.save(UserEntity("u2", "pw"))
        assertNotNull(entity.createdAt)
        assertNotNull(entity.lastModifiedAt)
    }

    @Test
    fun `should update email successfully`() {
        val entity = userRepository.save(UserEntity("u3", "pw"))
        entity.email = "new@test.com"
        val updated = userRepository.save(entity)

        assertEquals("new@test.com", updated.email)

        entity.email = "new"
        assertThrows<TransactionSystemException> {
            userRepository.save(entity)
        }
    }

    @Test
    fun `should fail when updated email is invalid`() {
        val entity = userRepository.save(UserEntity("u3", "pw"))
        entity.email = "new"

        assertThrows<TransactionSystemException> {
            userRepository.save(entity)
        }
    }
}