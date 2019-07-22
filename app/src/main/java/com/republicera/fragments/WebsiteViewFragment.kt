package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.republicera.R
import kotlinx.android.synthetic.main.fragment_web_view.*

class WebsiteViewFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_web_view, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val webView = webview_view

        val url = "https://dfsdfsd}"

        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.builtInZoomControls = true
        webView.settings.pluginState = WebSettings.PluginState.ON
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)

    }
}