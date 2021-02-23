package com.ashwinrao.cyclescout.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ashwinrao.cyclescout.R
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.databinding.ViewHolderNearbyShopBinding

class NearbyShopAdapter : RecyclerView.Adapter<NearbyShopAdapter.NearbyShopViewHolder>() {

    var data: List<NearbySearch.Result> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyShopViewHolder =
        NearbyShopViewHolder(ViewHolderNearbyShopBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: NearbyShopViewHolder, position: Int) {
        val binding = ViewHolderNearbyShopBinding.bind(holder.itemView)
        val context = binding.root.context
        binding.shopName.text = data[position].name
        if (data[position].openingHours.openNow) {
            binding.openNow.text = context.getString(R.string.open_now)
            binding.openNow.setTextColor(context.resources.getColor(R.color.green, context.theme))
        } else {
            binding.openNow.text = context.getString(R.string.closed_now)
            binding.openNow.setTextColor(context.resources.getColor(R.color.red, context.theme))
        }
        // todo: set image thumbnail using Glide
    }

    override fun getItemCount(): Int = data.size

    inner class NearbyShopViewHolder(binding: ViewHolderNearbyShopBinding) : RecyclerView.ViewHolder(binding.root)
}