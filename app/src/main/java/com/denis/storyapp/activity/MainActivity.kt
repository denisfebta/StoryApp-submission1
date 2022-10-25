package com.denis.storyapp.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.denis.storyapp.R
import com.denis.storyapp.data.ListAdapter
import com.denis.storyapp.data.Resource
import com.denis.storyapp.databinding.ActivityMainBinding
import com.denis.storyapp.utils.UserPreferences
import com.denis.storyapp.viewmodel.LoginViewModel
import com.denis.storyapp.viewmodel.MainViewModel
import com.denis.storyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
    private lateinit var mainViewModel: MainViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var listAdapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Home"

        setupViewModel()
        setupView()
        action()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        setupView()
    }

    private fun setupView() {
        listAdapter = ListAdapter()

        loginViewModel.getUserToken().observe(this){ token ->
            if (token.isNotEmpty()){
                mainViewModel.stories.observe(this) {
                    when (it) {
                        is Resource.Loading -> showLoading(true)
                        is Resource.Success -> {
                            it.data?.let { stories -> listAdapter.setStory(stories) }
                            showLoading(false)
                        }
                        is Resource.Error -> {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    mainViewModel.getAllStories()
                }
            }
            else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        with(binding.rvStory) {
            setHasFixedSize(true)
            adapter = listAdapter
        }
    }

    private fun action() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity as Activity).toBundle()
            )
        }
    }

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        val viewModelFactory = ViewModelFactory(pref)

        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        loginViewModel= ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_logout) {
            showAlertDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val alert = builder.create()
        builder
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.you_sure))
            .setPositiveButton(getString(R.string.no)) { _, _ ->
                alert.cancel()
            }
            .setNegativeButton(getString(R.string.yes)) { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout() {
            loginViewModel.logout()
            Log.d("tag", "token deleted")
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
    }
}