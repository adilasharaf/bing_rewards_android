package com.adil.bing_one.methods

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.adil.bing_one.R
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

class Methods {
    companion object {
        @JvmStatic
        fun setCountValue(
            dropdown: Spinner?,
            adapter: SpinnerAdapter,
            newValue: String,
            view: View?,
            activity: String
        ) {
            try {
                for (i in 0 until adapter.count) {
                    if (adapter.getItem(i).toString() == newValue) {
                        dropdown?.setSelection(i)
                        break
                    }
                }
            } catch (e: Error) {
                showError(view, e.message, "getWords", activity)

            }
        }

        @JvmStatic
        fun setTimeValue(
            dropdown: Spinner?,
            adapter: SpinnerAdapter,
            newValue: String,
            view: View?,
            activity: String
        ) {
            try {
                for (i in 0 until adapter.count) {
                    if (adapter.getItem(i).toString() == newValue) {
                        dropdown?.setSelection(i)
                        break
                    }
                }
            } catch (e: Error) {
                showError(view, e.message, "getWords", activity)

            }
        }

        @JvmStatic
        fun getWords(
            shuffledList: List<String>,
            count: Int, view: View?, activity: String
        ): List<String> {
            val randomWords = mutableListOf<String>()
            try {
                for (i in 0 until count) {
                    if (i >= shuffledList.size) {
                        break
                    }
                    randomWords.add(shuffledList[i])
                }
            } catch (e: Error) {
                showError(view, e.message, "getWords", activity)
            }
            return randomWords

        }

        @JvmStatic
        fun showError(view: View?, error: String?, method: String, activity: String) {
            try {
                bingLogger("$activity : $method error : $error")
                showSnackBar(view, "$activity : $method error : $error", 2000)
            } catch (e: Error) {
                Log.e("Bing Logger", "showError error : $e")
            }
        }

        @JvmStatic
        fun bingLogger(msg: String) {
            Log.d("Bing Logger", msg)
        }

        @JvmStatic
        fun showSnackBar(view: View?, text: String, duration: Int) {
            try {
                val snackBar: Snackbar? = view?.let {
                    Snackbar.make(
                        it,
                        text,
                        duration
                    )
                }
                snackBar?.show()
            } catch (e: Error) {
                Log.e("Bing Logger", "showSnackBar error : $e")

            }
        }

        @JvmStatic
        fun getWordsFromUrl(
            view: View?, activity: String, count: Int
        ): List<String> {
            var list: MutableList<String> = mutableListOf()
            if (isOnline(view?.context!!)) {
                try {
                    val url = URL("${view.context?.getString(R.string.getWordsUrl)}$count")
                    val connection = url.openConnection() as HttpURLConnection
                    if (isOnline(view.context!!)) {
                        connection.connect()
                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val inputStream = connection.inputStream
                            val gson = Gson()
                            val listType =
                                object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                            list = gson.fromJson(inputStream.bufferedReader(), listType)

                        } else {
                            bingLogger("Failed to get Data the connection in not ok")
                        }
                        connection.disconnect()
                    } else {
                        showError(
                            view, "Not Internet Connection", "FetchData", activity
                        )

                    }
                } catch (e: Error) {
                    showError(view, e.message, "getWordsFromUrl", activity)
                }
            }

            return list
        }

        @JvmStatic
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    bingLogger("Internet Status : NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    bingLogger("Internet Status : NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    bingLogger("Internet Status : NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun getJsCode(currentWord: String): String {
            return """
                            const searchInput = document.getElementById('sb_form_q');
                            const form = searchInput && searchInput.closest('form');
                            const randomX = Math.floor(Math.random() * document.body.scrollWidth);
                            const randomY = Math.floor(
                            Math.random() * Math.max(document.body.scrollHeight, window.innerHeight)
                            );
                            window.scrollTo(randomX, randomY);
                            searchInput.focus();
                            searchInput.value = '';
                            const term = '$currentWord'
                            let i = 0;
                            const interval = setInterval(function () {
                            searchInput.value += term[i];
                            i++;
                            if (i === term.length) {
                            clearInterval(interval);
                            form.submit();
                            }
                            },
                            Math.floor(Math.random() * (200  + 1)) + 100);
                        """.trimIndent()
        }
    }

}