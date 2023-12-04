package ddwu.com.mobile.finalreport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import ddwu.com.mobile.finalreport.data.ParkingRoot

import ddwu.com.mobile.finalreport.databinding.ActivitySearchAddrBinding
import ddwu.com.mobile.finalreport.network.ParkingAPIService
import ddwu.com.mobile.finalreport.ui.ParkingAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchAddrActivity : AppCompatActivity() {
    private val TAG = "SearchAddrActivity"
    lateinit var searchAddrBinding : ActivitySearchAddrBinding
    lateinit var adapter : ParkingAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchAddrBinding = ActivitySearchAddrBinding.inflate(layoutInflater)
        setContentView(searchAddrBinding.root)

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

    }


}