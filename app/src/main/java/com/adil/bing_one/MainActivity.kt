package com.adil.bing_one

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.adil.bing_one.databinding.ActivityMainBinding
import com.adil.bing_one.methods.Methods
import com.adil.bing_one.ui.desktop.DesktopFragment
import com.adil.bing_one.ui.mobile.MobileFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var fragment1: Fragment = MobileFragment()
    private var fragment2: Fragment = DesktopFragment()
    private var fm: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fm.beginTransaction().add(R.id.nav_controller, fragment2, "2").hide(fragment2).commit()
        fm.beginTransaction().add(R.id.nav_controller, fragment1, "1").commit()
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
    }

    private fun replaceFragment(fragment: Fragment) {
        try {
            when (fragment) {
                fragment1 -> {
                    fm.beginTransaction().hide(fragment2).show(fragment1).commit()
                }
                fragment2 -> {
                    fm.beginTransaction().hide(fragment1).show(fragment2).commit()
                }
                else -> {
                    Methods.bingLogger("No match found on Fragment")
                }
            }
        } catch (e: Error) {
            Methods.showError(binding.root, e, "replaceFragment", getString(R.string.main_activity))
        }
    }
}