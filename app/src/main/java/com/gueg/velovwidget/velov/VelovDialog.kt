package com.gueg.velovwidget.velov

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.gueg.velovwidget.R
import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment

class VelovDialog : SupportBlurDialogFragment() {

    private lateinit var rootView: View
    private lateinit var title: TextView

    private lateinit var list: RecyclerView
    private lateinit var noVelov: TextView
    private lateinit var progress: ProgressBar

    private lateinit var stationName: String
    private var stationNumber = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.dialog_velov, container, false)

        title = rootView.findViewById(R.id.dialog_velov_stationname)
        title.text = stationName

        noVelov = rootView.findViewById(R.id.dialog_velov_novelov)
        progress = rootView.findViewById(R.id.dialog_velov_loading)

        list = rootView.findViewById(R.id.dialog_velov_stationlist)
        list.layoutManager = LinearLayoutManager(context)

        TokenManager.getToken(context!!, object: TokenManager.TokenManagerListener {
            override fun onTokenParsed(token: String?) {
                if(token == null) {
                    onError()
                    return
                }

                VelovParser.parse(context!!, VelovRequest(stationNumber, token), object: VelovParser.VelovParserListener {
                    override fun onParseComplete(velovs: ArrayList<Velov>) {
                        activity!!.runOnUiThread {
                            progress.visibility = GONE
                            if(velovs.size>0) {
                                list.visibility = VISIBLE

                                list.adapter = VelovAdapter(velovs.sortedWith(compareBy{ it.standNumber }))
                            } else {
                                noVelov.visibility = VISIBLE
                            }
                        }
                    }
                    override fun onParseError() {
                        onError()
                    }
                })
            }
        })


        return rootView
    }

    private fun onError() {
        activity!!.runOnUiThread {
            progress.visibility = GONE
            noVelov.visibility = VISIBLE
            noVelov.text = "Erreur de récupération des vélovs."
        }
    }

    fun setStationName(stationName: String) : VelovDialog {
        this.stationName = stationName
        return this
    }

    fun setStationNb(stationNb: Int) : VelovDialog {
        this.stationNumber = stationNb
        return this
    }
}