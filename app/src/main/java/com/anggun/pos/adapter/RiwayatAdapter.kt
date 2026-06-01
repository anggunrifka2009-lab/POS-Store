package com.anggun.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.model.ModelRiwayat
import java.text.NumberFormat
import java.util.Locale

class RiwayatAdapter(
    private val list: List<ModelRiwayat>,
    private val onClick: (ModelRiwayat) -> Unit
) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvIdTransaksi)
        val tvWaktu: TextView = view.findViewById(R.id.tvWaktu)
        val tvKasir: TextView = view.findViewById(R.id.tvKasir)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalRiwayat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvId.text = "ID: ${item.idTransaksi?.takeLast(6)}"
        holder.tvWaktu.text = "${item.tanggal} | ${item.jam}"
        holder.tvKasir.text = "Kasir: ${item.kasir}"
        holder.tvTotal.text = "Rp ${formatRupiah(item.total ?: 0)}"

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = list.size

    private fun formatRupiah(number: Int): String {
        return NumberFormat
            .getNumberInstance(Locale("id", "ID"))
            .format(number)
    }
}
