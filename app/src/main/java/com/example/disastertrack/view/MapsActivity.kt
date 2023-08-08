package com.example.disastertrack.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.disastertrack.R
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*

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

    private lateinit var filterInformation: TextView

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

        findViewById<FloatingActionButton>(R.id.fabGetCurrentLocation).setOnClickListener {
            getCurrentLocation()
        }

        // follow this code to get current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Bottom Sheet
        BottomSheetBehavior.from(findViewById(R.id.bottom_sheet)).apply {
            peekHeight = 400
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // Hide Action Bar
//        supportActionBar?.hide()

//        setSupportActionBar(findViewById(R.id.toolbar_custom))


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

        val items: List<String> =
            listOf(
                getString(R.string.aceh),
                getString(R.string.bali),
                getString(R.string.bangka_belitung),
                getString(R.string.banten),
                getString(R.string.bengkulu),
                getString(R.string.jawa_tengah),
                getString(R.string.kalimantan_tengah),
                getString(R.string.sulawesi_tengah),
                getString(R.string.jawa_timur),
                getString(R.string.kalimantan_timur),
                getString(R.string.nusa_tenggara_timur),
                getString(R.string.gorontalo),
                getString(R.string.jakarta),
                getString(R.string.jambi),
                getString(R.string.lampung),
                getString(R.string.maluku),
                getString(R.string.kalimantan_utara),
                getString(R.string.maluku_utara),
                getString(R.string.sulawesi_utara),
                getString(R.string.sumatra_utara),
                getString(R.string.papua),
                getString(R.string.riau),
                getString(R.string.kepulauan_riau),
                getString(R.string.sulawesi_tenggara),
                getString(R.string.kalimantan_selatan),
                getString(R.string.sulawesi_selatan),
                getString(R.string.sumatra_selatan),
                getString(R.string.yogyakarta),
                getString(R.string.jawa_barat),
                getString(R.string.kalimantan_barat),
                getString(R.string.nusa_tenggara_barat),
                getString(R.string.papua_barat),
                getString(R.string.sulawesi_barat),
                getString(R.string.sumatra_barat)
            )

        val selectAdapter = ArrayAdapter(this, R.layout.list_item_province, items)
        val exposedDropdownLayout: TextInputLayout = findViewById(R.id.choose_province_menu)
        val exposedDrowdown: AutoCompleteTextView = findViewById(R.id.choose_provinc_menu_dropdown)

        exposedDrowdown.setAdapter(selectAdapter)
        exposedDrowdown.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = selectAdapter.getItem(position) ?: return@setOnItemClickListener
            Log.d("MainChooseProvince", selectedItem)
            getReportsByProvince(selectedItem)
        }


        // get date
        val mPickDateButton = findViewById<FloatingActionButton>(R.id.fabGetDateByCalendar)
//        val mShowSelectedDateText = findViewById<>()

        val materialDateBuilder : MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> = MaterialDatePicker.Builder.dateRangePicker()

        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        mPickDateButton.setOnClickListener { // getSupportFragmentManager() to
            // interact with the fragments
            // associated with the material design
            // date picker tag is to get any error
            // in logcat
            Log.d("MainDate", "Opening..")
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        materialDatePicker.addOnPositiveButtonClickListener {
//            val inputDateString: String = materialDatePicker.headerText
//            val inputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
//            val outputFormat = SimpleDateFormat("yyyy-MM-dd")
//
//            val date = inputFormat.parse(inputDateString)
//            val outputDateString = outputFormat.format(date)

            var convertedDate = convertDateRange(materialDatePicker.headerText)

            Log.d("MainDate", "${convertedDate}")
        }
    }

    fun convertDateRange(dateRange: String): String {
        val dateParts = dateRange.split(" – ")
        val startDate = parseDate(dateParts[0])
        val endDate = parseEndDate(dateParts[1])

        val convertedStartDate = SimpleDateFormat("yyyy-MM-dd").format(startDate)
        val convertedEndDate = SimpleDateFormat("yyyy-MM-dd").format(endDate)

        return "$convertedStartDate – $convertedEndDate"
    }

    fun parseDate(dateString: String): Date {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = if (dateString.contains(",")) "MMM d, yyyy" else "MMM d"
//         SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString)

        return if ( dateString.contains(",")) {
            SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString)
        } else {
            val dateWithoutYear = SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString)
            val calendar = Calendar.getInstance()
            calendar.time = dateWithoutYear
            calendar.set(Calendar.YEAR, currentYear)

            calendar.time
        }
    }

    fun parseEndDate(endDateString: String): Date {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val dateFormat = if (endDateString.contains(",")) "MMM d, yyyy" else "MMM d"



        return if ( endDateString.contains(",")) {
            SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(endDateString)
        } else {
            val dateWithoutYear = SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(endDateString)
            val calendar = Calendar.getInstance()
            calendar.time = dateWithoutYear
            calendar.set(Calendar.YEAR, currentYear)

            calendar.time
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
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

    private fun getCurrentLocation() {

        // Request and display the current location on the map
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle the case where permissions are not granted
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                val currentLatLong = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            }
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
                            reportAdapter = ReportAdapter(
                                geometries,
                                onReportItemClick = { position -> onReportItemClick(position) })
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

    private fun getReportResponseByPickedDate() {
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
                            reportAdapter = ReportAdapter(
                                geometries,
                                onReportItemClick = { position -> onReportItemClick(position) })
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

    private fun getReportsByProvince(province: String) {
        val emptyResultTextView: TextView = findViewById(R.id.no_result)
        val emptyResultImageView: ImageView = findViewById(R.id.no_result_image)

        lateinit var selectedProvince: String

        when (province) {
            getString(R.string.aceh) -> selectedProvince = "ID-AC"
            getString(R.string.bali) -> selectedProvince = "ID-BA"
            getString(R.string.bangka_belitung) -> selectedProvince = "ID-BB"
            getString(R.string.banten) -> selectedProvince = "ID-BT"
            getString(R.string.bengkulu) -> selectedProvince = "ID-BE"
            getString(R.string.jawa_tengah) -> selectedProvince = "ID-JT"
            getString(R.string.kalimantan_tengah) -> selectedProvince = "ID-KT"
            getString(R.string.sulawesi_tengah) -> selectedProvince = "ID-ST"
            getString(R.string.jawa_timur) -> selectedProvince = "ID-JI"
            getString(R.string.kalimantan_timur) -> selectedProvince = "ID-KI"
            getString(R.string.nusa_tenggara_timur) -> selectedProvince = "ID-NT"
            getString(R.string.gorontalo) -> selectedProvince = "ID-GO"
            getString(R.string.jakarta) -> selectedProvince = "ID-JK"
            getString(R.string.jambi) -> selectedProvince = "ID-JA"
            getString(R.string.lampung) -> selectedProvince = "ID-LA"
            getString(R.string.maluku) -> selectedProvince = "ID-MA"
            getString(R.string.kalimantan_utara) -> selectedProvince = "ID-KU"
            getString(R.string.maluku_utara) -> selectedProvince = "ID-MU"
            getString(R.string.sulawesi_utara) -> selectedProvince = "ID-SA"
            getString(R.string.sumatra_utara) -> selectedProvince = "ID-SU"
            getString(R.string.papua) -> selectedProvince = "ID-PA"
            getString(R.string.riau) -> selectedProvince = "ID-RI"
            getString(R.string.kepulauan_riau) -> selectedProvince = "ID-KR"
            getString(R.string.sulawesi_tenggara) -> selectedProvince = "ID-SG"
            getString(R.string.kalimantan_selatan) -> selectedProvince = "ID-KS"
            getString(R.string.sulawesi_selatan) -> selectedProvince = "ID-SN"
            getString(R.string.sumatra_selatan) -> selectedProvince = "ID-SS"
            getString(R.string.yogyakarta) -> selectedProvince = "ID-YO"
            getString(R.string.jawa_barat) -> selectedProvince = "ID-JB"
            getString(R.string.kalimantan_barat) -> selectedProvince = "ID-KB"
            getString(R.string.nusa_tenggara_barat) -> selectedProvince = "ID-NB"
            getString(R.string.papua_barat) -> selectedProvince = "ID-PB"
            getString(R.string.sulawesi_barat) -> selectedProvince = "ID-SR"
            getString(R.string.sumatra_barat) -> selectedProvince = "ID-SB"
        }

        val call = reportApiServiceImpl.getReportByProvinceLocation(selectedProvince)
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
                            reportAdapter = ReportAdapter(
                                geometries,
                                onReportItemClick = { position -> onReportItemClick(position) })
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

        filterInformation = findViewById(R.id.text_filter)

        filterInformation.text = "Provinsi: $province"
        filterInformation.visibility = View.VISIBLE

        clearDisasterMarkers()

        val adapter = ButtonAdapter(
            buttonActions,
            onButtonClick = { position -> onButtonClick(position) },
            onButtonMarkerClick = { position -> onButtonMarkerClick(position) })
        recyclerViewButton.adapter = adapter
        recyclerViewButton.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Clear the button state and background when a new province is selected
        adapter.resetButtonState()


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
                            reportAdapter = ReportAdapter(
                                geometries,
                                onReportItemClick = { position -> onReportItemClick(position) })
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

        filterInformation = findViewById(R.id.text_filter)
        var disasterTypeInfo = when (disasterType) {
            DisasterType.BANJIR.url -> "Banjir"
            DisasterType.GEMPA.url -> "Gempa Bumi"
            DisasterType.KABUT.url -> "Kabut"
            DisasterType.GUNUNGMELETUS.url -> "Gunung Meletus"
            DisasterType.KEBAKARAN.url -> "Kebakaran"
            DisasterType.BERANGIN.url -> "Berangin"
            else -> "Tidak ada Filter Bencana"
        }

        filterInformation.text = "Disaster: $disasterTypeInfo"
        filterInformation.visibility = View.VISIBLE
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