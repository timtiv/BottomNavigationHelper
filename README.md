#BottomNavigationHelper
BottomNavigationHelper is a support library which helps android developer use fragments in [BottomNavigationView](https://developer.android.com/reference/android/support/design/widget/BottomNavigationView.html).

The library include fragment transaction, custom animation, and status storage.

## Installation

## How To Start
1. First, You need to initialize a list of baseFragments
```kotlin
    val baseFragments = ArrayList<Fragment>()
    baseFragments.add(Tab1Fragment())
    baseFragments.add(Tab2Fragment())
    baseFragments.add(Tab3Fragment())
```
2. Initialize TabHelper with **SavedInstanceState**,**SupportFragmentManager**,**FrameLayout Resource**,and **BaseFragments**.
```kotlin
    tabHelper = TabHelper(savedInstanceState, supportFragmentManager, R.id.mainConatiner, baseFragments)
```
3. Implements **FragmentNavigation**
```kotlin
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
}
```