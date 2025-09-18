package com.group.redesmascotas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView

// La MainActivity implementa la interfaz del MenuFragment para poder recibir los eventos de clic.
class MainActivity : AppCompatActivity(), MenuFragment.OnOptionClickListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configurar el Toolbar como ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Configurar DrawerLayout y toggle
        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        
        // Habilitar el indicador del drawer (ícono hamburguesa)
        toggle.setDrawerIndicatorEnabled(true)
        
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        // Configurar el título de la aplicación
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayShowTitleEnabled(true)
        }

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
            "interactions" -> ButtonsFragment()
            else -> ProfileFragment() // Caso por defecto
        }

        // Inicia una transacción de fragmentos para reemplazar el contenido del contenedor principal.
        supportFragmentManager.commit {
            replace(R.id.content_fragment_container, fragment as Fragment)
            setReorderingAllowed(true) // Optimización para las transiciones
        }
        
        // Cerrar el drawer después de seleccionar una opción
        drawerLayout.closeDrawers()
    }
    
    // Método para gestionar el botón de retroceso cuando el drawer está abierto
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(findViewById<NavigationView>(R.id.nav_view))) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}