package com.example.dms.network

import com.example.dms.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ================= AUTH =================
    @POST("api/login")
    @Headers("Accept: application/json")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/logout")
    fun logout(): Call<Void>

    @POST("api/reset-password")
    @Headers("Accept: application/json")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<Void>

    // ================= PROFILE =================
    @GET("api/me")
    @Headers("Accept: application/json")
    suspend fun getProfile(): ApiResponse<UserProfile>

    // ================= NEWS =================
    @GET("api/news")
    @Headers("Accept: application/json")
    fun getAllNews(): Call<ApiResponse<List<News>>>

    @GET("api/news/{id}")
    @Headers("Accept: application/json")
    fun getNewsById(@Path("id") id: Int): Call<ApiResponse<News>>

    @POST("api/news")
    @Headers("Accept: application/json")
    fun createNews(@Body news: NewsRequest): Call<Void>

    @PUT("api/news/{id}")
    @Headers("Accept: application/json")
    fun updateNews(@Path("id") id: Int, @Body news: NewsRequest): Call<News>

    @DELETE("api/news/{id}")
    fun deleteNews(@Path("id") id: Int): Call<Void>

    // ================= DORMITORY - BUILDINGS =================
    @GET("api/buildings")
    @Headers("Accept: application/json")
    fun getBuildings(): Call<ApiResponse<List<Building>>>

    @GET("api/buildings/{id}")
    @Headers("Accept: application/json")
    fun getBuilding(@Path("id") id: Int): Call<ApiResponse<Building>>

    @POST("api/buildings")
    @Headers("Accept: application/json")
    fun createBuilding(@Body building: BuildingCreate): Call<ApiResponse<Building>>

    @PUT("api/buildings/{id}")
    @Headers("Accept: application/json")
    fun updateBuilding(@Path("id") id: Int, @Body building: BuildingUpdate): Call<ApiResponse<Building>>

    @DELETE("api/buildings/{id}")
    @Headers("Accept: application/json")
    fun deleteBuilding(@Path("id") id: Int): Call<ApiResponse<Unit>>

    // ================= DORMITORY - FLOORS (by building) =================
    @GET("api/building/{id}/floors")
    @Headers("Accept: application/json")
    fun getFloorsByBuilding(@Path("id") buildingId: Int): Call<ApiResponse<List<Floor>>>

    @GET("api/floors")
    @Headers("Accept: application/json")
    fun getAllFloors(): Call<ApiResponse<List<Floor>>>

    @GET("api/floors/{id}")
    @Headers("Accept: application/json")
    fun getFloor(@Path("id") id: Int): Call<ApiResponse<Floor>>

    @POST("api/floors")
    @Headers("Accept: application/json")
    fun createFloor(@Body floor: FloorCreate): Call<ApiResponse<Floor>>

    @PUT("api/floors/{id}")
    @Headers("Accept: application/json")
    fun updateFloor(@Path("id") id: Int, @Body floor: FloorUpdate): Call<ApiResponse<Floor>>

    @DELETE("api/floors/{id}")
    @Headers("Accept: application/json")
    fun deleteFloor(@Path("id") id: Int): Call<ApiResponse<Unit>>

    // ================= DORMITORY - ROOMS (by floor) =================
    @GET("api/floor/{id}/rooms/")
    @Headers("Accept: application/json")
    fun getRoomsByFloor(@Path("id") floorId: Int): Call<ApiResponse<List<Room>>>

    @GET("api/rooms")
    @Headers("Accept: application/json")
    fun getAllRooms(): Call<ApiResponse<List<Room>>>

    @GET("api/rooms/{id}")
    @Headers("Accept: application/json")
    fun getRoom(@Path("id") id: Int): Call<ApiResponse<Room>>

    @POST("api/rooms")
    @Headers("Accept: application/json")
    fun createRoom(@Body room: RoomCreate): Call<ApiResponse<Room>>

    @PUT("api/rooms/{id}")
    @Headers("Accept: application/json")
    fun updateRoom(@Path("id") id: Int, @Body room: RoomUpdate): Call<ApiResponse<Room>>

    @DELETE("api/rooms/{id}")
    fun deleteRoom(@Path("id") id: Int): Call<ApiResponse<Unit>>

    // ================= REQUESTS LIVE (Student) =================
    @POST("api/requests/live")
    @Headers("Accept: application/json")
    fun createRequestLive(@Body request: RequestLiveCreate): Call<ApiResponse<RequestLive>>

    // ================= REQUESTS LIVE (Manager) =================
    @GET("api/requests/live")
    @Headers("Accept: application/json")
    fun getLiveRequests(): Call<ApiResponse<List<RequestLive>>>

    @POST("api/requests/live/{id}/approve")
    @Headers("Accept: application/json")
    fun approveLiveRequest(@Path("id") id: Int): Call<ApiResponse<Unit>>

    @POST("api/requests/live/{id}/reject")
    @Headers("Accept: application/json")
    fun rejectLiveRequest(@Path("id") id: Int): Call<ApiResponse<Unit>>

    // --- Legacy (Repair, Sports - if backend supports) ---
    @POST("api/requests/repair")
    fun createRequestRepair(@Body request: RequestRepairCreate): Call<ApiResponse<RequestRepair>>

    @GET("api/requests/repair")
    fun getMyRequestsRepair(): Call<ApiResponse<List<RequestRepair>>>

    @POST("api/requests/sports")
    fun createRequestSports(@Body request: RequestSportsCreate): Call<ApiResponse<RequestSports>>

    @GET("api/requests/sports")
    fun getMyRequestsSports(): Call<ApiResponse<List<RequestSports>>>
}
