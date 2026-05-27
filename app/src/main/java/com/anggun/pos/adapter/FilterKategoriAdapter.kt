package com.anggun.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.google.android.material.button.MaterialButton

class FilterKategoriAdapter(
    private val listKategori: List<String>
) : RecyclerView.Adapter<FilterKategoriAdapter.ViewHolder>() {

    interface OnItemClickListener {

        fun onItemClick(kategori: String)
    }

    private var listener:
            OnItemClickListener? = null

    fun setOnItemClickListener(
        listener: OnItemClickListener
    ) {

        this.listener = listener
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val btnKategori =
            itemView.findViewById<MaterialButton>(
                R.id.btnKategori
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_filter_kategori,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return listKategori.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val kategori =
            listKategori[position]

        holder.btnKategori.text =
            kategori

        holder.btnKategori.setOnClickListener {

            listener?.onItemClick(kategori)
        }
    }
}