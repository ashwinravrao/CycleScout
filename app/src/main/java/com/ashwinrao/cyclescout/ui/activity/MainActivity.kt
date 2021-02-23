package com.ashwinrao.cyclescout.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = this@MainActivity.javaClass.simpleName

    private lateinit var binding: ActivityMainBinding
    private lateinit var placeholder: LayoutPlaceholderBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var shopList: RecyclerView

    private lateinit var adapter: NearbyShopAdapter
    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        placeholder = binding.placeholder

        initializeMap()
        initializeShopList()
    }

    private fun initializeMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initializeShopList() {
        binding.shimmer.visibility = View.VISIBLE
        binding.shimmer.startShimmer()
        shopList = binding.recyclerView
        shopList.setHasFixedSize(true)
        shopList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = NearbyShopAdapter()
        shopList.adapter = adapter
    }

    private fun populateShopList(shops: List<NearbySearch.Result>) {
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
        placeholder.root.visibility = View.GONE
        shopList.visibility = View.VISIBLE
        adapter.data = shops
        adapter.notifyDataSetChanged()
    }

    override fun onMapReady(map: GoogleMap?) {
        // Hardcoding in the interest of time; normally would get user input or device location
        val startLocation =
            LatLng(41.88744282963304, -87.65274711534346)   // SRAM HQ in downtown Chicago

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12f))
        map?.uiSettings?.isTiltGesturesEnabled = false
        map?.uiSettings?.isZoomGesturesEnabled = false
        map?.uiSettings?.isScrollGesturesEnabled = false
        map?.uiSettings?.isMapToolbarEnabled = false
        val marker = map?.addMarker(MarkerOptions().position(startLocation))
        if (marker != null) animateDropPin(marker, 500)
        fetchNearbyShops(map, startLocation)
    }

    /**
     * Asynchronously fetch the first set of results matching our keyword from the Google Places API.
     * Once we have a response, we must switch contexts to the Main thread in order to perform UI work
     * such as populating our RecyclerView and dropping pins (adding markers) to the map.
     */

    private fun fetchNearbyShops(map: GoogleMap?, locationBias: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            val nearbyShops = mainViewModel.fetchNearbyShops(locationBias)
            withContext(Dispatchers.Main) {
                if (nearbyShops != null) {
                    when (nearbyShops.status) {
                        "OK" -> populateShopList(nearbyShops.results)
                        "ZERO_RESULTS" -> showNoResultsPlaceholder()
                        else -> showNoResultsPlaceholder(true)
                    }
                }
            }
        }
    }

    private fun showNoResultsPlaceholder(errorStatus: Boolean = false) {
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
            placeholder.label.text = this.resources.getString(R.string.label_placeholder_error)
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
        shopList.visibility = View.GONE
        placeholder.root.visibility = View.VISIBLE
    }
}