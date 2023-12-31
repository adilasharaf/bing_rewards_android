package com.adil.bing_one.ui.mobile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.adil.bing_one.R
import com.adil.bing_one.app.Values
import com.adil.bing_one.databinding.FragmentMobileBinding
import com.adil.bing_one.methods.Methods
import com.google.android.material.bottomsheet.BottomSheetDialog

class MobileFragment : Fragment() {
    companion object {
        @JvmStatic
        var name: String = "Mobile Fragment"

    }

    private var _binding: FragmentMobileBinding? = null
    private lateinit var mobileViewModel: MobileViewModel;

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


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
        mobileViewModel._selectedTime.apply {
            value = mobileViewModel.sharedPref.getLong(
                getString(R.string.selected_mobile_time),
                3
            )
        }
        mobileViewModel._selectedCount.apply {
            value = mobileViewModel.sharedPref.getInt(
                getString(R.string.selected_mobile_count),
                30
            )
        }
        mobileViewModel.userAgent = mobileViewModel.sharedPref.getString(
            getString(R.string.user_agent_mobile), getString(R.string.mobileEdge)
        ) ?: ""
        mobileViewModel._wordsList.apply { value = listOf<String>() }
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
        mobileViewModel.setCountAndTme()
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
            mobileViewModel.currentTimeText.text = it.toString()
            mobileViewModel.sharedEditor.putLong(getString(R.string.selected_mobile_time), it)
            mobileViewModel.sharedEditor.commit()
        }

        mobileViewModel.selectedCount.observe(viewLifecycleOwner) {
            mobileViewModel.sharedEditor.putInt(getString(R.string.selected_mobile_count), it)
            mobileViewModel.sharedEditor.commit()
        }

        mobileViewModel.currentCount.observe(viewLifecycleOwner) {
            mobileViewModel.currentCountText.text = it.toString()
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
            mobileViewModel.resetApi()
            true
        }

        // app-bar
        mobileViewModel.homeIcon.setOnClickListener {
            mobileViewModel.goToHome()
        }

        mobileViewModel.reloadIcon.setOnClickListener {
            mobileViewModel.reload()
        }

        mobileViewModel.settingsIcon.setOnClickListener {
            showBottomSheet()
        }
        // web-view
        mobileViewModel.settings = mobileViewModel.webView.settings
        mobileViewModel.webView.webViewClient = mobileViewModel.getClient()
        mobileViewModel.settings.javaScriptEnabled = true
        mobileViewModel.settings.setSupportMultipleWindows(true)
        mobileViewModel.settings.setGeolocationEnabled(false)
        mobileViewModel.settings.javaScriptCanOpenWindowsAutomatically = true
        mobileViewModel.settings.setSupportMultipleWindows(true)
        mobileViewModel.settings.setGeolocationEnabled(false)
        mobileViewModel.settings.loadWithOverviewMode = true
        mobileViewModel.settings.domStorageEnabled = true
        mobileViewModel.settings.userAgentString = mobileViewModel.userAgent



        mobileViewModel.loadUrl(getString(R.string.bingHomeUrl))

        mobileViewModel.webView.webViewClient = mobileViewModel.getClient()
        mobileViewModel.webView.webChromeClient = WebChromeClient()


        return mobileViewModel.root
    }

    private fun showBottomSheet() {
        try {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)
            bottomSheetDialog.behavior.isDraggable = false
            mobileViewModel.dropDownCount = bottomSheetDialog.findViewById(R.id.dropdown_count)
            mobileViewModel.dropDownTime = bottomSheetDialog.findViewById(R.id.dropdown_time)
            mobileViewModel.uaEditText = bottomSheetDialog.findViewById(R.id.user_agent)
            mobileViewModel.saveButton = bottomSheetDialog.findViewById(R.id.save_button)
            mobileViewModel.closeButton = bottomSheetDialog.findViewById(R.id.close_button)

            // drop-down-count
            mobileViewModel.adapterCount = ArrayAdapter(
                requireContext(), R.layout.spinner_item, Values.dropDownCountItems
            )
            mobileViewModel.dropDownCount?.adapter = mobileViewModel.adapterCount
            mobileViewModel.dropDownCount?.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        mobileViewModel._selectedCount.apply {
                            value = parent.getItemAtPosition(position).toString().toInt()
                        }
                        Methods.setCountValue(
                            mobileViewModel.dropDownCount,
                            mobileViewModel.adapterCount,
                            parent.getItemAtPosition(position).toString(),
                            view,
                            name
                        )
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Handle case when nothing is selected
                        Methods.bingLogger("Nothing selected In dropDownCount Spinner $name")
                    }
                }

            // drop-down-time
            mobileViewModel.adapterTime = ArrayAdapter(
                requireContext(), R.layout.spinner_item, Values.dropDownTimeItems
            )

            mobileViewModel.dropDownTime?.adapter = mobileViewModel.adapterTime
            mobileViewModel.dropDownTime?.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        mobileViewModel._selectedTime.apply {
                            value = parent.getItemAtPosition(position).toString().toLong()
                        }
                        Methods.setTimeValue(
                            mobileViewModel.dropDownTime,
                            mobileViewModel.adapterTime,
                            parent.getItemAtPosition(position).toString(),
                            view,
                            name
                        )
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Handle case when nothing is selected
                        Methods.bingLogger("Nothing selected In dropDownTime Spinner $name")
                    }
                }
            //    user-agent-edit-text
            mobileViewModel.uaEditText?.setText(mobileViewModel.settings.userAgentString)
            Methods.setCountValue(
                mobileViewModel.dropDownCount,
                mobileViewModel.adapterCount,
                mobileViewModel.selectedCount.toString(),
                view,
                name
            )
            Methods.setTimeValue(
                mobileViewModel.dropDownTime,
                mobileViewModel.adapterCount,
                mobileViewModel.selectedTime.toString(),
                view,
                name
            )

            mobileViewModel.saveButton?.setOnClickListener {
                mobileViewModel.saveSettings(bottomSheetDialog)
            }
            mobileViewModel.closeButton?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()

        } catch (e: Error) {
            Methods.showError(view, e, "showBottomSheet", name)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            mobileViewModel.webView.destroy()
        } catch (e: Error) {
            Methods.showError(view, e, "onDestroy", name)
        }
        _binding = null
    }

}