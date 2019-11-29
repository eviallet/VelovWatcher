package com.gueg.velovwidget.velov

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import java.util.*

class TokenRequestOptions {

    companion object {
        @Suppress("PrivatePropertyName")
        private val LINK = "https://api.cyclocity.fr/auth/environments/PRD/client_tokens"

        fun build(responseListener: Response.Listener<String>, errorListener: Response.ErrorListener): StringRequest {
            return object : StringRequest(
                    Method.OPTIONS,
                    LINK,
                    responseListener,
                    errorListener
            ) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Sec-Fetch-Mode"] = "cors"
                    headers["Sec-Fetch-Site"] = "cross-site"
                    headers["DNT"] = "1"
                    headers["Connection"] = "keep-alive"
                    headers["Content-Type"] = "application/json"
                    headers["Accept"] = "*/*"
                    headers["Host"] = "api.cyclocity.fr"
                    headers["Origin"] = "https://velov.grandlyon.com"
                    headers["Referer"] = "https://velov.grandlyon.com/mapping"
                    headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36 OPR/64.0.3417.92"
                    headers["Access-Control-Request-Headers"] = "access-control-allow-headers,access-control-allow-origin,content-type"
                    headers["Access-Control-Request-Method"] = "POST"

                    return headers
                }
            }
        }
    }
}