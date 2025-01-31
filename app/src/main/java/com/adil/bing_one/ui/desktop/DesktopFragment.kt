package com.adil.bing_one.ui.desktop

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.adil.bing_one.R
import com.adil.bing_one.databinding.FragmentDesktopBinding
import com.adil.bing_one.methods.Methods

class DesktopFragment : Fragment() {
    companion object {
        @JvmStatic
        var name: String = "Desktop Fragment"
         @SuppressLint("StaticFieldLeak")
         lateinit var desktopViewModel: DesktopViewModel

    }

    private var _binding: FragmentDesktopBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        desktopViewModel =
            ViewModelProvider(this)[DesktopViewModel::class.java]

        _binding = FragmentDesktopBinding.inflate(inflater, container, false)
        desktopViewModel.root = binding.root
        desktopViewModel.sharedPref = requireContext().getSharedPreferences(
            name, Context.MODE_PRIVATE
        )
        desktopViewModel.sharedEditor = desktopViewModel.sharedPref.edit()
        desktopViewModel.mSelectedTime.apply {
            value = desktopViewModel.sharedPref.getLong(
                getString(R.string.selected_desktop_time),
                10
            )
        }
        desktopViewModel.mSelectedCount.apply {
            value = desktopViewModel.sharedPref.getInt(
                getString(R.string.selected_desktop_count),
                35
            )
        }
        desktopViewModel.mSelectedImage.apply {
            value = desktopViewModel.sharedPref.getBoolean(
                getString(R.string.selected_desktop_image),
                false
            )
        }
        desktopViewModel.mSelectedWindows.apply {
            value = desktopViewModel.sharedPref.getBoolean(
                getString(R.string.selected_desktop_windows),
                true
            )
        }
        desktopViewModel.userAgent = desktopViewModel.sharedPref.getString(
            getString(R.string.user_agent_desktop), getString(R.string.desktopAgent)
        ) ?: ""
        desktopViewModel.mWordsList.apply { value = listOf() }
        desktopViewModel.webView = desktopViewModel.root.findViewById(R.id.web_view)
        desktopViewModel.autoSearch = desktopViewModel.root.findViewById(R.id.state_layout)
        desktopViewModel.countLayout = desktopViewModel.root.findViewById(R.id.count_layout)
        desktopViewModel.currentStateText =
            desktopViewModel.root.findViewById(R.id.currentState_label)
        desktopViewModel.currentStateIcon =
            desktopViewModel.root.findViewById(R.id.currentState_icon)
        desktopViewModel.currentCountText = desktopViewModel.root.findViewById(R.id.current_count)
        desktopViewModel.totalCountText = desktopViewModel.root.findViewById(R.id.total_count)
        desktopViewModel.homeIcon = desktopViewModel.root.findViewById(R.id.home_icon)
        desktopViewModel.reloadIcon = desktopViewModel.root.findViewById(R.id.reload_icon)
        desktopViewModel.settingsIcon = desktopViewModel.root.findViewById(R.id.settings_icon)
        desktopViewModel.currentTimeText = desktopViewModel.root.findViewById(R.id.current_time)

        // methods
        desktopViewModel.changeApiState(false)
        desktopViewModel.configTimer()

//        observables
        desktopViewModel.isApiRunning.observe(viewLifecycleOwner) {
            if (it) {
                desktopViewModel.currentStateText.setText(R.string.stop)
                desktopViewModel.currentStateIcon.setImageResource(R.drawable.ic_stop)
            } else {
                desktopViewModel.currentStateText.setText(R.string.start)
                desktopViewModel.currentStateIcon.setImageResource(R.drawable.ic_play)
            }
        }

        desktopViewModel.wordsList.observe(viewLifecycleOwner) {
            desktopViewModel.totalCountText.text = it.count().toString()
        }

        desktopViewModel.selectedTime.observe(viewLifecycleOwner) {
            desktopViewModel.configTimer()
            desktopViewModel.currentTimeText.text = it.toString()
            desktopViewModel.sharedEditor.putLong(getString(R.string.selected_desktop_time), it)
            desktopViewModel.sharedEditor.commit()
        }

        desktopViewModel.selectedCount.observe(viewLifecycleOwner) {
            desktopViewModel.setCount(lifecycleScope)
            desktopViewModel.sharedEditor.putInt(getString(R.string.selected_desktop_count), it)
            desktopViewModel.sharedEditor.commit()
        }

        desktopViewModel.currentCount.observe(viewLifecycleOwner) {
            desktopViewModel.currentCountText.text = it.toString()
        }

        desktopViewModel.selectedImage.observe(viewLifecycleOwner) {
            desktopViewModel.settings.loadsImagesAutomatically = it
            desktopViewModel.settings.blockNetworkImage = !it
            desktopViewModel.sharedEditor.putBoolean(getString(R.string.selected_mobile_image), it)
            desktopViewModel.sharedEditor.commit()
        }

        desktopViewModel.selectedWindows.observe(viewLifecycleOwner) {
            desktopViewModel.settings.setSupportMultipleWindows(it)
            desktopViewModel.sharedEditor.putBoolean(
                getString(R.string.selected_mobile_windows),
                it
            )
            desktopViewModel.sharedEditor.commit()
        }
        // autoSearch-button
        desktopViewModel.autoSearch.setOnClickListener {
            if (desktopViewModel.isApiRunning.value!!) {
                desktopViewModel.changeApiState(false)
            } else {
                desktopViewModel.changeApiState(true)
            }
        }

        // count-layout
        desktopViewModel.countLayout.setOnLongClickListener {
            desktopViewModel.resetApi(lifecycleScope)
            true
        }

        // app-bar
        desktopViewModel.homeIcon.setOnClickListener {
            desktopViewModel.goToRewardsHome()
        }

        desktopViewModel.reloadIcon.setOnClickListener {
            desktopViewModel.reload()
        }

        desktopViewModel.settingsIcon.setOnClickListener {
            desktopViewModel.showBottomSheet()
        }


        // web-view
        desktopViewModel.settings = desktopViewModel.webView.settings
        desktopViewModel.webView.webViewClient = desktopViewModel.getClient()
        desktopViewModel.settings.javaScriptEnabled = true
        desktopViewModel.settings.setGeolocationEnabled(false)
        desktopViewModel.settings.domStorageEnabled = true
        desktopViewModel.settings.javaScriptCanOpenWindowsAutomatically = true
        desktopViewModel.settings.allowFileAccess = true
        desktopViewModel.settings.databaseEnabled = true
        desktopViewModel.settings.loadsImagesAutomatically = desktopViewModel.selectedImage.value!!
        desktopViewModel.settings.blockNetworkImage = !(desktopViewModel.selectedImage.value!!)
        desktopViewModel.settings.setSupportMultipleWindows(desktopViewModel.selectedWindows.value!!)
        desktopViewModel.settings.javaScriptCanOpenWindowsAutomatically = true
        desktopViewModel.settings.loadWithOverviewMode = true
        desktopViewModel.settings.userAgentString = desktopViewModel.userAgent
        desktopViewModel.goToHome()
        desktopViewModel.webView.webChromeClient = WebChromeClient()

        return desktopViewModel.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            desktopViewModel.webView.destroy()
        } catch (e: Error) {
            Methods.showError(view, e.message, "onDestroy", name)
        }
        _binding = null
    }

}