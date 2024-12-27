package com.adil.bing_one

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.adil.bing_one.databinding.ActivityMainBinding
import com.adil.bing_one.methods.Methods
import com.adil.bing_one.ui.desktop.DesktopFragment
import com.adil.bing_one.ui.desktop.DesktopFragment.Companion.desktopViewModel
import com.adil.bing_one.ui.mobile.MobileFragment
import com.adil.bing_one.ui.mobile.MobileFragment.Companion.mobileViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var fragment1: Fragment = MobileFragment()
    private var fragment2: Fragment = DesktopFragment()
    private lateinit var currentFragment: Fragment

    private var fm: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fm.beginTransaction().add(R.id.nav_controller, fragment2, "2").hide(fragment2).commit()
        fm.beginTransaction().add(R.id.nav_controller, fragment1, "1").commit()
        currentFragment = fragment1

        //navigation
        binding.bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_mobile -> {
                    replaceFragment(fragment1)
                }

                R.id.navigation_desktop -> {
                    replaceFragment(fragment2)
                }

                else -> {
                    Methods.bingLogger("No match found on Bottom Navigation Item")
                }
            }
            true
        }

        //backPress
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (currentFragment == fragment1) {
                        Methods.bingLogger("${MobileFragment.name} Is Reward : ${mobileViewModel.isRewards.value}")
                        if (mobileViewModel.isRewards.value == true) {
                            if (mobileViewModel.webView.canGoBack()) {
                                mobileViewModel.webView.goBack()
                            } else {
                                mobileViewModel.goToHome()
                            }
                        }
                        Methods.bingLogger("Called OnBackPressed in ${MobileFragment.name}")
                    } else if (currentFragment == fragment2) {
                        Methods.bingLogger("${DesktopFragment.name} Is Reward : ${desktopViewModel.isRewards.value}")
                        if (desktopViewModel.isRewards.value == true) {
                            if (desktopViewModel.webView.canGoBack()) {
                                desktopViewModel.webView.goBack()
                            } else {
                                desktopViewModel.goToHome()
                            }
                        }
                        Methods.bingLogger("Called OnBackPressed in ${DesktopFragment.name}")
                    }

                }
            }
        )


    }

    private fun replaceFragment(fragment: Fragment) {
        try {
            when (fragment) {
                fragment1 -> {
                    fm.beginTransaction().hide(fragment2).show(fragment1).commit()
                    currentFragment = fragment1
                }

                fragment2 -> {
                    fm.beginTransaction().hide(fragment1).show(fragment2).commit()
                    currentFragment = fragment2

                }

                else -> {
                    Methods.bingLogger("No match found on Fragment")
                }
            }
        } catch (e: Error) {
            Methods.showError(
                binding.root, e.message, "replaceFragment", getString(R.string.main_activity)
            )
        }
    }
}