package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AuthManager
import com.vedatturkkal.stajokulu2025yoklama.databinding.ActivityMainBinding
import com.vedatturkkal.stajokulu2025yoklama.ui.auth.LoginActivity
import com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance.AttendanceFragment
import com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance.AttendanceScheduleFragment
import com.vedatturkkal.stajokulu2025yoklama.ui.main.home.HomeFragment
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import kotlin.getValue

//navigasyonun ve ana view modelin aktivitesi.
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val mainViewModel : MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.primary50)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view : View = binding.root
        setContentView(binding.root)
        setSupportActionBar(binding.materialToolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigationView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
        binding.materialToolbar.title = "Etkinlik Yönetim"
        replaceFragment(HomeFragment())
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.action_home -> {
                    replaceFragment(HomeFragment())
                    binding.materialToolbar.title = "Etkinlik Yönetim"
                }
                R.id.action_takeAttendance -> {
                    replaceFragment(AttendanceFragment())
                    binding.materialToolbar.title = "Etkinlik Kontrol"
                }
                R.id.action_attendanceList -> {
                    replaceFragment(AttendanceScheduleFragment())
                    binding.materialToolbar.title = "Etkinlik Rapor"
                }
                else -> true
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_logOut ->{
                AuthManager.signOut()
                val intent = Intent(applicationContext, LoginActivity :: class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainFrameLayout,fragment)
        fragmentTransaction.commit()
    }

}

