package com.skydoves.moviecompose.models.entities

import androidx.compose.runtime.Immutable

@Immutable
data class OdooContext(
    val lang: String,
    val tz: String)
