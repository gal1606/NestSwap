package com.example.nestswap

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.nestswap.Model.Model
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private var signOutMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.main_toolbar))


        val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.mainNavHost) as? NavHostFragment
        navController = navHostFragment?.navController ?: throw IllegalStateException("NavHostFragment not found")

        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        if (Model.instance.isUserSignedIn()) {
            graph.setStartDestination(R.id.profileFragment)
            navController.setGraph(graph, Bundle().apply {
                putString("userId", Model.instance.getCurrentUserId())
            })
        } else {
            graph.setStartDestination(R.id.loginFragment)
            navController.setGraph(graph,null)
        }

        NavigationUI.setupActionBarWithNavController(this, navController)

        bottomNavigationView = findViewById(R.id.bottom_bar)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.signupFragment -> {
                    bottomNavigationView.visibility = View.GONE
                    signOutMenuItem?.isVisible = false
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                    signOutMenuItem?.isVisible = Model.instance.isUserSignedIn()
                }
            }
        }

        val navHostFragmentView = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.mainNavHost)
        ViewCompat.setOnApplyWindowInsetsListener(navHostFragmentView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        signOutMenuItem = menu?.findItem(R.id.action_sign_out)
        signOutMenuItem?.isVisible = Model.instance.isUserSignedIn() && navController.currentDestination?.id !in listOf(R.id.loginFragment, R.id.signupFragment)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                Model.instance.signOut()
                navController.navigate(R.id.action_profile_to_login)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun setBottomNavigationVisibility(isVisible: Boolean) {
        bottomNavigationView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}