package io.devexpert.appfloracdmx

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapaScreen(lat: Double, lng: Double) {
    val mapView = rememberMapViewWithLifecycle()
    AndroidView({ mapView }) { map ->
        map.getMapAsync { googleMap ->
            val location = LatLng(lat, lng)

            // Configurar los controles del mapa
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Agregar un círculo para representar el área aproximada
            googleMap.addCircle(
                CircleOptions()
                    .center(location) // Centro del círculo
                    .radius(250.0) // Radio del círculo en metros
                    .strokeColor(Color.RED) // Color del borde del círculo
                    .fillColor(Color.argb(50, 255, 0, 0)) // Color de relleno con opacidad
                    .strokeWidth(2f) // Grosor del borde
            )

            // Mover la cámara para enfocar el área
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }
}


@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}
