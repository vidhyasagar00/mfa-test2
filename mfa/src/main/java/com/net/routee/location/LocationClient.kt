package com.net.routee.location

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.net.routee.database.AppDatabase
import com.net.routee.database.DatabaseHelperImpl
import com.net.routee.database.LocationDataClass
import com.net.routee.preference.SharedPreference
import com.net.routee.retrofit.APISupport
import com.net.routee.services.Authenticator
import com.net.routee.services.FCMServices
import com.net.routee.utils.Constants
import com.net.routee.utils.PermissionReqCreator
import kotlinx.android.synthetic.main.authentication_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList


class LocationClient(context: Context, val callback: LocationPermissionCallback?, private val extrasIntent: Intent?=null) :
    ContextWrapper(context) {
    private val tag = "LocationClient"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    val dbHelper =
        AppDatabase.getInstance(this)?.let { DatabaseHelperImpl(it.locationDao()) }

    private var locationRequest: LocationRequest? = null

    val preference = SharedPreference(this)
    val configurationObject = JSONObject(preference.getInitialConfiguration())
    private var locationCallback: LocationCallback? = null


    /**
     * Checks whether the location permission is enabled on the device.
     *
     * @return A boolean value whether the device has allowed location permissions.
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * This function is to update the location if there were permissions.
     *
     */
    fun startLocationTracking() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (checkPermission()) {
            if (isLocationEnabled()) {
                //context.startService(Intent(this, MyLocationService::class.java))
                //Instantiating the Location request and setting the priority and the interval I need to update the location.
                locationRequest = LocationRequest.create()
                locationRequest!!.interval = 2000
                locationRequest!!.fastestInterval = 2000
                locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


                //instantiating the LocationCallBack
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val intent = Intent("LocationUpdation")
                        if (isNetworkAvailable()) {
                            intent.putExtra("sendToServer", true)
                            LocalBroadcastManager.getInstance(this@LocationClient)
                                .sendBroadcast(intent)
                            updateLocations(getRequiredLocationDataClass(locationResult.locations))
                        } else {

                            intent.putExtra("isUpdated", true)
                            LocalBroadcastManager.getInstance(this@LocationClient)
                                .sendBroadcast(intent)

                            //Showing the latitude, longitude and accuracy on the home screen.
                            for (location in locationResult.locations) {

                                scope.launch {
                                    dbHelper?.addLocation(
                                        LocationDataClass(
                                            version = if (configurationObject.has("version")) configurationObject.getString(
                                                "version") else "",
                                            dateTime = DateFormat.getDateTimeInstance()
                                                .format(Date()).toString(),
                                            latitude = location.latitude,
                                            longitude = location.longitude,
                                            accuracy = location.accuracy.toDouble())
                                    )
                                }
                            }
                        }
                    }
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest!!,
                    locationCallback!!,
                    Looper.getMainLooper())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    startInternetCheckForLocation()
                }
            } else {
                Toast.makeText(this, "Please Enable your location services", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        } else {
            PermissionReqCreator(this).requestPermission(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION),
                object : PermissionReqCreator.PermissionCallback {
                    override fun onPermissionGranted(granted: Boolean) {
                        if (granted) {
                            callback?.onGranted(granted)
                            startLocationTracking()
                        }
                    }

                }
            )
        }
    }

    fun stopLocationTracking() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
    }

    private fun getRequiredLocationDataClass(locations: List<Location>): List<LocationDataClass> {
        val requiredLocationObjects = ArrayList<LocationDataClass>()
        for (location in locations) {
            requiredLocationObjects.add(LocationDataClass(
                version = if (configurationObject.has("version")) configurationObject.getString(
                    "version") else "",
                dateTime = DateFormat.getDateTimeInstance()
                    .format(Date()).toString(),
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy.toDouble())
            )
        }
        return requiredLocationObjects
    }

    /**
     * It is to check whether the application has permissions or not.
     *
     * @return A boolean value if there were permission allowed or not.
     */
    private fun checkPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }


    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw)
            actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(
                NetworkCapabilities.TRANSPORT_BLUETOOTH))
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo
            nwInfo != null && nwInfo.isConnected
        }
    }

    fun updateLocations(locations: List<LocationDataClass>? = null) {
        if (locations != null) {
            val locationObject = LocationObject(preference.getDeviceUUID(), "0", locations)
            callLocationApi(locationObject)
        } else {
            scope.launch {
                val locationsFromDB = dbHelper?.getAllLocation()
                locationsFromDB?.size?.let {
                    if (it > 0) {
                        val locationObject =
                            LocationObject(preference.getDeviceUUID(), "0", locationsFromDB)
                        callLocationApi(locationObject)

                    }
                }
            }
        }

    }

    private fun callLocationApi(singleLocationObject: SingleLocationObject) {
        val call = APISupport.postAPIForLocationUpdate(Constants.SINGLE_LOCATION_UPDATE_URL,
            singleLocationObject)
        val progress = ProgressDialog(this)
        progress.setTitle("Loading")
        progress.show()

        call?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progress.dismiss()
                if (response.isSuccessful) {
                    if (extrasIntent != null) {
                        Authenticator.publishResult(Authenticator.success,
                            this@LocationClient, extrasIntent)
                    }
                    Toast.makeText(this@LocationClient, "Location sent successfully.",Toast.LENGTH_SHORT).show()
                } else {
                    if (extrasIntent != null) {
                        Authenticator.publishResult(Authenticator.failed,
                            this@LocationClient, extrasIntent)
                    }
                    Toast.makeText(this@LocationClient, "Location sent failed.",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                if (extrasIntent != null) {
                    Authenticator.publishResult(Authenticator.failed,
                        this@LocationClient, extrasIntent)
                }
                Toast.makeText(this@LocationClient, "Location sent failed.",Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun callLocationApi(locationObject: LocationObject) {
        val call = APISupport.postAPIForLocationUpdate(Constants.LOCATION_UPDATE_URL,
            locationObject)
//        val progress = ProgressDialog(this)
//        progress.setTitle("Loading")
//        progress.show()

        call?.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
//                progress.dismiss()
                if (response.isSuccessful) {
                    scope.launch {
                        dbHelper?.deleteAllLocations()
                    }
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
//                progress.dismiss()
            }

        })
    }

    fun getLatestLocation() {
        extrasIntent?.putExtra("authTypes", extrasIntent.getStringExtra("authTypes")?.replace(FCMServices.AuthType.LOCATION.type, ""))
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener {
                        val locationDataClass = LocationDataClass(
                            version = if (configurationObject.has("version")) configurationObject.getString(
                                "version") else "",
                            dateTime = DateFormat.getDateTimeInstance()
                                .format(Date()).toString(),
                            latitude = it.latitude,
                            longitude = it.longitude,
                            accuracy = it.accuracy.toDouble()
                        )
                        callLocationApi(SingleLocationObject(locationDataClass))
                    }
            } else {
                if (extrasIntent != null) {
                    Authenticator.publishResult(Authenticator.failed,
                        this@LocationClient, extrasIntent)
                }
                Toast.makeText(this, "Please Enable your location services", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            PermissionReqCreator(this).requestPermission(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION),
                object : PermissionReqCreator.PermissionCallback {
                    override fun onPermissionGranted(granted: Boolean) {
                        if (granted) {
                            getLatestLocation()
                            callback?.onGranted(granted)
                            startLocationTracking()
                        } else {
                            if (extrasIntent != null) {
                                Authenticator.publishResult(Authenticator.failed,
                                    this@LocationClient, extrasIntent)
                            }
                        }
                    }

                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun startInternetCheckForLocation() {
        updateLocations()
        val connectivityManager = getSystemService(ConnectivityManager::class.java)

        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.e(tag, "The default network is now: $network")
                updateLocations()
            }

            override fun onLost(network: Network) {
                Log.e(tag,
                    "The application no longer has a default network. The last default network was $network")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                Log.e(tag, "The default network changed capabilities: $networkCapabilities")
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                Log.e(tag, "The default network changed link properties: $linkProperties")
            }
        })
    }


    init {
        isPermissionGranted()
    }

    private fun isPermissionGranted() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                callback?.onGranted(true)
            }
            }
    }
}

