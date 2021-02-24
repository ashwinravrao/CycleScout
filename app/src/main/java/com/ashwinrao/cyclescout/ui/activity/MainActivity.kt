package com.ashwinrao.cyclescout.ui.activity

import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ashwinrao.cyclescout.R
import com.ashwinrao.cyclescout.animateDropPin
import com.ashwinrao.cyclescout.bitmapFromDrawable
import com.ashwinrao.cyclescout.data.remote.response.NearbySearch
import com.ashwinrao.cyclescout.databinding.ActivityMainBinding
import com.ashwinrao.cyclescout.ui.adapter.NearbyShopAdapter
import com.ashwinrao.cyclescout.viewmodel.MainViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener {

    private val tag = this@MainActivity.javaClass.simpleName

    private lateinit var binding: ActivityMainBinding
    private lateinit var shopList: RecyclerView
    private lateinit var adapter: NearbyShopAdapter

    private var snackBar: Snackbar? = null
    private val mainViewModel: MainViewModel by viewModel()

    /**
     * Start location that serves as the bias for Google Places API calls.
     * The value has been hardcoded in the interest of time. Normally I would want to either
     * get user input or access the device location.
     */
    private val startLocation = LatLng(41.88744282963304, -87.65274711534346)   // SRAM HQ in downtown Chicago

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.swipeRefreshLayout.setOnRefreshListener(this@MainActivity)
        setContentView(binding.root)

        initializeMap()
        initializeShopList()
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initializeShopList() {
        startLoadingAnimation()
        shopList = binding.recyclerView
        shopList.setHasFixedSize(true)
        shopList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = NearbyShopAdapter()
        shopList.adapter = adapter
        fetchDataLazily()
    }

    /**
     * In order to lazily fetch the next set of results, we observe a Boolean in the recyclerview adapter which indicates
     * when we need to make another request. The token is handled by the view model, in keeping with separation of concerns.
     * And the trigger for the observable to flip to `true` is if we scroll to the bottom of our existing result set and
     * show the loading indicator (it's a different view holder instantiated based on item type).
     */
    private fun fetchDataLazily() {
        fetchNearbyShops(startLocation)
        adapter.fetchNextPage.observe(this) {
            if (it) fetchNearbyShops(startLocation, nextPage = true)
        }
    }

    /**
     * Bind data to our recycler view adapter. If we are not appending new results from a recent API call,
     * we can assume we are refreshing the page. If so, we need to make sure to clear out the stale data
     * in the adapter so we can bind some fresh ones.
     */
    private fun populateShopList(shops: List<NearbySearch.Result>, appendingResults: Boolean = false) {
        stopLoadingAnimation()
        shopList.visibility = View.VISIBLE
        if (!appendingResults && adapter.data.isNotEmpty()) adapter.data.clear() // clear stale data on refresh
        adapter.data.addAll(shops)
        adapter.notifyDataSetChanged()
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12f))
        map?.uiSettings?.isTiltGesturesEnabled = false
        map?.uiSettings?.isMapToolbarEnabled = false
        val marker = map?.addMarker(
            MarkerOptions().position(startLocation).icon(
                BitmapDescriptorFactory.fromBitmap(
                    bitmapFromDrawable(
                        ResourcesCompat.getDrawable(
                            this.resources,
                            R.drawable.ic_bicycle,
                            this.theme
                        ) as VectorDrawable
                    )
                )
            )
        )
        if (marker != null) animateDropPin(marker, 500)
    }

    /**
     * Asynchronously fetch the first set of results matching our keyword from the Google Places API.
     * Once we have a response, we must switch contexts to the Main thread in order to perform UI work
     * such as populating our RecyclerView and dropping pins (adding markers) to the map.
     *
     * @return success: Boolean (whether the outcome was as expected - fresh data fetched)
     */
    private fun fetchNearbyShops(locationBias: LatLng, refresh: Boolean = false, nextPage: Boolean = false): Boolean {
        var success = false
        var message: String? = null
        CoroutineScope(Dispatchers.IO).launch {
            var nearbyShops: NearbySearch? = null
            try {
                nearbyShops = mainViewModel.fetchNearbyShops(locationBias, refresh)
            } catch (e: Exception) {
                message = e.message
                Log.d(tag, "fetchNearbyShops: ${e.printStackTrace()}")
            }
            withContext(Dispatchers.Main) {
                if (nearbyShops != null) {
                    when (nearbyShops.status) {
                        "OK" -> {
                            success = true
                            populateShopList(nearbyShops.results, nextPage)
                        }
                        "ZERO_RESULTS" -> handleNoResults(message = "No matching results found")
                        "INVALID_REQUEST" -> Log.e(tag, "fetchNearbyShops: INVALID_REQUEST")
                        else -> handleNoResults(message = "Something unexpected happened. Try again later")
                    }
                    Log.d(tag, "fetchNearbyShops: ${nearbyShops.status}")
                }
                message?.let { handleNoResults(it) }
            }
        }
        return success
    }

    private fun handleNoResults(message: String) {
        stopLoadingAnimation(hideLayout = false)
        snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
        snackBar?.show()
    }

    private fun startLoadingAnimation() {
        binding.shimmer.visibility = View.VISIBLE
        binding.shimmer.startShimmer()
    }

    private fun stopLoadingAnimation(hideLayout: Boolean = true) {
        binding.shimmer.stopShimmer()
        if (hideLayout) binding.shimmer.visibility = View.GONE
    }

    override fun onRefresh() {
        fetchNearbyShops(startLocation, refresh = true)
        snackBar?.dismiss()

        // swipe to refresh animation will be dismissed when fresh data is bound to the recyclerview adapter
        adapter.watchData().observe(this@MainActivity) {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}