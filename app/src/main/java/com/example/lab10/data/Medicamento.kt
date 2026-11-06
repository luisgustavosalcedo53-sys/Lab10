package com.example.lab10.data

import com.google.gson.annotations.SerializedName

data class Medicamento(
    @SerializedName("id")
    var id: Int,
    @SerializedName("nombre")
    var nombre: String,
    @SerializedName("presentacion")
    var presentacion: String,
    @SerializedName("cantidad")
    var cantidad: Int,
    @SerializedName("ubicacion")
    var ubicacion: String
)