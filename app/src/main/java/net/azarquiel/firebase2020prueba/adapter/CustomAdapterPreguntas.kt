package net.azarquiel.firebase2020prueba.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowcategorias.view.*
import kotlinx.android.synthetic.main.rowpreguntas.view.*
import net.azarquiel.firebase2020prueba.model.Categorias
import net.azarquiel.firebase2020prueba.model.Preguntas


/**
 * Created by pacopulido on 9/10/18.
 */
class CustomAdapterPreguntas(val context: Context,
                             val layout: Int
                    ) : RecyclerView.Adapter<CustomAdapterPreguntas.ViewHolder>() {

    private var dataList: List<Preguntas> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setPreguntas(preguntas: List<Preguntas>) {
        this.dataList = preguntas
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Preguntas){
            // itemview es el item de diseño
            // al que hay que poner los datos del objeto dataItem
            itemView.tvPregunta.text = dataItem.titulo
            itemView.tvOwnerPregunta.text = dataItem.owner
//            itemView.tvlast.text = dataItem.last
//            itemView.tvborn.text = dataItem.born.toString()

            itemView.tag = dataItem

        }

    }
}