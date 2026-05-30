package com.anggun.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.model.ModelPegawai
import com.bumptech.glide.Glide

class PegawaiAdapter(
    private val listPegawai: List<ModelPegawai>
) : RecyclerView.Adapter<PegawaiAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(pegawai: ModelPegawai)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPegawai)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatPegawai)
        val tvRole: TextView = itemView.findViewById(R.id.tvRolePegawai)
        val ivFoto: ImageView = itemView.findViewById(R.id.ivFotoPegawai)

        fun bind(pegawai: ModelPegawai) {
            tvNama.text = pegawai.nama
            tvAlamat.text = pegawai.alamat
            tvRole.text = pegawai.role
            
            Glide.with(itemView.context)
                .load(pegawai.foto)
                .placeholder(R.drawable.img_2)
                .error(R.drawable.img_2)
                .into(ivFoto)

            itemView.setOnClickListener {
                listener?.onItemClick(pegawai)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listPegawai.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listPegawai[position])
    }
}