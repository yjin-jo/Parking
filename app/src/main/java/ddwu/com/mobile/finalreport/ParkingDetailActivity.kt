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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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

        // Intent를 가져오기
        val intent: Intent = intent

        // Intent로 전달된 데이터 확인
        val parking: Parking? = intent.getSerializableExtra("PARKING") as Parking

        // parking 객체를 사용하여 원하는 작업 수행
        if (parking != null) {
            // 여기에서 parking 객체를 사용하는 코드 추가
            parkingDetailBinding.tvName.setText(parking.parkingName.toString())
            parkingDetailBinding.tvCount.setText("잔여 :  + ${parking.capacity-parking.curParking}")

            geocoder.getFromLocationName(parking.parkingName, 1) {
                    addresses ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (addresses.isNotEmpty()) {
                        val targetLoc = LatLng(addresses[0].latitude, addresses[0].longitude)
                        Toast.makeText(this@ParkingDetailActivity,
                            "${parking.parkingName}, ${addresses[0].latitude}, ${addresses[0].longitude}",
                            Toast.LENGTH_SHORT).show()
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 12F))
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

            googleMap.setOnMarkerClickListener { marker
                -> Toast.makeText(this@ParkingDetailActivity, marker.tag.toString(), Toast.LENGTH_SHORT).show()
                false
            }

            googleMap.setOnInfoWindowClickListener { marker ->
                Toast.makeText(this@ParkingDetailActivity, marker.title, Toast.LENGTH_SHORT).show()
            }
        }
    }



}

