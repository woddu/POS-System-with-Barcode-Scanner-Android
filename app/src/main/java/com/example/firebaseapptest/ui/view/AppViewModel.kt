package com.example.firebaseapptest.ui.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.repository.InventoryRepository
import com.example.firebaseapptest.ui.view.screen.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.example.firebaseapptest.ui.event.InventoryEvent
import com.example.firebaseapptest.ui.state.InventoryState
import kotlinx.coroutines.flow.first

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {
    private val inventoryPageSize = 20
    private var _currentPage = MutableStateFlow(1)
    private val _items = repository.getAllItemsPaginated(inventoryPageSize, pageNumber(pageNumber(_currentPage.value))).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    private val _sales = repository.getSales().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    private val _state = MutableStateFlow(AppState())
    private val _inventoryState = MutableStateFlow(InventoryState())

    val state = combine(_state, _sales){state, sales ->
        state.copy(
            sales = sales
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppState())

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
            val totalCount = repository.getItemsCount()
            val lastPageIndex = if (totalCount == 0) 0 else (totalCount - 1) / inventoryPageSize
            val items = repository.getAllItemsPaginated(inventoryPageSize, pageNumber(1)).first()
            _inventoryState.update {
                it.copy(
                    currentPage = 1,
                    items = items,
                    lastPage = lastPageIndex + 1
                )
            }
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

                        _state.update {
                            it.copy(
                                itemsInCounter = state.value.itemsInCounter + item
                            )
                        }
                    }
                } else if (_state.value.navigateBackTo == Route.Inventory.path){
                    _inventoryState.update { it.copy(itemCode = event.text) }
                }

            }
            AppEvent.OnScannerConsumed -> {
                _state.update { it.copy(navigateToScanner = false) }
            }

            AppEvent.OnScanComplete -> {

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
                    _inventoryState.update { it.copy(showFormDialog = false) }
                    _inventoryState.update {
                        it.copy(
                            itemName = "",
                            itemPrice = "",
                            itemQuantity = "",
                            itemDescription = "",
                            itemColor = "",
                            itemCode = "",
                            itemSize = "",
                            itemSold = ""
                        )
                    }
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
                            itemCode = item.code.toString(),
                            itemName = item.name,
                            itemPrice = item.price.toString(),
                            itemQuantity = item.quantity.toString(),
                            itemDescription = if (item.description.isNullOrEmpty()) "" else item.description,
                            itemColor = if (item.color.isNullOrEmpty()) "" else item.color,
                            itemSize = if (item.size.isNullOrEmpty()) "" else item.size,
                            itemSold = item.sold.toString()
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
                    _inventoryState.update {
                        it.copy(
                            showFormDialog = false,
                            itemName = "",
                            itemPrice = "",
                            itemQuantity = "",
                            itemDescription = "",
                            itemColor = "",
                            itemCode = "",
                            itemSize = "",
                            itemSold = ""
                        )
                    }
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
                    _currentPage.update { inventoryState.value.currentPage }
                }
            }
            InventoryEvent.OnInventoryLastPage -> {
                viewModelScope.launch {
                    val totalCount = repository.getItemsCount()
                    val lastPageIndex = if (totalCount == 0) 0 else (totalCount - 1) / inventoryPageSize
                    val items = repository.getAllItemsPaginated(inventoryPageSize, pageNumber(lastPageIndex)).first()
                    _inventoryState.update {
                        it.copy(
                            currentPage = lastPageIndex + 1,
                            items = items,
                            lastPage = lastPageIndex + 1
                        )
                    }
                    _currentPage.update { inventoryState.value.currentPage }
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
                    _currentPage.update { inventoryState.value.currentPage }
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
                    _currentPage.update { inventoryState.value.currentPage }
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
                    _currentPage.update { inventoryState.value.currentPage }
                }

            }
        }
    }

}