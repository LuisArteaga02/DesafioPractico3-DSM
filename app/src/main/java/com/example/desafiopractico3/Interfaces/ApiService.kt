package com.example.desafiopractico3.Interfaces

import retrofit2.Response
import retrofit2.http.*
import com.example.desafiopractico3.models.Recurso

interface ApiService {
    @GET("recursos")
    suspend fun getRecursos(): Response<List<Recurso>>

    @POST("recursos")
    suspend fun crearRecurso(@Body recurso: Recurso): Response<Recurso>

    @PUT("recursos/{id}")
    suspend fun actualizarRecurso(@Path("id") id: Int, @Body recurso: Recurso): Response<Recurso>

    @DELETE("recursos/{id}")
    suspend fun eliminarRecurso(@Path("id") id: Int): Response<Unit>
}