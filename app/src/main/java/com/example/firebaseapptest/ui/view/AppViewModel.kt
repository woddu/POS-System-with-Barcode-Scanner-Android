package com.example.firebaseapptest.ui.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {
    private val _items = repository.getAllItems().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    private val _sales = repository.getSales().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    private val _saleItems = repository.getSaleItems().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    private val _state = MutableStateFlow(AppState())
    val state = combine(_state, _items, _sales, _saleItems){state, items, sales, saleItems ->
        state.copy(
            sales = sales,
            items = items,
            saleItems = saleItems
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppState())


    fun onEvent(
        event : AppEvent
    ) {
        when (event){
            AppEvent.OnScanButtonClicked -> {
                _state.update { it.copy(navigateToScanner = true) }
            }
            is AppEvent.OnBarcodeScanned -> {
                _state.update { it.copy(scannedText = event.text) }
            }
            AppEvent.OnScannerConsumed -> {
                _state.update { it.copy(navigateToScanner = false) }
            }

            AppEvent.OnScanComplete -> {

            }

            AppEvent.OnInventoryAddButtonClicked -> {
                _state.update { it.copy(inventoryShowFormDialog = true) }
            }

            AppEvent.OnInventoryAddCanceled -> {
                _state.update { it.copy(inventoryShowFormDialog = false) }
            }

            AppEvent.OnInventoryAddConfirmed -> {
                viewModelScope.launch {
                    val item = Item(
                        code = state.value.inventoryItemCode.toLong(),
                        name = state.value.inventoryItemName,
                        price = state.value.inventoryItemPrice.toDouble(),
                        quantity = state.value.inventoryItemQuantity.toInt(),
                        description = state.value.inventoryItemDescription,
                        color = state.value.inventoryItemColor,
                        size = state.value.inventoryItemSize,
                        sold = state.value.inventoryItemSold.toInt()
                    )
                    repository.insertItem(item)
                    _state.update { it.copy(inventoryShowFormDialog = false) }
                    _state.update {
                        it.copy(
                            inventoryItemName = "",
                            inventoryItemPrice = "",
                            inventoryItemQuantity = "",
                            inventoryItemDescription = "",
                            inventoryItemColor = "",
                            inventoryItemCode = "",
                            inventoryItemSize = "",
                            inventoryItemSold = ""
                        )
                    }
                }
            }
            is AppEvent.OnInventorySetItemCode -> {
                _state.update { it.copy(inventoryItemCode = event.code) }
            }
            is AppEvent.OnInventorySetItemColor -> {
                _state.update { it.copy(inventoryItemColor = event.color) }
            }
            is AppEvent.OnInventorySetItemDescription -> {
                _state.update { it.copy(inventoryItemDescription = event.description) }
            }
            is AppEvent.OnInventorySetItemName -> {
                _state.update { it.copy(inventoryItemName = event.name) }
            }
            is AppEvent.OnInventorySetItemPrice -> {
                _state.update { it.copy(inventoryItemPrice = event.price) }
            }
            is AppEvent.OnInventorySetItemQuantity -> {
                _state.update { it.copy(inventoryItemQuantity = event.quantity) }
            }
            is AppEvent.OnInventorySetItemSize -> {
                _state.update { it.copy(inventoryItemSize = event.size) }
            }
            is AppEvent.OnInventorySetItemSold -> {
                _state.update { it.copy(inventoryItemSold = event.sold) }
            }
        }

    }

}