package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

import java.io.IOException
import java.util.Locale


import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainActivity : AppCompatActivity() {

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var wayLatitude = 0.0
    private var wayLongitude = 0.0
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private val txtLocation: TextView? = null
    private var txtContinueLocation: TextView? = null
    private var stringBuilder: StringBuilder? = null
    internal var email: String? = ""
    private var editText: EditText? = null
     private var button: Button? = null
    private var isContinue = false
    private var isGPS = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById<View>(R.id.editText) as EditText
        button = findViewById<View>(R.id.button) as Button

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val email1 = prefs.getString("email", "")
        editText!!.setText(email1)

        email = email1
        Log.i("info", "onCreate: email1" + email1!!)

        email = email1



        button!!.setOnClickListener(View.OnClickListener {
            email = editText!!.getText().toString()
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            val editor = prefs.edit()
            editor.putString("email", email)
            editor.apply()

            Log.i("info", "onClick: Butten is clicked $email")
        })

        this.txtContinueLocation = findViewById(R.id.txtContinueLocation)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.interval = (10 * 1000).toLong() // 10 seconds
        locationRequest!!.fastestInterval = (10 * 1000).toLong() // 10 seconds


        GpsUtils(this).turnGPSOn(object : GpsUtils.OnGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                // turn on GPS
                isGPS = isGPSEnable
            }
        })

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude
                        if (!isContinue) {
                            txtLocation!!.text = String.format(Locale.US, "%s / %s", wayLatitude, wayLongitude)
                        } else {
                            stringBuilder!!.append(wayLatitude)
                            stringBuilder!!.append(" / ")
                            stringBuilder!!.append(wayLongitude)
                            stringBuilder!!.append("\n\n")

                            val triosLocation = Location("")
                            triosLocation.latitude = 43.4493124
                            triosLocation.longitude = -80.4868219
                            val distanceInMeters = location.distanceTo(triosLocation)

                            if (distanceInMeters < 50) {
                                Toast.makeText(this@MainActivity, "I Am here", Toast.LENGTH_LONG).show()

                                takeAttendance_to_Server()
                            } else {
                                Toast.makeText(this@MainActivity, "you left", Toast.LENGTH_LONG).show()
                            }
                            stringBuilder!!.append("distance from triOS:")
                            stringBuilder!!.append(distanceInMeters)
                            stringBuilder!!.append("\n\n")
                            txtContinueLocation!!.text = stringBuilder!!.toString()

                        }
                        if (!isContinue && mFusedLocationClient != null) {
                            mFusedLocationClient!!.removeLocationUpdates(locationCallback!!)
                        }
                    }
                }
            }
        }


        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show()
            return
        }
        isContinue = true
        stringBuilder = StringBuilder()
        getLocation()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    AppConstants.LOCATION_REQUEST)

        } else {
            if (isContinue) {
                mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
            } else {
                mFusedLocationClient!!.lastLocation.addOnSuccessListener(this@MainActivity) { location ->
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude

                        txtLocation!!.text = String.format(Locale.US, "%s  %s", wayLatitude, wayLongitude)
                    } else {
                        mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (isContinue) {
                        mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
                    } else {
                        mFusedLocationClient!!.lastLocation.addOnSuccessListener(this@MainActivity) { location ->
                            if (location != null) {
                                wayLatitude = location.latitude
                                wayLongitude = location.longitude
                                txtLocation!!.text = String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude)
                            } else {
                                mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true // flag maintain before get location
            }
        }
    }


    protected fun takeAttendance_to_Server() {
        val final_url = "https://hellotrioskitchener.appspot.com/take_attendance/"+ email
        Log.i("info", "takeAttendance_to_Server: $final_url")
        //val final_url = "http://10.3.200.182:8080/take_attendance"

        val okHttpClient = OkHttpClient()

        val request = Request.Builder().url(final_url).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("INFO", e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.i("DEBUG", "response received")
            }

        })
    }
}
