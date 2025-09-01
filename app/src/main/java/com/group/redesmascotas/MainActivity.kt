package com.group.redesmascotas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

// La MainActivity implementa la interfaz del MenuFragment para poder recibir los eventos de clic.
class MainActivity : AppCompatActivity(), MenuFragment.OnOptionClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Si es la primera vez que se crea la actividad, carga el fragmento por defecto.
        // Esto evita que se recargue el fragmento al girar la pantalla, por ejemplo.
        if (savedInstanceState == null) {
            onOptionClicked("profile")
        }
    }

    // Este es el método obligatorio que implementa la interfaz. Se ejecuta cuando se pulsa un botón en el menú.
    override fun onOptionClicked(option: String) {
        // Usa una expresión 'when' (similar a un switch) para decidir qué fragmento mostrar.
        val fragment: Any = when (option) {
            "photos" -> PhotosFragment()
            "video" -> VideoFragment()
            "web" -> WebFragment()
            else -> ProfileFragment() // Caso por defecto
        }

        // Inicia una transacción de fragmentos para reemplazar el contenido del contenedor derecho.
        supportFragmentManager.commit {
            replace(R.id.content_fragment_container, fragment as Fragment)
            setReorderingAllowed(true) // Optimización para las transiciones
        }
    }
}