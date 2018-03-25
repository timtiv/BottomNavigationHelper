package com.ecloudmobile.ecloudinvoicekotlin

import android.content.Context
import android.support.v4.app.Fragment

/**
 * Created by tim on 2018/3/6.
 */
open class BaseFragment : Fragment() {
    lateinit var fragmentNavigation: FragmentNavigation

    interface FragmentNavigation {
        fun popFragment()
        fun pushFragment(fragment: Fragment)
        fun popToFirst()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FragmentNavigation) {
            fragmentNavigation = context
        }
    }

    protected fun setTitle(title: String) {
        if (activity is MainContainerActivity) activity.title = title
    }
}