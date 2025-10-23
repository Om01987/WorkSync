package com.worksync.app.presentation.ui.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun <T> DropdownMenuTrigger(
    label: String,
    items: List<T>,
    selected: T?,
    onSelected: (T?) -> Unit
) {
    var expanded = remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded.value = true }) {
            Text(label)
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onSelected(null)
                    expanded.value = false
                }
            )
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        onSelected(item)
                        expanded.value = false
                    }
                )
            }
        }
    }
}
