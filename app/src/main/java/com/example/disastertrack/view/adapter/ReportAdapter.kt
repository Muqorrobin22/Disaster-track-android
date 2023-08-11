package com.example.disastertrack.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.disastertrack.R
import com.example.disastertrack.model.data.Geometry
import com.example.disastertrack.utils.DisasterType
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class ReportAdapter(
    private val reports: List<Geometry>,
    private val onReportItemClick: (Geometry) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reports_layout, parent, false)
        return ReportViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        val imageUrl = report.properties.image_url
        val titleText = report.properties.title
        val descText = report.properties.text
        val tagDisaster = report.properties.disaster_type

        // Load the image into the ImageView using Picasso
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imageView)
        } else {
            // Handle case where there's no image available or show a default image
            holder.imageView.setImageResource(R.drawable.no_image)
        }

        if (titleText == null || titleText.isEmpty()) {
            holder.titleItem.text = "Judul Tidak Tersedia"
        } else {
            holder.titleItem.text = titleText
        }

        if (descText == null || descText.isEmpty()) {
            holder.descriptionItem.text = "Deskripsi tidak tersedia"
        } else {
            holder.descriptionItem.text = descText
        }

        if (tagDisaster == null || tagDisaster.isEmpty()) {
            holder.tagDisasterType.text = "Unknown disaster"
        } else {
            var disasterTypeInfo = when (tagDisaster) {
                DisasterType.BANJIR.url -> "Banjir"
                DisasterType.GEMPA.url -> "Gempa Bumi"
                DisasterType.KABUT.url -> "Kabut"
                DisasterType.GUNUNGMELETUS.url -> "Gunung Meletus"
                DisasterType.KEBAKARAN.url -> "Kebakaran"
                DisasterType.BERANGIN.url -> "Berangin"
                else -> "Unknown Disaster"
            }
            holder.tagDisasterType.text = disasterTypeInfo
        }

        holder.itemView.setOnClickListener {
            onReportItemClick(report)
        }
    }

    override fun getItemCount(): Int = reports.size

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleItem: TextView = itemView.findViewById(R.id.title_item)
        val descriptionItem: TextView = itemView.findViewById(R.id.desc_item)
        val tagDisasterType: TextView = itemView.findViewById(R.id.tag_disaster)
    }

}