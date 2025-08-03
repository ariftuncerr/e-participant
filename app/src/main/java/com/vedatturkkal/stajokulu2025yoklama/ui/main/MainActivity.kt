package com.vedatturkkal.stajokulu2025yoklama.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.vedatturkkal.stajokulu2025yoklama.Home
import com.vedatturkkal.stajokulu2025yoklama.R
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AuthManager
import com.vedatturkkal.stajokulu2025yoklama.databinding.ActivityMainBinding
import com.vedatturkkal.stajokulu2025yoklama.ui.auth.LoginActivity
import com.vedatturkkal.stajokulu2025yoklama.ui.main.attendance.AttendanceFragment
import com.vedatturkkal.stajokulu2025yoklama.viewmodel.MainViewModel
import kotlin.getValue

//navigasyonun ve ana view modelin aktivitesi.
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val mainViewModel : MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view : View = binding.root
        setContentView(binding.root)
        setSupportActionBar(binding.materialToolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        replaceFragment(HomeFragment())
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.action_home -> replaceFragment(HomeFragment())
                R.id.action_takeAttendance -> replaceFragment(AttendanceFragment())
                R.id.action_attendanceList -> replaceFragment(AttendanceScheduleFragment())
                R.id.action_settings -> replaceFragment(SettingsFragment())
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

