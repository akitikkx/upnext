package com.theupnextapp.domain

data class TableUpdate(
    val id: Long,
    val tableName: String,
    val lastUpdated: Long
)