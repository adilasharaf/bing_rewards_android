package com.adil.bing_one.ui.mobile

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
import com.adil.bing_one.databinding.FragmentMobileBinding
import com.adil.bing_one.methods.Methods

class MobileFragment : Fragment() {
    companion object {
        @JvmStatic
        var name: String = "Mobile Fragment"

        @SuppressLint("StaticFieldLeak")
        lateinit var mobileViewModel: MobileViewModel
    }

    private var _binding: FragmentMobileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mobileViewModel =
            ViewModelProvider(this)[MobileViewModel::class.java]

        _binding = FragmentMobileBinding.inflate(inflater, container, false)
        mobileViewModel.root = binding.root
        mobileViewModel.sharedPref = requireContext().getSharedPreferences(
            name, Context.MODE_PRIVATE
        )
        mobileViewModel.sharedEditor = mobileViewModel.sharedPref.edit()
        mobileViewModel.mSelectedTime.apply {
            value = mobileViewModel.sharedPref.getLong(
                getString(R.string.selected_mobile_time),
                10
            )
        }
        mobileViewModel.mSelectedCount.apply {
            value = mobileViewModel.sharedPref.getInt(
                getString(R.string.selected_mobile_count),
                25
            )
        }
        mobileViewModel.mSelectedImage.apply {
            value = mobileViewModel.sharedPref.getBoolean(
                getString(R.string.selected_mobile_image),
                false
            )
        }
        mobileViewModel.mSelectedWindows.apply {
            value = mobileViewModel.sharedPref.getBoolean(
                getString(R.string.selected_mobile_windows),
                true
            )
        }
        mobileViewModel.userAgent = mobileViewModel.sharedPref.getString(
            getString(R.string.user_agent_mobile), getString(R.string.mobileEdge)
        ) ?: ""
        mobileViewModel.mWordsList.apply { value = listOf() }
        mobileViewModel.webView = mobileViewModel.root.findViewById(R.id.web_view)
        mobileViewModel.autoSearch = mobileViewModel.root.findViewById(R.id.state_layout)
        mobileViewModel.countLayout = mobileViewModel.root.findViewById(R.id.count_layout)
        mobileViewModel.currentStateText =
            mobileViewModel.root.findViewById(R.id.currentState_label)
        mobileViewModel.currentStateIcon = mobileViewModel.root.findViewById(R.id.currentState_icon)
        mobileViewModel.currentCountText = mobileViewModel.root.findViewById(R.id.current_count)
        mobileViewModel.totalCountText = mobileViewModel.root.findViewById(R.id.total_count)
        mobileViewModel.homeIcon = mobileViewModel.root.findViewById(R.id.home_icon)
        mobileViewModel.reloadIcon = mobileViewModel.root.findViewById(R.id.reload_icon)
        mobileViewModel.settingsIcon = mobileViewModel.root.findViewById(R.id.settings_icon)
        mobileViewModel.currentTimeText = mobileViewModel.root.findViewById(R.id.current_time)
        // methods
        mobileViewModel.changeApiState(false)
        mobileViewModel.configTimer()

//        observables
        mobileViewModel.isApiRunning.observe(viewLifecycleOwner) {
            if (it) {
                mobileViewModel.currentStateText.setText(R.string.stop)
                mobileViewModel.currentStateIcon.setImageResource(R.drawable.ic_stop)
            } else {
                mobileViewModel.currentStateText.setText(R.string.start)
                mobileViewModel.currentStateIcon.setImageResource(R.drawable.ic_play)
            }
        }

        mobileViewModel.wordsList.observe(viewLifecycleOwner) {
            mobileViewModel.totalCountText.text = it.count().toString()
        }

        mobileViewModel.selectedTime.observe(viewLifecycleOwner) {
            mobileViewModel.configTimer()
            mobileViewModel.currentTimeText.text = it.toString()
            mobileViewModel.sharedEditor.putLong(getString(R.string.selected_mobile_time), it)
            mobileViewModel.sharedEditor.commit()
        }

        mobileViewModel.selectedCount.observe(viewLifecycleOwner) {
            mobileViewModel.setCount(lifecycleScope)
            mobileViewModel.sharedEditor.putInt(getString(R.string.selected_mobile_count), it)
            mobileViewModel.sharedEditor.commit()
        }

        mobileViewModel.currentCount.observe(viewLifecycleOwner) {
            mobileViewModel.currentCountText.text = it.toString()
        }

        mobileViewModel.selectedImage.observe(viewLifecycleOwner) {
            mobileViewModel.settings.loadsImagesAutomatically = it
            mobileViewModel.settings.blockNetworkImage = !it
            mobileViewModel.sharedEditor.putBoolean(getString(R.string.selected_mobile_image), it)
            mobileViewModel.sharedEditor.commit()
        }

        mobileViewModel.selectedWindows.observe(viewLifecycleOwner) {
            mobileViewModel.settings.setSupportMultipleWindows(it)
            mobileViewModel.sharedEditor.putBoolean(getString(R.string.selected_mobile_windows), it)
            mobileViewModel.sharedEditor.commit()
        }

        // autoSearch-button
        mobileViewModel.autoSearch.setOnClickListener {
            if (mobileViewModel.isApiRunning.value!!) {
                mobileViewModel.changeApiState(false)
            } else {
                mobileViewModel.changeApiState(true)
            }
        }

        // count-layout
        mobileViewModel.countLayout.setOnLongClickListener {
            mobileViewModel.resetApi(lifecycleScope)
            true
        }

        // app-bar
        mobileViewModel.homeIcon.setOnClickListener {
            mobileViewModel.goToRewardsHome()
        }

        mobileViewModel.reloadIcon.setOnClickListener {
            mobileViewModel.reload()
        }

        mobileViewModel.settingsIcon.setOnClickListener {
            mobileViewModel.showBottomSheet()
        }

        // web-view
        mobileViewModel.settings = mobileViewModel.webView.settings
        mobileViewModel.webView.webViewClient = mobileViewModel.getClient()
        mobileViewModel.settings.javaScriptEnabled = true
        mobileViewModel.settings.setGeolocationEnabled(false)
        mobileViewModel.settings.domStorageEnabled = true
        mobileViewModel.settings.javaScriptCanOpenWindowsAutomatically = true
        mobileViewModel.settings.allowFileAccess = true
        mobileViewModel.settings.databaseEnabled = true
        mobileViewModel.settings.blockNetworkImage = !(mobileViewModel.selectedImage.value!!)
        mobileViewModel.settings.loadsImagesAutomatically = mobileViewModel.selectedImage.value!!
        mobileViewModel.settings.setSupportMultipleWindows(mobileViewModel.selectedWindows.value!!)
        mobileViewModel.settings.javaScriptCanOpenWindowsAutomatically = true
        mobileViewModel.settings.loadWithOverviewMode = true
        mobileViewModel.settings.userAgentString = mobileViewModel.userAgent
        mobileViewModel.goToHome()
        mobileViewModel.webView.webChromeClient = WebChromeClient()

        return mobileViewModel.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            mobileViewModel.webView.destroy()
        } catch (e: Error) {
            Methods.showError(view, e.message, "onDestroy", name)
        }
        _binding = null
    }

}