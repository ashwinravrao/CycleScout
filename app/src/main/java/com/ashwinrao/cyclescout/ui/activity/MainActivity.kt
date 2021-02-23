package com.ashwinrao.cyclescout.ui.activity

import android.graphics.drawable.VectorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.facebook.shimmer.ShimmerFrameLayout
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
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var shopList: RecyclerView
    private lateinit var shimmerEffect: ShimmerFrameLayout
    private lateinit var adapter: NearbyShopAdapter

    private var snackBar: Snackbar? = null
    private val mainViewModel: MainViewModel by viewModel()

    // Hardcoding in the interest of time; normally would get user input or device location
    private val startLocation = LatLng(41.88744282963304, -87.65274711534346)   // SRAM HQ in downtown Chicago

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        shimmerEffect = binding.shimmer
        binding.swipeRefreshLayout.setOnRefreshListener(this@MainActivity)

        initializeShopList()
        initializeMap()
    }

    private fun initializeMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
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
     * In order to lazily load the next set of results, we need to listen to shopList's scroll position.
     * Once we reach the "bottom" of the recycler view, we can fetch the next set of results from the API.
     * In keeping with separation of concerns, the view model keeps track of page tokens. So the first call
     * to fetchNearbyShops() will either return the first set of results or the next page. The activity shouldn't
     * have to know which.
     */
    private fun fetchDataLazily() {
        fetchNearbyShops(startLocation)
        shopList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

        })
    }

    private fun populateShopList(shops: List<NearbySearch.Result>) {
        stopLoadingAnimation()
        shopList.visibility = View.VISIBLE
        adapter.data = shops
        adapter.notifyDataSetChanged()
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 14f))
        map?.uiSettings?.isTiltGesturesEnabled = false
        map?.uiSettings?.isZoomGesturesEnabled = false
        map?.uiSettings?.isScrollGesturesEnabled = false
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
            ).title(this.getString(R.string.label_location)).anchor(0.5f, 0.5f)
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
    private fun fetchNearbyShops(locationBias: LatLng): Boolean {
        var success = false
        var message: String? = null
        CoroutineScope(Dispatchers.IO).launch {
            var nearbyShops: NearbySearch? = null
            try {
                nearbyShops = mainViewModel.fetchNearbyShops(locationBias)
            } catch (e: Exception) {
                message = e.message
                Log.d(tag, "fetchNearbyShops: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                if (nearbyShops != null) {
                    when (nearbyShops.status) {
                        "OK" -> {
                            success = true
                            populateShopList(nearbyShops.results)
                        }
                        "ZERO_RESULTS" -> handleNoResults(message = "No matching results found")
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
        shimmerEffect.visibility = View.VISIBLE
        shimmerEffect.startShimmer()
    }

    private fun stopLoadingAnimation(hideLayout: Boolean = true) {
        shimmerEffect.stopShimmer()
        if (hideLayout) shimmerEffect.visibility = View.GONE
    }

    override fun onRefresh() {
        fetchNearbyShops(startLocation)
        snackBar?.dismiss()

        // swipe to refresh will be dismissed when fresh data is bound to the recyclerview adapter
        adapter.watchData().observe(this@MainActivity) {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}