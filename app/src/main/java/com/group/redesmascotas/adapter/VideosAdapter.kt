package com.group.redesmascotas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group.redesmascotas.R
import com.group.redesmascotas.database.VideoEntity

class VideosAdapter(
    private var videos: List<VideoEntity>,
    private val onVideoClick: (VideoEntity) -> Unit,
    private val onVideoLongClick: (VideoEntity) -> Unit
) : RecyclerView.Adapter<VideosAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoIcon: ImageView = itemView.findViewById(R.id.ivVideoIcon)
        val videoName: TextView = itemView.findViewById(R.id.tvVideoName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_simple, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        
        holder.videoName.text = video.name
        holder.videoIcon.setImageResource(R.drawable.ic_video_play)
        
        // Click listeners
        holder.itemView.setOnClickListener {
            onVideoClick(video)
        }
        
        holder.itemView.setOnLongClickListener {
            onVideoLongClick(video)
            true
        }
    }

    override fun getItemCount(): Int = videos.size
    
    fun updateVideos(newVideos: List<VideoEntity>) {
        videos = newVideos
        notifyDataSetChanged()
    }
}

