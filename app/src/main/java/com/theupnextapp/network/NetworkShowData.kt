package com.theupnextapp.network

data class NetworkShowData(
    var id: Int,
    val url: String?,
    val name: String?,
    val status: String?,
    val air_time: String?,
    val runtime: String?,
    val premiered: String?,
    val trailer_url: String?,
    val medium_image_url: String?,
    val original_image_url: String?,
    val create_date: String? = null,
    val update_date: String?,
    val local_image_url: String?
)