package com.gueg.velovwidget.velov

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class TokenManager {
	companion object {
		private var token = ""

		fun getToken(context: Context, listener: TokenManagerListener) {
			if(token.isNotEmpty())
				listener.onTokenParsed(token)
			else {
				val queue = Volley.newRequestQueue(context)
                queue.add(
                        TokenRequestOptions.build(
                                Response.Listener {
                                    queue.add(TokenRequest.build(
                                            Response.Listener { tokenResponse ->
                                                val tokenJsonResponse = JSONObject(tokenResponse)

                                                token = tokenJsonResponse["accessToken"] as String

                                                listener.onTokenParsed(token)
                                            },
                                            Response.ErrorListener { err ->
                                                err.printStackTrace()
                                                listener.onTokenParsed(null)
                                    }))
                                },
                                Response.ErrorListener { err ->
                                    err.printStackTrace()
                                    listener.onTokenParsed(null)
                                }
                        )
				)
			}
		}
	}

	interface TokenManagerListener {
		fun onTokenParsed(token: String?)
	}
}