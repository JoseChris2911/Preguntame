package net.azarquiel.firebase2020prueba.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rowcategorias.view.*
import net.azarquiel.firebase2020prueba.model.Categorias


/**
 * Created by pacopulido on 9/10/18.
 */
class CustomAdapterCategorias(val context: Context,
                              val layout: Int
                    ) : RecyclerView.Adapter<CustomAdapterCategorias.ViewHolder>() {

    private var dataList: List<Categorias> = emptyList()

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

    internal fun setCategorias(categorias: List<Categorias>) {
        this.dataList = categorias
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Categorias){
            // itemview es el item de diseño
            // al que hay que poner los datos del objeto dataItem
            itemView.tvCategoria.text = dataItem.nombre
//            itemView.tvlast.text = dataItem.last
//            itemView.tvborn.text = dataItem.born.toString()

            itemView.tag = dataItem

        }

    }
}