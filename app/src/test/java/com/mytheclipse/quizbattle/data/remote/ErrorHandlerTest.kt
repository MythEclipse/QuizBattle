package com.mytheclipse.quizbattle.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorHandlerTest {

    @Test
    fun toUserFriendlyMessage_mapsNetwork() {
        val msg = ApiException.NetworkException("x").toUserFriendlyMessage()
        assertEquals("Tidak ada koneksi internet", msg)
    }

    @Test
    fun toUserFriendlyMessage_mapsTimeout() {
        val msg = ApiException.TimeoutException("x").toUserFriendlyMessage()
        assertEquals("Koneksi timeout, coba lagi", msg)
    }

    @Test
    fun toUserFriendlyMessage_mapsServer() {
        val msg = ApiException.ServerException(500, "x").toUserFriendlyMessage()
        assertEquals("Server sedang bermasalah", msg)
    }

    @Test
    fun toUserFriendlyMessage_mapsAuth() {
        val msg = ApiException.AuthException("x").toUserFriendlyMessage()
        assertEquals("Sesi berakhir, silakan login kembali", msg)
    }

    @Test
    fun toUserFriendlyMessage_mapsNotFound() {
        val msg = ApiException.NotFoundException("x").toUserFriendlyMessage()
        assertEquals("Data tidak ditemukan", msg)
    }

    @Test
    fun toUserFriendlyMessage_mapsBadRequest() {
        val msg = ApiException.BadRequestException("x").toUserFriendlyMessage()
        assertEquals("Permintaan tidak valid", msg)
    }

    @Test
    fun toUserFriendlyMessage_mapsForbidden() {
        val msg = ApiException.ForbiddenException("x").toUserFriendlyMessage()
        assertEquals("Akses ditolak", msg)
    }

    @Test
    fun toUserFriendlyMessage_mapsConflict() {
        val msg = ApiException.ConflictException("x").toUserFriendlyMessage()
        assertEquals("Data konflik", msg)
    }

    @Test
    fun toUserFriendlyMessage_mapsFallback() {
        val msg = RuntimeException("Unknown").toUserFriendlyMessage()
        assertEquals("Unknown", msg)
    }
}
