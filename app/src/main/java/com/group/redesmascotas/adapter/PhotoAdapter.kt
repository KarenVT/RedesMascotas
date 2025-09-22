package com.group.redesmascotas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.group.redesmascotas.R
import com.group.redesmascotas.database.PhotoEntity
import java.io.File

class PhotoAdapter(
    private val photos: MutableList<PhotoEntity>,
    private val onPhotoAction: (PhotoEntity, Action) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    enum class Action {
        TOGGLE_FAVORITE, EDIT, VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_card, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoImage: ImageView = itemView.findViewById(R.id.photoImage)
        private val photoDescription: TextView = itemView.findViewById(R.id.photoDescription)
        private val btnFavorite: MaterialButton = itemView.findViewById(R.id.btnFavorite)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val categoryTags: LinearLayout = itemView.findViewById(R.id.categoryTags)
        private val chipWithFriends: Chip = itemView.findViewById(R.id.chipWithFriends)

        fun bind(photo: PhotoEntity) {
            // Configurar descripción
            photoDescription.text = photo.description

            // Configurar imagen desde almacenamiento interno
            val file = File(photo.internalPath)
            if (file.exists()) {
                photoImage.setImageURI(file.toUri())
            }

            // Configurar estado del botón favorito
            updateFavoriteButton(photo.isFavorite)

            // Configurar chips de categoría
            setupCategoryTags(photo)

            // Configurar listeners
            setupListeners(photo)
        }

        private fun updateFavoriteButton(isFavorite: Boolean) {
            if (isFavorite) {
                btnFavorite.setIconResource(R.drawable.ic_favorite)
                btnFavorite.iconTint = itemView.context.getColorStateList(R.color.red_like)
            } else {
                btnFavorite.setIconResource(R.drawable.ic_favorite_border)
                btnFavorite.iconTint = itemView.context.getColorStateList(R.color.white)
            }
        }

        private fun setupCategoryTags(photo: PhotoEntity) {
            if (photo.isWithFriends) {
                categoryTags.visibility = View.VISIBLE
                chipWithFriends.visibility = View.VISIBLE
            } else {
                categoryTags.visibility = View.GONE
                chipWithFriends.visibility = View.GONE
            }
        }

        private fun setupListeners(photo: PhotoEntity) {
            btnFavorite.setOnClickListener {
                onPhotoAction(photo, Action.TOGGLE_FAVORITE)
            }

            btnEdit.setOnClickListener {
                onPhotoAction(photo, Action.EDIT)
            }

            itemView.setOnClickListener {
                onPhotoAction(photo, Action.VIEW)
            }
        }
    }
}