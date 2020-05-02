package com.theupnextapp

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.collection.CollectionFragment

class MainActivity : AppCompatActivity() {

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    fun hideBottomNavigation() {
        if (::bottomNavigationView.isInitialized) {
            if (bottomNavigationView.visibility == View.VISIBLE) {
                bottomNavigationView.visibility = View.GONE
            }
        }
    }

    fun showBottomNavigation() {
        if (::bottomNavigationView.isInitialized) {
            if (bottomNavigationView.visibility == View.GONE) {
                bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }

    fun hideToolbar() {
        if (::toolbar.isInitialized) {
            if (toolbar.visibility == View.VISIBLE) {
                toolbar.visibility = View.GONE
            }
        }
    }

    fun showToolbar() {
        if (::toolbar.isInitialized) {
            if (toolbar.visibility == View.GONE) {
                toolbar.visibility = View.VISIBLE
            }
        }
    }
}
