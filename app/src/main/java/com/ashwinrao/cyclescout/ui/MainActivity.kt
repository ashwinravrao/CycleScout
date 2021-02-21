package com.ashwinrao.cyclescout.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ashwinrao.cyclescout.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeMap()
        initializeBottomSheet()
    }

    private fun initializeMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initializeBottomSheet() {
        // TODO: Populate recyclerview with nearby shop details
    }

    override fun onMapReady(map: GoogleMap?) {
        // TODO: Request current location, get nearby shop coordinates, drop pins
    }
}