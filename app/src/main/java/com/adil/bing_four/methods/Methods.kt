package com.adil.bing_four.methods

import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

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

        @JvmStatic
        fun randomDelay(minDelay: Long, maxDelay: Long): Long {
            if (minDelay > maxDelay) {
                throw IllegalArgumentException("minDelay must be less than or equal to maxDelay")
            }
            val random = Random(System.currentTimeMillis()) // Seed for more randomness
            return random.nextLong(maxDelay - minDelay + 1) + minDelay
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
                            Math.floor(Math.random() * (200 - 100 + 1)) + 100);
                        """.trimIndent()
        }
    }
}