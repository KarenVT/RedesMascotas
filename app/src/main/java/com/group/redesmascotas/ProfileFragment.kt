package com.group.redesmascotas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

// Clase de datos para el perfil de la mascota
data class PetProfile(
    val petName: String,
    val petBreed: String,
    val petAge: String,
    val ownerName: String,
    val interests: String,
    val profileImageResource: Int = R.drawable.ic_paw_logo
)

// Este fragmento representa la sección de perfil de la mascota
class ProfileFragment : Fragment() {
    
    // Variables para las vistas
    private lateinit var profileImage: ImageView
    private lateinit var petNameText: TextView
    private lateinit var petBreedText: TextView
    private lateinit var petAgeText: TextView
    private lateinit var ownerNameText: TextView
    private lateinit var petInterestsText: TextView
    
    // Datos del perfil (se pueden cargar desde una base de datos o API en el futuro)
    private lateinit var currentProfile: PetProfile

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout para este fragmento
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializar el perfil con datos por defecto desde strings.xml
        initializeProfile()
        
        // Inicializar las vistas
        initializeViews(view)
        
        // Configurar los datos del perfil
        setupProfileData()
    }
    
    private fun initializeProfile() {
        // Inicializar el perfil con datos por defecto desde resources
        currentProfile = PetProfile(
            petName = getString(R.string.profile_pet_name_default),
            petBreed = getString(R.string.profile_pet_breed_default),
            petAge = getString(R.string.profile_pet_age_default),
            ownerName = getString(R.string.profile_owner_name_default),
            interests = getString(R.string.profile_interests_default)
        )
    }
    
    private fun initializeViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        petNameText = view.findViewById(R.id.petName)
        petBreedText = view.findViewById(R.id.petBreed)
        petAgeText = view.findViewById(R.id.petAge)
        ownerNameText = view.findViewById(R.id.ownerName)
        petInterestsText = view.findViewById(R.id.petInterests)
    }
    
    private fun setupProfileData() {
        // Configurar la imagen de perfil
        profileImage.setImageResource(currentProfile.profileImageResource)
        
        // Configurar los textos con los datos del perfil
        petNameText.text = currentProfile.petName
        petBreedText.text = currentProfile.petBreed
        petAgeText.text = currentProfile.petAge
        ownerNameText.text = currentProfile.ownerName
        petInterestsText.text = currentProfile.interests
    }
    
    // Método para actualizar el perfil (útil para futuras funcionalidades)
    fun updateProfile(newProfile: PetProfile) {
        // Se pueden agregar validaciones aquí
        
        // Actualizar la UI con los nuevos datos
        profileImage.setImageResource(newProfile.profileImageResource)
        petNameText.text = newProfile.petName
        petBreedText.text = newProfile.petBreed
        petAgeText.text = newProfile.petAge
        ownerNameText.text = newProfile.ownerName
        petInterestsText.text = newProfile.interests
    }
    
    // Método estático para crear una nueva instancia del fragmento
    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}