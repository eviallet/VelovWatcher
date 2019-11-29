package com.gueg.velovwidget.velov

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import java.util.*

class TokenRequest {

    companion object {
        @Suppress("PrivatePropertyName")
        private val LINK = "https://api.cyclocity.fr/auth/environments/PRD/client_tokens"

        fun build(responseListener: Response.Listener<String>, errorListener: Response.ErrorListener): StringRequest {
            return object : StringRequest(
                    Method.POST,
                    LINK,
                    responseListener,
                    errorListener
            ) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json, text/plain, */*"
                    headers["Accept-Language"] = "fr-FR,fr;en-US,en;q=0.8;q=0.6;q=0.4"
                    headers["Access-Control-Allow-Headers"] = "*"
                    headers["Access-Control-Allow-Origin"] = "*"
                    headers["Connection"] = "keep-alive"
                    headers["Content-Length"] = "100"
                    headers["Content-Type"] = "application/json"
                    headers["DNT"] = "1"
                    headers["Host"] = "api.cyclocity.fr"
                    headers["Origin"] = "https://velov.grandlyon.com"
                    headers["Referer"] = "https://velov.grandlyon.com/mapping"
                    headers["Sec-Fetch-Mode"] = "cors"
                    headers["Sec-Fetch-Site"] = "cross-site"
                    headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36 OPR/64.0.3417.92"

                    headers["refreshToken"] = "c1108f14-714c-4ef9-8fc1-5cd8bc127ae6"

                    return headers
                }

                override fun getBody(): ByteArray {
                    val payload =
                            "{\"code\":\"vls.web.lyon:PRD\","+
                            "\"key\":\"c3d9f5c22a9157a7cc7fe0e38269573bdd2f13ec48f867360ecdcbd35b196f87\"}"

                    return payload.toByteArray()
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
        }
    }
}