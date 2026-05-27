package com.anggun.pos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.model.ModelKategori

class KategoriAdapter(
    private val kategorilist: List<ModelKategori>
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    lateinit var appContext: Context

    interface OnItemClickListener {
        fun onItemClick(kategori: ModelKategori)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): KategoriViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kategori, parent, false)

        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: KategoriViewHolder,
        position: Int
    ) {

        val kategori = kategorilist[position]
        holder.bind(kategori)
    }

    override fun getItemCount(): Int {
        return kategorilist.size
    }

    inner class KategoriViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvKategori =
            itemView.findViewById<TextView>(R.id.tvKategori)

        private val chipStatusKategori =
            itemView.findViewById<TextView>(R.id.chipStatusKategori)

        fun bind(kategori: ModelKategori) {

            tvKategori.text = kategori.namaKategori
            chipStatusKategori.text = kategori.status

            if (kategori.status == "Aktif") {

                chipStatusKategori.setBackgroundResource(
                    R.drawable.bg_chip_aktif
                )

            } else {

                chipStatusKategori.setBackgroundResource(
                    R.drawable.bg_chip_nonaktif
                )
            }

            itemView.setOnClickListener {
                listener?.onItemClick(kategori)
            }
        }
    }
}