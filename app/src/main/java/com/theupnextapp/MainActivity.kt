package com.theupnextapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.common.utils.FeedBackStatus
import com.theupnextapp.common.utils.Feedback
import com.theupnextapp.common.utils.customTab.CustomTabComponent
import com.theupnextapp.common.utils.customTab.TabConnectionCallback
import com.theupnextapp.common.utils.customTab.WebviewFallback
import com.theupnextapp.domain.TraktConnectionArg
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TabConnectionCallback {

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    private var _bottomNavigationView: BottomNavigationView? = null
    private val bottomNavigationView get() = _bottomNavigationView

    private var _toolbar: Toolbar? = null
    private val toolbar get() = _toolbar

    private var _container: ConstraintLayout? = null
    private val container get() = _container

    private lateinit var snackbar: Snackbar

    private lateinit var customTabComponent: CustomTabComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _toolbar = findViewById(R.id.toolbar)
        _bottomNavigationView = findViewById(R.id.bottom_navigation)
        _container = findViewById(R.id.container)

        customTabComponent = CustomTabComponent()
        customTabComponent.setConnectionCallback(this)

        setSupportActionBar(toolbar)

        val appBarConfiguration = AppBarConfiguration
            .Builder(
                R.id.splashScreenFragment,
                R.id.searchFragment,
                R.id.dashboardFragment,
                R.id.exploreFragment
            )
            .build()

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView?.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                navController.navigate(R.id.settingsFragment)
                true
            }
            R.id.menu_account -> {
                if (navController.currentDestination?.id != R.id.traktAccountFragment) {
                    navController.navigate(R.id.traktAccountFragment)
                }
                true
            }
            else -> item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(
                item
            )
        }
    }

    override fun onStart() {
        super.onStart()
        customTabComponent.bindCustomService(this)
    }

    override fun onStop() {
        super.onStop()
        customTabComponent.unBindCustomTabService(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _bottomNavigationView = null
        _toolbar = null
        _container = null
        customTabComponent.setConnectionCallback(null)
    }

    override fun onTabConnected() {
        customTabComponent.mayLaunchUrl(Uri.parse(TRAKT_AUTH_URL), null, null)
    }

    override fun onTabDisconnected() {
        customTabComponent.mayLaunchUrl(null, null, null)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val code = intent?.data?.getQueryParameter("code")
        val traktConnectionArg = TraktConnectionArg(code)

        val bundle = bundleOf(EXTRA_TRAKT_URI to traktConnectionArg)
        navController.navigate(R.id.traktAccountFragment, bundle)
    }

    fun hideBottomNavigation() {
        if (bottomNavigationView != null) {
            if (bottomNavigationView?.visibility == View.VISIBLE) {
                bottomNavigationView?.visibility = View.GONE
            }
        }
    }

    fun showBottomNavigation() {
        if (bottomNavigationView != null) {
            if (bottomNavigationView?.visibility == View.GONE) {
                bottomNavigationView?.visibility = View.VISIBLE
            }
        }
    }

    fun hideToolbar() {
        if (toolbar?.visibility == View.VISIBLE) {
            toolbar?.visibility = View.GONE
        }
    }

    fun showToolbar() {
        if (toolbar?.visibility == View.GONE) {
            toolbar?.visibility = View.VISIBLE
        }
    }

    fun displayConnectionErrorMessage() {
        // Network settings dialog only available from Q and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            container?.let {
                Feedback(this).showSnackBar(
                    view = it,
                    type = FeedBackStatus.NO_CONNECTION,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    listener = { showNetworkSettings() })
            }
        } else {
            container?.let {
                Feedback(this).showSnackBar(
                    view = it,
                    type = FeedBackStatus.NO_CONNECTION,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    listener = null
                )
            }
        }
    }

    fun hideConnectionErrorMessage() {
        if (::snackbar.isInitialized) {
            if (snackbar.isShown) {
                snackbar.dismiss()
            }
        }
    }

    private fun showNetworkSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            startActivityForResult(intent, REQUEST_CODE_INTERNET)
        }
    }

    fun connectToTrakt() {
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
        val toolbarColor = ContextCompat.getColor(this, typedValue.resourceId)

        val customTabsIntent = CustomTabsIntent.Builder(customTabComponent.getSession())
            .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
            .setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(toolbarColor)
                    .build()
            )
            .build()

        customTabComponent.openCustomTab(
            this,
            customTabsIntent,
            Uri.parse(TRAKT_AUTH_URL),
            WebviewFallback()
        )
    }

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val inputMethodManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {
        const val TRAKT_AUTH_URL =
            "https://trakt.tv/oauth/authorize?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}"
        const val REQUEST_CODE_INTERNET = 10
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
    }
}
