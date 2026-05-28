package com.anggun.pos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.model.ModelCabang
import com.google.android.material.chip.Chip

class CabangAdapter(
    private val cabangList: List<ModelCabang>
) : RecyclerView.Adapter<CabangAdapter.CabangViewHolder>() {

    lateinit var appContext: Context

    interface OnItemClickListener {
        fun onItemClick(cabang: ModelCabang)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CabangViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cabang, parent, false)
        return CabangViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CabangViewHolder,
        position: Int
    ) {
        val cabang = cabangList[position]
        holder.bind(cabang)
    }

    override fun getItemCount(): Int {
        return cabangList.size
    }

    inner class CabangViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvNamaCabang =
            itemView.findViewById<TextView>(R.id.tvNamaCabang)

        private val tvAlamatCabang =
            itemView.findViewById<TextView>(R.id.tvAlamatCabang)

        private val chipStatusCabang =
            itemView.findViewById<Chip>(R.id.chipStatusCabang)

        fun bind(cabang: ModelCabang) {
            tvNamaCabang.text = cabang.namaCabang
            tvAlamatCabang.text = cabang.alamatCabang
            chipStatusCabang.text = cabang.status

            itemView.setOnClickListener {
                listener?.onItemClick(cabang)
            }
        }
    }
}