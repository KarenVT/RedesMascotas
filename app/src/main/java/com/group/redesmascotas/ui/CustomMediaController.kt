package com.group.redesmascotas.ui

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.group.redesmascotas.R
import java.util.concurrent.TimeUnit

class CustomMediaController @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    // Views del layout personalizado
    private lateinit var playPauseButton: ImageButton
    private lateinit var rewindButton: ImageButton
    private lateinit var fastForwardButton: ImageButton
    private lateinit var volumeButton: ImageButton
    private lateinit var fullscreenButton: ImageButton
    private lateinit var progressBar: SeekBar
    private lateinit var timeCurrentText: TextView
    private lateinit var timeTotalText: TextView
    
    // Audio manager para control de volumen
    private lateinit var audioManager: AudioManager
    private var isFullscreen = false
    
    // Auto-hide functionality
    private val handler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null
    private val hideDelayMs = 3000L // 3 segundos
    
    // Callbacks para comunicación con VideoView
    var onPlayPause: (() -> Unit)? = null
    var onSeek: ((Int) -> Unit)? = null
    var onRewind: (() -> Unit)? = null
    var onFastForward: (() -> Unit)? = null
    var onVolumeToggle: (() -> Unit)? = null
    var onFullscreenToggle: ((Boolean) -> Unit)? = null
    
    // Referencias al VideoView
    var videoView: VideoView? = null
    
    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initializeView()
    }

    private fun initializeView() {
        // Crear vista personalizada
        val inflater = LayoutInflater.from(context)
        val customView = inflater.inflate(R.layout.custom_media_controller, this, true)
        
        initializeViews(customView)
        setupClickListeners()
    }
    
    private fun initializeViews(view: View) {
        playPauseButton = view.findViewById(R.id.pause)
        rewindButton = view.findViewById(R.id.rew)
        fastForwardButton = view.findViewById(R.id.ffwd)
        volumeButton = view.findViewById(R.id.volume_button)
        fullscreenButton = view.findViewById(R.id.fullscreen_button)
        progressBar = view.findViewById(R.id.mediacontroller_progress)
        timeCurrentText = view.findViewById(R.id.time_current)
        timeTotalText = view.findViewById(R.id.time)
        
        // Configurar SeekBar
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && videoView != null) {
                    // Calcular posición basada en el progreso (0-1000 mapeado a duración del video)
                    val duration = videoView!!.duration
                    if (duration > 0) {
                        val position = (progress * duration / 1000).toInt()
                        onSeek?.invoke(position)
                        updateTimeDisplays(position, duration)
                    }
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Opcional: Pausar actualizaciones automáticas mientras el usuario arrastra
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Opcional: Reanudar actualizaciones automáticas
            }
        })
    }
    
    private fun setupClickListeners() {
        playPauseButton.setOnClickListener {
            onPlayPause?.invoke()
        }
        
        rewindButton.setOnClickListener {
            onRewind?.invoke()
        }
        
        fastForwardButton.setOnClickListener {
            onFastForward?.invoke()
        }
        
        volumeButton.setOnClickListener {
            toggleVolume()
        }
        
        fullscreenButton.setOnClickListener {
            toggleFullscreen()
        }
    }
    
    private fun toggleVolume() {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        
        if (currentVolume > 0) {
            // Mutear
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            volumeButton.setImageResource(R.drawable.ic_volume_off)
        } else {
            // Restaurar volumen (50% del máximo)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0)
            volumeButton.setImageResource(R.drawable.ic_volume)
        }
    }
    
    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        onFullscreenToggle?.invoke(isFullscreen)
        
        fullscreenButton.setImageResource(
            if (isFullscreen) R.drawable.ic_fullscreen_exit
            else R.drawable.ic_fullscreen
        )
    }
    
    fun updateProgress(currentPosition: Int, duration: Int, isPlaying: Boolean) {
        if (duration > 0) {
            val progress = (1000L * currentPosition / duration).toInt()
            progressBar.progress = progress
            updateTimeDisplays(currentPosition, duration)
        }
        
        // Actualizar icono de play/pausa
        playPauseButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause 
            else R.drawable.ic_video_play
        )
    }
    
    private fun updateTimeDisplays(current: Int, total: Int) {
        timeCurrentText.text = formatTime(current)
        timeTotalText.text = formatTime(total)
    }
    
    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) - 
                     TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun show() {
        visibility = View.VISIBLE
        scheduleAutoHide()
    }
    
    fun hide() {
        visibility = View.GONE
        cancelAutoHide()
    }
    
    fun isShowing(): Boolean {
        return visibility == View.VISIBLE
    }
    
    private fun scheduleAutoHide() {
        cancelAutoHide()
        hideRunnable = Runnable {
            hide()
        }
        handler.postDelayed(hideRunnable!!, hideDelayMs)
    }
    
    private fun cancelAutoHide() {
        hideRunnable?.let { handler.removeCallbacks(it) }
        hideRunnable = null
    }
    
    fun showWithoutAutoHide() {
        visibility = View.VISIBLE
        cancelAutoHide()
    }
    
    fun testControls() {
        show()
        // Hacer que se mantenga visible por más tiempo para testing
        cancelAutoHide()
        handler.postDelayed({
            hide()
        }, 10000L) // 10 segundos para probar
    }
}
