package id.my.pandakodedev.pandanexusos

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("panda_nexus_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: UserData) {
        prefs.edit().apply {
            putInt("user_id", user.id)
            putString("full_name", user.full_name)
            putString("business_name", user.business_name)
            putString("role", user.role)
            putString("category", user.business_category)
            putString("theme", user.selected_theme)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt("user_id", 0)
    fun getFullName(): String = prefs.getString("full_name", "") ?: ""
    fun getBusinessName(): String = prefs.getString("business_name", "") ?: ""
    fun getRole(): String = prefs.getString("role", "") ?: ""
    fun getCategory(): String = prefs.getString("category", "laundry") ?: "laundry"
    fun getTheme(): String = prefs.getString("theme", "sky_white") ?: "sky_white"

    fun setTheme(themeKey: String) {
        prefs.edit().putString("theme", themeKey).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}