package com.ecloudmobile.ecloudinvoicekotlin

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.timtiv.bottomnavigationhelper.TabHelper
import kotlinx.android.synthetic.main.activity_main_container.*

class MainContainerActivity : AppCompatActivity(), BaseFragment.FragmentNavigation {
    override fun popFragment() {
        when {
            tabHelper.getCurrentStack().size > 1 -> tabHelper.pop()
            else -> finish()
        }
    }

    override fun pushFragment(fragment: Fragment) {
        tabHelper.push(fragment)
    }

    override fun popToFirst() {
        tabHelper.popToFirst()
    }

    private lateinit var tabHelper: TabHelper
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                tabHelper.switchTab(TabHelper.Tab.Tab1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                tabHelper.switchTab(TabHelper.Tab.Tab2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                tabHelper.switchTab(TabHelper.Tab.Tab3)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val mOnNavigationItemReSelectedListener = BottomNavigationView.OnNavigationItemReselectedListener {
        tabHelper.popToFirst()
        return@OnNavigationItemReselectedListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)
        val baseFragments = ArrayList<Fragment>()
        baseFragments.add(Tab1Fragment())
        baseFragments.add(Tab2Fragment())
        baseFragments.add(Tab3Fragment())

        tabHelper = TabHelper(savedInstanceState, supportFragmentManager, R.id.mainConatiner, baseFragments)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.setOnNavigationItemReselectedListener(mOnNavigationItemReSelectedListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        tabHelper.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        popFragment()
    }
}
