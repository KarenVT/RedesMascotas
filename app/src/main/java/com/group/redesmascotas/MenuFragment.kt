package com.group.redesmascotas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.lang.ClassCastException

// La clase hereda de Fragment 
class MenuFragment : Fragment() {

    // Define una interfaz para comunicarse con la Activity. Es un contrato.
    // La Activity que contenga este fragmento DEBE implementar esta interfaz.
    interface OnOptionClickListener {
        fun onOptionClicked(option: String)
    }

    private var listener: OnOptionClickListener? = null

    // Infla la vista
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    // Este método se llama justo después de que la vista del fragmento ha sido creada.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Se comprueba si la Activity contenedora implementa la interfaz de comunicación.
        if (context is OnOptionClickListener) {
            listener = context as OnOptionClickListener
        } else {
            // Si no la implementa, lanza un error para avisar al desarrollador.
            throw ClassCastException("$context must implement OnOptionClickListener")
        }

        // Se asigna un listener a cada opción del menú usando findViewById
        view.findViewById<View>(R.id.btn_profile).setOnClickListener { 
            listener?.onOptionClicked("profile") 
        }
        view.findViewById<View>(R.id.btn_photos).setOnClickListener { 
            listener?.onOptionClicked("photos") 
        }
        view.findViewById<View>(R.id.btn_video).setOnClickListener { 
            listener?.onOptionClicked("video") 
        }
        view.findViewById<View>(R.id.btn_web).setOnClickListener { 
            listener?.onOptionClicked("web") 
        }
        view.findViewById<View>(R.id.btn_interactions).setOnClickListener { 
            listener?.onOptionClicked("interactions") 
        }
    }
}