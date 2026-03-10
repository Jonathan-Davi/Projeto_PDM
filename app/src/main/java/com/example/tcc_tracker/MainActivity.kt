package com.example.tcc_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.tcc_tracker.data.AppDatabase
import com.example.tcc_tracker.ui.HomeScreen
import com.example.tcc_tracker.viewmodel.TccViewModel
import com.example.tcc_tracker.viewmodel.TccViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)

        val factory = TccViewModelFactory(database.projetoDao(), database.marcoDao())

        val viewModel = ViewModelProvider(this, factory)[TccViewModel::class.java]

        setContent {
            HomeScreen(viewModel = viewModel)
        }
    }
}