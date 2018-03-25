package com.timtiv.bottomnavigationhelper

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by tim on 2018/3/6.
 */
class TabHelper(savedInstancesState: Bundle?, fragmentManager: FragmentManager, id:
Int, baseList: List<Fragment>) {
    private var manager: FragmentManager = fragmentManager
    private var currentFragment: Fragment? = null
    private lateinit var fragmentStacks: ArrayList<Stack<Fragment>>
    private var containerId = id

    init {
        if (savedInstancesState == null) {
            fragmentStacks = ArrayList(baseList.size)
            for (fragment: Fragment in baseList) {
                val stack = Stack<Fragment>()
                stack.add(fragment)
                fragmentStacks.add(stack)
            }
        } else {
            onRestoreFromBundle(savedInstancesState, baseList)
        }
    }

    enum class Tab {
        Tab1, Tab2, Tab3
    }

    private val extraTagCount = TabHelper::class.java.name + ":extraTagCount"
    private val extraSelectedTabIndex = TabHelper::class.java.name + ":extraSelectedTabIndex"
    private val extraCurrentFragment = TabHelper::class.java.name + ":extraCurrentFragment"
    private val extraFragmentStack = TabHelper::class.java.name + ":extraFragmentStack"

    private var selectedTab = Tab.Tab1
    private var tagCount = 0

    private fun onRestoreFromBundle(savedInstancesState: Bundle, baseList: List<Fragment>) {
        when (savedInstancesState.getInt(extraSelectedTabIndex, -1)) {
            0 -> selectedTab = Tab.Tab1
            1 -> selectedTab = Tab.Tab2
            2 -> selectedTab = Tab.Tab3
        }

        tagCount = savedInstancesState.getInt(extraTagCount, 0)
        currentFragment = manager.findFragmentByTag(savedInstancesState.getString(extraCurrentFragment))
        try {
            val stackArrays = JSONArray(savedInstancesState.getString(extraFragmentStack))
            fragmentStacks = ArrayList(baseList.size)
            for (i in 0..(stackArrays.length())) {
                val stackArray = stackArrays.getJSONArray(i)
                val stack = Stack<Fragment>()

                if (stackArray.length() == 1) {
                    val tag = stackArray.getString(0)
                    val fragment = findFragment(tag, baseList, i)
                    stack.add(fragment)
                } else {
                    for (j in 0..(stackArray.length())) {
                        val tag = stackArray.getString(j)
                        val fragment = findFragment(tag, baseList, j)
                        stack.add(fragment)
                    }
                }

                fragmentStacks.add(stack)
            }
        } catch (t: Throwable) {
            fragmentStacks = ArrayList(baseList.size)
            for (fragment: Fragment in baseList) {
                val stack = Stack<Fragment>()
                stack.add(fragment)
                fragmentStacks.add(stack)
            }
        }
    }

    private fun findFragment(tag: String, baseList: List<Fragment>, i: Int): Fragment {
        return when ("null".equals(tag, true)) {
            true -> baseList[i]
            false -> manager.findFragmentByTag(tag)
        }
    }

    private fun getTabIndex(tab: Tab): Int {
        return when (tab) {
            Tab.Tab1 -> 0
            Tab.Tab2 -> 1
            Tab.Tab3 -> 2
        }
    }

    fun switchTab(tab: Tab) {
        val index = getTabIndex(tab)
        when {
            index >= fragmentStacks.size -> throw IndexOutOfBoundsException("Can't switch to a tab that hasn't been initialized, Index : " + index + ", current stack size : " + fragmentStacks.size + ". Make sure to create all of the tabs you need in the Constructor")
            index == fragmentStacks.size -> throw IndexOutOfBoundsException("Can't switch to a tab that hasn't been initialized, Index : " + index + ", current stack size : " + fragmentStacks.size + ". Make sure to create all of the tabs you need in the Constructor")
        }

        val fragmentStack = fragmentStacks.get(index)
        if (fragmentStack.isEmpty()) {
            return
        }

        if (selectedTab != tab) {
            selectedTab = tab

            val ft = manager.beginTransaction()
            detachCurrentFragment(ft)

            var fragment = reattachPreviousFragment(ft)
            if (fragment != null) {
                ft.commit()
            } else {
                fragment = fragmentStacks.get(getTabIndex(selectedTab)).peek()
                ft.add(containerId, fragment, generateTag(fragment))
                ft.commit()
            }

            currentFragment = fragment
        }
    }

    private fun detachCurrentFragment(ft: FragmentTransaction) {
        val oldFrag = getCurrentFrag()
        if (oldFrag != null) {
            ft.detach(oldFrag)
        }
    }

    private fun getCurrentFrag(): Fragment? {
        if (currentFragment != null) return currentFragment
        val fragmentStack = fragmentStacks[getTabIndex(selectedTab)]
        return when {
            !fragmentStack.isEmpty() -> manager.findFragmentByTag(fragmentStacks[getTabIndex(selectedTab)].peek().tag)
            else -> null
        }
    }

    private fun reattachPreviousFragment(ft: FragmentTransaction): Fragment? {
        val fragmentStack = fragmentStacks[getTabIndex(selectedTab)]
        if (!fragmentStack.isEmpty()) {
            val frag = manager.findFragmentByTag(fragmentStack.peek().tag)
            if (frag != null) ft.attach(frag)
            return frag
        }
        return null
    }

    private fun generateTag(fragment: Fragment): String {
        return fragment::class.java.name + selectedTab
    }

    fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(extraTagCount, tagCount)
        outState?.putInt(extraSelectedTabIndex, getTabIndex(selectedTab))
        if (currentFragment != null) {
            outState?.putString(extraCurrentFragment, currentFragment?.tag)
        }

        try {
            val stackArrays = JSONArray()
            for (stack in fragmentStacks) {
                val stackArray = JSONArray()
                for (fragment in stack) stackArray.put(fragment.tag)
                stackArrays.put(stackArray)
            }
            outState?.putString(extraFragmentStack, stackArrays.toString())
        } catch (t: Throwable) {

        }
    }

    fun getCurrentStack(): Stack<Fragment> {
        return fragmentStacks[getTabIndex(selectedTab)]
    }

    private fun pop(popToFirst: Boolean) {
        val poppingFrag = getCurrentFrag()
        if (poppingFrag != null) {
            val ft = manager.beginTransaction()
            val fragmentStack = fragmentStacks[getTabIndex(selectedTab)]

            when (popToFirst) {
                true ->
                    when (fragmentStack.size) {
                        1 -> return
                        else ->
                            for (i in 0 until fragmentStack.size - 1) {
                                fragmentStack.pop()
                            }
                    }
                false ->
                    if (!fragmentStack.isEmpty()) fragmentStack.pop()
            }


            val fragment = reattachPreviousFragment(ft)
            if (fragment == null && !fragmentStack.isEmpty()) {
                val frag = fragmentStack.peek()
                ft.add(containerId, frag, frag.tag)
            }

            ft.remove(poppingFrag)

            ft.commit()
            manager.executePendingTransactions()
            currentFragment = fragment
        }
    }

    fun pop() {
        pop(false)
    }

    fun push(fragment: Fragment) {
        val ft = manager.beginTransaction()
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        ft.add(containerId, fragment, generateTag(fragment))
        detachCurrentFragment(ft)
        ft.commit()

        manager.executePendingTransactions()
        fragmentStacks[getTabIndex(selectedTab)].push(fragment)

        currentFragment = fragment
    }

    fun popToFirst() {
        pop(true)
    }
}