package ddwu.com.mobile.finalreport

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ddwu.com.mobile.finalreport.data.ParkingRoot
import ddwu.com.mobile.finalreport.databinding.ActivityMyAddrBinding
import ddwu.com.mobile.finalreport.databinding.ActivitySearchAddrBinding
import ddwu.com.mobile.finalreport.network.ParkingAPIService
import ddwu.com.mobile.finalreport.ui.ParkingAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MyAddrActivity : AppCompatActivity() {
    private val TAG = "MyAddrActivityTag"
    lateinit var searchAddrBinding : ActivitySearchAddrBinding
    lateinit var adapter : ParkingAdapter

    val myAddrBinding by lazy {
        ActivityMyAddrBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var geocoder : Geocoder
    private lateinit var currentLoc : Location

    private lateinit var googleMap : GoogleMap
    var centerMarker : Marker? = null
    private lateinit var markers : List<Marker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(myAddrBinding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())
        getLastLocation()   // 최종위치 확인

        adapter = ParkingAdapter()
        myAddrBinding.rvParking.adapter = adapter
        myAddrBinding.rvParking.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.parking_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ParkingAPIService::class.java)
        var totalCount : Long = 0

        myAddrBinding.btnPermit.setOnClickListener {
            checkPermissions()
        }

        myAddrBinding.btnLastLoc.setOnClickListener {
            getLastLocation()
        }

        myAddrBinding.btnSearch.setOnClickListener {
            val targetAddr = myAddrBinding.tvAddr.text.toString()

            val apiCallback_getTotalCount = object: Callback<ParkingRoot> {
                override fun onResponse(call: Call<ParkingRoot>, response: Response<ParkingRoot>) {
                    if (response.isSuccessful) {
                        val root : ParkingRoot? = response.body()

                        Log.d(TAG, "Root: $root")

                        val parkingInfo = root?.getParkingInfo
                        Log.d(TAG, "ParkingInfo: $parkingInfo")

                        val parkings = parkingInfo?.parkings
                        Log.d(TAG, "Parkings: $parkings")

                        adapter.parkings = parkings
                        Log.d(TAG, "First ParkingName: ${adapter.parkings?.get(0)?.parkingName ?: "null"}")

                        totalCount = response.body()?.getParkingInfo?.listTotalCount ?: 0

                        if(totalCount.toInt() == 0) {
                            Toast.makeText(this@MyAddrActivity, "주차장이 없습니다.", Toast.LENGTH_SHORT).show()
                            return
                        }
                        val apiCallback = object: Callback<ParkingRoot> {
                            override fun onResponse(call: Call<ParkingRoot>, response: Response<ParkingRoot>) {
                                if (response.isSuccessful) {
                                    val root : ParkingRoot? = response.body()

                                    Log.d(TAG, "Root: $root")

                                    val parkingInfo = root?.getParkingInfo
                                    Log.d(TAG, "ParkingInfo: $parkingInfo")

                                    val parkings = parkingInfo?.parkings
                                    Log.d(TAG, "Parkings: $parkings")

                                    adapter.parkings = parkings
                                    Log.d(TAG, "First ParkingName: ${adapter.parkings?.get(0)?.parkingName ?: "null"}")


                                    adapter.parkings = root?.getParkingInfo?.parkings
                                    Log.d(TAG, adapter.parkings?.get(0)?.parkingName ?: "null")

                                    /*주소를 위도,경도로 바꿔서 마커 표시*/
                                    suspend fun getLatLngFromAddress(address: String): LatLng? {
                                        return withContext(Dispatchers.IO) {
                                            try {
                                                val geocoder = Geocoder(this@MyAddrActivity, Locale.getDefault())
                                                val addresses = geocoder.getFromLocationName(address, 1)

                                                if (addresses!!.isNotEmpty()) {
                                                    val latitude = addresses!![0].latitude
                                                    val longitude = addresses[0].longitude
                                                    LatLng(latitude, longitude)
                                                } else {
                                                    null
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                null
                                            }
                                        }
                                    }
                                    if (parkings != null) {
                                        for (parking in parkings){
//                                            geocoder.getFromLocationName(parking.parkingName, totalCount.toInt()) {
//                                                    addresses ->
//                                                CoroutineScope(Dispatchers.Main).launch {
//                                                    if (addresses.isNotEmpty()) {
//                                                        val targetLoc = LatLng(addresses[0].latitude, addresses[0].longitude)
//                                                        addMarker(targetLoc)
//                                                    } else {
//                                                        // 주소를 찾을 수 없는 경우에 대한 처리
//                                                        // 예를 들어, 토스트 메시지를 표시할 수 있습니다.
//                                                        Toast.makeText(this@MyAddrActivity, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
//                                                    }
//                                                }
//                                            }
                                            CoroutineScope(Dispatchers.Main).launch {
                                                val targetLoc = getLatLngFromAddress(parking.addr)

                                                if (targetLoc != null) {
                                                    addMarker(targetLoc, parking.parkingName, parking.capacity.toInt(), parking.curParking.toInt())
                                                } else {
                                                    // 주소를 찾을 수 없는 경우에 대한 처리
                                                    Toast.makeText(this@MyAddrActivity, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                    adapter.setOnItemClickListener(object : ParkingAdapter.OnItemClickListner {
                                        override fun onItemClick(view: View, position: Int) {
                                            // 클릭된 주차장 정보를 다음 Activity로 전달하고 해당 Activity를 시작
                                            val intent = Intent(this@MyAddrActivity, ParkingDetailActivity::class.java)
                                            intent.putExtra("PARKING", adapter.parkings?.get(position))
                                            startActivity(intent)
                                        }
                                    })

                                    adapter.notifyDataSetChanged()
                                }
                                else {
                                    Log.d(TAG, "Unsuccessful Response")
                                }
                            }

                            override fun onFailure(call: Call<ParkingRoot>, t: Throwable) {
                                Log.d(TAG, "OpenAPI Call Failure ${t.message}")
                            }
                        }

                        val apiCall_2 = service.getParkingResult(resources.getString(R.string.parking_key), totalCount, targetAddr.toString())
                        apiCall_2.enqueue(apiCallback)
                    }
                    else {
                        Log.d(TAG, "Unsuccessful Response")
                    }
                }

                override fun onFailure(call: Call<ParkingRoot>, t: Throwable) {
                    Log.d(TAG, "OpenAPI Call Failure ${t.message}")
                }
            }

            val apiCall_1 : Call<ParkingRoot> =
                service.getParkingResult(resources.getString(R.string.parking_key), 5, targetAddr.toString())
            Log.d(TAG, targetAddr)
            Log.d(TAG, "요청 URL: ${apiCall_1.request().url()}")
            Log.d(TAG, "요청 매개변수: key=${resources.getString(R.string.parking_key)}, addr=$targetAddr")
            apiCall_1.enqueue(apiCallback_getTotalCount)
        }


//        myAddrBinding.btnLocStart.setOnClickListener {
//            startLocUpdates()
//        }
//
//        myAddrBinding.btnLocStop.setOnClickListener {
//            fusedLocationClient.removeLocationUpdates(locCallback)
//        }

        val mapFragment : SupportMapFragment
        = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(mapReadyCallback)



    }

    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "GoogleMap is Ready")

            googleMap.setOnMarkerClickListener { marker
                -> Toast.makeText(this@MyAddrActivity, marker.tag.toString(), Toast.LENGTH_SHORT).show()
                false
            }

            googleMap.setOnInfoWindowClickListener { marker ->
                Toast.makeText(this@MyAddrActivity, marker.title, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* 마커 추가 */
    fun addMarker(targetLoc: LatLng, parkingName: String, totalSpaces: Int, currentSpaces: Int) {
        val markerOptions = MarkerOptions()
        markerOptions.position(targetLoc)
            .title(parkingName)
            .snippet("잔여 : ${totalSpaces-currentSpaces}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        centerMarker = googleMap.addMarker(markerOptions)
        centerMarker?.showInfoWindow()
        centerMarker?.tag = targetLoc
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
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 12F))
                geocoder.getFromLocation(location.latitude, location.longitude, 5){
                    addresses ->
                    CoroutineScope(Dispatchers.Main).launch {
                        myAddrBinding.tvAddr.setText(addresses.get(0).subLocality.toString())
                        showData(addresses.get(0).subLocality)
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