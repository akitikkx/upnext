package com.theupnextapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        setSupportActionBar(toolbar)

        val appBarConfiguration = AppBarConfiguration
            .Builder(
                R.id.splashScreenFragment,
                R.id.searchFragment,
                R.id.dashboardFragment,
                R.id.collectionFragment
            )
            .build()

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    fun hideBottomNavigation() {
        if (bottomNavigationView.visibility == View.VISIBLE) {
            bottomNavigationView.visibility = View.GONE
        }
    }

    fun showBottomNavigation() {
        if (bottomNavigationView.visibility == View.GONE) {
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
}
