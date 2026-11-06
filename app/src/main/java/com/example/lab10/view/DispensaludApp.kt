package com.example.lab10.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab10.data.MedicamentoApiService
import com.example.lab10.data.Medicamento
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val URL_SERVIDOR = "http://127.0.0.1:8000/"

@Composable
fun DispensaludApp() {
    val servicio = remember {
        Retrofit.Builder().baseUrl(URL_SERVIDOR)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MedicamentoApiService::class.java)
    }
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.padding(top = 40.dp),
        topBar = { BarraSuperior() },
        bottomBar = { BarraInferior(navController) },
        floatingActionButton = { BotonFAB(navController) },
        content = { paddingValues -> Contenido(paddingValues, navController, servicio) }
    )
}

@Composable
fun BotonFAB(navController: NavHostController) {
    val cbeState by navController.currentBackStackEntryAsState()
    val rutaActual = cbeState?.destination?.route
    if (rutaActual == "medicamentos") {
        FloatingActionButton(
            containerColor = Color.Magenta,
            contentColor = Color.White,
            onClick = { navController.navigate("medicamentoNuevo") }
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Añadir medicamento")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior() {
    CenterAlignedTopAppBar(
        title = { Text(text = "DISPENSALUD", color = Color.White, fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun BarraInferior(navController: NavHostController) {
    val entrada by navController.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    NavigationBar(containerColor = Color.LightGray) {
        listOf(
            Triple("inicio", "Inicio", Icons.Outlined.Home),
            Triple("medicamentos", "Inventario", Icons.Outlined.Favorite),
            Triple("compartimentos", "Ubicación", Icons.Outlined.Place),
            Triple("configuracion", "Ajustes", Icons.Outlined.Settings)
        ).forEach { (destino, etiqueta, icono) ->
            NavigationBarItem(
                icon = { Icon(icono, contentDescription = null) },
                label = { Text(etiqueta) },
                selected = ruta == destino,
                onClick = {
                    navController.navigate(destino) {
                        popUpTo("inicio")
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun Contenido(pv: PaddingValues, navController: NavHostController, servicio: MedicamentoApiService) {
    Box(modifier = Modifier.fillMaxSize().padding(pv)) {
        NavHost(navController = navController, startDestination = "inicio") {
            composable("inicio") { Text("Inventario de medicamentos", modifier = Modifier.padding(16.dp)) }
            composable("medicamentos") { ContenidoMedicamentosListado(navController, servicio) }
            composable("compartimentos") { Compartimentos(servicio) }
            composable("configuracion") { Configuracion(servicio) }
            composable("medicamentoNuevo") { ContenidoMedicamentoEditar(navController, servicio, 0) }
            composable("medicamentoVer/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                ContenidoMedicamentoEditar(navController, servicio, it.arguments!!.getInt("id"))
            }
            composable("medicamentoEliminar/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                ContenidoMedicamentoEliminar(navController, servicio, it.arguments!!.getInt("id"))
            }
        }
    }
}

@Composable
private fun Compartimentos(servicio: MedicamentoApiService) {
    var inventario by remember { mutableStateOf<List<Medicamento>?>(null) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(servicio) {
        try {
            inventario = servicio.selectMedicamentos()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = true
        }
    }

    val grupos = inventario.orEmpty().groupBy { it.ubicacion.trim() }.toSortedMap()
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Compartimentos", style = MaterialTheme.typography.headlineSmall)
        }
        when {
            error -> item { Text("No se pudo cargar el inventario. Revisa la conexión en Ajustes.") }
            inventario == null -> item { Text("Cargando inventario…") }
            grupos.isEmpty() -> item { Text("Agrega un medicamento con una ubicación en Inventario.") }
        }
        items(grupos.entries.toList(), key = { it.key }) { (ubicacion, medicamentos) ->
            Card {
                Column(
                    modifier = Modifier.fillParentMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(ubicacion, style = MaterialTheme.typography.titleMedium)
                    Text("Total de unidades: ${medicamentos.sumOf { it.cantidad.toLong() }}")
                    medicamentos.forEach { medicamento ->
                        Text("${medicamento.nombre} · ${medicamento.cantidad} unidades")
                    }
                }
            }
        }
    }
}

@Composable
private fun Configuracion(servicio: MedicamentoApiService) {
    val scope = rememberCoroutineScope()
    var comprobando by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf("Conexión sin comprobar") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Configuración", style = MaterialTheme.typography.headlineSmall)
        Text("Servidor Django")
        Text("${URL_SERVIDOR}medicamentos")
        Button(
            enabled = !comprobando,
            onClick = {
                comprobando = true
                scope.launch {
                    try {
                        val medicamentos = servicio.selectMedicamentos()
                        estado = "Conexión correcta. Registros: ${medicamentos.size}."
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        estado = "No se pudo conectar. Comprueba que Django esté iniciado."
                    } finally {
                        comprobando = false
                    }
                }
            }
        ) {
            Text(if (comprobando) "Comprobando…" else "Probar conexión")
        }
        Text(estado)
    }
}
