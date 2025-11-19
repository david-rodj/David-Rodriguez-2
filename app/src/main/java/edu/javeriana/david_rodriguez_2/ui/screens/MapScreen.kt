package edu.javeriana.david_rodriguez_2.ui.screens

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Minimize
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import edu.javeriana.david_rodriguez_2.R
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MapScreen() {
    val bogotaCoords = LatLng(4.658768900734289, -74.0934688649813)

    val routePoints = remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val totalDistanceKm = remember { mutableDoubleStateOf(0.0) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogotaCoords, 14f)
    }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapLongClick = { latLng ->
                routePoints.value = routePoints.value + latLng
                totalDistanceKm.doubleValue = calculateTotalDistanceKm(routePoints.value)
            }
        ) {
            for (i in 0 until routePoints.value.size - 1) {
                val start = routePoints.value[i]
                val end = routePoints.value[i + 1]

                val color = when {
                    totalDistanceKm.doubleValue < 2 -> Color.Green
                    totalDistanceKm.doubleValue <= 5 -> Color.Yellow
                    else -> Color.Red
                }

                Polyline(
                    points = listOf(start, end),
                    color = color,
                    width = 20f
                )
            }

            // Marcador de inicio
            if (routePoints.value.isNotEmpty()) {
                val inicioState = remember(routePoints.value.first()) {
                    MarkerState(position = routePoints.value.first())
                }
                Marker(
                    state = inicioState,
                    title = "Inicio",
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.inicio)
                )
            }

            // Marcador de fin
            if (routePoints.value.size >= 2) {
                val finState = remember(routePoints.value.last()) {
                    MarkerState(position = routePoints.value.last())
                }
                Marker(
                    state = finState,
                    title = "Fin",
                    snippet = String.format(Locale.US, "%.2f km", totalDistanceKm.doubleValue),
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.fin)
                )
            }
        }

        // Botones de zoom en la parte inferior
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.zoomIn()
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(24.dp))
            }

            FloatingActionButton(
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.zoomOut()
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Minimize,
                    contentDescription = "Zoom Out",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Botón de reiniciar en la esquina superior derecha
        FloatingActionButton(
            onClick = {
                routePoints.value = emptyList()
                totalDistanceKm.doubleValue = 0.0
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(bogotaCoords, 14f)
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reiniciar")
        }

        // Información en la parte superior
        if (routePoints.value.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Puntos: ${routePoints.value.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Distancia: %.2f km".format(totalDistanceKm.doubleValue),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Mantén presionado para agregar puntos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun calculateTotalDistanceKm(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0
    var totalDistanceMeters = 0.0
    val results = FloatArray(1)

    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)
        totalDistanceMeters += results[0]
    }

    return totalDistanceMeters / 1000.0
}