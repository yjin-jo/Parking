package ddwu.com.mobile.finalreport.network

import ddwu.com.mobile.finalreport.data.ParkingRoot
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ParkingAPIService {
    @GET("{key}/json/GetParkingInfo/1/{count}/{addr}")
    fun getParkingResult(@Path("key") key: String,
                         @Path("count") count: Long,
                         @Path("addr") addr: String)
    : Call<ParkingRoot>
}