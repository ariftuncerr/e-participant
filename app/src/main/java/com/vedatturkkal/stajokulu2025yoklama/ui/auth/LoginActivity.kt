package com.vedatturkkal.stajokulu2025yoklama.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AuthManager
import com.vedatturkkal.stajokulu2025yoklama.databinding.ActivityLoginBinding
import com.vedatturkkal.stajokulu2025yoklama.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEditTxt.text.toString().trim()
            val password = binding.passWordEditTxt.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.authResult.observe(this) { (success, message) ->
            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Giriş Başarısız: $message", Toast.LENGTH_LONG).show()
            }
        }


        binding.notRegistered.setOnClickListener {
            startActivity(Intent(this, RegisterActivity :: class.java))
        }
    }

    override fun onResume() {
        if(AuthManager.getCurrentUser() != null){
            val intent = Intent(applicationContext, MainActivity :: class.java)
            startActivity(intent)
            finish()
        }
        super.onResume()
    }
}
