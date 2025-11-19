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
import androidx.compose.material.icons.filled.Close

@Composable
fun MapScreen() {
    val bogotaCoords = LatLng(4.658768900734289, -74.0934688649813)

    // Estado para los puntos de la ruta
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var totalDistanceKm by remember { mutableStateOf(0.0) }
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogotaCoords, 14f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // GoogleMap
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapLongClick = { latLng ->
                routePoints = routePoints + latLng
                totalDistanceKm = calculateTotalDistanceKm(routePoints)
            }
        ) {
            // Dibujar polylines para cada segmento
            for (i in 0 until routePoints.size - 1) {
                val start = routePoints[i]
                val end = routePoints[i + 1]

                val color = when {
                    totalDistanceKm < 2 -> Color.Green
                    totalDistanceKm <= 5 -> Color.Yellow
                    else -> Color.Red
                }

                Polyline(
                    points = listOf(start, end),
                    color = color,
                    width = 20f
                )
            }

            // Marcador de inicio
            if (routePoints.isNotEmpty()) {
                Marker(
                    state = MarkerState(position = routePoints.first()),
                    title = "Inicio"
                )
            }

            // Marcador de fin
            if (routePoints.size >= 2) {
                Marker(
                    state = MarkerState(position = routePoints.last()),
                    title = "Fin",
                    snippet = String.format("%.2f km", totalDistanceKm)
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
                    cameraPositionState.animate(
                        CameraUpdateFactory.zoomIn()
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(24.dp))
            }

            FloatingActionButton(
                onClick = {
                    cameraPositionState.animate(
                        CameraUpdateFactory.zoomOut()
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Zoom Out",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Botón de reiniciar en la esquina superior derecha
        FloatingActionButton(
            onClick = {
                routePoints = emptyList()
                totalDistanceKm = 0.0
                cameraPositionState.position = CameraPosition.fromLatLngZoom(bogotaCoords, 14f)
            },
            containerColor = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reiniciar")
        }

        // Información en la parte superior
        if (routePoints.isNotEmpty()) {
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
                        "Puntos: ${routePoints.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Distancia: %.2f km".format(totalDistanceKm),
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