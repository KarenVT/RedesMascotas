package com.group.redesmascotas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.group.redesmascotas.adapter.PhotoAdapter
import com.group.redesmascotas.database.AllDatabase
import com.group.redesmascotas.repository.PhotoRepository
import com.group.redesmascotas.database.PhotoEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest


// Enum para los tipos de filtros
enum class PhotoFilter { ALL, FAVORITES, WITH_FRIENDS }

class PhotosFragment : Fragment() {
    
    private lateinit var recyclerViewPhotos: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var emptyState: View
    private lateinit var fabAddPhoto: FloatingActionButton
    private lateinit var btnAllPhotos: MaterialButton
    private lateinit var btnFavorites: MaterialButton
    private lateinit var btnWithFriends: MaterialButton
    
    // Repository para persistencia
    private lateinit var photoRepository: PhotoRepository
    
    // Lista de fotos en UI
    private var allPhotos = mutableListOf<PhotoEntity>()
    private var filteredPhotos = mutableListOf<PhotoEntity>()
    private var currentFilter = PhotoFilter.ALL
    
    // Variable temporal para URI de nueva foto
    private var pendingPhotoUri: Uri? = null
    
    // Launcher para seleccionar imágenes
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                pendingPhotoUri = uri
                showAddPhotoDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        initializeRepository()
        setupRecyclerView()
        setupFilterButtons()
        setupFab()
        observePhotos()
    }

    private fun initializeViews(view: View) {
        recyclerViewPhotos = view.findViewById(R.id.recyclerViewPhotos)
        emptyState = view.findViewById(R.id.emptyStateContainer)
        fabAddPhoto = view.findViewById(R.id.fabAddPhoto)
        btnAllPhotos = view.findViewById(R.id.btnAllPhotos)
        btnFavorites = view.findViewById(R.id.btnFavorites)
        btnWithFriends = view.findViewById(R.id.btnWithFriends)
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(filteredPhotos) { photo, action ->
            when (action) {
                PhotoAdapter.Action.TOGGLE_FAVORITE -> toggleFavorite(photo)
                PhotoAdapter.Action.EDIT -> editPhoto(photo)
                PhotoAdapter.Action.VIEW -> viewPhoto(photo)
            }
        }
        
        recyclerViewPhotos.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = photoAdapter
        }
    }

    private fun setupFilterButtons() {
        btnAllPhotos.setOnClickListener { 
            setFilter(PhotoFilter.ALL) 
        }
        
        btnFavorites.setOnClickListener { 
            setFilter(PhotoFilter.FAVORITES) 
        }
        
        btnWithFriends.setOnClickListener { 
            setFilter(PhotoFilter.WITH_FRIENDS) 
        }
        
        // Establecer estado inicial
        updateFilterButtonsUI()
    }

    private fun setupFab() {
        fabAddPhoto.setOnClickListener {
            openImagePicker()
        }
    }

    private fun initializeRepository() {
        val database = AllDatabase.getDatabase(requireContext())
        photoRepository = PhotoRepository(database.photoDao(), requireContext())
    }
    
    private fun observePhotos() {
        lifecycleScope.launch {
            photoRepository.getAllPhotos().collectLatest { photos ->
                allPhotos.clear()
                allPhotos.addAll(photos)
                applyFilter()
            }
        }
    }

    private fun setFilter(filter: PhotoFilter) {
        currentFilter = filter
        updateFilterButtonsUI()
        applyFilter()
    }

    private fun updateFilterButtonsUI() {
        // Resetear todos los botones
        resetFilterButton(btnAllPhotos)
        resetFilterButton(btnFavorites)
        resetFilterButton(btnWithFriends)
        
        // Activar el botón seleccionado
        when (currentFilter) {
            PhotoFilter.ALL -> activateFilterButton(btnAllPhotos)
            PhotoFilter.FAVORITES -> activateFilterButton(btnFavorites)
            PhotoFilter.WITH_FRIENDS -> activateFilterButton(btnWithFriends)
        }
    }

    private fun resetFilterButton(button: MaterialButton) {
        button.apply {
            setTextColor(requireContext().getColor(R.color.text_secondary))
            backgroundTintList = requireContext().getColorStateList(R.color.white)
            strokeColor = requireContext().getColorStateList(R.color.soft_gray)
        }
    }

    private fun activateFilterButton(button: MaterialButton) {
        button.apply {
            setTextColor(requireContext().getColor(R.color.primary_text))
            backgroundTintList = requireContext().getColorStateList(R.color.soft_gray)
            strokeColor = requireContext().getColorStateList(R.color.soft_gray)
        }
    }

    private fun applyFilter() {
        filteredPhotos.clear()
        
        when (currentFilter) {
            PhotoFilter.ALL -> filteredPhotos.addAll(allPhotos)
            PhotoFilter.FAVORITES -> filteredPhotos.addAll(allPhotos.filter { it.isFavorite })
            PhotoFilter.WITH_FRIENDS -> filteredPhotos.addAll(allPhotos.filter { it.isWithFriends })
        }
        
        photoAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredPhotos.isEmpty()) {
            recyclerViewPhotos.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            recyclerViewPhotos.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun toggleFavorite(photo: PhotoEntity) {
        lifecycleScope.launch {
            try {
                val newFavoriteStatus = !photo.isFavorite
                photoRepository.updatePhotoFavorite(photo.id, newFavoriteStatus)
                
                val message = if (newFavoriteStatus) {
                    getString(R.string.photo_favorite_added)
                } else {
                    getString(R.string.photo_favorite_removed)
                }
                
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al actualizar favorito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editPhoto(photo: PhotoEntity) {
        showEditPhotoDialog(photo)
    }

    private fun viewPhoto(photo: PhotoEntity) {
        // Aquí implementarías la lógica para ver la foto en detalle
        Toast.makeText(requireContext(), "Viendo: ${photo.description}", Toast.LENGTH_SHORT).show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun addNewPhoto(uri: Uri, description: String, isFavorite: Boolean, isWithFriends: Boolean) {
        lifecycleScope.launch {
            try {
                val photoId = photoRepository.insertPhoto(uri, description, isFavorite, isWithFriends)
                if (photoId != null) {
                    Toast.makeText(requireContext(), getString(R.string.photo_added_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar la foto", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al procesar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAddPhotoDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_photo_details, null)
        
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextPhotoName)
        val checkBoxFavorite = dialogView.findViewById<CheckBox>(R.id.checkBoxFavorite)
        val checkBoxWithFriends = dialogView.findViewById<CheckBox>(R.id.checkBoxWithFriends)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.photo_add_dialog_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_save)) { _, _ ->
                val name = editTextName.text.toString().trim()
                if (name.isNotEmpty()) {
                    pendingPhotoUri?.let { uri ->
                        addNewPhoto(
                            uri = uri,
                            description = name,
                            isFavorite = checkBoxFavorite.isChecked,
                            isWithFriends = checkBoxWithFriends.isChecked
                        )
                    }
                    pendingPhotoUri = null
                } else {
                    Toast.makeText(requireContext(), getString(R.string.photo_name_required), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ ->
                pendingPhotoUri = null
            }
            .create()
        
        dialog.show()
    }
    
    private fun showEditPhotoDialog(photo: PhotoEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_photo_details, null)
        
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextPhotoName)
        val checkBoxFavorite = dialogView.findViewById<CheckBox>(R.id.checkBoxFavorite)
        val checkBoxWithFriends = dialogView.findViewById<CheckBox>(R.id.checkBoxWithFriends)
        
        // Llenar con datos actuales
        editTextName.setText(photo.description)
        checkBoxFavorite.isChecked = photo.isFavorite
        checkBoxWithFriends.isChecked = photo.isWithFriends
        
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.photo_edit_dialog_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_save)) { _, _ ->
                val name = editTextName.text.toString().trim()
                if (name.isNotEmpty()) {
                    updatePhoto(
                        photo = photo,
                        newDescription = name,
                        newIsFavorite = checkBoxFavorite.isChecked,
                        newIsWithFriends = checkBoxWithFriends.isChecked
                    )
                } else {
                    Toast.makeText(requireContext(), getString(R.string.photo_name_required), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .create()
        
        dialog.show()
    }
    
    private fun updatePhoto(photo: PhotoEntity, newDescription: String, newIsFavorite: Boolean, newIsWithFriends: Boolean) {
        lifecycleScope.launch {
            try {
                photoRepository.updatePhotoDetails(photo.id, newDescription, newIsFavorite, newIsWithFriends)
                Toast.makeText(requireContext(), getString(R.string.photo_updated_success), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al actualizar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }
}