package com.ashwinrao.cyclescout.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ashwinrao.cyclescout.*
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.data.remote.response.ShopAddress
import com.ashwinrao.cyclescout.databinding.ActivityDetailBinding
import com.ashwinrao.cyclescout.viewmodel.DetailViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : AppCompatActivity() {

    private val tag = this@DetailActivity.javaClass.simpleName

    private lateinit var binding: ActivityDetailBinding

    private var result: NearbySearch.Result? = null
    private val detailViewModel: DetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        result = intent?.extras?.get(RESULT_EXTRA) as NearbySearch.Result

        loadShopPhoto()
        populateTextViews()
    }

    private fun loadShopPhoto() {
        Glide
            .with(this@DetailActivity)
            .load(result?.let {
                buildImageUrl(
                    this@DetailActivity,
                    it,
                    windowManager.defaultDisplay.width
                )
            })
            .priority(Priority.HIGH)
            .centerCrop()
            .into(binding.shopPhoto)
    }

    private fun populateTextViews() {
        binding.shopName.text = result?.name
        val openStatus = binding.openStatus
        when (result?.openingHours?.openNow) {
            true -> {
                openStatus.text = getString(R.string.open_now)
                openStatus.setTextColor(getColor(R.color.green))
            }
            false -> {
                openStatus.text = getString(R.string.closed_now)
                openStatus.setTextColor(getColor(R.color.red))
            }
        }
        val proximity = "${
            result?.geometry?.location?.lat?.let {
                result?.geometry?.location?.lng?.let { it1 ->
                    detailViewModel.calculateProximity(
                        START_LATITUDE,
                        START_LONGITUDE,
                        it, it1
                    )
                }
            }
        } miles away"
        binding.proximity.text = proximity
        fetchShopAddress()
    }

    // Asynchronously fetches and binds the address for the current shop to the corresponding TextView
    private fun fetchShopAddress() {
        var address: ShopAddress? = null
        CoroutineScope(Dispatchers.IO).launch {
            address = result?.placeId?.let { detailViewModel.fetchShopAddress(it) }
            withContext(Dispatchers.Main) {
                if (address != null) {
                    binding.address.text = address?.result?.addressComponents?.let {
                        detailViewModel.buildAddressString(
                            it
                        )
                    }
                }
            }
        }
    }

}