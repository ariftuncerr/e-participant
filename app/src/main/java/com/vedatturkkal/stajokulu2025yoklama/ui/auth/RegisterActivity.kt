package com.vedatturkkal.stajokulu2025yoklama.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vedatturkkal.stajokulu2025yoklama.databinding.ActivityRegisterBinding
import com.vedatturkkal.stajokulu2025yoklama.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerBtn.setOnClickListener {
            val name = binding.nameEditTxt.text.toString().trim()
            val email = binding.emailEditTxtReg.text.toString().trim()
            val password = binding.passWordEditTxt.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.register(name, email, password)
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.authResult.observe(this) { (success, message) ->
            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Kayıt Başarısız: $message", Toast.LENGTH_LONG).show()
            }
        }

    }
}
