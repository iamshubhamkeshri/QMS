package com.spcodelab.qms.authentication

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.spcodelab.qms.CommanClass.*
import com.spcodelab.qms.mainActivity.MainActivityPartner
import com.spcodelab.qms.R
import com.spcodelab.qms.models.PartnerDataModel
import com.wang.avi.AVLoadingIndicatorView
import org.angmarch.views.NiceSpinner
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.regex.Pattern


class SignupActivityPartner : AppCompatActivity() {
    private lateinit var imageView: ShapeableImageView
    private lateinit var firm_name: EditText

    //private lateinit var service_type: NiceSpinner
    //private lateinit var location: NiceSpinner
    private lateinit var address: EditText
    private lateinit var average_service_time: EditText
    private lateinit var email_: EditText

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

    private var Location: String? = null
    private var Service: String? = null

    var locations: ArrayList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_partner)
        loadLoactionArray()

        //firebase
        mStorage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        imageSelectError = findViewById(R.id.image_error_text)
        firm_name = findViewById(R.id.signup_name)
        //service_type = findViewById(R.id.signup_service_type)
        //location = findViewById(R.id.signup_location)
        address = findViewById(R.id.signup_address)
        average_service_time = findViewById(R.id.signup_service_time)
        email_ = findViewById(R.id.signup_email)
        password_ = findViewById(R.id.signup_password)
        passwordConfirm = findViewById(R.id.signup_password_confirm)
        avLoadingIndicatorView = findViewById(R.id.avi)

        imageView = findViewById(R.id.signup_image_upload)
        imageView.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 101)
        }

        val location_spinner = findViewById<View>(R.id.signup_location) as NiceSpinner

        findViewById<TextView>(R.id.signup_location).setOnClickListener {
            location_spinner.attachDataSource(locations)
        }

        location_spinner.setOnSpinnerItemSelectedListener { parent, view, position, id ->
            Location = parent.getItemAtPosition(position).toString();
        }

        val service_spinner = findViewById<View>(R.id.signup_service_type) as NiceSpinner
        val dataset: List<String> = LinkedList(Arrays.asList( "Hospital","Hospital","Bank","RailWays","Airport","Movie", "Saloon", "Restaurant", "ServiceCenter"))

        findViewById<TextView>(R.id.signup_service_type).setOnClickListener {
            service_spinner.attachDataSource(dataset)
        }

        service_spinner.setOnSpinnerItemSelectedListener { parent, view, position, id ->
            Service = parent.getItemAtPosition(position).toString();
        }


        findViewById<Button>(R.id.signup_btn).setOnClickListener {

            if (isNetworkAvailable(this@SignupActivityPartner)) {
                if (
                        checkErrors(
                                imageSelected,
                                firm_name,
                                service_spinner,
                                location_spinner,
                                address,
                                average_service_time,
                                email_,
                                password_,
                                passwordConfirm
                        )
                ) {
                    signUpWith(email_.text.toString(), password_.text.toString())
                } else {
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                }
            } else
                alertNoConnection(this@SignupActivityPartner)
        }

        //Already have an account navigate to SignIn Screen
        findViewById<TextView>(R.id.signin_text).setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadLoactionArray() {
        //addListenerForSingleValueEvent
        FirebaseDatabase.getInstance().reference.child("ServiceableLocation").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                locations.add("Delhi")
                if(dataSnapshot.exists()){
                    for (snapshot in dataSnapshot.children) {
                        locations.add(snapshot.key.toString())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                avLoadingIndicatorView!!.visibility = View.GONE
                avLoadingIndicatorView!!.hide()
                Log.e("The read failed: ", databaseError.message)
            }
        })
    }


    private fun checkErrors(
            imageSelected: Boolean,
            firm_name: EditText,
            service_spinner: NiceSpinner,
            location_spinner: NiceSpinner,
            address: EditText,
            average_Service_Time: EditText,
            email: EditText,
            password: EditText,
            passwordConfirm: EditText
    ): Boolean {
        if (!imageSelected) {
            imageSelectError.visibility = View.VISIBLE
            Toast.makeText(this, "Upload image", Toast.LENGTH_SHORT).show()
            return false
        }
        if (firm_name.text.isNullOrEmpty()) {
            firm_name.error = "Required!"
            return false
        }
        if (!firm_name.text.matches(Pattern.compile("^[a-zA-Z\\s]+").toRegex())) {
            firm_name.error = "Invalid name"
            return false
        }
        if (passwordConfirm.text.toString() != password.text.toString()) {
            passwordConfirm.error = "Password unmatched"
            return false
        }

        if (Service.isNullOrEmpty()) {
            service_spinner.error = "Required!"
            return false
        }
        if (Location.isNullOrEmpty()) {
            location_spinner.error = "Required!"
            return false
        }
        if (address.text.isNullOrEmpty()) {
            address.error = "Required!"
            return false
        }
        if (average_Service_Time.text.isNullOrEmpty()) {
            average_Service_Time.error = "Required!"
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
                    //uploading the profile image
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
        val path = user?.uid + "/profile.png"
        val reference: StorageReference = mStorage.reference.child("PartnerLogo").child(user?.uid + ".png")
        val metadata = StorageMetadata.Builder()
                .setCustomMetadata("text", "Profile pic of ${firm_name.text}").build()




        reference.putBytes(bytes, metadata).addOnSuccessListener { taskSnapshot ->
            //getting image url for showing user's profile pic with Glide
            reference.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                //saving user's data
                //navigating to different screen
                saveData(
                        firm_name.text.toString(),
                        address.text.toString(),
                        average_service_time.text.toString(),
                        email_.text.toString(),
                        imageUrl
                )
            }
        }
    }

    private fun saveData(firmName: String, address: String, avg_service_time: String, email: String, imageUrl: String) {

        val mUserData = PartnerDataModel(firmName, Service, Location, address,  email, imageUrl,mAuth.currentUser?.uid,"0","0","0","0",avg_service_time)
        val user: FirebaseUser? = mAuth.currentUser

        if (user != null) {
            //saving data to shared preferences
            val sharedPref: SharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(UserType,"Partner")
            editor.apply()
            editor.commit()
            FirebaseDatabase.getInstance().reference.child("UserType").child(user.uid)
                    .setValue("Partner")

            FirebaseDatabase.getInstance().reference.child("Queue").child(user.uid)
                    .setValue("Not Started")

            FirebaseDatabase.getInstance().reference.child("ServicesAtLocation")
                    .child(Location.toString()).child(Service.toString()).child(user.uid).setValue(mUserData)



            mDatabaseReference.child("PartnersData").child(user.uid)
                    .setValue("ServicesAtLocation/"+Location.toString()+"/"+Service.toString()+"/"+user.uid) //need an object of a

            avLoadingIndicatorView!!.visibility = View.GONE
            avLoadingIndicatorView!!.hide()
            Toast.makeText(this, "Welcome to QMS", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivityPartner::class.java))
            finish()

        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}