package ddwu.com.mobile.finalreport

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import ddwu.com.mobile.finalreport.databinding.ActivityMyAddrBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MyAddrActivity : AppCompatActivity() {
    private val TAG = "MyAddrActivityTag"

    val myAddrBinding by lazy {
        ActivityMyAddrBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var geocoder : Geocoder
    private lateinit var currentLoc : Location

    private lateinit var googleMap : GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(myAddrBinding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())
        getLastLocation()   // 최종위치 확인

        myAddrBinding.btnPermit.setOnClickListener {
            checkPermissions()
        }

        myAddrBinding.btnLastLoc.setOnClickListener {
            getLastLocation()
        }

        myAddrBinding.btnLocStart.setOnClickListener {
            startLocUpdates()
        }

        myAddrBinding.btnLocStop.setOnClickListener {
            fusedLocationClient.removeLocationUpdates(locCallback)
        }

        val mapFragment : SupportMapFragment
        = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(mapReadyCallback)

    }

    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "GoogleMap is Ready")
        }
    }
    fun checkPermissions() {
        if (checkSelfPermission(ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                showData("Permissions are already granted")
            } else {
                locationPermissionRequest.launch(arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION))
        }
    }

    val locationPermissionRequest
    = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
        when {
            permissions.getOrDefault(ACCESS_FINE_LOCATION, false) ->
            {
                showData("FINE_LOCATION is granted")
            }
            permissions.getOrDefault(ACCESS_COARSE_LOCATION, false) ->
            {
                showData("COARSE_LOCATION is granted")
            }
            else -> {
                showData("Location permissions are required")
            }
        }
    }

    val locRequest : com.google.android.gms.location.LocationRequest = com.google.android.gms.location.LocationRequest.Builder(0)
        .setMinUpdateIntervalMillis(0)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .build()

    val locCallback : LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locResult: LocationResult) {
            val currentLoc : Location = locResult.locations[0]
//            val targetLoc: LatLng = LatLng(currentLoc.latitude, currentLoc.longitude)
//
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))

            geocoder.getFromLocation(currentLoc.latitude, currentLoc.longitude, 5) {
                addresses ->
                CoroutineScope(Dispatchers.Main).launch {
                    showData(addresses.get(0).getAddressLine(0).toString())
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locRequest,
            locCallback,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener{
            location ->
            if (location != null) {
                val targetLoc: LatLng = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))
                geocoder.getFromLocation(location.latitude, location.longitude, 5){
                    addresses ->
                    CoroutineScope(Dispatchers.Main).launch {
                        showData(addresses.get(0).getAddressLine(0))
                    }
                }
            }
            else {
                currentLoc = Location("기본 위치")      // Last Location 이 null 경우 기본으로 설정
                currentLoc.latitude = 37.606816
                currentLoc.longitude = 127.042383
            }
        }
    }
    private fun showData(data : String) {
        myAddrBinding.tvData.setText(myAddrBinding.tvData.text.toString() + "\n${data}")
    }
}