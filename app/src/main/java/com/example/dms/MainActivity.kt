package com.example.dms

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dms.adapter.NotificationsAdapter
import com.example.dms.ui.*
import com.example.dms.utils.SessionManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.Glide
import com.example.dms.network.RetrofitClient


class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var notificationsDrawer: LinearLayout
    private lateinit var menuButton: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var blurOverlay: View
    private lateinit var sessionManager: SessionManager

    private val CURRENT_FRAGMENT_KEY = "current_fragment"
    private val CURRENT_TITLE_KEY = "current_title"

    private val PREFS_NAME = "user_prefs"
    private val KEY_PROFILE_IMAGE = "profile_image"


    private var currentFragmentTag: String? = null
    private var currentTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Проверка токена
        val token = sessionManager.getToken()
        if (token == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        notificationsDrawer = findViewById(R.id.notifications_drawer) // Правая панель
        blurOverlay = findViewById(R.id.blurOverlay)

        menuButton = findViewById(R.id.menuButton)
        toolbarTitle = findViewById(R.id.fragmentTitle)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // ☰ ЛЕВОЕ МЕНЮ
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 🔔 УВЕДОМЛЕНИЯ (drawer справа + blur)
        findViewById<ImageView>(R.id.notificationIcon).setOnClickListener {
            blurOverlay.visibility = View.VISIBLE
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // 👤 ПРОФИЛЬ
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            openFragment(ProfileFragment(), R.id.nav_profile, "Личная информация")
            highlightMenuItem(R.id.nav_profile)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Убираем blur при закрытии drawer
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                if (drawerView.id == R.id.notifications_drawer) {
                    blurOverlay.visibility = View.GONE
                }
            }
        })

        // ================= ИНИЦИАЛИЗАЦИЯ КАСТОМНОГО DRAWER =================
        initCustomDrawer() // <- добавляем нашу новую функцию

        // ================= ИНИЦИАЛИЗАЦИЯ УВЕДОМЛЕНИЙ =================
        initNotificationsDrawer()
        loadUserData()
        loadFooterAvatar()
        loadFooterName()

        // Восстановление состояния
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_KEY)
            currentTitle = savedInstanceState.getString(CURRENT_TITLE_KEY)

            val fragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, it, currentFragmentTag)
                    .commit()
                toolbarTitle.text = currentTitle
            }
        } else {
            // по умолчанию открываем Новости
            openFragment(NewsFragment(), R.id.nav_home, "Новости")
            highlightMenuItem(R.id.nav_home)
        }
    }

    private fun openProfile() {
        openFragment(ProfileFragment(), R.id.main_container, "Личная информация")
        drawerLayout.closeDrawers()
    }



    private fun initCustomDrawer() {
        // Список пунктов меню: Triple<id, icon, текст>
        val menuItems = listOf(
            Triple(R.id.nav_home, R.drawable.ic_home, "Новости"),
            Triple(R.id.nav_living, R.drawable.ic_housing, "Проживание"),
            Triple(R.id.nav_docs, R.drawable.ic_doc, "Мои запросы"),
            Triple(R.id.nav_finance, R.drawable.ic_money, "Финансовый кабинет"),
            Triple(R.id.nav_fines, R.drawable.ic_fines, "Штрафы"),
            Triple(R.id.nav_sport, R.drawable.ic_sport, "Спорт"),
            Triple(R.id.nav_market, R.drawable.ic_market, "Маркет")
        )

        menuItems.forEach { (itemId, iconRes, text) ->
            val itemLayout = findViewById<LinearLayout>(itemId)
            val iconView = itemLayout.findViewById<ImageView>(R.id.icon)
            val titleView = itemLayout.findViewById<TextView>(R.id.title)
            val indicator = itemLayout.findViewById<View>(R.id.indicator)

            iconView.setImageResource(iconRes)
            titleView.text = text

            itemLayout.setOnClickListener {

                highlightMenuItem(itemId) // 🔥 ВОТ КЛЮЧ

                when (itemId) {
                    R.id.nav_home -> openFragment(NewsFragment(), itemId, "Новости")
                    R.id.nav_living -> openFragment(ResidenceFragment(), itemId, "Проживание")
                    R.id.nav_docs -> openFragment(MyRequestsFragment(), itemId, "Мои запросы")
                    R.id.nav_finance -> showPlaceholder("Финансовый кабинет", itemId)
                    R.id.nav_fines -> showPlaceholder("Штрафы", itemId)
                    R.id.nav_sport -> openFragment(
                        SportsRegistrationFragment(),
                        itemId,
                        "Запись на занятие физкультурой"
                    )
                    R.id.nav_market -> showPlaceholder("Маркет", itemId)
                }

                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            openProfile()
        }

        findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
            openProfile()
        }
    }

    // Вспомогательная функция для подсветки пункта программно
    private fun highlightMenuItem(itemId: Int) {
        val menuItems = listOf(
            R.id.nav_home,
            R.id.nav_living,
            R.id.nav_docs,
            R.id.nav_finance,
            R.id.nav_fines,
            R.id.nav_sport,
            R.id.nav_market
        )

        // Сброс всех
        menuItems.forEach { id ->
            val itemLayout = findViewById<LinearLayout>(id)
            val indicator = itemLayout.findViewById<View>(R.id.indicator)
            val icon = itemLayout.findViewById<ImageView>(R.id.icon)
            val title = itemLayout.findViewById<TextView>(R.id.title)

            indicator.visibility = View.GONE
            icon.setColorFilter(getColor(R.color.black))   // ← важно
            title.setTextColor(getColor(R.color.black))
        }

        // Активный пункт
        val selected = findViewById<LinearLayout>(itemId)
        selected.findViewById<View>(R.id.indicator).visibility = View.VISIBLE
        selected.findViewById<ImageView>(R.id.icon)
            .setColorFilter(getColor(R.color.blue))
        selected.findViewById<TextView>(R.id.title)
            .setTextColor(getColor(R.color.blue))
    }




    // ================= УВЕДОМЛЕНИЯ =================
    private fun initNotificationsDrawer() {

        val recycler = findViewById<RecyclerView>(R.id.notificationsRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val notifications = listOf(
            NotificationsFragment.NotificationItem(
                R.drawable.ic_money,
                "Напоминание об оплате",
                "Проверьте статус оплаты проживания."
            ),
            NotificationsFragment.NotificationItem(
                R.drawable.ic_profile,
                "Обновите данные",
                "Пожалуйста, обновите профиль."
            ),
            NotificationsFragment.NotificationItem(
                android.R.drawable.ic_dialog_alert,
                "Требуется внимание",
                "Есть информация, требующая действия."
            ),
            NotificationsFragment.NotificationItem(
                R.drawable.ic_housing,
                "Статус проживания обновлён",
                "Проверьте информацию в профиле."
            )
        )

        recycler.adapter = NotificationsAdapter(notifications)
    }

    // ================= ДАННЫЕ =================
    private fun loadUserData() {
        val token = sessionManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getInstance(token)
                val res = api.getProfile()
                val user = res.data

                if (user?.name.isNullOrBlank() || user?.email.isNullOrBlank()) {
                    Log.e("MainActivity", "loadUserData: empty profile data. apiMessage=${res.message}")
                    return@launch
                }

                val fullName = listOfNotNull(user.lastname, user.name, user.middlename)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                sessionManager.saveUserName(fullName.ifBlank { user.name })
                sessionManager.saveUserEmail(user.email)
                sessionManager.saveUserId(user.id.toString())
            } catch (e: Exception) {
                Log.e("MainActivity", "loadUserData: profile load failed", e)
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка загрузки профиля",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ================= НАВИГАЦИЯ =================
    private fun openFragment(fragment: Fragment, menuId: Int, title: String) {
        val tag = fragment::class.java.simpleName

        currentFragmentTag = tag
        currentTitle = title

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment, tag)
            .commit()

        toolbarTitle.text = title
    }

    private fun showPlaceholder(text: String, menuId: Int) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        toolbarTitle.text = text
        currentFragmentTag = null
        currentTitle = text
    }

    private fun loadFooterAvatar() {
        val footerAvatar = findViewById<ImageView>(R.id.footerAvatar)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val userId = sessionManager.getUserId() ?: "default"
        val key = "profile_image_$userId"
        val uriString = prefs.getString(key, null)

        if (!uriString.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(uriString))
                .circleCrop()
                .into(footerAvatar)
        }
    }

    private fun loadFooterName() {
        val footerName = findViewById<TextView>(R.id.footerName)
        val name = sessionManager.getUserName()
        if (!name.isNullOrBlank()) {
            footerName.text = name
        }
    }


    // ================= LOGOUT =================
    private fun logout() {
        val token = sessionManager.getToken()

        RetrofitClient.getInstance(token).logout()
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    sessionManager.clearToken()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    sessionManager.clearToken()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_KEY, currentFragmentTag)
        outState.putString(CURRENT_TITLE_KEY, currentTitle)
    }
}
