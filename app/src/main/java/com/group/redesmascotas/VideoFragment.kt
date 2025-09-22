package com.group.redesmascotas

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.group.redesmascotas.adapter.VideosAdapter
import com.group.redesmascotas.database.VideoEntity
import com.group.redesmascotas.ui.CustomMediaController
import com.group.redesmascotas.database.AllDatabase
import com.group.redesmascotas.repository.VideoRepository
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Este fragmento representa la sección de videos de la mascota
class VideoFragment : Fragment() {
    
    // Views del layout
    private lateinit var btnSelectVideo: com.google.android.material.button.MaterialButton
    private lateinit var layoutVideoPlayer: com.google.android.material.card.MaterialCardView
    private lateinit var videoView: VideoView
    private lateinit var layoutVideoLoading: LinearLayout
    private lateinit var customMediaController: CustomMediaController
    private lateinit var recyclerViewVideos: RecyclerView
    private lateinit var layoutNoVideos: com.google.android.material.card.MaterialCardView
    private lateinit var tvUploadedVideosLabel: LinearLayout
    
    // Repositorio para persistencia
    private lateinit var videoRepository: VideoRepository
    
    // Adaptador y datos
    private lateinit var videosAdapter: VideosAdapter
    private var allVideos = mutableListOf<VideoEntity>()
    private var currentVideoUri: Uri? = null
    private var isPlaying = false
    
    // Handler para actualizar progreso
    private val handler = Handler(Looper.getMainLooper())
    private var progressUpdateRunnable: Runnable? = null
    
    // Launcher para seleccionar video
    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleVideoSelection(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        
        // Inicializar repositorio
        val database = AllDatabase.getDatabase(requireContext())
        videoRepository = VideoRepository(database.videoDao(), requireContext())
        
        initViews(view)
        setupRecyclerView()
        setupVideoPlayer()
        setupClickListeners()
        loadPersistedVideos() // Cargar videos persistidos
        
        return view
    }
    
    private fun initViews(view: View) {
        btnSelectVideo = view.findViewById(R.id.btnSelectVideo)
        layoutVideoPlayer = view.findViewById(R.id.layoutVideoPlayer)
        videoView = view.findViewById(R.id.videoView)
        layoutVideoLoading = view.findViewById(R.id.layoutVideoLoading)
        customMediaController = view.findViewById(R.id.customMediaController)
        recyclerViewVideos = view.findViewById(R.id.recyclerViewVideos)
        layoutNoVideos = view.findViewById(R.id.layoutNoVideos)
        tvUploadedVideosLabel = view.findViewById(R.id.tvUploadedVideosLabel)
    }
    
    private fun setupRecyclerView() {
        videosAdapter = VideosAdapter(
            videos = allVideos,
            onVideoClick = { video ->
                playVideoFromList(video)
            },
            onVideoLongClick = { video ->
                showEditVideoDialog(video)
            }
        )
        
        recyclerViewVideos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = videosAdapter
        }
    }
    
    private fun setupVideoPlayer() {
        // Configurar CustomMediaController con callbacks
        customMediaController.videoView = videoView
        
        // Configurar callbacks del CustomMediaController
        customMediaController.onPlayPause = {
            togglePlayPause()
        }
        
        customMediaController.onSeek = { position ->
            videoView.seekTo(position)
        }
        
        customMediaController.onRewind = {
            val currentPos = videoView.currentPosition
            val newPos = maxOf(0, currentPos - 10000) // Retroceder 10 segundos
            videoView.seekTo(newPos)
        }
        
        customMediaController.onFastForward = {
            val currentPos = videoView.currentPosition
            val duration = videoView.duration
            val newPos = minOf(duration, currentPos + 10000) // Avanzar 10 segundos
            videoView.seekTo(newPos)
        }
        
        customMediaController.onFullscreenToggle = { isFullscreen ->
            // TODO: Implementar funcionalidad de pantalla completa
            Toast.makeText(context, getString(R.string.videos_fullscreen_soon), Toast.LENGTH_SHORT).show()
        }
        
        // Configurar listeners del VideoView
        videoView.setOnPreparedListener { _ ->
            layoutVideoLoading.visibility = View.GONE
            customMediaController.testControls() // Mostrar por 10 segundos para testing
            startProgressUpdate()
        }
        
        // Tap en VideoView para mostrar/ocultar controles
        videoView.setOnClickListener {
            if (customMediaController.isShowing()) {
                customMediaController.hide()
            } else {
                customMediaController.show()
            }
        }
        
        videoView.setOnCompletionListener {
            isPlaying = false
            stopProgressUpdate()
            customMediaController.updateProgress(0, videoView.duration, false)
        }
        
        videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(context, getString(R.string.videos_error_playing), Toast.LENGTH_SHORT).show()
            layoutVideoLoading.visibility = View.GONE
            customMediaController.hide()
            true
        }
    }
    
    private fun setupClickListeners() {
        btnSelectVideo.setOnClickListener {
            selectVideoFromGallery()
        }
    }
    
    private fun selectVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
            type = "video/*"
        }
        videoPickerLauncher.launch(intent)
    }
    
    private fun handleVideoSelection(uri: Uri) {
        try {
            // Validar que el URI es válido
            context?.contentResolver?.getType(uri) ?: run {
                Toast.makeText(context, getString(R.string.videos_error_processing), Toast.LENGTH_SHORT).show()
                return
            }
            
            // Obtener información del video
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: "Video_${System.currentTimeMillis()}"
            
            retriever.release()
            
            // Validar duración del video (evitar videos muy largos)
            if (duration > 600000) { // 10 minutos máximo
                Toast.makeText(context, "El video es muy largo. Máximo 10 minutos.", Toast.LENGTH_LONG).show()
                return
            }
            
            // Guardar video usando el repositorio (persistencia)
            lifecycleScope.launch {
                videoRepository.saveVideo(uri, title).fold(
                    onSuccess = { videoId ->
                        // El video se guardó exitosamente
                        // No necesitamos actualizar manualmente allVideos porque
                        // loadPersistedVideos ya observa los cambios via Flow
                        
                        // Reproducir el video recién guardado
                        // Buscar el video por ID en la lista actualizada
                        val savedVideo = allVideos.find { it.id == videoId }
                        savedVideo?.internalPath?.let { path ->
                            playVideoFromFilePath(path)
                        }
                        
                        Toast.makeText(context, getString(R.string.videos_added_success), Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, getString(R.string.videos_error_processing), Toast.LENGTH_LONG).show()
                    }
                )
            }
            
        } catch (e: SecurityException) {
            Toast.makeText(context, "Sin permisos para acceder al video", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.videos_error_processing), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun playVideoFromList(video: VideoEntity) {
        video.internalPath.let { path ->
            // Determinar si es una ruta de archivo local o URI
            if (path.startsWith("/")) {
                playVideoFromFilePath(path)
            } else {
                playVideoFromUri(Uri.parse(path))
            }
        }
    }
    
    private fun playVideoFromUri(uri: Uri) {
        currentVideoUri = uri
        // Mostrar reproductor y estado de carga
        layoutVideoPlayer.visibility = View.VISIBLE
        layoutVideoLoading.visibility = View.VISIBLE
        
        // Configurar el video - MediaController manejará los controles
        videoView.setVideoURI(uri)
        videoView.requestFocus()
    }
    
    private fun playVideoFromFilePath(filePath: String) {
        currentVideoUri = Uri.parse("file://$filePath")
        // Mostrar reproductor y estado de carga
        layoutVideoPlayer.visibility = View.VISIBLE
        layoutVideoLoading.visibility = View.VISIBLE
        
        // Configurar el video desde archivo interno
        videoView.setVideoPath(filePath)
        videoView.requestFocus()
    }
    
    private fun togglePlayPause() {
        if (currentVideoUri == null) {
            Toast.makeText(context, getString(R.string.videos_select_first), Toast.LENGTH_SHORT).show()
            return
        }
        
        if (isPlaying) {
            videoView.pause()
            stopProgressUpdate()
        } else {
            videoView.start()
            startProgressUpdate()
        }
        isPlaying = !isPlaying
    }
    
    private fun startProgressUpdate() {
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                if (isPlaying && videoView.isPlaying) {
                    val currentPosition = videoView.currentPosition
                    val duration = videoView.duration
                    customMediaController.updateProgress(currentPosition, duration, isPlaying)
                    handler.postDelayed(this, 1000)
                }
            }
        }
        progressUpdateRunnable?.let { handler.post(it) }
    }
    
    private fun stopProgressUpdate() {
        progressUpdateRunnable?.let { handler.removeCallbacks(it) }
    }
    
    private fun stopCurrentVideo() {
        if (isPlaying) {
            videoView.pause()
            isPlaying = false
        }
        videoView.stopPlayback()
        currentVideoUri = null
        stopProgressUpdate()
        customMediaController.hide()
        layoutVideoPlayer.visibility = View.GONE
    }
    
    private fun loadPersistedVideos() {
        // Cargar videos desde la base de datos
        lifecycleScope.launch {
            videoRepository.getAllVideos().collect { videos ->
                allVideos.clear()
                allVideos.addAll(videos)
                updateVideosList()
            }
        }
    }
    
    private fun updateVideosList() {
        if (allVideos.isEmpty()) {
            showEmptyState()
        } else {
            showVideosContent()
        }
        
        videosAdapter.updateVideos(allVideos)
    }
    
    private fun showEmptyState() {
        layoutNoVideos.visibility = View.VISIBLE
        recyclerViewVideos.visibility = View.GONE
        tvUploadedVideosLabel.visibility = View.GONE
        // Ocultar reproductor cuando no hay videos
        layoutVideoPlayer.visibility = View.GONE
    }
    
    private fun showVideosContent() {
        layoutNoVideos.visibility = View.GONE
        recyclerViewVideos.visibility = View.VISIBLE
        tvUploadedVideosLabel.visibility = View.VISIBLE
        // El reproductor se mostrará cuando se seleccione un video
    }
    
    private fun toggleFavorite(video: VideoEntity) {
        lifecycleScope.launch {
            try {
                val newFavoriteStatus = !video.isFavorite
                videoRepository.updateVideoFavorite(video.id, newFavoriteStatus)
                
                // La actualización se refleja automáticamente vía Flow de Room
                val message = if (newFavoriteStatus) {
                    "Video agregado a favoritos"
                } else {
                    "Video removido de favoritos"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(context, "Error al actualizar favorito", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showEditVideoDialog(video: VideoEntity) {
        val options = arrayOf(getString(R.string.videos_edit_name), getString(R.string.videos_delete))
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(video.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameVideoDialog(video)
                    1 -> showDeleteVideoDialog(video)
                }
            }
            .show()
    }
    
    private fun showRenameVideoDialog(video: VideoEntity) {
        val editText = EditText(requireContext()).apply {
            setText(video.name)
            selectAll()
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.videos_edit_title))
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch {
                        videoRepository.updateVideoName(video.id, newName).fold(
                            onSuccess = {
                                Toast.makeText(context, getString(R.string.videos_updated_success), Toast.LENGTH_SHORT).show()
                                // La lista se actualiza automáticamente via Flow
                            },
                            onFailure = {
                                Toast.makeText(context, "Error al actualizar el video", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showDeleteVideoDialog(video: VideoEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.videos_delete_title))
            .setMessage(getString(R.string.videos_delete_message))
            .setPositiveButton(getString(R.string.videos_delete)) { _, _ ->
                // Si es el video actualmente reproduciéndose, detenerlo
                if (currentVideoUri != null && video.internalPath == currentVideoUri.toString()) {
                    stopCurrentVideo()
                }
                
                lifecycleScope.launch {
                    videoRepository.deleteVideo(video).fold(
                        onSuccess = {
                            Toast.makeText(context, getString(R.string.videos_deleted_success), Toast.LENGTH_SHORT).show()
                            // La lista se actualiza automáticamente via Flow
                        },
                        onFailure = {
                            Toast.makeText(context, "Error al eliminar el video", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) - 
                     TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    override fun onPause() {
        super.onPause()
        // Pausar video si está reproduciéndose
        if (isPlaying) {
            videoView.pause()
            stopProgressUpdate()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Reanudar actualizaciones si el video estaba reproduciéndose
        if (currentVideoUri != null && isPlaying) {
            startProgressUpdate()
        }
    }
}