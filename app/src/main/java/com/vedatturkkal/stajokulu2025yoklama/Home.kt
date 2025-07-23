package com.vedatturkkal.stajokulu2025yoklama

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate

class Home : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var yoklamaAl: Button
    private lateinit var yoklamaKontrol: Button
    private lateinit var yoklamaListe: Button
    private lateinit var yoklamaTakvim: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        imageView = findViewById(R.id.imageView)
        yoklamaAl = findViewById(R.id.yoklamaAl)
        yoklamaKontrol = findViewById(R.id.yoklamaKontrol)
        yoklamaListe = findViewById(R.id.yoklamaListe)
        yoklamaTakvim = findViewById(R.id.yoklamaTakvim)

      /*  yoklamaAl.setOnClickListener {
            val intent = Intent(this, YoklamaCameraActivity::class.java)
            startActivity(intent)
        }
*/
        yoklamaKontrol.setOnClickListener {
            val intent = Intent(this, YoklamaListActivity::class.java)
            startActivity(intent)
        }

        yoklamaListe.setOnClickListener {
            val intent = Intent(this, YoklamaListActivity::class.java)
            startActivity(intent)
        }

        yoklamaTakvim.setOnClickListener {
            val intent = Intent(this, TakvimActivity::class.java)
            startActivity(intent)
        }
    }
}