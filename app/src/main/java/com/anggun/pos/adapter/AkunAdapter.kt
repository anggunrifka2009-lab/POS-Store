package com.anggun.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.model.ModelAkun

class AkunAdapter(
    private val listAkun: ArrayList<ModelAkun>
) : RecyclerView.Adapter<AkunAdapter.ViewHolder>() {

    class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        val tvNama: TextView =
            itemView.findViewById(
                R.id.tvNamaAkun
            )

        val tvEmail: TextView =
            itemView.findViewById(
                R.id.tvEmailAkun
            )

        val tvRole: TextView =
            itemView.findViewById(
                R.id.tvRoleAkun
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_akun,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listAkun.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val akun =
            listAkun[position]

        holder.tvNama.text =
            akun.nama

        holder.tvEmail.text =
            akun.email

        holder.tvRole.text =
            akun.role
    }
}