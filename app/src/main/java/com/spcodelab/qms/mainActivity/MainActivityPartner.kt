package com.spcodelab.qms.mainActivity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.spcodelab.qms.AboutUsActivity
import com.spcodelab.qms.R
import com.spcodelab.qms.authentication.SigninActivity
import com.spcodelab.qms.mainPartnerFragments.ProfilePartnerFragment
import com.spcodelab.qms.mainPartnerFragments.QrFragment
import com.spcodelab.qms.mainPartnerFragments.QueuePartnerFragment

class MainActivityPartner : AppCompatActivity() {
    lateinit var fragment: Fragment

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mStorage: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_partner)
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        findViewById<ImageView>(R.id.signOut).setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setMessage("Do You want to Log Out?")
            dialogBuilder.setPositiveButton("Yes") { dialog, whichButton ->
                dialog.dismiss()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, SigninActivity::class.java))
                finish()
            }
            dialogBuilder.setNegativeButton("No") { dialog, whichButton ->
                dialog.dismiss()
            }
            val b = dialogBuilder.create()
            b.show()



        }
        findViewById<TextView>(R.id.toolbar_title).setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))

        }
        init()
    }
    private fun init() {

        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.color.purple))
        loadFragment(QueuePartnerFragment())
        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_Queue -> {
                    fragment = QueuePartnerFragment()
                    supportActionBar?.title = resources.getString(R.string.Services)
                }
                R.id.navigation_qr -> {
                    fragment = QrFragment()
                    supportActionBar?.title = resources.getString(R.string.WaitingList)
                }
                R.id.navigation_profile_partner -> {
                    fragment = ProfilePartnerFragment()
                    supportActionBar?.title = resources.getString(R.string.profile)

                }
            }

            loadFragment(fragment)
        }
    }
    private fun loadFragment(fragment: Fragment): Boolean {
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            return true
        }
        return false
    }
}