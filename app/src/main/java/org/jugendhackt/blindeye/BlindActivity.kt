package org.jugendhackt.blindeye

import android.annotation.SuppressLint
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.google.android.gms.location.LocationRequest
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class BlindActivity : AppCompatActivity() {
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_blind)
        getLocation()
    }

    //define the listener
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val request: LocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(5)
                .setInterval(100)

        val locationProvider = ReactiveLocationProvider(this)
        val subscription = locationProvider.getUpdatedLocation(request)
                .subscribe {
                    sendGet("obstacles?lat_like=${String.format("%.2f", it.latitude)}&long_like=${String.format("%.2f", it.longitude)}")
                }
    }

    private fun sendGet(path: String): String {
        val url = "http://192.168.137.1:4242/$path" // TODO: No hardcoded IP
        val obj = URL(url)

        with(obj.openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                return response.toString()
            }
        }
    }

    private fun getDistance(lat_a: Float, lng_a: Float, lat_b: Float, lng_b: Float): Double {
        val pk = (180f / Math.PI).toFloat()

        val a1 = lat_a / pk
        val a2 = lng_a / pk
        val b1 = lat_b / pk
        val b2 = lng_b / pk

        val t1 = Math.cos(a1.toDouble()) * Math.cos(a2.toDouble()) * Math.cos(b1.toDouble()) * Math.cos(b2.toDouble())
        val t2 = Math.cos(a1.toDouble()) * Math.sin(a2.toDouble()) * Math.cos(b1.toDouble()) * Math.sin(b2.toDouble())
        val t3 = Math.sin(a1.toDouble()) * Math.sin(b1.toDouble())
        val tt = Math.acos(t1 + t2 + t3)

        return 6366000 * tt
    }
}
