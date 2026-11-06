package com.tecsup.lab10.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tecsup.lab10.data.SerieApiService
import com.tecsup.lab10.data.SerieModel
import kotlinx.coroutines.delay

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    var listaSeries: SnapshotStateList<SerieModel> = remember { mutableStateListOf() }

    LaunchedEffect(Unit) {
        val listado = servicio.selectSeries()
        listado.forEach { listaSeries.add(it) }
    }

    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text("SERIE", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                Text("Accion", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f))
            }
        }
        items(listaSeries) { item ->
            Row(
                modifier = Modifier.padding(start = 8.dp).fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = item.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                IconButton(
                    onClick = { navController.navigate("seriever/${item.id}") },
                    Modifier.weight(0.15f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver")
                }
                IconButton(
                    onClick = { navController.navigate("serieDel/${item.id}") },
                    Modifier.weight(0.15f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    var id by remember { mutableStateOf(pid) }
    var name by remember { mutableStateOf("") }
    var release_date by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var grabar by remember { mutableStateOf(false) }

    if (id != 0) {
        LaunchedEffect(Unit) {
            val objSerie = servicio.selectSerie(id.toString())
            delay(100)
            name = objSerie.body()?.name ?: ""
            release_date = objSerie.body()?.release_date ?: ""
            rating = objSerie.body()?.rating?.toString() ?: ""
            category = objSerie.body()?.category ?: ""
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = id.toString(), onValueChange = { }, label = { Text("ID (solo lectura)") }, readOnly = true, singleLine = true)
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name: ") }, singleLine = true)
        TextField(value = release_date, onValueChange = { release_date = it }, label = { Text("Release Date:") }, singleLine = true)
        TextField(value = rating, onValueChange = { rating = it }, label = { Text("Rating:") }, singleLine = true)
        TextField(value = category, onValueChange = { category = it }, label = { Text("Category:") }, singleLine = true)
        Button(onClick = { grabar = true }) { Text("Grabar", fontSize = 16.sp) }
    }

    if (grabar) {
        val objSerie = SerieModel(id, name, release_date, rating.toIntOrNull() ?: 0, category)
        LaunchedEffect(Unit) {
            if (id == 0) servicio.insertSerie(objSerie) else servicio.updateSerie(id.toString(), objSerie)
            grabar = false
            navController.navigate("series")
        }
    }
}

@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    var showDialog by remember { mutableStateOf(true) }
    var borrar by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = { Button(onClick = { showDialog = false; borrar = true }) { Text("Aceptar") } },
            dismissButton = { Button(onClick = { showDialog = false; navController.navigate("series") }) { Text("Cancelar") } }
        )
    }

    if (borrar) {
        LaunchedEffect(Unit) {
            servicio.deleteSerie(id.toString())
            borrar = false
            navController.navigate("series")
        }
    }
}