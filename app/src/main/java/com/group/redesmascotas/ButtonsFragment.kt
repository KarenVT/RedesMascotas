package com.group.redesmascotas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.Intent

/**
 * Fragmento que maneja las interacciones sociales de la aplicación.
 * Permite a los usuarios seguir mascotas, dar me gusta, comentar y compartir contenido.
 */
class ButtonsFragment : Fragment() {
    
    // Variables para controlar los estados de interacción
    private var isFollowing = false
    private var isLiked = false
    
    // Variables para los elementos de la UI
    private lateinit var btnFollowPet: LinearLayout
    private lateinit var btnLike: LinearLayout
    private lateinit var btnComment: LinearLayout
    private lateinit var btnShare: LinearLayout
    private lateinit var ivLikeIcon: ImageView
    private lateinit var tvInteractionStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout para este fragmento
        return inflater.inflate(R.layout.fragment_buttons, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializa las vistas
        initializeViews(view)
        
        // Configura los listeners
        setupClickListeners()
    }
    
    /**
     * Inicializa todas las vistas del fragmento
     */
    private fun initializeViews(view: View) {
        btnFollowPet = view.findViewById(R.id.btn_follow_pet)
        btnLike = view.findViewById(R.id.btn_like)
        btnComment = view.findViewById(R.id.btn_comment)
        btnShare = view.findViewById(R.id.btn_share)
        ivLikeIcon = view.findViewById(R.id.iv_like_icon)
        tvInteractionStatus = view.findViewById(R.id.tv_interaction_status)
    }
    
    /**
     * Configura los listeners para todos los botones
     */
    private fun setupClickListeners() {
        // Botón seguir mascota
        btnFollowPet.setOnClickListener {
            toggleFollowStatus()
        }
        
        // Botón me gusta
        btnLike.setOnClickListener {
            toggleLikeStatus()
        }
        
        // Botón comentar
        btnComment.setOnClickListener {
            showCommentAction()
        }
        
        // Botón compartir
        btnShare.setOnClickListener {
            shareContent()
        }
    }
    
    /**
     * Cambia el estado de seguimiento de la mascota
     */
    private fun toggleFollowStatus() {
        isFollowing = !isFollowing
        
        val message = if (isFollowing) {
            updateInteractionStatus("✓ ${getString(R.string.follow_pet_button)}")
            "¡Ahora sigues a esta mascota!"
        } else {
            updateInteractionStatus("Has dejado de seguir a esta mascota")
            "Ya no sigues a esta mascota"
        }
        
        showToast(message)
        
        // Opcional: Cambiar el estilo del botón para indicar el estado
        updateFollowButtonStyle()
    }
    
    /**
     * Cambia el estado del botón de me gusta
     */
    private fun toggleLikeStatus() {
        isLiked = !isLiked
        
        // Cambia el icono según el estado
        val iconResource = if (isLiked) {
            R.drawable.ic_favorite
        } else {
            R.drawable.ic_favorite_border
        }
        
        ivLikeIcon.setImageResource(iconResource)
        
        // Cambia el color del icono
        val iconColor = if (isLiked) {
            ContextCompat.getColor(requireContext(), R.color.red_like)
        } else {
            ContextCompat.getColor(requireContext(), R.color.gray_icon)
        }
        ivLikeIcon.setColorFilter(iconColor)
        
        val message = if (isLiked) {
            updateInteractionStatus("❤️ ${getString(R.string.like_button)}")
            "¡Te gusta esta mascota!"
        } else {
            updateInteractionStatus("Has quitado el me gusta")
            "Ya no te gusta esta mascota"
        }
        
        showToast(message)
    }
    
    /**
     * Muestra la acción de comentar
     */
    private fun showCommentAction() {
        updateInteractionStatus("💭 Escribiendo comentario...")
        showToast("Función de comentarios próximamente")
        
        // Aquí podrías abrir un diálogo o navegar a una pantalla de comentarios
        // Por ahora solo mostramos el mensaje
    }
    
    /**
     * Comparte el contenido usando el Intent de Android
     */
    private fun shareContent() {
        updateInteractionStatus("📤 ${getString(R.string.share_button)}")
        
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "¡Mira esta mascota en PawConnect!")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "¡Encontré una mascota increíble en PawConnect! " +
                    "Únete para conectar con más amigos peludos. 🐕🐱"
                )
            }
            
            val chooser = Intent.createChooser(shareIntent, "Compartir mascota")
            startActivity(chooser)
            showToast("¡Contenido compartido!")
        } catch (e: Exception) {
            showToast("Error al compartir: ${e.message}")
            updateInteractionStatus("Error al compartir contenido")
        }
    }
    
    /**
     * Actualiza el estilo del botón de seguir según el estado
     */
    private fun updateFollowButtonStyle() {
        // Opcional: Cambiar el fondo o texto del botón según el estado
        // Por ahora mantenemos el estilo original
    }
    
    /**
     * Actualiza el texto de estado de interacciones
     */
    private fun updateInteractionStatus(status: String) {
        tvInteractionStatus.text = status
        tvInteractionStatus.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.primary_text)
        )
    }
    
    /**
     * Muestra un mensaje Toast al usuario
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
