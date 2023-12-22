package ddwu.com.mobile.finalreport

import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ddwu.com.mobile.finalreport.data.ParkingRoot

import ddwu.com.mobile.finalreport.databinding.ActivitySearchAddrBinding
import ddwu.com.mobile.finalreport.network.ParkingAPIService
import ddwu.com.mobile.finalreport.ui.ParkingAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale


class SearchAddrActivity : AppCompatActivity() {
    private val TAG = "SearchAddrActivity"
    lateinit var searchAddrBinding : ActivitySearchAddrBinding
    lateinit var adapter : ParkingAdapter
    private lateinit var googleMap : GoogleMap
    private lateinit var geocoder : Geocoder
    var centerMarker : Marker? = null
    private lateinit var markers : List<Marker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchAddrBinding = ActivitySearchAddrBinding.inflate(layoutInflater)
        setContentView(searchAddrBinding.root)
        geocoder = Geocoder(this)

        adapter = ParkingAdapter()
        searchAddrBinding.rvParking.adapter = adapter
        searchAddrBinding.rvParking.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.parking_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ParkingAPIService::class.java)
        var totalCount : Long = 0

        searchAddrBinding.btnSearch.setOnClickListener {
            //주소 (**구) 입력 받기
            val targetAddr = searchAddrBinding.etAddr.text.toString()

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

                        geocoder.getFromLocationName(targetAddr, totalCount.toInt()) {
                            addresses ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (addresses.isNotEmpty()) {
                                    val targetLoc = LatLng(addresses[0].latitude, addresses[0].longitude)
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 12F))
                                } else {
                                    Toast.makeText(
                                        this@SearchAddrActivity,
                                        "해당 주소의 위치를 찾을 수 없습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
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
                                                val geocoder = Geocoder(this@SearchAddrActivity, Locale.getDefault())
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
                                                    Toast.makeText(this@SearchAddrActivity, "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }

                                    }
                                    adapter.setOnItemClickListener(object : ParkingAdapter.OnItemClickListner {
                                        override fun onItemClick(view: View, position: Int) {
                                            // 클릭된 주차장 정보를 다음 Activity로 전달하고 해당 Activity를 시작
                                            val intent = Intent(this@SearchAddrActivity, ParkingDetailActivity::class.java)
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

        val mapFragment : SupportMapFragment
                = supportFragmentManager.findFragmentById(R.id.search_map) as SupportMapFragment

        mapFragment.getMapAsync(mapReadyCallback)

    }

    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "GoogleMap is Ready")

            googleMap.setOnMarkerClickListener { marker
                -> Toast.makeText(this@SearchAddrActivity, marker.tag.toString(), Toast.LENGTH_SHORT).show()
                false
            }

            googleMap.setOnInfoWindowClickListener { marker ->
                Toast.makeText(this@SearchAddrActivity, marker.title, Toast.LENGTH_SHORT).show()
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



}