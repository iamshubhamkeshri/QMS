package com.spcodelab.qms

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.spcodelab.qms.CommanClass.MyPREFERENCES
import com.spcodelab.qms.CommanClass.UserType
import com.spcodelab.qms.authentication.SigninActivity
import com.spcodelab.qms.mainActivity.MainActivityPartner
import com.spcodelab.qms.mainActivity.MainActivityUser
import es.dmoral.toasty.Toasty


@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var mProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        //firebase
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser


        //creating animation object an animating the image
        val animation = AnimationUtils.loadAnimation(this, R.anim.scale)
        findViewById<ImageView>(R.id.splash_image).startAnimation(animation)

        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        val sharedPref: SharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE)
        Handler().postDelayed({
            if (currentUser != null) {
                val UserType = sharedPref.getString(UserType,"defaultname")
                if(UserType.equals("Partner")) {
                    startActivity(Intent(this, MainActivityPartner::class.java))
                }else if (UserType.equals("Consumer")){
                    startActivity(Intent(this, MainActivityUser::class.java))
                }else{
                    Toasty.info(this, "User Not Found", Toasty.LENGTH_LONG).show()
                    mAuth!!.signOut()
                }
            } else {
                startActivity(Intent(this, SigninActivity::class.java))
            }

            finish()
        }, 4000) // 3000 is the delayed time in milliseconds.

    }
}