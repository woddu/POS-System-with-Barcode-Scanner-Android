package com.example.firebaseapptest.ui.view

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.SaleItem
import com.example.firebaseapptest.data.repository.InventoryRepository
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.event.InventoryEvent
import com.example.firebaseapptest.ui.state.AppState
import com.example.firebaseapptest.ui.state.InventoryState
import com.example.firebaseapptest.ui.view.screen.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {
    private val inventoryPageSize = 10
    private val _searchQuery = MutableStateFlow("")
    private var _inventoryCurrentPage = MutableStateFlow(1)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _items = combine(_searchQuery, _inventoryCurrentPage) { query, page ->
        query to page
    }.flatMapLatest { (query, page) ->
        if (query.isBlank()) {
            repository.getAllItemsPaginated(inventoryPageSize, pageNumber(page))
        } else {
            repository.getSearchQueryPaginated(query, inventoryPageSize, pageNumber(page))
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val salePageSize = 10
    private var _saleCurrentPage = MutableStateFlow(1)
    private val _salesFilter = MutableStateFlow(SalesFilter.ALL)
    private val _betweenDates = MutableStateFlow<Pair<Long, Long>?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _sales: StateFlow<List<Sale>> = combine(_salesFilter, _saleCurrentPage, _betweenDates) { filter, page, between ->
        Triple(filter, page, between)
    }.flatMapLatest { (filter, page, between) ->
        val offset = pageNumber(page) // your existing page offset logic
        when (filter) {
            SalesFilter.ALL -> repository.getSalesPaginated(salePageSize, offset)
            SalesFilter.TODAY -> repository.getTodaySalesPaginated(salePageSize, offset)
            SalesFilter.BETWEEN -> {
                val (start, end) = between ?: return@flatMapLatest flowOf(emptyList())
                repository.getSalesBetween(start, end, salePageSize, offset)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    private val _state = MutableStateFlow(AppState())
    val state = combine(_state, _sales, _saleCurrentPage){state, sales, saleCurrentPage->
        state.copy(
            sales = sales,
            currentPage = saleCurrentPage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppState())


    private val _inventoryState = MutableStateFlow(InventoryState())
    val inventoryState = combine(_inventoryState, _items){state, items ->
        state.copy(
            items = items
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryState())

    private fun pageNumber(page: Int): Int{
        return (page - 1) * inventoryPageSize
    }

    init {
        viewModelScope.launch {

            val itemsTotalCount = repository.getItemsCount()
            setItemsLastPage(itemsTotalCount)
            val saleTotalCount = repository.getSaleCount()
            setSaleLastPage(saleTotalCount)
        }
    }

    private fun setItemsLastPage(itemsTotalCount: Int){
        Log.d("ItemsTotalCount", itemsTotalCount.toString())
        _inventoryState.update {
            it.copy(
                lastPage = if (itemsTotalCount <= inventoryPageSize) 1 else maxOf(1, ((itemsTotalCount + inventoryPageSize - 1) / inventoryPageSize))
            )
        }
    }

    private fun setSaleLastPage(saleTotalCount: Int){
        Log.d("SaleTotalCount", saleTotalCount.toString())
        _state.update {
            it.copy(
                lastPage = if (saleTotalCount <= salePageSize) 1 else maxOf(1, ((saleTotalCount + salePageSize - 1) / salePageSize))
            )
        }
    }

    fun onEvent(
        event : AppEvent
    ) {
        when (event){
            AppEvent.OnScanButtonClickedFromHome -> {
                _state.update { it.copy(navigateToScanner = true, navigateBackTo = Route.Home.path) }
            }

            is AppEvent.OnBarcodeScanned -> {
                if (_state.value.navigateBackTo == Route.Home.path){
                    viewModelScope.launch {
                        val item = repository.getItemByCodeForSale(event.text.toLong())

                        if (item != null){
                            if (item.isDiscountPercentage){
                                item.price.minus((item.price.times(item.discount)))
                            }
                            else {
                                item.price.minus(item.discount)
                            }
                        }

                        _state.update {
                            it.copy(
                                itemsInCounter = if (item == null) state.value.itemsInCounter else state.value.itemsInCounter + item,
                                itemsInCounterTotalPrice = if (item == null) state.value.itemsInCounterTotalPrice else state.value.itemsInCounterTotalPrice + item.price,
                                itemNotFound = item == null
                            )
                        }

                        delay(1000)
                        _state.update { it.copy(itemNotFound = false) }

                    }
                } else if (_state.value.navigateBackTo == Route.Inventory.path){
                    _inventoryState.update { it.copy(itemCode = event.text) }
                }

            }

            is AppEvent.OnItemCodeTyped -> {
                viewModelScope.launch {
                    val item = repository.getItemByCodeForSale(event.code.toLong())

                    if (item != null){
                        if (item.isDiscountPercentage){
                            item.price.minus((item.price.times(item.discount)))
                        }
                        else {
                            item.price.minus(item.discount)
                        }
                    }

                    _state.update {
                        it.copy(
                            itemsInCounter = if (item == null) state.value.itemsInCounter else state.value.itemsInCounter + item,
                            itemsInCounterTotalPrice = if (item == null) state.value.itemsInCounterTotalPrice else state.value.itemsInCounterTotalPrice + item.price,
                            itemNotFound = item == null
                        )
                    }

                    delay(1000)
                    _state.update { it.copy(itemNotFound = false) }

                }
            }

            AppEvent.OnScannerConsumed -> {
                _state.update { it.copy(navigateToScanner = false) }
            }

            is AppEvent.OnAddSale -> {
                viewModelScope.launch{

                    val sale = Sale(
                        date = LocalDateTime.now(),
                        total = state.value.itemsInCounterTotalPrice,
                        paymentMethod = state.value.paymentMethod.ifEmpty { "Cash" },
                        amountPaidCash = state.value.amountPaidCash.toDouble(),
                        amountPaidGCash = state.value.amountPaidGCash.toDouble(),
                        change = state.value.change.toDouble()
                    )
                    val saleId = repository.addSale(sale)
                    val saleItems = state.value.itemsInCounter
                        .groupingBy { it.code }
                        .eachCount()
                        .map { (code, quantity) ->
                            val item = state.value.itemsInCounter.first { it.code == code }
                            SaleItem(
                                saleId = saleId.toInt(),
                                itemCode = item.code,
                                price = item.price,
                                quantity = quantity,
                                discount = item.discount,
                                isDiscountPercentage = item.isDiscountPercentage
                            )
                        }
                    repository.addSaleItems(saleItems)
                    saleItems.forEach { saleItem ->
                        repository.itemSold(saleItem.itemCode, saleItem.quantity)
                    }
                }
            }

            AppEvent.OnCancelSale -> {
                state.value.tempImageFile?.delete()
                _state.update {
                    it.copy(
                        itemsInCounter = emptyList(),
                        itemsInCounterTotalPrice = 0.0,
                        imageUri = null,
                        isImageCropped = false,
                        paymentMethod = "",
                        tempImageFile = null
                    )
                }
            }

            is AppEvent.OnSaleDetails -> {
                viewModelScope.launch {
                    val saleWithItems = repository.getSaleWithItems(event.saleId)
                    _state.update {
                        it.copy(
                            saleWithItemNames = saleWithItems,
                        )
                    }
                }
            }

            is AppEvent.OnFilterSales -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            salesFilter = event.salesFilter
                        )
                    }
                    if (event.salesFilter == SalesFilter.BETWEEN && event.betweenDates != null) {
                        _salesFilter.update { event.salesFilter }
                        _betweenDates.update { event.betweenDates }
                        _state.update {
                            it.copy(
                                startDate = event.betweenDates.first,
                                endDate = event.betweenDates.second
                            )
                        }
                        val totalCount = repository.getSaleCountBetween(
                            event.betweenDates.first,
                            event.betweenDates.second
                        )
                        setSaleLastPage(totalCount)
                    } else {
                        _salesFilter.update { event.salesFilter }
                        _betweenDates.update { null }
                        _state.update {
                            it.copy(
                                startDate = null,
                                endDate = null
                            )
                        }
                    }

                    if (event.salesFilter == SalesFilter.ALL){
                        val totalCount = repository.getSaleCount()
                        setSaleLastPage(totalCount)
                    } else if(event.salesFilter == SalesFilter.TODAY){
                        val totalCount = repository.getSaleCountToday()
                        setSaleLastPage(totalCount)
                    }

                    _saleCurrentPage.update { 1 }
                    _state.update {
                        it.copy(
                            currentPage = 1
                        )
                    }
                }
            }

            is AppEvent.OnImageCropped -> {
                _state.update { it.copy(
                    imageUri = event.uri,
                    isImageCropped = true
                ) }
            }
            is AppEvent.OnImageUriChanged -> {
                viewModelScope.launch {
                    if (event.uri == null) {
                        state.value.imageUri?.path?.let { path ->
                            val file = File(path)
                            if (file.exists()) file.delete()
                        }
                        state.value.tempImageFile?.delete()
                        _state.update {
                            it.copy(
                                imageUri = event.uri,
                                paymentMethod = "",
                                isImageCropped = false,
                                tempImageFile = null
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                imageUri = event.uri
                            )
                        }
                    }
                }
            }

            is AppEvent.OnChoosePaymentMethod -> {
                _state.update{ it.copy(
                    paymentMethod = event.method,
                    isPaymentMethodChosen = true
                ) }
            }

            is AppEvent.OnTempImageFileCreated -> {
                _state.update { it.copy(tempImageFile = event.photoFile) }
            }

            is AppEvent.OnAmountPaidChanged -> {
                if(event.isGash){
                    _state.update { it.copy(amountPaidGCash = event.amount) }
                } else {
                    _state.update { it.copy(amountPaidCash = event.amount) }
                }
            }

            is AppEvent.OnGCashReferenceChanged -> {
                _state.update{ it.copy(gCashReference = event.reference) }
            }
        }
    }

    fun onInventoryEvent(
        event: InventoryEvent
    ){
        when (event){

            InventoryEvent.OnScanButtonClickedFromInventory -> {
                _state.update { it.copy(navigateToScanner = true, navigateBackTo = Route.Inventory.path) }
            }

            InventoryEvent.OnInventoryAddButtonClicked -> {
                _inventoryState.update { it.copy(
                    showFormDialog = true,
                    itemName = "",
                    itemPrice = "",
                    itemQuantity = "",
                    itemDescription = "",
                    itemColor = "",
                    itemCode = "",
                    itemSize = "",
                    itemSold = ""
                ) }
            }

            InventoryEvent.OnInventoryAddCanceled -> {
                _inventoryState.update { it.copy(
                    showFormDialog = false,
                    itemName = "",
                    itemPrice = "",
                    itemQuantity = "",
                    itemDescription = "",
                    itemColor = "",
                    itemCode = "",
                    itemSize = "",
                    itemSold = ""
                ) }
            }

            InventoryEvent.OnInventoryAddConfirmed -> {
                viewModelScope.launch {
                    val item = Item(
                        code = inventoryState.value.itemCode.toLong(),
                        name = inventoryState.value.itemName,
                        price = inventoryState.value.itemPrice.toDouble(),
                        quantity = if (inventoryState.value.itemQuantity.isEmpty()) 0 else inventoryState.value.itemQuantity.toInt(),
                        description = inventoryState.value.itemDescription,
                        color = inventoryState.value.itemColor,
                        size = inventoryState.value.itemSize,
                        sold = if (inventoryState.value.itemSold.isEmpty()) 0 else inventoryState.value.itemSold.toInt()
                    )
                    repository.upsertItem(item)
                    _inventoryState.update {
                        it.copy(
                            itemName = "",
                            itemPrice = "",
                            itemQuantity = "",
                            itemDescription = "",
                            itemColor = "",
                            itemCode = "",
                            itemSize = "",
                            itemSold = "",
                            showFormDialog = false
                        )
                    }
                    val totalCount = repository.getItemsCount()
                    setItemsLastPage(totalCount)
                }
            }
            is InventoryEvent.OnInventorySetItemCode -> {
                _inventoryState.update { it.copy(itemCode = event.code) }
            }
            is InventoryEvent.OnInventorySetItemColor -> {
                _inventoryState.update { it.copy(itemColor = event.color) }
            }
            is InventoryEvent.OnInventorySetItemDescription -> {
                _inventoryState.update { it.copy(itemDescription = event.description) }
            }
            is InventoryEvent.OnInventorySetItemName -> {
                _inventoryState.update { it.copy(itemName = event.name) }
            }
            is InventoryEvent.OnInventorySetItemPrice -> {
                _inventoryState.update { it.copy(itemPrice = event.price) }
            }
            is InventoryEvent.OnInventorySetItemQuantity -> {
                _inventoryState.update { it.copy(itemQuantity = event.quantity) }
            }
            is InventoryEvent.OnInventorySetItemSize -> {
                _inventoryState.update { it.copy(itemSize = event.size) }
            }
            is InventoryEvent.OnInventorySetItemSold -> {
                _inventoryState.update { it.copy(itemSold = event.sold) }
            }

            is InventoryEvent.OnInventoryItemDetails -> {
                viewModelScope.launch {
                    val item = repository.getItem(event.code)
                    _inventoryState.update {
                        it.copy(
                            itemCode = item?.code.toString(),
                            itemName = item?.name.toString(),
                            itemPrice = item?.price.toString(),
                            itemQuantity = item?.quantity.toString(),
                            itemDescription = if (item?.description.isNullOrEmpty()) "" else item.description,
                            itemColor = if (item?.color.isNullOrEmpty()) "" else item.color,
                            itemSize = if (item?.size.isNullOrEmpty()) "" else item.size,
                            itemSold = item?.sold.toString()
                        )
                    }
                }
            }

            InventoryEvent.OnInventoryDeleteCanceled -> {
                _inventoryState.update { it.copy(deleteConfirmationDialog = false) }
            }
            is InventoryEvent.OnInventoryDeleteConfirmed -> {
                viewModelScope.launch {
                    val item = repository.getItem(event.code)
                    repository.deleteItem(item)
                    _inventoryState.update { it.copy(
                        deleteConfirmationDialog = false,
                        itemName = "",
                        itemPrice = "",
                        itemQuantity = "",
                        itemDescription = "",
                        itemColor = "",
                        itemCode = "",
                        itemSize = "",
                        itemSold = ""
                    ) }
                    val totalCount = repository.getItemsCount()
                    setItemsLastPage(totalCount)

                }
            }
            InventoryEvent.OnInventoryDeleteWarning -> {
                _inventoryState.update {
                    it.copy(deleteConfirmationDialog = true)
                }
            }
            InventoryEvent.OnInventoryEditConfirmed -> {
                viewModelScope.launch {
                    val item = Item(
                        code = inventoryState.value.itemCode.toLong(),
                        name = inventoryState.value.itemName,
                        price = inventoryState.value.itemPrice.toDouble(),
                        description = inventoryState.value.itemDescription,
                        color = inventoryState.value.itemColor,
                        size = inventoryState.value.itemSize,
                        sold = if (inventoryState.value.itemSold.isEmpty()) 0 else inventoryState.value.itemSold.toInt(),
                        quantity = if (inventoryState.value.itemQuantity.isEmpty()) 0 else inventoryState.value.itemQuantity.toInt()
                    )
                    repository.upsertItem(item)
                }
            }

            InventoryEvent.OnInventoryFirstPage -> {
                viewModelScope.launch {
                    val items = repository.getAllItemsPaginated(inventoryPageSize, pageNumber(1)).first()
                    _inventoryState.update {
                        it.copy(
                            currentPage = 1,
                            items = items
                        )
                    }
                    _inventoryCurrentPage.update { inventoryState.value.currentPage }
                }
            }
            InventoryEvent.OnInventoryLastPage -> {
                viewModelScope.launch {
                    val totalCount = repository.getItemsCount()
                    val lastPageIndex = maxOf(1, ((totalCount + inventoryPageSize - 1) / inventoryPageSize))
                    val items = repository.getAllItemsPaginated(inventoryPageSize, pageNumber(lastPageIndex)).first()
                    _inventoryState.update {
                        it.copy(
                            currentPage = state.value.lastPage,
                            items = items
                        )
                    }
                    _inventoryCurrentPage.update { inventoryState.value.lastPage }
                }
            }
            InventoryEvent.OnInventoryNextPage -> {
                viewModelScope.launch {
                    val items = repository.getAllItemsPaginated(inventoryPageSize, pageNumber(inventoryState.value.currentPage + 1)).first()

                    _inventoryState.update {
                        it.copy(
                            currentPage = inventoryState.value.currentPage + 1,
                            items = items
                        )
                    }
                    _inventoryCurrentPage.update { inventoryState.value.currentPage }
                }
            }
            is InventoryEvent.OnInventoryPageChanged -> {
                viewModelScope.launch {
                    val items = repository.getAllItemsPaginated(inventoryPageSize, pageNumber(event.page)).first()
                    _inventoryState.update {
                        it.copy(
                            currentPage = event.page,
                            items = items
                        )
                    }
                    _inventoryCurrentPage.update { inventoryState.value.currentPage }
                }
            }
            InventoryEvent.OnInventoryPreviousPage -> {
                viewModelScope.launch {
                    val previousPage = if (inventoryState.value.currentPage > 1) inventoryState.value.currentPage - 1 else 1
                    val items = repository.getAllItemsPaginated(inventoryPageSize, pageNumber(previousPage)).first()
                    _inventoryState.update {
                        it.copy(
                            currentPage = previousPage,
                            items = items
                        )
                    }
                    _inventoryCurrentPage.update { inventoryState.value.currentPage }
                }

            }

            InventoryEvent.OnScannerConsumed -> {
                _state.update { it.copy(navigateToScanner = false) }
            }

            is InventoryEvent.OnSearchQueryChanged -> {
                _searchQuery.update { event.searchTerm }
            }
        }
    }

}