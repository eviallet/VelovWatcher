package com.gueg.velovwidget.velov

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VelovParser {
    companion object {
        fun parse(context: Context, request: VelovRequest, listener: VelovParserListener) {
            Thread {
                val queue = Volley.newRequestQueue(context)

                queue.add(request.build(
                        Response.Listener { response ->
                            val jsonResponse = JSONArray(response)
                            val velovs = ArrayList<Velov>()

                             if(jsonResponse.length()>0) {
                                 for(i in 0 until jsonResponse.length()) {
                                     val json = jsonResponse[i] as JSONObject
                                     val rating = json["rating"] as JSONObject

                                     var ratingNone = false
                                     if(rating["count"] as Int == 0)
                                         ratingNone = true

                                     val velov = Velov(
                                             json["standNumber"] as Int,
                                             json["type"] as String,
                                             json["status"] as String,
                                             if(ratingNone) 0.0 else rating["value"] as Double,
                                             rating["count"] as Int,
                                             if(ratingNone) Date(0) else formatDate(rating["lastRatingDateTime"] as String),
                                             formatDate(json["createdAt"] as String),
                                             formatDate(json["updatedAt"] as String),
                                             ratingNone
                                     )

                                     velovs.add(velov)
                                 }
                             }

                            listener.onParseComplete(velovs)
                        },
                        Response.ErrorListener { err ->
                            err.printStackTrace()
                            listener.onParseError()
                        }
                ))

            }.start()
        }

        // 2019-11-04T19:42:21.325
        private fun formatDate(str: String): Date {
            var d = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS").parse(str)
            if(d== null)
                d = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(str)!!
            return d
        }

    }


    interface VelovParserListener {
        fun onParseComplete(velovs: ArrayList<Velov>)
        fun onParseError()
    }
}