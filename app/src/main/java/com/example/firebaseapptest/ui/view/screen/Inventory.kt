package com.example.firebaseapptest.ui.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inventory(){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BasicAlertDialog(
            onDismissRequest = { /*TODO*/ },

        ){
            Text("Add Inventory")
            TextField(
                value = "aa",
                onValueChange = {

                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvPreview(){
    Inventory()
}