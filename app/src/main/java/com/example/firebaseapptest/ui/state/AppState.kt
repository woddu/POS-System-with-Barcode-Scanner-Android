package com.example.firebaseapptest.ui.state

import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.helpermodels.ItemForSale
import com.example.firebaseapptest.data.local.entity.helpermodels.SaleWithItemNames
import com.example.firebaseapptest.ui.view.SalesFilter

data class AppState(
    val items : List<ItemForSale> = emptyList(),

    val sales : List<Sale> = emptyList(),

    val itemsInCounter: List<ItemForSale> = emptyList(),

    val navigateToScanner: Boolean = false,
    val navigateBackTo: String = "",

    val itemsInCounterTotalPrice: Double = 0.0,

    val  itemNotFound: Boolean = false,

    val salesFilter: SalesFilter = SalesFilter.ALL,
    val startDate: Long? = null,
    val endDate: Long? = null,

    val currentPage: Int = 1,
    val lastPage: Int = 1,


    val saleWithItemNames: SaleWithItemNames? = null
)