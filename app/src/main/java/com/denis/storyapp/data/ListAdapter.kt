package com.denis.storyapp.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denis.storyapp.data.model.stories.Story
import com.denis.storyapp.activity.DetailActivity
import com.denis.storyapp.databinding.AdapterListBinding

class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    private val listStory = ArrayList<Story>()

    @SuppressLint("NotifyDataSetChanged")
    fun setStory(story: ArrayList<Story>) {
        listStory.clear()
        listStory.addAll(story)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = AdapterListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listStory[position])
    }

    override fun getItemCount(): Int = listStory.size

    inner class ViewHolder(private val binding: AdapterListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            with(binding) {
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(binding.imageView)
                title.text = story.name
                description.text = story.description
            }
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_DETAIL, story)
                }
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.imageView, "image"),
                        Pair(binding.title, "name"),
                        Pair(binding.description, "description"),
                    )
                it.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }
}