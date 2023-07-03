package com.example.sqlitebrowser.ui.View

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sqlitebrowser.Domain.UseCase.MainViewEvent
import com.example.sqlitebrowser.R
import com.example.sqlitebrowser.ViewModels.MainViewModel
import com.example.sqlitebrowser.ViewModels.MainViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showSystemUi = true)
fun MainView() {
    val context = LocalContext.current

    val viewModel: MainViewModel =
        viewModel(factory = MainViewModelFactory(context.applicationContext as Application))
    val state = viewModel.state

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
            if (result == null) return@rememberLauncherForActivityResult
            viewModel.onEvent(MainViewEvent.FileSelected(result))
        }

    LaunchedEffect(key1 = context) {
        viewModel.events.collect { event ->
            when (event) {
                MainViewModel.UiEvents.SelectFile -> {
                    launcher.launch("*/*")
                }

                MainViewModel.UiEvents.SqlExportedMessage -> {
                    Toast.makeText(
                        context,
                        "File transferred successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "SQLite Browser", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { viewModel.onEvent(MainViewEvent.DropDrownOpen(!state.isMenuOpen)) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = state.isMenuOpen,
                            onDismissRequest = { viewModel.onEvent(MainViewEvent.DropDrownOpen(!state.isMenuOpen)) }) {
                            DropdownMenuItem(
                                onClick = { viewModel.onEvent(MainViewEvent.DropDownExportDatabaseSelected) },
                                text = {
                                    Text(text = "Export Database")
                                }
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.onEvent(MainViewEvent.OpenFileSelector) }) {
                        val color = if (state.selectedSQLFileError != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_open),
                            contentDescription = "Open",
                            tint = color
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(MainViewEvent.Execute) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_execute),
                            contentDescription = "Execute"
                        )
                    }
                }
            )
        },
        content = {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxSize()
                ) {
                    TextField(
                        modifier = Modifier.fillMaxSize(),
                        value = state.sqlCode,
                        onValueChange = { text ->
                            viewModel.onEvent(MainViewEvent.SQLCodeChanged(text))
                        },
                        label = {
                            Text(text = "SQL Command")
                        },
                        isError = state.sqlCodeError != null
                    )
                }
                if (state.sqlCodeError != null) {
                    Text(text = state.sqlCodeError, color = MaterialTheme.colorScheme.error)
                }
                if (state.selectedSQLFileError != null) {
                    Text(text = state.selectedSQLFileError, color = MaterialTheme.colorScheme.error)
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    state.queryResult?.let { result ->
                        if (result.columns.size > 0) {
                            LazyVerticalGrid(columns = GridCells.Fixed(result.columns.size)) {
                                items(result.columns) { content ->
                                    Box(
                                        modifier = Modifier
                                            .heightIn(60.dp, 60.dp)
                                            .border(2.dp, Color.Black)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = content.uppercase(),
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                items(result.cells) { content ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .border(1.dp, Color.Black)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = content,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
