package com.adil.bing_one.ui.desktop

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adil.bing_one.R
import com.adil.bing_one.app.Values
import com.adil.bing_one.methods.Methods
import com.adil.bing_one.ui.desktop.DesktopFragment.Companion.name
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText

@SuppressLint("StaticFieldLeak")
class DesktopViewModel : ViewModel() {
    //    views
    lateinit var root: View
    lateinit var webView: WebView
    var dropDownCount: Spinner? = null
    var dropDownTime: Spinner? = null

    //    app-bar
    lateinit var homeIcon: ImageView
    lateinit var reloadIcon: ImageView
    lateinit var settingsIcon: ImageView


    //    state-layout
    lateinit var autoSearch: LinearLayout
    lateinit var currentStateText: TextView
    lateinit var currentStateIcon: ImageView

    //    count-layout
    lateinit var countLayout: LinearLayout
    lateinit var currentCountText: TextView
    lateinit var totalCountText: TextView

    //    time-layout
    lateinit var currentTimeText: TextView

    //    bottom_panel
    private var saveButton: LinearLayout? = null
    private var uaEditText: TextInputEditText? = null
    private var closeButton: ImageView? = null

    //    observables
    val _wordsList = MutableLiveData<List<String>>()
    val _currentCount = MutableLiveData<Int>().apply { value = 0 }
    val _isApiRunning = MutableLiveData<Boolean>().apply { value = false }
    val _selectedTime = MutableLiveData<Long>().apply { value = 0 }
    val _selectedCount = MutableLiveData<Int>().apply { value = 0 }

    // values
    val wordsList: LiveData<List<String>> = _wordsList
    val currentCount: LiveData<Int> = _currentCount
    val isApiRunning: LiveData<Boolean> = _isApiRunning
    val selectedTime: LiveData<Long> = _selectedTime
    val selectedCount: LiveData<Int> = _selectedCount
    var userAgent: String = ""
    private var currentWord: String = ""

    //    late-init
    lateinit var sharedPref: SharedPreferences
    lateinit var sharedEditor: SharedPreferences.Editor
    lateinit var adapterCount: SpinnerAdapter
    lateinit var adapterTime: SpinnerAdapter
    lateinit var timer: CountDownTimer
    lateinit var settings: WebSettings

    fun setCountAndTme() {
        _wordsList.apply {
            value = Methods.getWords(
                Values.wordsList, selectedCount.value!!, root,
                name
            )
        }
    }

    fun changeCount() {
        try {
            _wordsList.apply {
                value = Methods.getWords(
                    Values.wordsList,
                    selectedCount.value!!,
                    root,
                    name
                )
            }
        } catch (e: Error) {
            Methods.showError(root, e, "changeCount", name)
        }
    }

    fun configTimer() {
        if (isApiRunning.value!!) {
            timer.cancel()
        }
        timer = object : CountDownTimer(selectedTime.value!! * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                Methods.bingLogger("Timer Finished in $name")
                timer.cancel()
                api()
            }
        }
        if (isApiRunning.value!!) {
            timer.start()
        }
        Methods.bingLogger("Timer config changed in $name ")
    }

    fun changeApiState(isLoading: Boolean) {
        try {
            _isApiRunning.apply { value = isLoading }
            if (isApiRunning.value!!) {
                api()
            }
            Methods.bingLogger("changed $name ApiState")
        } catch (e: Error) {
            Methods.showError(root, e, "changeApiState", name)

        }
    }

    fun changePageLoadState() {
        try {
            if (wordsList.value!!.isNotEmpty() && isApiRunning.value!!) {
                timer.start()
            }
            Methods.bingLogger("changePageLoadState Calling in $name")
        } catch (e: Error) {
            Methods.showError(root, e, "changePageLoadState", name)

        }
    }

    fun api() {
        try {
            Methods.bingLogger("Api Calling in $name")
            if (wordsList.value?.count() != currentCount.value) {
                if (currentWord != wordsList.value?.elementAt(currentCount.value!!)) {
                    currentWord = wordsList.value?.elementAt(currentCount.value!!).toString()
                    if (webView.url!!.contains(root.context.getString(R.string.bingSearchUrl))) {
                        webView.evaluateJavascript(
                            "document.getElementById('sb_form_q').focus()'"
                        ) {r-> Methods.bingLogger(r)}
                        webView.evaluateJavascript(
                            "document.getElementById('sb_form_q').value='$currentWord'"
                        ) {}
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            webView.evaluateJavascript(
                                "document.getElementById('sb_form').submit()"
                            ) {}
                        }, 3000)
                    } else {
                        loadUrl(
                            "${root.context.getString(R.string.bingSearchUrl)}$currentWord"
                        )
                        Methods.bingLogger("$name Not in Bing Search")
                    }
                    _currentCount.apply { value = value!! + 1 }
                } else {
                    Methods.bingLogger("Returned in $name")
                }
            }
        } catch (e: Error) {
            Methods.showError(root, e, "api", name)
        }
    }

    fun loadUrl(url: String) {
        try {
            webView.loadUrl(url)
            Methods.bingLogger("current $name UserAgent : ${webView.settings.userAgentString}")
            Methods.bingLogger("Loaded Url")
        } catch (e: Error) {
            Methods.showError(root, e, "loadUrl", name)

        }
    }

    fun goToHome() {
        try {
            changeApiState(false)
            loadUrl(root.context.getString(R.string.bingRewardsHome))
        } catch (e: Error) {
            Methods.showError(root, e, "goToHome", name)
        }
    }

    fun resetApi() {
        try {
            timer.cancel()
            changeApiState(false)
            _currentCount.apply { value = 0 }
        } catch (e: Error) {
            Methods.showError(root, e, "resetApi", name)
        }
    }

    fun reload() {
        try {
            changeApiState(false)
            webView.reload()
        } catch (e: Error) {
            Methods.showError(root, e, "reload", name)

        }
    }

    fun getClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                try {
                    changePageLoadState()
                } catch (e: Error) {
                    Methods.showError(view, e, "onPageCommitVisible", name)
                }
            }
        }

    }


    private fun changeUserAgent() {
        try {
            userAgent = uaEditText?.text.toString()
            if (settings.userAgentString != userAgent) {
                settings.userAgentString = userAgent
                Methods.bingLogger("New UserAgent in $name : $userAgent")
                sharedEditor.putString(R.string.user_agent_desktop.toString(), userAgent)
                sharedEditor.commit()
                reload()
            } else {
                Methods.bingLogger("No change in $name User Agent : $userAgent")
            }

        } catch (e: Error) {
            Methods.showError(root, e, "changeUserAgent", name)
        }

    }

    private fun saveSettings(bottomSheetDialog: BottomSheetDialog) {
        configTimer()
        changeUserAgent()
        bottomSheetDialog.dismiss()
        bottomSheetDialog.dismiss()
    }

    fun showBottomSheet() {
        try {
            val bottomSheetDialog = BottomSheetDialog(root.context)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)
            bottomSheetDialog.behavior.isDraggable = false
            dropDownCount = bottomSheetDialog.findViewById(R.id.dropdown_count)
            dropDownTime = bottomSheetDialog.findViewById(R.id.dropdown_time)
            uaEditText = bottomSheetDialog.findViewById(R.id.user_agent)
            saveButton = bottomSheetDialog.findViewById(R.id.save_button)
            closeButton = bottomSheetDialog.findViewById(R.id.close_button)

            // drop-down-count
            adapterCount = ArrayAdapter(
                root.context, R.layout.spinner_item, Values.dropDownCountItems
            )
            dropDownCount?.adapter = adapterCount
            dropDownCount?.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        _selectedCount.apply {
                            value = parent.getItemAtPosition(position).toString().toInt()
                        }
                        Methods.setCountValue(
                            dropDownCount,
                            adapterCount,
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
            adapterTime = ArrayAdapter(
                root.context, R.layout.spinner_item, Values.dropDownTimeItems
            )

            dropDownTime?.adapter = adapterTime
            dropDownTime?.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        _selectedTime.apply {
                            value = parent.getItemAtPosition(position).toString().toLong()
                        }
                        Methods.setTimeValue(
                            dropDownTime,
                            adapterTime,
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
            uaEditText?.setText(settings.userAgentString)
            Methods.setCountValue(
                dropDownCount,
                adapterCount,
                selectedCount.value.toString(),
                root,
                name
            )
            Methods.setTimeValue(
                dropDownTime,
                adapterCount,
                selectedTime.value.toString(),
                root,
                name
            )

            saveButton?.setOnClickListener {
                saveSettings(bottomSheetDialog)
            }
            closeButton?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()

        } catch (e: Error) {
            Methods.showError(root, e, "showBottomSheet", name)
        }

    }
}