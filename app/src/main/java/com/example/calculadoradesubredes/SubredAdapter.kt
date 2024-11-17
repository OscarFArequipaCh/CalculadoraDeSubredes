package com.example.calculadoradesubredes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubredAdapter(private val subredes: List<Subred>) :
    RecyclerView.Adapter<SubredAdapter.SubredViewHolder>() {

    // ViewHolder interno para manejar cada ítem
    class SubredViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val IDTextView: TextView = itemView.findViewById(R.id.idTextView)
        val direccionRedTextView: TextView = itemView.findViewById(R.id.textViewDireccionRed)
        val rangoTextView: TextView = itemView.findViewById(R.id.textViewRango)
        val direccionBroadcastTextView: TextView = itemView.findViewById(R.id.textViewDireccionBroadcast)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubredViewHolder {
        // Infla el layout para cada ítem
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_subredes, parent, false)
        return SubredViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubredViewHolder, position: Int) {
        // Obtén la subred en la posición actual
        val subred = subredes[position]

        // Vincula los datos al ViewHolder
        holder.IDTextView.text = subred.id.toString()
        holder.direccionRedTextView.text = subred.direccionRed
        holder.rangoTextView.text = subred.rango
        holder.direccionBroadcastTextView.text = subred.direccionBroadcast
    }

    override fun getItemCount(): Int {
        // Devuelve el número de elementos en la lista
        return subredes.size
    }
}