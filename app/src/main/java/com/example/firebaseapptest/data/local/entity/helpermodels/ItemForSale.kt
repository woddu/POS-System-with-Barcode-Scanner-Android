package com.example.firebaseapptest.data.local.entity.helpermodels

data class ItemForSale(
    val code: Long,
    val name: String,
    val price: Double,
    val discount: Double,
    val isDiscountPercentage: Boolean
)