package com.anggun.pos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.model.ModelProduk
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class ProdukAdapter(
    private val produkList: List<ModelProduk>
) : RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    lateinit var appContext: Context

    interface OnItemClickListener {
        fun onItemClick(produk: ModelProduk)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProdukViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)

        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProdukViewHolder,
        position: Int
    ) {

        val produk = produkList[position]
        holder.bind(produk)
    }

    override fun getItemCount(): Int {
        return produkList.size
    }

    inner class ProdukViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvNama =
            itemView.findViewById<TextView>(R.id.tvNama)

        private val tvHarga =
            itemView.findViewById<TextView>(R.id.tvHarga)

        private val tvKategori =
            itemView.findViewById<TextView>(R.id.tvKategori)

        private val tvStok =
            itemView.findViewById<TextView>(R.id.tvStok)

        private val tvCabang =
            itemView.findViewById<TextView>(R.id.tvCabang)

        private val chipStatus =
            itemView.findViewById<Chip>(R.id.chipStatusKategori)

        private val ivProduk =
            itemView.findViewById<ImageView>(R.id.imgProduk)

        fun bind(produk: ModelProduk) {

            tvNama.text = produk.namaProduk

            val rupiah =
                NumberFormat.getCurrencyInstance(
                    Locale("id", "ID")
                )

            tvHarga.text =
                rupiah.format(produk.hargaProduk)

            tvKategori.text = produk.idKategori

            if (produk.tanpaBatas == true) {

                tvStok.text = "Tak Terbatas"

            } else {

                tvStok.text =
                    produk.stokProduk.toString()
            }

            tvCabang.text = produk.idCabang ?: "Utama"

            chipStatus.text = produk.statusProduk
            
            Glide.with(itemView.context)
                .load(produk.fotoProduk)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivProduk)

            itemView.setOnClickListener {
                listener?.onItemClick(produk)
            }
        }
    }
}