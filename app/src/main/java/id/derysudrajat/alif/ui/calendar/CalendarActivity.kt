package id.derysudrajat.alif.ui.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import id.derysudrajat.alif.data.model.Schedule
import id.derysudrajat.alif.databinding.ActivityCalendarBinding
import id.derysudrajat.alif.ui.main.HomeViewModel
import id.derysudrajat.alif.utils.LocationUtils
import id.derysudrajat.alif.utils.LocationUtils.checkLocationPermission
import id.derysudrajat.alif.utils.TimeUtils.getCalendar
import id.derysudrajat.alif.utils.TimeUtils.stringFormat
import id.derysudrajat.alif.utils.TimeUtils.timeStamp
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val homeViewModel: HomeViewModel by viewModels()
    private val scope = lifecycleScope
    private var currentLat = 0.0
    private var currentLong = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAppbar()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission { requestLocationPermission() }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            currentLat = it.latitude
            currentLong = it.longitude
            homeViewModel.getPrayerSchedule(it.latitude, it.longitude, Timestamp.now())
        }
        scope.launch { homeViewModel.currentSchedule.collect(::populateCurrentSchedule) }

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            homeViewModel.getPrayerSchedule(
                currentLat, currentLong, getCalendar(year, month, day).time.stringFormat.timeStamp
            )
        }
    }

    private fun setupAppbar() = binding.actionBar.apply {
        tvTitle.text = buildString { append("Calendar") }
        btnBack.setOnClickListener { finish() }
    }

    private fun populateCurrentSchedule(it: Schedule) {
        binding.rvEvent.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = EventAdapter(it)
        }
    }

    @SuppressLint("NewApi")
    fun requestLocationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions -> LocationUtils.handlePermission(permissions) }
        LocationUtils.launchPermission(locationPermissionRequest)
    }
}