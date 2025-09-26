package com.group.redesmascotas

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.google.android.material.button.MaterialButton
import com.group.redesmascotas.database.AllDatabase
import com.group.redesmascotas.database.BookmarkEntity
import com.group.redesmascotas.repository.BookmarkRepository

// Este fragmento representa la sección de navegador web.
class WebFragment : Fragment() {
    
    private lateinit var webView: WebView
    private lateinit var etUrl: EditText
    private lateinit var btnGo: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var tvCurrentUrl: TextView
    private lateinit var btnSaveUrl: ImageButton
    private lateinit var bookmarksContainer: LinearLayout
    private lateinit var bookmarksScrollView: ScrollView
    
    // Botones de categorías
    private lateinit var btnAll: MaterialButton
    private lateinit var btnBlog: MaterialButton
    private lateinit var btnPetshop: MaterialButton
    private lateinit var btnVeterinario: MaterialButton
    
    // Repository para manejar bookmarks
    private lateinit var bookmarkRepository: BookmarkRepository
    private val savedBookmarks = mutableListOf<BookmarkEntity>()
    private var currentCategory = "Todos"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        initializeRepository()
        setupWebView()
        setupNavigationButtons()
        setupBookmarks()
        setupCategoryFilters()
        setupScrollViewTouch()
        observeBookmarks()
        
        // No cargar ninguna página por defecto, mostrar página en blanco
        tvCurrentUrl.text = "Escribe una URL para navegar"
    }
    
    private fun initializeViews(view: View) {
        webView = view.findViewById(R.id.webview)
        etUrl = view.findViewById(R.id.et_url)
        btnGo = view.findViewById(R.id.btn_go)
        btnBack = view.findViewById(R.id.btn_back)
        btnForward = view.findViewById(R.id.btn_forward)
        btnRefresh = view.findViewById(R.id.btn_refresh)
        tvCurrentUrl = view.findViewById(R.id.tv_current_url)
        btnSaveUrl = view.findViewById(R.id.btn_save_url)
        bookmarksContainer = view.findViewById(R.id.bookmarks_container)
        bookmarksScrollView = view.findViewById(R.id.bookmarks_scroll_view)
        
        // Botones de categorías
        btnAll = view.findViewById(R.id.btn_all)
        btnBlog = view.findViewById(R.id.btn_blog)
        btnPetshop = view.findViewById(R.id.btn_petshop)
        btnVeterinario = view.findViewById(R.id.btn_veterinario)
    }
    
    private fun initializeRepository() {
        val database = AllDatabase.getDatabase(requireContext())
        bookmarkRepository = BookmarkRepository(database.bookmarkDao(), requireContext())
    }
    
    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            
            // Configuraciones para scroll compatible
            setSupportZoom(true)
            
        }
        
        // Configurar scroll anidado compatible
        webView.isNestedScrollingEnabled = true
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let {
                    tvCurrentUrl.text = it
                    etUrl.setText(it)
                    updateNavigationButtons()
                }
            }
        }
        
        // Manejar gestos de scroll para mejor compatibilidad
        webView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Permitir que el WebView maneje su propio scroll
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    // Devolver control al NestedScrollView cuando termine el gesto
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }
    
    private fun setupNavigationButtons() {
        btnGo.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                loadUrl(url)
            }
        }
        
        btnBack.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
        
        btnForward.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }
        
        btnRefresh.setOnClickListener {
            webView.reload()
        }
        
        // Navegar al presionar Enter en el campo URL
        etUrl.setOnEditorActionListener { _, _, _ ->
            btnGo.performClick()
            true
        }
        
        // Actualizar estado del botón guardar según el contenido del campo
        etUrl.doOnTextChanged { text, _, _, _ ->
            val hasValidUrl = !text.isNullOrEmpty() && text.contains(".")
            btnSaveUrl.isEnabled = hasValidUrl
            btnSaveUrl.alpha = if (hasValidUrl) 1.0f else 0.5f
        }
    }
    
    private fun setupBookmarks() {
        // Guardar URL desde el campo de texto
        btnSaveUrl.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotEmpty() && url.contains(".")) {
                val title = if (webView.url == url || webView.url?.contains(url) == true) {
                    webView.title ?: "Página web"
                } else {
                    "Enlace personalizado"
                }
                showCategorySelectionDialog(title, url)
                showToast("URL lista para guardar")
            } else {
                showToast("Escribe una URL válida para guardar")
            }
        }
    }

    private fun setupCategoryFilters() {
        btnAll.setOnClickListener { filterByCategory("Todos") }
        btnBlog.setOnClickListener { filterByCategory("Blogs") }
        btnPetshop.setOnClickListener { filterByCategory("Tiendas") }
        btnVeterinario.setOnClickListener { filterByCategory("Veterinarias") }

        // Inicializar con "Todos" seleccionado
        filterByCategory("Todos")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupScrollViewTouch() {
        // Configurar el ScrollView para manejar correctamente el scroll táctil dentro del NestedScrollView padre
        bookmarksScrollView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Permitir que el ScrollView maneje su propio scroll
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                android.view.MotionEvent.ACTION_UP, 
                android.view.MotionEvent.ACTION_CANCEL -> {
                    // Devolver control al NestedScrollView padre cuando termine el gesto
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false // Permitir que el ScrollView procese el evento normalmente
        }
    }
    
    private fun filterByCategory(category: String) {
        currentCategory = category
        updateCategoryButtonsUI()
        updateBookmarksObserver()
    }

    private fun updateCategoryButtonsUI() {
        // Resetear todos los botones
        resetCategoryButton(btnAll)
        resetCategoryButton(btnBlog)
        resetCategoryButton(btnPetshop)
        resetCategoryButton(btnVeterinario)
        
        // Activar el botón seleccionado
        when (currentCategory) {
            "Todos" -> activateCategoryButton(btnAll)
            "Blogs" -> activateCategoryButton(btnBlog)
            "Tiendas" -> activateCategoryButton(btnPetshop)
            "Veterinarias" -> activateCategoryButton(btnVeterinario)
        }
    }

    private fun resetCategoryButton(button: MaterialButton) {
        button.apply {
            setTextColor(requireContext().getColor(R.color.primary_text))
            backgroundTintList = requireContext().getColorStateList(R.color.white)
            strokeColor = requireContext().getColorStateList(R.color.soft_gray)
        }
    }

    private fun activateCategoryButton(button: MaterialButton) {
        button.apply {
            setTextColor(requireContext().getColor(R.color.white))
            backgroundTintList = requireContext().getColorStateList(R.color.blue_primary)
            strokeColor = requireContext().getColorStateList(R.color.soft_gray)
        }
    }
    
    private fun observeBookmarks() {
        // Observar cambios en bookmarks usando Flow
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                bookmarkRepository.getBookmarksByCategory(currentCategory).collect { bookmarks ->
                    savedBookmarks.clear()
                    savedBookmarks.addAll(bookmarks)
                    updateBookmarksDisplay()
                }
            }
        }
    }
    
    private fun addBookmark(title: String, url: String, category: String) {
        // Verificar si ya existe
        val exists = savedBookmarks.any { it.url == url }
        if (!exists) {
            viewLifecycleOwner.lifecycleScope.launch {
                val result = bookmarkRepository.saveBookmark(title, url, category)
                result.onSuccess {
                    showToast("Enlace guardado en $category")
                }.onFailure { exception ->
                    showToast("Error al guardar enlace: ${exception.message}")
                }
            }
        } else {
            showToast("Este enlace ya está guardado")
        }
    }
    
    private fun showCategorySelectionDialog(title: String, url: String) {
        val categories = arrayOf("Todos", "Blogs", "Tiendas", "Veterinarias")
        
        val alertDialog = android.app.AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Seleccionar categoría")
        alertDialog.setItems(categories) { _, which ->
            addBookmark(title, url, categories[which])
        }
        alertDialog.setNegativeButton("Cancelar", null)
        alertDialog.show()
    }
    
    private fun updateBookmarksObserver() {
        // Cambiar el observer cuando cambie la categoría
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                bookmarkRepository.getBookmarksByCategory(currentCategory).collect { bookmarks ->
                    savedBookmarks.clear()
                    savedBookmarks.addAll(bookmarks)
                    updateBookmarksDisplay()
                }
            }
        }
    }
    
    private fun updateBookmarksDisplay() {
        bookmarksContainer.removeAllViews()
        
        for (bookmark in savedBookmarks) {
            addBookmarkView(bookmark)
        }
        
        // Mostrar mensaje si no hay enlaces
        if (savedBookmarks.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No hay enlaces guardados en esta categoría"
                textSize = 14f
                setTextColor(requireContext().resources.getColor(R.color.accent, null))
                gravity = android.view.Gravity.CENTER
                setPadding(16, 32, 16, 32)
            }
            bookmarksContainer.addView(emptyView)
        }
    }
    
    private fun addBookmarkView(bookmark: BookmarkEntity) {
        val bookmarkView = LayoutInflater.from(requireContext()).inflate(R.layout.bookmark_item, bookmarksContainer, false)
        
        val tvTitle = bookmarkView.findViewById<TextView>(R.id.tv_bookmark_title)
        val tvUrl = bookmarkView.findViewById<TextView>(R.id.tv_bookmark_url)
        val tvCategory = bookmarkView.findViewById<TextView>(R.id.tv_bookmark_category)
        val btnDelete = bookmarkView.findViewById<ImageButton>(R.id.btn_delete_bookmark)
        
        tvTitle.text = bookmark.title
        tvUrl.text = bookmark.url
        tvCategory.text = bookmark.category
        
        // Click para navegar
        bookmarkView.setOnClickListener {
            loadUrl(bookmark.url)
            showToast("Navegando a ${bookmark.title}")
        }
        
        // Click para eliminar
        btnDelete.setOnClickListener {
            showDeleteConfirmation(bookmark)
        }
        
        bookmarksContainer.addView(bookmarkView)
    }
    
    private fun showDeleteConfirmation(bookmark: BookmarkEntity) {
        val alertDialog = android.app.AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Eliminar enlace")
        alertDialog.setMessage("¿Estás seguro de que quieres eliminar \"${bookmark.title}\"?")
        alertDialog.setPositiveButton("Eliminar") { _, _ ->
            viewLifecycleOwner.lifecycleScope.launch {
                val result = bookmarkRepository.deleteBookmark(bookmark)
                result.onSuccess {
                    showToast("Enlace eliminado")
                }.onFailure { exception ->
                    showToast("Error al eliminar enlace: ${exception.message}")
                }
            }
        }
        alertDialog.setNegativeButton("Cancelar", null)
        alertDialog.show()
    }
    
    private fun loadUrl(url: String) {
        var finalUrl = url
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            finalUrl = "https://$url"
        }
        webView.loadUrl(finalUrl)
    }
    
    private fun updateNavigationButtons() {
        btnBack.isEnabled = webView.canGoBack()
        btnForward.isEnabled = webView.canGoForward()
        
        // Cambiar opacidad visual según estado
        btnBack.alpha = if (webView.canGoBack()) 1.0f else 0.5f
        btnForward.alpha = if (webView.canGoForward()) 1.0f else 0.5f
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}