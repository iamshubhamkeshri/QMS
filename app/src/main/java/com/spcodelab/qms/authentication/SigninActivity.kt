package com.spcodelab.qms.authentication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.spcodelab.qms.CommanClass.*
import com.spcodelab.qms.mainActivity.MainActivityPartner
import com.spcodelab.qms.mainActivity.MainActivityUser
import com.spcodelab.qms.R
import com.wang.avi.AVLoadingIndicatorView
import es.dmoral.toasty.Toasty
import java.util.*


class SigninActivity : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private var currentUser: FirebaseUser? = null
    private var avLoadingIndicatorView: AVLoadingIndicatorView? = null

    //firebase
    private var mAuth: FirebaseAuth? = null
    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth?.currentUser

        email = findViewById(R.id.signin_email)
        password = findViewById(R.id.signin_password)
        avLoadingIndicatorView = findViewById(R.id.avi)

        //shared preference


        findViewById<Button>(R.id.signin_btn).setOnClickListener {



            if (isNetworkAvailable(this@SigninActivity)) {
                if (
                        checkErrors(
                                email,
                                password
                        )
                ) {
                    signInWith(email.text.toString(), password.text.toString())
                }
            } else
                alertNoConnection(this@SigninActivity)
        }

        //Don't have an account navigate to SignUp Screen
        findViewById<TextView>(R.id.signup_Partner).setOnClickListener {
            startActivity(Intent(this, SignupActivityPartner::class.java))
        }
        findViewById<TextView>(R.id.signup_Consumer).setOnClickListener {
            startActivity(Intent(this, SignupActivityUser::class.java))
        }
    }


    private fun checkErrors(
            email: EditText,
            password: EditText
    ): Boolean {
        if (email.text.isNullOrEmpty()) {
            email.error = "Required!"
            return false
        }
        if (password.text.isNullOrEmpty()) {
            password.error = "Required!"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
            email.error = "Not a valid email"
            return false
        }
        return true
    }

    private fun signInWith(email: String, password: String) {
        if (password.length >= 6) {
            val sharedPref: SharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE)
            val editor = sharedPref.edit()

            avLoadingIndicatorView!!.visibility = View.VISIBLE
            avLoadingIndicatorView!!.show()
            mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    //var name: String? = null
                    checkForUserType(MyCallback { value ->
                        editor.putString(UserType, value)
                        editor.apply()
                        editor.commit()
                        when {
                            value.equals("Consumer") -> {
                                startActivity(Intent(this, MainActivityUser::class.java))
                                finish()
                            }
                            value.equals("Partner") -> {
                                startActivity(Intent(this, MainActivityPartner::class.java))
                                finish()
                            }
                            else -> {
                                Toasty.info(this, "User Not Found", Toasty.LENGTH_LONG).show()
                                mAuth!!.signOut()
                            }
                        }
                    }, FirebaseAuth.getInstance().currentUser?.uid.toString())



                    avLoadingIndicatorView!!.visibility = View.GONE
                    avLoadingIndicatorView!!.hide()
                } else {
                    Toast.makeText(
                            this,
                            "Try Again !",
                            Toast.LENGTH_SHORT
                    ).show()
                    avLoadingIndicatorView!!.visibility = View.GONE
                    avLoadingIndicatorView!!.hide()
                }
            }
        } else {
            Toast.makeText(this, "SignIn Task Failed!", Toast.LENGTH_SHORT).show()
        }
    }


}