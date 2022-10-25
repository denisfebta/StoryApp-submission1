package com.denis.storyapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.denis.storyapp.data.model.stories.Story
import com.denis.storyapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    companion object {
        const val EXTRA_DETAIL = "extra_detail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater )
        setContentView(binding.root)
        supportActionBar?.hide()
        setDetail()
    }

    private fun setDetail() {
        val detail = intent.getParcelableExtra<Story>(EXTRA_DETAIL)

        binding.apply {
            tvName.text = detail?.name
            tvDesc.text = detail?.description
            Glide.with(this@DetailActivity)
                .load(detail?.photoUrl)
                .into(ivStory)
        }
    }
}