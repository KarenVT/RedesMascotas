package com.group.redesmascotas

import android.app.AlertDialog
import android.view.inputmethod.EditorInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.group.redesmascotas.database.ProfileEntity
import com.group.redesmascotas.database.AllDatabase
import com.group.redesmascotas.repository.ProfileRepository
import com.group.redesmascotas.utils.ImageUtils
import kotlinx.coroutines.launch

// Este fragmento representa la sección de perfil de la mascota
class ProfileFragment : Fragment() {
    
    // Variables para las vistas
    private lateinit var profileImage: ImageView
    private lateinit var petNameText: TextView
    private lateinit var petBreedText: TextView
    private lateinit var petAgeText: TextView
    private lateinit var ownerNameText: TextView
    private lateinit var petInterestsText: TextView
    private lateinit var interestsChipGroup: ChipGroup
    private lateinit var emptyStateMessage: TextView
    private lateinit var emptyStateContainer: com.google.android.material.card.MaterialCardView
    
    // Botones de edición
    private lateinit var editInfoButton: ImageButton
    private lateinit var editOwnerButton: ImageButton
    private lateinit var editInterestsButton: ImageButton
    private lateinit var changeProfileImageButton: ImageButton
    
    // Repository para manejar datos del perfil
    private lateinit var profileRepository: ProfileRepository
    
    // Perfil actual cargado desde la base de datos
    private var currentProfile: ProfileEntity? = null
    
    // Launcher para seleccionar imagen de la galería
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            updateProfileImage(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout para este fragmento
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializar Repository
        initializeRepository()
        
        // Inicializar las vistas
        initializeViews(view)
        
        // Configurar los listeners de los botones
        setupButtonListeners()
        
        // Cargar y observar los datos del perfil
        observeProfileData()
    }
    
    private fun initializeRepository() {
        val database = AllDatabase.getDatabase(requireContext())
        profileRepository = ProfileRepository(database.profileDao(), requireContext())
    }
    
    private fun initializeViews(view: View) {
        // Inicializar vistas de texto
        profileImage = view.findViewById(R.id.profileImage)
        petNameText = view.findViewById(R.id.petName)
        petBreedText = view.findViewById(R.id.petBreed)
        petAgeText = view.findViewById(R.id.petAge)
        ownerNameText = view.findViewById(R.id.ownerName)
        petInterestsText = view.findViewById(R.id.petInterests)
        interestsChipGroup = view.findViewById(R.id.interestsChipGroup)
        emptyStateMessage = view.findViewById(R.id.emptyStateMessage)
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)
        
        // Inicializar botones de edición
        editInfoButton = view.findViewById(R.id.editInfoButton)
        editOwnerButton = view.findViewById(R.id.editOwnerButton)
        editInterestsButton = view.findViewById(R.id.editInterestsButton)
        changeProfileImageButton = view.findViewById(R.id.changeProfileImageButton)
    }
    
    private fun setupButtonListeners() {
        editInfoButton.setOnClickListener { 
            showEditInfoDialog() 
        }
        
        editOwnerButton.setOnClickListener { 
            showEditOwnerDialog() 
        }
        
        editInterestsButton.setOnClickListener { 
            showEditInterestsDialog() 
        }
        
        changeProfileImageButton.setOnClickListener {
            openImagePicker()
        }
    }
    
    private fun observeProfileData() {
        // Observar cambios en el perfil usando Flow
        lifecycleScope.launch {
            profileRepository.getProfile().collect { profile ->
                currentProfile = profile
                updateUI(profile)
            }
        }
    }
    
    private fun updateUI(profile: ProfileEntity?) {
        if (profile == null || isProfileEmpty(profile)) {
            // Mostrar estado vacío
            showEmptyState()
        } else {
            // Mostrar datos del perfil
            showProfileData(profile)
        }
    }
    
    private fun isProfileEmpty(profile: ProfileEntity): Boolean {
        return profile.petName.isEmpty() && 
               profile.petBreed.isEmpty() && 
               profile.petAge.isEmpty() && 
               profile.ownerName.isEmpty() && 
               profile.interests.isEmpty()
    }
    
    private fun showEmptyState() {
        emptyStateContainer.visibility = View.VISIBLE
        
        // Mostrar placeholders con colores de texto secundario
        displayFieldData(petNameText, null, getString(R.string.profile_pet_name_placeholder))
        displayFieldData(petBreedText, null, getString(R.string.profile_pet_breed_placeholder))
        displayFieldData(petAgeText, null, getString(R.string.profile_pet_age_placeholder))
        displayFieldData(ownerNameText, null, getString(R.string.profile_owner_name_placeholder))
        
        // Mostrar placeholder de intereses
        displayInterests("")
        
        // Configurar imagen por defecto
        loadProfileImage("")
    }
    
    private fun showProfileData(profile: ProfileEntity) {
        emptyStateContainer.visibility = View.GONE
        
        // Mostrar datos del perfil con colores de texto primario
        displayFieldData(petNameText, profile.petName, getString(R.string.profile_pet_name_placeholder))
        displayFieldData(petBreedText, profile.petBreed, getString(R.string.profile_pet_breed_placeholder))
        displayFieldData(petAgeText, profile.petAge, getString(R.string.profile_pet_age_placeholder))
        displayFieldData(ownerNameText, profile.ownerName, getString(R.string.profile_owner_name_placeholder))
        
        // Manejar múltiples intereses
        displayInterests(profile.interests)
        
        // Configurar imagen de perfil
        loadProfileImage(profile.profileImagePath)
    }
    
    private fun displayFieldData(textView: TextView, data: String?, placeholder: String) {
        if (data.isNullOrEmpty()) {
            textView.text = placeholder
            textView.setTextColor(resources.getColor(R.color.profile_empty_text, null))
            textView.setTypeface(textView.typeface, android.graphics.Typeface.NORMAL)
        } else {
            textView.text = data
            textView.setTextColor(resources.getColor(R.color.text_primary, null))
            textView.setTypeface(textView.typeface, android.graphics.Typeface.BOLD)
        }
    }
    
    private fun showEditInfoDialog() {
        // Mostrar diálogos secuenciales para la información de la mascota
        showEditFieldDialog(
            getString(R.string.profile_edit_pet_name),
            getString(R.string.profile_pet_name_placeholder),
            currentProfile?.petName ?: ""
        ) { newPetName ->
            showEditBreedDialog(newPetName)
        }
    }
    
    private fun showEditBreedDialog(petName: String) {
        showEditFieldDialog(
            getString(R.string.profile_edit_pet_breed),
            getString(R.string.profile_pet_breed_placeholder),
            currentProfile?.petBreed ?: ""
        ) { newPetBreed ->
            showEditAgeDialog(petName, newPetBreed)
        }
    }
    
    private fun showEditAgeDialog(petName: String, petBreed: String) {
        showEditFieldDialog(
            getString(R.string.profile_edit_pet_age),
            getString(R.string.profile_pet_age_placeholder),
            currentProfile?.petAge ?: ""
        ) { newPetAge ->
            saveInfoData(petName, petBreed, newPetAge)
        }
    }
    
    private fun showEditFieldDialog(
        title: String, 
        hint: String, 
        currentValue: String, 
        onSave: (String) -> Unit
    ) {
        val editText = EditText(requireContext()).apply {
            this.hint = hint
            setText(currentValue)
            setPadding(50, 30, 50, 30)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(editText)
            .setPositiveButton(getString(R.string.profile_save_button)) { _, _ ->
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    onSave(newValue)
                } else {
                    showErrorMessage(getString(R.string.profile_field_required))
                }
            }
            .setNegativeButton(getString(R.string.profile_cancel_button), null)
            .show()
    }
    
    private fun showEditOwnerDialog() {
        showEditFieldDialog(
            getString(R.string.profile_edit_owner_name),
            getString(R.string.profile_owner_name_placeholder),
            currentProfile?.ownerName ?: ""
        ) { newValue ->
            lifecycleScope.launch {
                val result = profileRepository.updateOwnerName(newValue)
                if (result.isSuccess) {
                    showSuccessMessage()
                } else {
                    showErrorMessage(getString(R.string.profile_error_saving))
                }
            }
        }
    }
    
    private fun showEditInterestsDialog() {
        val currentInterestsString = currentProfile?.interests ?: ""
        val currentInterestsList = parseInterestsFromString(currentInterestsString)
        
        // Crear diálogo personalizado para múltiples intereses
        showMultipleInterestsDialog(currentInterestsList) { newInterestsList ->
            val newInterestsString = formatInterestsToString(newInterestsList)
            lifecycleScope.launch {
                val result = profileRepository.updateInterests(newInterestsString)
                if (result.isSuccess) {
                    showSuccessMessage()
                    // Forzar actualización inmediata de la UI
                    displayInterests(newInterestsString)
                } else {
                    showErrorMessage(getString(R.string.profile_error_saving))
                }
            }
        }
    }
    
    private fun saveInfoData(petName: String, petBreed: String, petAge: String) {
        lifecycleScope.launch {
            val result = if (currentProfile != null) {
                // Actualizar perfil existente manteniendo otros datos
                profileRepository.saveProfile(
                    petName = petName,
                    petBreed = petBreed,
                    petAge = petAge,
                    ownerName = currentProfile!!.ownerName,
                    interests = currentProfile!!.interests,
                    profileImagePath = currentProfile!!.profileImagePath
                )
            } else {
                // Crear nuevo perfil
                profileRepository.saveProfile(
                    petName = petName,
                    petBreed = petBreed,
                    petAge = petAge,
                    ownerName = "",
                    interests = "",
                    profileImagePath = ""
                )
            }
            
            if (result.isSuccess) {
                showSuccessMessage()
            } else {
                showErrorMessage(getString(R.string.profile_error_saving))
            }
        }
    }
    
    private fun showSuccessMessage() {
        Toast.makeText(requireContext(), getString(R.string.profile_updated_success), Toast.LENGTH_SHORT).show()
    }
    
    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    // Abrir selector de imagen
    private fun openImagePicker() {
        try {
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            showErrorMessage("Error al abrir la galería")
        }
    }
    
    // Actualizar imagen de perfil
    private fun updateProfileImage(uri: android.net.Uri) {
        lifecycleScope.launch {
            try {
                val result = profileRepository.updateProfileImage(uri)
                if (result.isSuccess) {
                    val imagePath = result.getOrNull()
                    if (imagePath != null) {
                        loadProfileImage(imagePath)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.profile_image_updated),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    showErrorMessage(getString(R.string.profile_image_error))
                }
            } catch (e: Exception) {
                showErrorMessage(getString(R.string.profile_image_error))
            }
        }
    }
    
    // Cargar imagen de perfil
    private fun loadProfileImage(imagePath: String) {
        if (imagePath.isNotEmpty() && ImageUtils.imageExists(imagePath)) {
            try {
                val bitmap = ImageUtils.loadImageFromInternalStorage(imagePath)
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap)
                } else {
                    profileImage.setImageResource(R.drawable.ic_paw_logo)
                }
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.ic_paw_logo)
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_paw_logo)
        }
    }
    
    // Método para limpiar todos los datos del perfil (útil para testing o reset)
    fun clearProfileData() {
        lifecycleScope.launch {
            // Eliminar imagen de perfil si existe
            currentProfile?.let { profile ->
                if (profile.profileImagePath.isNotEmpty()) {
                    ImageUtils.deleteImageFromInternalStorage(profile.profileImagePath)
                }
            }
            // Limpiar datos de la base de datos
            profileRepository.clearProfile()
        }
    }
    
    // ======= FUNCIONES PARA MÚLTIPLES INTERESES =======
    
    private fun displayInterests(interestsString: String) {
        val interestsList = parseInterestsFromString(interestsString)
        
        if (interestsList.isEmpty()) {
            // Mostrar placeholder
            petInterestsText.visibility = View.VISIBLE
            interestsChipGroup.visibility = View.GONE
            petInterestsText.text = getString(R.string.profile_interests_placeholder)
            petInterestsText.setTextColor(resources.getColor(R.color.profile_empty_text, null))
        } else {
            // Mostrar chips
            petInterestsText.visibility = View.GONE
            interestsChipGroup.visibility = View.VISIBLE
            createInterestChips(interestsList)
        }
    }
    
    private fun createInterestChips(interestsList: List<String>) {
        // Limpiar chips existentes
        interestsChipGroup.removeAllViews()
        
        // Crear chip para cada interés
        interestsList.forEach { interest ->
            val chip = Chip(requireContext())
            chip.text = interest.trim()
            chip.isCloseIconVisible = false
            chip.isClickable = false
            chip.setChipBackgroundColorResource(R.color.blue_light)
            chip.setTextColor(resources.getColor(R.color.text_primary, null))
            chip.chipStrokeColor = resources.getColorStateList(R.color.blue_primary, null)
            chip.chipStrokeWidth = 2f
            
            interestsChipGroup.addView(chip)
        }
    }
    
    private fun parseInterestsFromString(interestsString: String): List<String> {
        if (interestsString.isBlank()) return emptyList()
        return interestsString.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    
    private fun formatInterestsToString(interestsList: List<String>): String {
        return interestsList.joinToString(",")
    }
    
    private fun showMultipleInterestsDialog(
        currentInterests: List<String>, 
        onSave: (List<String>) -> Unit
    ) {
        val mutableInterests = currentInterests.toMutableList()
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "Escribe un interés y presiona Enter"
            setPadding(50, 30, 50, 30)
        }
        
        val chipGroup = ChipGroup(requireContext()).apply {
            chipSpacingHorizontal = 16
            chipSpacingVertical = 8
        }
        
        // Función para actualizar chips
        fun updateChips() {
            chipGroup.removeAllViews()
            mutableInterests.forEachIndexed { index, interest ->
                val chip = Chip(requireContext()).apply {
                    text = interest.trim()
                    isCloseIconVisible = true
                    setChipBackgroundColorResource(R.color.blue_light)
                    setTextColor(resources.getColor(R.color.text_primary, null))
                    chipStrokeColor = resources.getColorStateList(R.color.blue_primary, null)
                    chipStrokeWidth = 2f
                    
                    setOnCloseIconClickListener {
                        if (index < mutableInterests.size) {
                            mutableInterests.removeAt(index)
                            updateChips()
                        }
                    }
                }
                chipGroup.addView(chip)
            }
        }
        
        updateChips()
        
        // Botón para agregar interés (más confiable que Enter)
        val addButton = android.widget.Button(requireContext()).apply {
            text = "Agregar"
            setPadding(32, 16, 32, 16)
            setBackgroundResource(R.color.blue_primary)
            setTextColor(resources.getColor(R.color.white, null))
        }
        
        // Función para agregar interés
        val addInterest = {
            val newInterest = editText.text.toString().trim()
            if (newInterest.isNotEmpty() && !mutableInterests.contains(newInterest)) {
                mutableInterests.add(newInterest)
                editText.text.clear()
                updateChips()
            } else if (newInterest.isEmpty()) {
                showErrorMessage("Escribe un interés")
            } else {
                showErrorMessage("Este interés ya existe")
            }
        }
        
        // Listener para Enter
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                addInterest()
                true
            } else false
        }
        
        // Listener para botón
        addButton.setOnClickListener { addInterest() }
        
        // Container para input + botón
        val inputContainer = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            addView(editText, android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 16
            })
            addView(addButton)
        }
        
        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
            addView(inputContainer)
            addView(android.widget.TextView(requireContext()).apply {
                text = "Intereses actuales (toca X para eliminar):"
                setPadding(0, 20, 0, 10)
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textSize = 14f
            })
            addView(chipGroup)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Intereses")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ ->
                // Limpiar lista antes de guardar
                val finalList = mutableInterests.map { it.trim() }.filter { it.isNotEmpty() }
                onSave(finalList)
            }
            .setNegativeButton("Cancelar", null)
            .setCancelable(true)
            .show()
    }
    
    // Método estático para crear una nueva instancia del fragmento
    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}