package com.ashwinrao.cyclescout.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ashwinrao.cyclescout.R
import com.ashwinrao.cyclescout.data.remote.Response
import com.ashwinrao.cyclescout.databinding.ActivityMainBinding
import com.ashwinrao.cyclescout.ui.adapter.NearbyShopAdapter
import com.ashwinrao.cyclescout.viewmodel.MainViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val tag = this@MainActivity.javaClass.simpleName

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var mapFragment: SupportMapFragment
    private val mainViewModel: MainViewModel by viewModel()

    private lateinit var nearbyShopList: RecyclerView
    private lateinit var adapter: NearbyShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        initializeMap()
        initializeBottomSheet()
    }

    private fun initializeMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initializeBottomSheet() {
        // TODO: Set bottom sheet params, instantiate RV + Adapter, start loading animation
    }

    private fun populateShopList(nearby: List<Response.Result>) {
        // TODO: Clear loading animation, set data on adapter
    }

    private fun dropPins(map: GoogleMap?, nearby: List<Response.Result>) {
        for (result in nearby) {
            val target = LatLng(result.geometry.location.lat, result.geometry.location.lng)
            map?.addMarker(MarkerOptions().position(target))
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        // Hardcoding in the interest of time; normally would get user input or device location
        val startLocation = LatLng(41.88744282963304, -87.65274711534346)   // SRAM HQ in downtown Chicago
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 14f))
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
                if (nearbyShops != null
                    && nearbyShops.status == "OK"
                    && nearbyShops.results.isNotEmpty()
                ) {
                    //populateShopList(nearbyShops.results)
                    //dropPins(map, nearbyShops.results)

                    // DEBUG, todo: replace with above commented lines
                    Snackbar.make(
                        mainBinding.root,
                        "Status: ${nearbyShops.status}. Showing ${nearbyShops.results.size} results",
                        Snackbar.LENGTH_LONG
                    ).show()
                    Log.d(tag, "HTTP Response Code: ${nearbyShops.status}. Received ${nearbyShops.results.size} results!")
                }
            }
        }
    }
}