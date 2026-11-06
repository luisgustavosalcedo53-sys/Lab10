package com.example.lab10.view

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.MedicamentoApiService
import com.example.lab10.data.Medicamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

@Composable
fun ContenidoMedicamentosListado(navController: NavHostController, servicio: MedicamentoApiService) {
    val context = LocalContext.current
    val listaMedicamentos: SnapshotStateList<Medicamento> = remember { mutableStateListOf() }

    LaunchedEffect(Unit) {
        try {
            val listado = withContext(Dispatchers.IO) {
                servicio.selectMedicamentos()
            }
            listaMedicamentos.clear()
            listado.forEach { listaMedicamentos.add(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo cargar el inventario", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text("MEDICAMENTO", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                Text("Acción", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f))
            }
        }
        items(listaMedicamentos) { item ->
            Row(
                modifier = Modifier.padding(start = 8.dp).fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = item.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                IconButton(
                    onClick = { navController.navigate("medicamentoVer/${item.id}") },
                    Modifier.weight(0.15f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver")
                }
                IconButton(
                    onClick = { navController.navigate("medicamentoEliminar/${item.id}") },
                    Modifier.weight(0.15f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
fun ContenidoMedicamentoEditar(navController: NavHostController, servicio: MedicamentoApiService, pid: Int = 0) {
    val context = LocalContext.current
    val id = pid
    var nombre by remember { mutableStateOf("") }
    var presentacion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var grabar by remember { mutableStateOf(false) }

    if (id != 0) {
        LaunchedEffect(Unit) {
            try {
                val medicamento = withContext(Dispatchers.IO) {
                    servicio.selectMedicamento(id.toString())
                }
                nombre = medicamento.nombre
                presentacion = medicamento.presentacion
                cantidad = medicamento.cantidad.toString()
                ubicacion = medicamento.ubicacion
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo cargar el medicamento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = id.toString(), onValueChange = { }, label = { Text("ID (solo lectura)") }, readOnly = true, singleLine = true)
        TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true)
        TextField(value = presentacion, onValueChange = { presentacion = it }, label = { Text("Presentación") }, singleLine = true)
        TextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") }, singleLine = true)
        TextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación") }, singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        Button(enabled = !grabar, onClick = {
            val unidades = cantidad.toIntOrNull()
            if (nombre.isBlank() || presentacion.isBlank() || ubicacion.isBlank() || unidades == null || unidades < 0) {
                Toast.makeText(context, "Completa los campos con una cantidad válida", Toast.LENGTH_SHORT).show()
            } else {
                grabar = true
            }
        }) {
            Text("Grabar", fontSize = 16.sp)
        }
    }

    if (grabar) {
        val cantidadIngresada = cantidad.toInt()
        val medicamento = Medicamento(id, nombre, presentacion, cantidadIngresada, ubicacion)

        LaunchedEffect(Unit) {
            try {
                withContext(Dispatchers.IO) {
                    if (id == 0) {
                        servicio.insertMedicamento(medicamento)
                    } else {
                        servicio.updateMedicamento(id.toString(), medicamento)
                    }
                }
                navController.popBackStack()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo guardar el medicamento", Toast.LENGTH_SHORT).show()
            } finally {
                grabar = false
            }
        }
    }
}

@Composable
fun ContenidoMedicamentoEliminar(navController: NavHostController, servicio: MedicamentoApiService, id: Int) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(true) }
    var borrar by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar el medicamento?") },
            confirmButton = { Button(onClick = { showDialog = false; borrar = true }) { Text("Aceptar") } },
            dismissButton = { Button(onClick = { showDialog = false; navController.popBackStack() }) { Text("Cancelar") } }
        )
    }

    if (borrar) {
        LaunchedEffect(Unit) {
            try {
                withContext(Dispatchers.IO) {
                    servicio.deleteMedicamento(id.toString())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo eliminar el medicamento", Toast.LENGTH_SHORT).show()
            }
            borrar = false
            navController.popBackStack()
        }
    }
}