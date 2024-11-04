package com.example.licentaincercarea1

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.licentaincercarea1.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class FragmentBase: AppCompatActivity() {
    private lateinit var appBarConfiguration : AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        if (bottomNav != null) {
            val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment? ?: return
            val navController = host.navController
            setupBottomNavMenu(navController)
        }
    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav?.setupWithNavController(navController)
        /*@Suppress("DEPRECATION")
         bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.categoriesFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.my_nav_host_fragment, CategoriesFragment())
                        .commitNow()
                    true
                }
                R.id.advanced -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.my_nav_host_fragment, AdvancedSearchFragment())
                        .commitNow()
                    true
                }
                else->{false}
            }
        }*/
    }

    /*private fun setupNavigationMenu(navController: NavController) {
        val sideNavView = findViewById<NavigationView>(R.id.nav_view)
        sideNavView?.setupWithNavController(navController)
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.my_nav_host_fragment))
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.my_nav_host_fragment).navigateUp(appBarConfiguration)
    }

}