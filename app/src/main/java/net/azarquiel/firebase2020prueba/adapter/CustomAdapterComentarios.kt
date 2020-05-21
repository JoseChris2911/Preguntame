package net.azarquiel.firebase2020prueba.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toIcon
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.rowcomentarios.view.*
import net.azarquiel.firebase2020prueba.R
import net.azarquiel.firebase2020prueba.model.Respuestas


/**
 * Created by pacopulido on 9/10/18.
 */
class CustomAdapterComentarios(val context: Context,
                               val layout: Int,
                               val storageReference: StorageReference
                    ) : RecyclerView.Adapter<CustomAdapterComentarios.ViewHolder>() {

    private var dataList: List<Respuestas> = emptyList()
    //private lateinit var

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context, storageReference)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setRespuestas(
        respuestas: List<Respuestas>
    ) {

        this.dataList = respuestas
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context, val mStorageReference: StorageReference) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Respuestas){
            // itemview es el item de diseño
            //var userRef = storage.child("images/island.jpg")
            // al que hay que poner los datos del objeto dataItem
            var photoRef = mStorageReference.child("images/${dataItem.idowner}.jpg")
            val ONE_MEGABYTE: Long = 1024 * 1024
            if(photoRef != null){
                photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                    // Data for "images/island.jpg" is returned, use this as needed
                    val icon: Icon = it.toIcon()
                    //solo esta disponible en api 23 pa arriba
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        itemView.civAvatarCajon.setImageIcon(icon)
                    }
                }.addOnFailureListener {
                    // Handle any errors
                }
            }else{
                itemView.civAvatarCajon.setImageResource(R.drawable.noimage)
            }

            itemView.tvNombreComment.text = dataItem.owner
            itemView.tvContentComment.text = dataItem.content


            itemView.tag = dataItem

        }

    }
}