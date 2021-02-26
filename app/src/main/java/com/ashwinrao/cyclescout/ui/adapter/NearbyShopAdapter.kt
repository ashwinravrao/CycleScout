package com.ashwinrao.cyclescout.ui.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.ashwinrao.cyclescout.R
import com.ashwinrao.cyclescout.RESULT_EXTRA
import com.ashwinrao.cyclescout.buildImageUrl
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.databinding.ViewHolderNearbyShopBinding
import com.ashwinrao.cyclescout.databinding.ViewHolderShimmerBinding
import com.ashwinrao.cyclescout.ui.activity.DetailActivity
import com.ashwinrao.cyclescout.ui.activity.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority

class NearbyShopAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val tag = this.javaClass.simpleName

    private val dataViewType = 0
    private val loadingViewType = 1

    private val shopPhotoMaxWidth = 100 //px

    var fetchNextPage = MutableLiveData<Boolean>()

    /**
     * The below setter logic ensures we show a loading animation at the end of our data set.
     * When we bind the view holder that contains this animation, fetchNextPage is updated.
     * The activity owner observes this value to know when to request the next set of results.
     * If no additional results are available, we access the loading view holder from the activity
     * and hide the animation. There is additional logic in the view model to ensure additional
     * requests are not sent after we've reached the end of the available results.
     */
    var data: MutableList<NearbySearch.Result> = mutableListOf()
        set(shops) {
            if (data.isEmpty()) {
                field = shops
            } else {
                field.removeLast()
                field.addAll(shops)
            }
            field.add(shops.last())
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

            try {
                when (data[position].openingHours.openNow) {
                    true -> {
                        binding.openNow.text = context.getString(R.string.open_now)
                        binding.openNow.setTextColor(ContextCompat.getColor(context, R.color.green))
                    }
                    false -> {
                        binding.openNow.text = context.getString(R.string.closed_now)
                        binding.openNow.setTextColor(ContextCompat.getColor(context, R.color.red))
                    }
                }

                loadThumbnail(context, position, binding.shopPhoto)

            } catch (e: Exception) {
                Log.e(
                    tag,
                    "onBindViewHolder: Exception thrown @ position: $position; message: ${e.message}"
                )
            }

        } else {
            fetchNextPage.value = true
            val binding = ViewHolderShimmerBinding.bind(holder.itemView)
            binding.shimmerFrameLayout.startShimmer()
        }
        Log.d(tag, "onBindViewHolder: ${getItemViewType(position)}")
    }

//    private fun buildUrl(context: Context, result: NearbySearch.Result) =
//        String.format(
//            context.getString(R.string.places_photo_url),
//            shopPhotoMaxWidth,
//            result.photos[0].photoReference,
//            context.getString(R.string.places_key)
//        )

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            data.lastIndex -> loadingViewType
            else -> dataViewType
        }
    }

    private fun loadThumbnail(context: Context, position: Int, imageView: ImageView) {
        Glide
            .with(context)
            .load(buildImageUrl(context, data[position]))
            .thumbnail(0.1f)
            .priority(Priority.HIGH)
            .centerCrop()
            .into(imageView)
    }

    override fun getItemCount(): Int = data.size

    inner class NearbyShopViewHolder(private val binding: ViewHolderNearbyShopBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this@NearbyShopViewHolder)
        }

        override fun onClick(v: View?) {
            val intent = Intent(binding.root.context, DetailActivity::class.java)
            intent.putExtra(RESULT_EXTRA, data[adapterPosition])
            (binding.root.context as MainActivity).startActivity(intent)
        }
    }

    inner class LoadingViewHolder(val binding: ViewHolderShimmerBinding) :
        RecyclerView.ViewHolder(binding.root)
}