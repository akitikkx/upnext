package com.theupnextapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.collection.CollectionFragment

class MainActivity : AppCompatActivity() {

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    private var _bottomNavigationView: BottomNavigationView? = null
    private val bottomNavigationView get() = _bottomNavigationView!!

    private var _toolbar: Toolbar? = null
    private val toolbar get() = _toolbar!!

    private var _container: ConstraintLayout? = null
    private val container get() = _container!!

    private var _firebaseAnalytics: FirebaseAnalytics? = null
    private val firebaseAnalytics get() = _firebaseAnalytics!!

    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        _toolbar = findViewById<Toolbar>(R.id.toolbar)
        _bottomNavigationView = findViewById(R.id.bottom_navigation)
        _container = findViewById(R.id.container)

        setSupportActionBar(toolbar)

        val appBarConfiguration = AppBarConfiguration
            .Builder(
                R.id.splashScreenFragment,
                R.id.searchFragment,
                R.id.dashboardFragment,
                R.id.watchlistFragment,
                R.id.historyFragment
            )
            .build()

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        handleDeepLinks()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun handleDeepLinks() {
        val uri: Uri? = intent?.data

        if (uri != null && uri.toString().startsWith(BuildConfig.TRAKT_REDIRECT_URI)) {
            val code = uri.getQueryParameter("code")

            val collectionFragmentArg = TraktConnectionArg(code)

            val bundle = bundleOf(CollectionFragment.EXTRA_TRAKT_URI to collectionFragmentArg)
            navController.navigate(R.id.watchlistFragment, bundle)
            clearIntent()
        }
    }

    private fun clearIntent() {
        intent.replaceExtras(Bundle())
        intent.action = ""
        intent.data = null
        intent.flags = 0
    }

    fun hideBottomNavigation() {
        if (bottomNavigationView != null && bottomNavigationView.visibility == View.VISIBLE) {
            bottomNavigationView.visibility = View.GONE
        }
    }

    fun showBottomNavigation() {
        if (bottomNavigationView != null && bottomNavigationView.visibility == View.GONE) {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    fun hideToolbar() {
        if (toolbar.visibility == View.VISIBLE) {
            toolbar.visibility = View.GONE
        }
    }

    fun showToolbar() {
        if (toolbar.visibility == View.GONE) {
            toolbar.visibility = View.VISIBLE
        }
    }

    fun displayConnectionErrorMessage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            snackbar = Snackbar.make(
                container,
                getString(R.string.device_not_connected_to_internet_error),
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction("Settings") { showNetworkSettings() }
            snackbar.show()
        } else {
            snackbar = Snackbar.make(
                container,
                getString(R.string.device_not_connected_to_internet_error),
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.show()
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

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val inputMethodManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _bottomNavigationView = null
        _toolbar = null
        _container = null
        _firebaseAnalytics = null
    }

    companion object {
        const val REQUEST_CODE_INTERNET = 10
    }
}
