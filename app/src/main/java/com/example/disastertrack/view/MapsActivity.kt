package com.example.disastertrack.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.disastertrack.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.disastertrack.databinding.ActivityMapsBinding
import com.example.disastertrack.model.data.Geometry
import com.example.disastertrack.model.data.ReportsData
import com.example.disastertrack.model.implement.*
import com.example.disastertrack.model.service.ReportApiService
import com.example.disastertrack.utils.BaseURL
import com.example.disastertrack.utils.DisasterType
import com.example.disastertrack.view.adapter.ButtonAdapter
import com.example.disastertrack.view.adapter.ReportAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val disasterMarkers: MutableList<Marker> = mutableListOf()

    // Adding this for current location
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerViewReport: RecyclerView
    private lateinit var recyclerViewButton: RecyclerView
    private lateinit var reportAdapter: ReportAdapter

    private val buttonActions = listOf(
        ActionBanjirImpl(),
        ActionKabutImpl(),
        ActionGunungImpl(),
        ActionKebakaranImpl(),
        ActionGempaImpl(),
        ActionBeranginImpl()
    )


    private val retrofit by lazy {
        Retrofit.Builder().baseUrl(BaseURL.BASE_URL.url)
            .addConverterFactory(MoshiConverterFactory.create()).build()
    }

    private val reportApiServiceImpl by lazy {
        retrofit.create(ReportApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // follow this code to get current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Bottom Sheet
        BottomSheetBehavior.from(findViewById(R.id.bottom_sheet)).apply {
            peekHeight = 400
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // Hide Action Bar
        supportActionBar?.hide()


        // report bencana
        recyclerViewReport = findViewById(R.id.recycler_view_report)
        recyclerViewReport.layoutManager = LinearLayoutManager(this)
        getReportResponseByTime()

        // button disaster
        recyclerViewButton = findViewById(R.id.recycler_view_buttons_disaster)
//        val buttonActions = listOf("Banjir", "Kabut", "Gempa", "Kebakaran", "Gunung Meletus", "Berangin")
//        val buttonActions = listOf(ActionBanjirImpl())

//        val adapter = ButtonAdapter(buttonActions) { position ->
//            onButtonClick(position)
//        }
        val adapter = ButtonAdapter(
            buttonActions,
            onButtonClick = { position -> onButtonClick(position) },
            onButtonMarkerClick = { position -> onButtonMarkerClick(position) })
        recyclerViewButton.adapter = adapter
        recyclerViewButton.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))

            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("${currentLatLong}")
        mMap.addMarker(markerOptions)
    }

    private fun placeMarkerOnMapDisaster(currentLatLong: LatLng): Marker? {
        val markerOptions = MarkerOptions().position(currentLatLong)
        Log.d("MainMap", "lat long : ${currentLatLong}")
        markerOptions.title("Lokasi : ${currentLatLong}") // You can set a title for the marker here
        return mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(p0: Marker) = false

    private fun onButtonMarkerClick(position: Int) {
        // Handle marker click for the button at the given position
        if (position < disasterMarkers.size) {
            // Get the marker associated with the button position
            val marker = disasterMarkers[position]
            // Do whatever you want with the marker (e.g., animate the camera to it)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))
        }
    }

    private fun getReportResponseByTime() {
        val emptyResultTextView: TextView = findViewById(R.id.no_result)
        val emptyResultImageView: ImageView = findViewById(R.id.no_result_image)
        val call = reportApiServiceImpl.getReportByYearStartToEnd(
            "2020-12-04T00:00:00+0700",
            "2020-12-06T05:00:00+0700"
        )
        call.enqueue(object : Callback<ReportsData> {
            override fun onFailure(call: Call<ReportsData>, t: Throwable) {
                Log.e("MainActivity", "Failed to get search result", t)
            }

            override fun onResponse(call: Call<ReportsData>, response: Response<ReportsData>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        val geometries = apiResponse.result.objects.output.geometries

                        if (geometries.isNullOrEmpty()) {
                            emptyResultTextView.visibility = View.VISIBLE
                            emptyResultImageView.visibility = View.VISIBLE
                            recyclerViewReport.visibility = View.GONE
                        } else {
                            emptyResultTextView.visibility = View.GONE
                            emptyResultImageView.visibility = View.GONE
                            recyclerViewReport.visibility = View.VISIBLE
                            reportAdapter = ReportAdapter(geometries, onReportItemClick = { position -> onReportItemClick(position)})
                            recyclerViewReport.adapter = reportAdapter
                        }

//                        reportAdapter = ReportAdapter(geometries)
//                        recyclerViewReport.adapter = reportAdapter
                    }

                    Log.d("MainActivity", "response : ${response.body()}")
                } else {
                    Log.e(
                        "MainActivity", "Failed to get search results\n${
                            response.errorBody()?.string().orEmpty()
                        }"
                    )
                }
            }
        })
    }

    private fun getReportsByDisaster(disasterType: String) {
        val emptyResultTextView: TextView = findViewById(R.id.no_result)
        val emptyResultImageView: ImageView = findViewById(R.id.no_result_image)
        val call = reportApiServiceImpl.getReportByDisaster(disasterType)
        call.enqueue(object : Callback<ReportsData> {
            override fun onFailure(call: Call<ReportsData>, t: Throwable) {
                Log.e("MainActivity", "Failed to get search result", t)
            }

            override fun onResponse(call: Call<ReportsData>, response: Response<ReportsData>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        val geometries = apiResponse.result.objects.output.geometries


                        clearDisasterMarkers()
                        if (geometries.isNullOrEmpty()) {
                            emptyResultTextView.visibility = View.VISIBLE
                            emptyResultImageView.visibility = View.VISIBLE
                            recyclerViewReport.visibility = View.GONE

                            // Clear existing markers before adding new ones
//                            mMap.clear()
                        } else {
                            emptyResultTextView.visibility = View.GONE
                            emptyResultImageView.visibility = View.GONE
                            recyclerViewReport.visibility = View.VISIBLE
                            reportAdapter = ReportAdapter(geometries, onReportItemClick = {position -> onReportItemClick(position)})
                            recyclerViewReport.adapter = reportAdapter

                            val mapBuilder = LatLngBounds.Builder()
                            for (geometry in geometries) {
                                val latLng =
                                    LatLng(geometry.coordinates[1], geometry.coordinates[0])
                                var marker = placeMarkerOnMapDisaster(latLng)!!
                                disasterMarkers.add(marker)
                                mapBuilder.include(latLng)
                            }
                            // Adjust the camera to show all markers on the map
                            val bounds = mapBuilder.build()
                            val padding = resources.getDimensionPixelSize(R.dimen.map_padding)
                            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                            mMap.moveCamera(cameraUpdate)

//                            if (disasterMarkers.isNotEmpty()) {
//                                val firstMarker = disasterMarkers[0]
//                                Log.d("MainMap", "${firstMarker}")
//                                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(firstMarker.position, 12f)
//                                mMap.moveCamera(cameraUpdate)
//                            }
                        }


//                        reportAdapter = ReportAdapter(geometries)
//                        recyclerViewReport.adapter = reportAdapter
                    }

                    Log.d("MainActivity", "response ${disasterType} : ${response.body()}")
                } else {
                    Log.e(
                        "MainActivity", "Failed to get search results\n${
                            response.errorBody()?.string().orEmpty()
                        }"
                    )
                }
            }
        })
    }

    private fun onReportItemClick(report: Geometry) {
        // Handle the click event for a specific report item here
        val latLng = LatLng(report.coordinates[1], report.coordinates[0])
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun onButtonClick(position: Int) {
        val action = buttonActions[position]
        action.performAction()

        when (action) {
            is ActionBanjirImpl -> getReportsByDisaster(DisasterType.BANJIR.url)
            is ActionKabutImpl -> getReportsByDisaster(DisasterType.KABUT.url)
            is ActionGunungImpl -> getReportsByDisaster(DisasterType.GUNUNGMELETUS.url)
            is ActionKebakaranImpl -> getReportsByDisaster(DisasterType.KEBAKARAN.url)
            is ActionGempaImpl -> getReportsByDisaster(DisasterType.GEMPA.url)
            is ActionBeranginImpl -> getReportsByDisaster(DisasterType.BERANGIN.url)
        }
    }

    private fun clearDisasterMarkers() {
        for (marker in disasterMarkers) {
            marker.remove()
        }
        disasterMarkers.clear()
    }


    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }
}