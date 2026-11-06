package com.example.lab10.data

import retrofit2.http.*

interface MedicamentoApiService {
    @GET("medicamentos")
    suspend fun selectMedicamentos(): ArrayList<Medicamento>

    @GET("medicamentos/{id}")
    suspend fun selectMedicamento(@Path("id") id: String): Medicamento

    @Headers("Content-Type: application/json")
    @POST("medicamentos")
    suspend fun insertMedicamento(@Body serie: Medicamento): Medicamento

    @PUT("medicamentos/{id}")
    suspend fun updateMedicamento(@Path("id") id: String, @Body serie: Medicamento): Medicamento

    @DELETE("medicamentos/{id}")
    suspend fun deleteMedicamento(@Path("id") id: String)
}