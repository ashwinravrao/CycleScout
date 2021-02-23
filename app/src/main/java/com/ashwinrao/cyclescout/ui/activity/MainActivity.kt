package com.ashwinrao.cyclescout.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ashwinrao.cyclescout.R
import com.ashwinrao.cyclescout.animateDropPin
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.databinding.ActivityMainBinding
import com.ashwinrao.cyclescout.databinding.LayoutPlaceholderBinding
import com.ashwinrao.cyclescout.ui.adapter.NearbyShopAdapter
import com.ashwinrao.cyclescout.viewmodel.MainViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener {

    private val TAG = this@MainActivity.javaClass.simpleName

    private lateinit var binding: ActivityMainBinding
    private lateinit var placeholder: LayoutPlaceholderBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var shopList: RecyclerView
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var startLocation: LatLng

    private lateinit var adapter: NearbyShopAdapter
    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        placeholder = binding.placeholder
        shimmer = binding.shimmer
        binding.swipeRefreshLayout.setOnRefreshListener(this@MainActivity)

        initializeShopList()
        initializeMap()
    }

    private fun initializeMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initializeShopList() {
        startShimmer()
        shopList = binding.recyclerView
        shopList.setHasFixedSize(true)
        shopList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = NearbyShopAdapter()
        shopList.adapter = adapter

        // swipe to refresh animation will be dismissed when the adapter data set has changed (covers valid and error cases)
        adapter.watchData().observe(this@MainActivity) { binding.swipeRefreshLayout.isRefreshing = false }
    }

    private fun populateShopList(shops: List<NearbySearch.Result>) {
        stopShimmer()
        showShopList()
        adapter.data = shops
        adapter.notifyDataSetChanged()
    }

    override fun onMapReady(map: GoogleMap?) {
        // Hardcoding in the interest of time; normally would get user input or device location
        startLocation = LatLng(41.88744282963304, -87.65274711534346)   // SRAM HQ in downtown Chicago
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12f))
        map?.uiSettings?.isTiltGesturesEnabled = false
        map?.uiSettings?.isZoomGesturesEnabled = false
        map?.uiSettings?.isScrollGesturesEnabled = false
        map?.uiSettings?.isMapToolbarEnabled = false
        val marker = map?.addMarker(MarkerOptions().position(startLocation))
        if (marker != null) animateDropPin(marker, 500)
        fetchNearbyShops(startLocation)
    }

    /**
     * Asynchronously fetch the first set of results matching our keyword from the Google Places API.
     * Once we have a response, we must switch contexts to the Main thread in order to perform UI work
     * such as populating our RecyclerView and dropping pins (adding markers) to the map.
     */

    private fun fetchNearbyShops(locationBias: LatLng) {
        var message: String? = null
        CoroutineScope(Dispatchers.IO).launch {
            var nearbyShops: NearbySearch? = null
            try {
                nearbyShops = mainViewModel.fetchNearbyShops(locationBias)
            } catch (e: Exception) {
                message = e.message
                Log.d(TAG, "fetchNearbyShops: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                if (nearbyShops != null) {
                    when (nearbyShops.status) {
                        "OK" -> populateShopList(nearbyShops.results)
                        "ZERO_RESULTS" -> showPlaceholder(errorStatus = false)
                        else -> showPlaceholder(errorStatus = true)
                    }
                    Log.d(TAG, "fetchNearbyShops: ${nearbyShops.status}")
                } else {
                    showPlaceholder(errorStatus = true, message)
                }
            }
        }
    }

    private fun showPlaceholder(errorStatus: Boolean, message: String? = null) {
        if (errorStatus) {
            placeholder.icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    this.resources,
                    R.drawable.ic_error,
                    this.theme
                )
            )
            placeholder.icon.scaleX = 0.8f
            placeholder.icon.scaleY = 0.8f
            placeholder.label.text = message ?: this.resources.getString(R.string.label_placeholder_error)
        } else {
            placeholder.icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    this.resources,
                    R.drawable.ic_sad_phone,
                    this.theme
                )
            )
            placeholder.icon.scaleX = 1f
            placeholder.icon.scaleY = 1f
            placeholder.label.text = this.resources.getString(R.string.label_placeholder_no_results)
        }
        stopShimmer()
        hideShopList()
    }

    private fun hideMap() {
        binding.mapContainer.visibility = View.GONE
    }

    private fun showMap() {
        binding.mapContainer.visibility = View.VISIBLE
    }

    private fun hideShopList() {
        hideMap()
        adapter.data = mutableListOf()
        adapter.notifyDataSetChanged()
        placeholder.root.visibility = View.VISIBLE
    }

    private fun showShopList() {
        showMap()
        shopList.visibility = View.VISIBLE
        placeholder.root.visibility = View.GONE
    }

    private fun startShimmer() {
        shimmer.visibility = View.VISIBLE
        shimmer.startShimmer()
    }

    private fun stopShimmer() {
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
    }

    override fun onRefresh() {
        initializeShopList()
        fetchNearbyShops(startLocation)
    }
}