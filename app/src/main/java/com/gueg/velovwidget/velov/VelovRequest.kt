package com.gueg.velovwidget.velov

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import java.util.*

class VelovRequest(private var stationNb: Int, private val token: String) {

    @Suppress("PrivatePropertyName")
    private val LINK = "https://api.cyclocity.fr/contracts/lyon/bikes?stationNumber=#STANB#"

    fun build(responseListener : Response.Listener<String>, errorListener : Response.ErrorListener) : StringRequest {
        return object : StringRequest(
                Method.GET,
                LINK.replace("#STANB#", stationNb.toString()),
                responseListener,
                errorListener
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Sec-Fetch-Mode"] = "cors"
                headers["DNT"] = "1"
                headers["Accept-Language"] = "fr"
                headers["Authorization"] = "Taknv1 $token"
                headers["Content-Type"] = "application/vnd.bikes.v2+json"
                headers["Accept"] = "application/vnd.bikes.v2+json"
                headers["Referer"] = "https://velov.grandlyon.com/mapping"
                headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36 OPR/64.0.3417.92"

                return headers
            }
        }
    }
}