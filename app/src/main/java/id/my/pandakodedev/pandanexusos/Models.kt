package id.my.pandakodedev.pandanexusos

data class UserData(
    val id: Int,
    val full_name: String,
    val business_name: String,
    val email: String,
    val role: String,
    val business_category: String,
    val account_status: String = "approved",
    val subscription_tier: String = "pro",
    val selected_theme: String = "sky_white",
    val tax_percent: Double = 10.0,
    val service_percent: Double = 0.0,
    val stages: List<String>? = null
)

data class AuthResponse(
    val status: String,
    val message: String?,
    val user: UserData?
)

data class AdminUsersResponse(
    val status: String,
    val users: List<UserData>
)

data class QueueData(
    val id: Int,
    val user_id: Int,
    val branch_id: Int,
    val queue_code: String,
    val customer_name: String,
    val customer_ref: String,
    val current_stage: Int,
    val subtotal_price: Double = 0.0,
    val tax_amount: Double = 0.0,
    val discount_amount: Double = 0.0,
    val estimated_price: Double = 0.0,
    val items_summary: String,
    val branch_name: String? = "",
    val business_name: String? = ""
)

data class CatalogData(
    val id: Int,
    val user_id: Int = 0,
    val item_name: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val unit: String
)

data class BranchData(
    val id: Int,
    val user_id: Int,
    val branch_name: String,
    val address: String,
    val is_open: Int,
    val max_capacity: Int,
    val active_queues: Int,
    val business_name: String,
    val business_category: String,
    val tax_percent: Double = 10.0,
    val service_percent: Double = 0.0
)

data class MerchantDashboardResponse(
    val status: String,
    val branch_id: Int,
    val branch_name: String,
    val tax_percent: Double,
    val service_percent: Double,
    val selected_theme: String,
    val prefix: String,
    val stages: List<String>,
    val queues: List<QueueData>,
    val catalog: List<CatalogData>
)

data class CustomerHomeResponse(
    val status: String,
    val branches: List<BranchData>,
    val queues: List<QueueData>,
    val catalog: List<CatalogData>
)

data class GenericResponse(
    val status: String,
    val message: String?,
    val queue_code: String?,
    val grand_total: Double?
)