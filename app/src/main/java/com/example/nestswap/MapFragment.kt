package com.example.nestswap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.nestswap.Model.Networking.NominatimResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layout = FrameLayout(requireContext())
        webView = WebView(requireContext())
        progressBar = ProgressBar(requireContext())
        layout.addView(webView)
        layout.addView(progressBar)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val city = arguments?.getString("city") ?: "Tel Aviv"

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        progressBar.visibility = View.VISIBLE

        NominatimClient.api.searchLocation(city).enqueue(object : Callback<List<NominatimResponse>> {
            override fun onResponse(
                call: Call<List<NominatimResponse>>,
                response: Response<List<NominatimResponse>>
            ) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val location = response.body()!![0]
                    val lat = location.lat
                    val lon = location.lon

                    val mapUrl = "https://www.openstreetmap.org/?mlat=$lat&mlon=$lon#map=12/$lat/$lon"
                    webView.loadUrl(mapUrl)
                } else {
                    Log.e("MapFragment", "No location found for city: $city")
                    webView.loadUrl("https://www.openstreetmap.org/search?query=${city.replace(" ", "%20")}")
                }
            }

            override fun onFailure(call: Call<List<NominatimResponse>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("MapFragment", "Nominatim API failed: ${t.message}")
                webView.loadUrl("https://www.openstreetmap.org/search?query=${city.replace(" ", "%20")}")
            }
        })
    }
}
