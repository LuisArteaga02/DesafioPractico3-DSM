package com.example.desafiopractico3.models

import com.google.gson.annotations.SerializedName

data class Recurso(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("enlace") val enlace: String,
    @SerializedName("imagen") val imagen: String,
    @SerializedName("rating_sum") val ratingSum: Int = 0,
    @SerializedName("rating_count") val ratingCount: Int = 0
)