package com.adil.bing_one.ui.mobile

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adil.bing_one.R
import com.adil.bing_one.app.Values
import com.adil.bing_one.methods.Methods
import com.adil.bing_one.ui.mobile.MobileFragment.Companion.name
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
class MobileViewModel : ViewModel() {
    //    views
    lateinit var root: View
    lateinit var webView: WebView
    var dropDownCount: Spinner? = null
    var dropDownTime: Spinner? = null
    private var switchImage: SwitchMaterial? = null
    private var switchWindows: SwitchMaterial? = null

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
    private var uaEditText: TextInputEditText? = null
    private var closeButton: ImageView? = null

    //    observables
    val mWordsList = MutableLiveData<List<String>>()
    private val mCurrentCount = MutableLiveData<Int>().apply { value = 0 }
    private val mIsApiRunning = MutableLiveData<Boolean>().apply { value = false }
    private val mIsRewards = MutableLiveData<Boolean>().apply { value = false }
    val mSelectedTime = MutableLiveData<Long>().apply { value = 0 }
    val mSelectedCount = MutableLiveData<Int>().apply { value = 0 }
    val mSelectedImage = MutableLiveData<Boolean>().apply { value = false }
    val mSelectedWindows = MutableLiveData<Boolean>().apply { value = false }

    // values
    val wordsList: LiveData<List<String>> = mWordsList
    val currentCount: LiveData<Int> = mCurrentCount
    val isApiRunning: LiveData<Boolean> = mIsApiRunning
    val selectedTime: LiveData<Long> = mSelectedTime
    val selectedCount: LiveData<Int> = mSelectedCount
    val selectedImage: LiveData<Boolean> = mSelectedImage
    val selectedWindows: LiveData<Boolean> = mSelectedWindows
    val isRewards: LiveData<Boolean> = mIsRewards

    var userAgent: String = ""
    private var currentWord: String = ""

    //    late-init
    lateinit var sharedPref: SharedPreferences
    lateinit var sharedEditor: SharedPreferences.Editor
    lateinit var adapterCount: SpinnerAdapter
    lateinit var adapterTime: SpinnerAdapter
    lateinit var timer: CountDownTimer
    lateinit var settings: WebSettings

    fun setCount(lifecycleScope: LifecycleCoroutineScope) {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                val list: List<String> = Methods.getWordsFromUrl(
                    root, name, selectedCount.value!!
                )
                withContext(Dispatchers.Main) {
                    Methods.bingLogger("List size : ${list.size} , list: $list in $name")
                    val shuffledList: List<String> = if (list.size >= selectedCount.value!!) {
                        list.shuffled()
                    } else {
                        Values.wordsList.shuffled()
                    }
                    mWordsList.apply {
                        value = Methods.getWords(
                            shuffledList, selectedCount.value!!, root, name
                        )
                    }
                }
            }
        } catch (e: Error) {
            Methods.showError(root, e.message, "changeCount", name)
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
            mIsApiRunning.apply { value = isLoading }
            if (isApiRunning.value!!) {
                api()
            }
            Methods.bingLogger("changed $name ApiState")
        } catch (e: Error) {
            Methods.showError(root, e.message, "changeApiState", name)

        }
    }

    fun changePageLoadState() {
        try {
            if (wordsList.value!!.isNotEmpty() && isApiRunning.value!!) {
                timer.start()
            }
            Methods.bingLogger("changePageLoadState Calling in $name")
        } catch (e: Error) {
            Methods.showError(root, e.message, "changePageLoadState", name)

        }
    }

    fun api() {
        mIsRewards.apply { value = false }
        if (Methods.isOnline(root.context)) {
            try {
                Methods.bingLogger("Api Calling in $name")
                if (wordsList.value?.count() != currentCount.value) {
                    if (currentWord != wordsList.value?.elementAt(currentCount.value!!)) {
                        currentWord = wordsList.value?.elementAt(currentCount.value!!).toString()
                        if (webView.url?.contains(root.context.getString(R.string.bingSearchUrl)) == true) {

                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed({
                                webView.evaluateJavascript(Methods.getJsCode(currentWord)) { r ->
                                    if (r == null || r == "null") {
                                        handler.postDelayed({
                                            webView.reload()
                                            changePageLoadState()
                                        }, 3000)
                                        Methods.bingLogger(
                                            "WebView Js Callback : $r"
                                        )
                                    }

                                }
                            }, 3000)

                        } else {
                            loadUrl(
                                "${root.context.getString(R.string.bingSearchUrl)}$currentWord"
                            )
                            Methods.bingLogger("$name Not in Bing Search")
                        }
                        mCurrentCount.apply { value = value!! + 1 }
                    } else {
                        Methods.bingLogger("Returned in $name")
                    }
                } else {
                    Methods.bingLogger("Words list Value is equal to current count in $name")
                }
            } catch (e: Error) {
                Methods.showError(root, e.message, "api", name)
            }
        } else {
            if (Methods.isOnline(root.context)) {
                webView.reload()
                Methods.bingLogger(root.context.getString(R.string.connection_retained))
            } else {
                Methods.bingLogger(root.context.getString(R.string.no_connection))
            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                changePageLoadState()
            }, 3000)
        }

    }

    private fun loadUrl(url: String) {
        try {
            webView.loadUrl(url)
            Methods.bingLogger("Loaded Url : $url")
        } catch (e: Error) {
            Methods.showError(root, e.message, "loadUrl", name)

        }
    }

    fun goToRewardsHome() {
        try {
            mIsRewards.apply { value = true }
            changeApiState(false)
            loadUrl(root.context.getString(R.string.bingRewardsHome))
        } catch (e: Error) {
            Methods.showError(root, e.message, "goToRewardsHome", name)
        }
    }

    fun goToHome() {
        try {
            mIsRewards.apply { value = false }
            loadUrl(root.context.getString(R.string.bingHomeUrl))
        } catch (e: Error) {
            Methods.showError(root, e.message, "goToHome", name)
        }
    }

    fun resetApi(lifecycleScope: LifecycleCoroutineScope) {
        try {
            setCount(lifecycleScope)
            timer.cancel()
            changeApiState(false)
            mCurrentCount.apply { value = 0 }
        } catch (e: Error) {
            Methods.showError(root, e.message, "resetApi", name)
        }
    }

    fun reload() {
        try {
            changeApiState(false)
            webView.reload()
        } catch (e: Error) {
            Methods.showError(root, e.message, "reload", name)

        }
    }

    fun getClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                try {
                    changePageLoadState()
                } catch (e: Error) {
                    Methods.showError(view, e.message, "onPageCommitVisible", name)
                }
            }

            override fun onReceivedHttpError(
                view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
            ) {
                Methods.showError(
                    view,
                    "Status Code : ${errorResponse?.statusCode} , Message : $errorResponse ",
                    "getClient",
                    name
                )
                changePageLoadState()
                super.onReceivedHttpError(view, request, errorResponse)
            }
        }

    }


    private fun changeUserAgent() {
        try {
            userAgent = uaEditText?.text.toString()
            if (settings.userAgentString != userAgent) {
                settings.userAgentString = userAgent
                Methods.bingLogger("New UserAgent in $name : $userAgent")
                sharedEditor.putString(R.string.user_agent_mobile.toString(), userAgent)
                sharedEditor.commit()
            } else {
                Methods.bingLogger("No change in $name User Agent : $userAgent")
            }

        } catch (e: Error) {
            Methods.showError(root, e.message, "changeUserAgent", name)
        }

    }

    fun showBottomSheet() {
        try {
            val bottomSheetDialog = BottomSheetDialog(root.context)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)
            bottomSheetDialog.behavior.isDraggable = false
            dropDownCount = bottomSheetDialog.findViewById(R.id.dropdown_count)
            dropDownTime = bottomSheetDialog.findViewById(R.id.dropdown_time)
            switchImage = bottomSheetDialog.findViewById(R.id.switch_image)
            switchWindows = bottomSheetDialog.findViewById(R.id.switch_windows)
            uaEditText = bottomSheetDialog.findViewById(R.id.user_agent)
            closeButton = bottomSheetDialog.findViewById(R.id.close_button)

            // drop-down-count
            adapterCount = ArrayAdapter(
                root.context, R.layout.spinner_item, Values.dropDownCountItems
            )
            dropDownCount?.adapter = adapterCount
            dropDownCount?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    mSelectedCount.apply {
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
            dropDownTime?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    mSelectedTime.apply {
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
            // selected-image
            switchImage?.setOnCheckedChangeListener { _, isChecked ->
                mSelectedImage.apply {
                    value = isChecked
                }
            }
            // selected-windows
            switchWindows?.setOnCheckedChangeListener { _, isChecked ->
                mSelectedWindows.apply {
                    value = isChecked
                }
            }
            //    user-agent-edit-text
            uaEditText?.setText(settings.userAgentString)
            uaEditText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                    changeUserAgent()
                }
            })

            Methods.setCountValue(
                dropDownCount, adapterCount, selectedCount.value.toString(), root, name
            )
            Methods.setTimeValue(
                dropDownTime, adapterCount, selectedTime.value.toString(), root, name
            )

            switchImage?.isChecked = selectedImage.value!!
            switchWindows?.isChecked = selectedWindows.value!!

            closeButton?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()

        } catch (e: Error) {
            Methods.showError(root, e.message, "showBottomSheet", name)
        }

    }


}