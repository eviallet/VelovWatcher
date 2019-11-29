package com.gueg.velovwidget.velov

import java.util.*

data class Velov(
        val standNumber: Int,
        val status: String,
        val rating: Double,
        val ratingCount: Int,
        val ratingLastDate: Date,
        val createdAt: Date,
        val updatedAt: Date,
        val ratingNone: Boolean
)