package com.group.redesmascotas


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.group.redesmascotas.databinding.FragmentMenuBinding
import java.lang.ClassCastException

// La clase hereda de Fragment y usa View Binding
class MenuFragment : Fragment() {

    // View Binding - Variables para manejar el binding
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    // Define una interfaz para comunicarse con la Activity. Es un contrato.
    // La Activity que contenga este fragmento DEBE implementar esta interfaz.
    interface OnOptionClickListener {
        fun onOptionClicked(option: String)
    }

    private var listener: OnOptionClickListener? = null

    // Infla la vista usando View Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
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

        // Se asigna un listener a cada botón usando View Binding
        binding.btnProfile.setOnClickListener { listener?.onOptionClicked("profile") }
        binding.btnPhotos.setOnClickListener { listener?.onOptionClicked("photos") }
        binding.btnVideo.setOnClickListener { listener?.onOptionClicked("video") }
        binding.btnWeb.setOnClickListener { listener?.onOptionClicked("web") }
    }

    // Importante: Limpiar el binding cuando la vista se destruya para evitar memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}