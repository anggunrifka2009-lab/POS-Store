package com.anggun.pos.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.model.ModelTransaksi

class TransaksiAdapter(
    private val list: ArrayList<ModelTransaksi>,
    private val listener: OnQtyChange
) : RecyclerView.Adapter<TransaksiAdapter.ViewHolder>() {

    interface OnQtyChange {
        fun onChanged(total: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvNama = view.findViewById<TextView>(R.id.tvNamaProduk)
        val tvHarga = view.findViewById<TextView>(R.id.tvHargaProduk)
        val tvQty = view.findViewById<TextView>(R.id.tvQty)

        val btnPlus = view.findViewById<ImageButton>(R.id.btnPlus)
        val btnMinus = view.findViewById<ImageButton>(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk_transaksi, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val produk = list[position]

        holder.tvNama.text = produk.namaProduk
        holder.tvHarga.text = "Rp. ${produk.hargaProduk}"
        holder.tvQty.text = produk.qty.toString()

        holder.btnPlus.setOnClickListener {

            if (produk.tanpaBatas == true || (produk.stokProduk ?: 0) > produk.qty) {
                produk.qty++

                holder.tvQty.text = produk.qty.toString()

                listener.onChanged(0) // Nilai 0 tidak masalah karena Activity hitung ulang dari listProdukFull
            } else {
                android.widget.Toast.makeText(holder.itemView.context, "Stok tidak mencukupi", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        holder.btnMinus.setOnClickListener {

            if (produk.qty > 0) {

                produk.qty--

                holder.tvQty.text = produk.qty.toString()

                listener.onChanged(0)
            }
        }
    }
}