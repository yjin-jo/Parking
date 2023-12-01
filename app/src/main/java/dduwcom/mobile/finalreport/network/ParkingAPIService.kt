package dduwcom.mobile.finalreport.network

import dduwcom.mobile.finalreport.data.ParkingRoot
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ParkingAPIService {
    @GET("{api_key}/xml/GetParkingInfo/1/5/")
    fun getParkingResult(@Path("api_key")type: String,
                         @Query("addr") addr: String) : Call<ParkingRoot>
}