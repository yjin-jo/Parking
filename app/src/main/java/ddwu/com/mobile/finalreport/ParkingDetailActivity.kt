package ddwu.com.mobile.finalreport

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ddwu.com.mobile.finalreport.data.Parking
import ddwu.com.mobile.finalreport.databinding.ActivityParkingDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ParkingDetailActivity : AppCompatActivity() {
    lateinit var parkingDetailBinding: ActivityParkingDetailBinding
    private lateinit var googleMap : GoogleMap
    private lateinit var geocoder : Geocoder
    var centerMarker : Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parkingDetailBinding = ActivityParkingDetailBinding.inflate(layoutInflater)
        setContentView(parkingDetailBinding.root)
        geocoder = Geocoder(this)

        parkingDetailBinding.btnBack.setOnClickListener {
            finish()
        }

        val intent: Intent = intent

        val parking: Parking? = intent.getSerializableExtra("PARKING") as Parking

        if (parking != null) {
            parkingDetailBinding.tvName.text=parking.parkingName
            parkingDetailBinding.tvCount.text="${parking.capacity-parking.curParking} 자리 남았어요!"
            parkingDetailBinding.tvDetailAddr.text=parking.addr
            parkingDetailBinding.tvCapacity.text="총 주차면 : ${parking.capacity}면"
            parkingDetailBinding.tvCurr.text="현재 주차 차량 수 : ${parking.curParking}대"
            parkingDetailBinding.tvRate.text="기본 요금 : ${parking.timeRate}분 ${parking.rates}원"
            parkingDetailBinding.tvAddRate.text="추가 요금 : ${parking.addTimeRate}분 당 ${parking.addRates}원"

            parkingDetailBinding.tvWeek.text="평일 운영 시간 : ${getTime(parking.weekdayBeginTime.toString())}" +
                    "~ ${getTime(parking.weekdayEndTime.toString())}"

            parkingDetailBinding.tvHoli.text="공휴일 운영 시간 : ${getTime(parking.holidayBeginTime.toString())}" +
                    "~ ${getTime(parking.holidayEndTime.toString())}"

            parkingDetailBinding.tvTel.text="전화번호 : ${parking.tel}"

            geocoder.getFromLocationName(parking.parkingName, 1) {
                    addresses ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (addresses.isNotEmpty()) {
                        val targetLoc = LatLng(addresses[0].latitude, addresses[0].longitude)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 12F))
                        addMarker(targetLoc, parking.parkingName, parking.capacity.toInt(), parking.curParking.toInt())

                    } else {
                        Toast.makeText(
                            this@ParkingDetailActivity,
                            "해당 주소의 위치를 찾을 수 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }

        val mapFragment : SupportMapFragment
                = supportFragmentManager.findFragmentById(R.id.detail_map) as SupportMapFragment

        mapFragment.getMapAsync(mapReadyCallback)
    }

    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "GoogleMap is Ready")
        }
    }
    fun getTime(time : String) : String{
        if (time.length >= 4) {
            val hour = time.substring(0, 2)
            val min = time.substring(2, 4)
            return "${hour}시 ${min}분"
        } else {
            return "00시 00분"
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

