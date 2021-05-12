package com.spcodelab.qms.authentication

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.spcodelab.qms.CommanClass
import com.spcodelab.qms.CommanClass.UserType
import com.spcodelab.qms.mainActivity.MainActivityUser
import com.spcodelab.qms.R
import com.spcodelab.qms.models.UserDataModel
import com.wang.avi.AVLoadingIndicatorView
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern


class SignupActivityUser : AppCompatActivity() {
    private lateinit var imageView: ShapeableImageView
    private lateinit var full_name: EditText
    private lateinit var address: EditText
    private lateinit var email_: EditText
    private lateinit var dob: EditText
    private lateinit var password_: EditText
    private lateinit var passwordConfirm: EditText
    private var imageUri: Uri? = null
    private var imageSelected: Boolean = false
    private lateinit var imageSelectError: TextView

    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mDatabaseReference: DatabaseReference

    private var avLoadingIndicatorView: AVLoadingIndicatorView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_user)

        //firebase
        mStorage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        imageSelectError = findViewById(R.id.image_error_text)
        full_name = findViewById(R.id.signup_name)
        address = findViewById(R.id.signup_address)
        email_ = findViewById(R.id.signup_email)
        password_ = findViewById(R.id.signup_password)
        dob = findViewById(R.id.signup_dob)
        passwordConfirm = findViewById(R.id.signup_password_confirm)
        avLoadingIndicatorView = findViewById(R.id.avi)

        imageView = findViewById(R.id.signup_image_upload)
        imageView.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 101)
        }

        //picking date of birth
        dob.setOnClickListener {
            val dialogInterface: DialogInterface = object : DialogInterface {
                override fun cancel() {
                    //TODO::
                }

                override fun dismiss() {
                    //TODO::
                }
            }

            val datePickerDialog = DatePickerDialog(this)
            datePickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
                val date = "$year/$month/$dayOfMonth"
                dob.setText(date)
            }
            datePickerDialog.onClick(dialogInterface, 2)
            datePickerDialog.show()
        }


        findViewById<Button>(R.id.signup_btn).setOnClickListener {


            if (CommanClass.isNetworkAvailable(this@SignupActivityUser)) {
                if (
                        checkErrors(
                                imageSelected,
                                full_name,
                                dob,
                                email_,
                                password_,
                                passwordConfirm,
                                address
                        )
                ) {
                    signUpWith(email_.text.toString(), password_.text.toString())
                } else {
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                }
            } else
                CommanClass.alertNoConnection(this@SignupActivityUser)
        }

        //Already have an account navigate to SignIn Screen
        findViewById<TextView>(R.id.signin_text).setOnClickListener {
            onBackPressed()
        }
    }


    private fun checkErrors(
            imageSelected: Boolean,
            fullName: EditText,
            dob: EditText,
            email: EditText,
            password: EditText,
            passwordConfirm: EditText,
            address: EditText
    ): Boolean {
        if (!imageSelected) {
            imageSelectError.visibility = View.VISIBLE
            Toast.makeText(this, "Upload image", Toast.LENGTH_SHORT).show()
            return false
        }
        if (fullName.text.isNullOrEmpty()) {
            fullName.error = "Required!"
            return false
        }
        if (!fullName.text.matches(Pattern.compile("^[a-zA-Z\\s]+").toRegex())) {
            fullName.error = "Invalid name"
            return false
        }
        if (address.text.isNullOrEmpty()) {
            address.error = "Required!"
            return false
        }
        if (passwordConfirm.text.toString() != password.text.toString()) {
            passwordConfirm.error = "Password unmatched"
            return false
        }


        if (dob.text.isNullOrEmpty()) {
            dob.error = "Required!"
            return false
        }

        if (password.text.isNullOrEmpty()) {
            password.error = "Required!"
            return false
        }
        if (password.text.length < 6) {
            password.error = "length must be greater than 6"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
            email.error = "Invalid email"
            return false
        }
        if (email.text.isNullOrEmpty()) {
            email.error = "Required!"
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
            imageSelectError.visibility = View.INVISIBLE
            imageSelected = true
        }
    }

    private fun signUpWith(email: String, password: String) {
        avLoadingIndicatorView!!.visibility = View.VISIBLE
        avLoadingIndicatorView!!.show()
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val user: FirebaseUser? = mAuth.currentUser
                if (user != null) {
                    //uploading the profile image and user data
                    uploadImage(imageView)
                }

            } else {
                // If sign up fails, display a message to the user.
                Toast.makeText(
                        this, "Account Not created.",
                        Toast.LENGTH_SHORT
                ).show()
                avLoadingIndicatorView!!.visibility = View.GONE
                avLoadingIndicatorView!!.hide()
            }
        }
    }

    //upload user's profile pic
    private fun uploadImage(imageView: ShapeableImageView) {
        imageView.setDrawingCacheEnabled(true)
        imageView.buildDrawingCache()
        val bitmap: Bitmap = imageView.getDrawingCache(true)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        imageView.setDrawingCacheEnabled(false)
        val bytes = byteArrayOutputStream.toByteArray()
        val user: FirebaseUser? = mAuth.currentUser
        //saving in mentioned dir
        val reference: StorageReference = mStorage.reference.child("ConsumerProfile").child(user?.uid + ".png")
        val metadata = StorageMetadata.Builder()
                .setCustomMetadata("text", "Profile pic of ${full_name.text}").build()

        reference.putBytes(bytes, metadata).addOnSuccessListener { taskSnapshot ->
            //getting image url for showing user's profile pic with Glide
            reference.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                //saving user's data
                //navigating to different screen
                saveData(
                        full_name.text.toString(),
                        dob.text.toString(),
                        email_.text.toString(),
                        imageUrl,
                        address.text.toString()
                )
            }
        }
    }

    private fun saveData(fullName: String, dob: String, email: String, imageUrl: String, address: String) {

        val mUserData = UserDataModel(fullName, dob, email, imageUrl,address)
        val user: FirebaseUser? = mAuth.currentUser

        if (user != null) {

            //saving data to shared preferences
            val sharedPref: SharedPreferences = getSharedPreferences(CommanClass.MyPREFERENCES, MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(UserType,"Consumer")
            editor.apply()
            editor.commit()


            FirebaseDatabase.getInstance().reference.child("UserType").child(user.uid)
                    .setValue("Consumer") //need an object of a

            mDatabaseReference.child("ConsumersData").child(user.uid)
                    .setValue(mUserData) //need an object of a


            Toast.makeText(this, "Welcome to QMS", Toast.LENGTH_SHORT).show()
            avLoadingIndicatorView!!.visibility = View.GONE
            avLoadingIndicatorView!!.hide()
            startActivity(Intent(this, MainActivityUser::class.java))
            finish()

        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}