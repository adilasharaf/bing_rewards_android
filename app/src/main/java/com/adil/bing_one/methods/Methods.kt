package com.adil.bing_one.methods

import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.google.android.material.snackbar.Snackbar

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
                showError(view, e, "getWords", activity)

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
                showError(view, e, "getWords", activity)

            }
        }

        @JvmStatic
        fun getWords(
            wordsList: List<String>,
            count: Int,
            view: View?,
            activity: String
        ): List<String> {
            val shuffledList = wordsList.shuffled()
            val randomWords = mutableListOf<String>()
            try {
                for (i in 0 until count) {
                    if (i >= shuffledList.size) {
                        break
                    }
                    randomWords.add(shuffledList[i])
                }
            } catch (e: Error) {
                showError(view, e, "getWords", activity)
            }
            return randomWords

        }

        @JvmStatic
        fun showError(view: View?, e: Error, method: String, activity: String) {
            try {
                bingLogger("$activity Fragment : $method error : $e")
                showSnackBar(view, "$activity Fragment : $method error : $e", 2000)
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

    }
}