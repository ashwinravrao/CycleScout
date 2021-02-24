package com.ashwinrao.cyclescout.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.ashwinrao.cyclescout.R
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.databinding.ShimmerRowBinding
import com.ashwinrao.cyclescout.databinding.ViewHolderNearbyShopBinding
import com.ashwinrao.cyclescout.databinding.ViewHolderShimmerBinding
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import java.lang.Exception
import java.lang.NullPointerException

class NearbyShopAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val tag = this.javaClass.simpleName

    private val dataViewType = 0
    private val loadingViewType = 1

    var fetchNextPage = MutableLiveData<Boolean>()
    var data: MutableList<NearbySearch.Result> = mutableListOf()
    set(shops) {
        if (data.isEmpty()) {
            field = shops
        } else {
            field.removeLast()
            field.addAll(shops)
            field.add(field.last())
        }
    }

    fun watchData() = MutableLiveData(data)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            dataViewType -> NearbyShopViewHolder(
                ViewHolderNearbyShopBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> LoadingViewHolder(
                ViewHolderShimmerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == dataViewType) {
            val binding = ViewHolderNearbyShopBinding.bind(holder.itemView)
            val context = binding.root.context
            binding.shopName.text = data[position].name

            // Handle potential null values
            try {
                if (data[position].openingHours.openNow) {
                    binding.openNow.text = context.getString(R.string.open_now)
                    binding.openNow.setTextColor(
                        context.resources.getColor(
                            R.color.green,
                            context.theme
                        )
                    )
                } else {
                    binding.openNow.text = context.getString(R.string.closed_now)
                    binding.openNow.setTextColor(context.resources.getColor(R.color.red, context.theme))
                }

                Glide
                    .with(context)
                    .load(buildUrl(context, data[position]))
                    .thumbnail(0.1f)
                    .centerCrop()
                    .into(binding.shopPhoto)

            } catch (e: Exception) {
                Log.e(tag, "onBindViewHolder: Exception thrown @ position: $position; message: ${e.message}")
            }
        } else {
            fetchNextPage.value = true
            val binding = ViewHolderShimmerBinding.bind(holder.itemView)
            binding.shimmerFrameLayout.startShimmer()
        }
        Log.d(tag, "onBindViewHolder: ${getItemViewType(position)}")
    }

    private fun buildUrl(context: Context, result: NearbySearch.Result) =
        String.format(context.getString(R.string.places_photo_url), result.photos[0].photoReference, context.getString(R.string.places_key))

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            data.lastIndex -> loadingViewType
            else -> dataViewType
        }
    }

    override fun getItemCount(): Int = data.size

    inner class NearbyShopViewHolder(binding: ViewHolderNearbyShopBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(binding: ViewHolderShimmerBinding) :
        RecyclerView.ViewHolder(binding.root)
}