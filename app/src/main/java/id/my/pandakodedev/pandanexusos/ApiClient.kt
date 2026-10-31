package id.my.pandakodedev.pandanexusos

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface NexusApi {
    @POST("api/mobile_api.php?action=login")
    suspend fun login(@Body body: Map<String, String>): AuthResponse

    @POST("api/mobile_api.php?action=register")
    suspend fun register(@Body body: Map<String, String>): GenericResponse

    // Endpoint SuperAdmin Native
    @GET("api/mobile_api.php?action=admin_users")
    suspend fun getAdminUsers(): AdminUsersResponse

    @POST("api/mobile_api.php?action=admin_save_user")
    suspend fun adminSaveUser(@Body body: Map<String, String>): GenericResponse

    @POST("api/mobile_api.php?action=admin_delete_user")
    suspend fun adminDeleteUser(@Body body: Map<String, String>): GenericResponse

    // Endpoint Mitra / Kasir POS
    @GET("api/mobile_api.php?action=merchant_data")
    suspend fun getMerchantData(@Query("user_id") userId: Int): MerchantDashboardResponse

    @POST("api/mobile_api.php?action=save_merchant_settings")
    suspend fun saveMerchantSettings(@Body body: Map<String, String>): GenericResponse

    @POST("api/mobile_api.php?action=update_queue")
    suspend fun updateQueue(@Body body: Map<String, Int>): GenericResponse

    @POST("api/mobile_api.php?action=create_queue")
    suspend fun createQueue(@Body body: Map<String, String>): GenericResponse

    // Endpoint Pelanggan
    @GET("api/mobile_api.php?action=customer_home")
    suspend fun getCustomerHome(): CustomerHomeResponse

    @FormUrlEncoded
    @POST("api/app_api.php?action=restock_item")
    suspend fun restockItem(
        @Query("user_id") userId: Int,
        @Field("item_id") itemId: Int,
        @Field("add_qty") addQty: Int
    ): GenericResponse

    @FormUrlEncoded
    @POST("api/app_api.php?action=add_catalog")
    suspend fun addCatalogItem(
        @Query("user_id") userId: Int,
        @Field("item_name") name: String,
        @Field("category") category: String,
        @Field("price") price: String,
        @Field("stock") stock: String,
        @Field("unit") unit: String
    ): GenericResponse

    companion object {
        private const val BASE_URL = "https://nexus.pandakode-dev.my.id/"

        fun create(): NexusApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NexusApi::class.java)
        }
    }
}