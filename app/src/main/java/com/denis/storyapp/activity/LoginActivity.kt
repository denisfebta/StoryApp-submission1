package com.denis.storyapp.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.denis.storyapp.R
import com.denis.storyapp.data.Resource
import com.denis.storyapp.databinding.ActivityLoginBinding
import com.denis.storyapp.viewmodel.LoginViewModel
import com.denis.storyapp.utils.UserPreferences
import com.denis.storyapp.viewmodel.ViewModelFactory

class LoginActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupViewModel()
        setupView()
        setupAction()
        setAnimation()
    }

    private fun setupView() {
        loginViewModel.userInfo.observe(this) {
            when (it) {
                is Resource.Success -> {
                    showLoading(false)
                    startActivity(Intent(this, MainActivity::class.java))
                    finishLogin()
                }
                is Resource.Loading -> showLoading(true)
                is Resource.Error -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            }
        }
    }


    private fun finishLogin() {
        Intent(this@LoginActivity, MainActivity::class.java)
            .apply {
                startActivity(this)
                finish()
            }
    }

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        loginViewModel = ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            if (valid()) {
                val email = binding.tvEmail.text.toString()
                val password = binding.tvPassword.text.toString()
                loginViewModel.login(email, password)
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.check_input),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun valid() =
        binding.tvEmail.error == null && binding.tvPassword.error == null && !binding.tvEmail.text.isNullOrEmpty() && !binding.tvPassword.text.isNullOrEmpty()

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    private fun setAnimation() {
        ObjectAnimator.ofFloat(binding.ivLogin, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val email = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(email)
            startDelay = 500
        }.start()
    }
}