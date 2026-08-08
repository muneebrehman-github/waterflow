package com.spgg.app.data.repository

import com.spgg.app.data.local.SessionStore
import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.AuthApi
import com.spgg.app.data.remote.AuthResponseDto
import com.spgg.app.data.remote.LoginRequestDto
import com.spgg.app.data.remote.RefreshRequestDto
import com.spgg.app.domain.AuthSession
import com.spgg.app.domain.UserRole
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private val authApi: AuthApi = mockk()
    private val sessionStore: SessionStore = mockk(relaxUnitFun = true)
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        repository = AuthRepository(authApi, sessionStore, Json { ignoreUnknownKeys = true })
    }

    @Test
    fun `login success saves and returns the session`() = runTest {
        val dto = AuthResponseDto(
            accessToken = "access",
            refreshToken = "refresh",
            userId = 2,
            fullName = "Ali Khan",
            role = "SHOP_OWNER",
            tenantId = 1
        )
        coEvery { authApi.login(LoginRequestDto("ali@clearsprings.test", "SecurePass1")) } returns
            ApiEnvelope(success = true, data = dto)

        val result = repository.login("ali@clearsprings.test", "SecurePass1")

        assertTrue(result.isSuccess)
        val session = result.getOrThrow()
        assertEquals(UserRole.SHOP_OWNER, session.role)
        assertEquals(1L, session.tenantId)
        verify { sessionStore.saveSession(session) }
    }

    @Test
    fun `login failure surfaces the backend message`() = runTest {
        coEvery { authApi.login(any()) } returns ApiEnvelope(success = false, message = "Invalid email or password")

        val result = repository.login("nobody@example.com", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Invalid email or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout clears the local session even if the network call fails`() = runTest {
        val session = AuthSession("a", "r", 1, "Ali", UserRole.SHOP_OWNER, 1)
        every { sessionStore.getSession() } returns session
        coEvery { authApi.logout(RefreshRequestDto("r")) } throws RuntimeException("network down")

        repository.logout()

        verify { sessionStore.clearSession() }
    }
}
