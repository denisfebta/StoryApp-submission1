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
import com.denis.storyapp.databinding.ActivityRegisterBinding
import com.denis.storyapp.utils.UserPreferences
import com.denis.storyapp.viewmodel.RegisterViewModel
import com.denis.storyapp.viewmodel.ViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupViewModel()
        setupView()
        setupAction()
        setAnimation()
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener{
            if (valid()) {
                val name = binding.tvName.text.toString()
                val email = binding.tvEmail.text.toString()
                val password = binding.tvPassword.text.toString()
                registerViewModel.register(name, email, password)
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.check_input),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun valid() =
        binding.tvEmail.error == null && binding.tvPassword.error == null && binding.tvName.error == null && !binding.tvEmail.text.isNullOrEmpty() && !binding.tvPassword.text.isNullOrEmpty() && !binding.tvName.text.isNullOrEmpty()


    private fun setupView() {
        registerViewModel.userInfo.observe(this) {
            when (it) {
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
                is Resource.Loading -> showLoading(true)
                is Resource.Error -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            }
        }
    }

    private fun setupViewModel() {
        val pref = UserPreferences.getInstance(dataStore)
        registerViewModel = ViewModelProvider(this, ViewModelFactory(pref))[RegisterViewModel::class.java]
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun setAnimation() {
        ObjectAnimator.ofFloat(binding.ivLogo, View.TRANSLATION_X, -30f, 30f).apply {
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