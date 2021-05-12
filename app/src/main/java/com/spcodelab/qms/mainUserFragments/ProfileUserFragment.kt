package com.spcodelab.qms.mainUserFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.spcodelab.qms.CommanClass
import com.spcodelab.qms.R
import com.spcodelab.qms.models.UserDataModel
import com.wang.avi.AVLoadingIndicatorView
import java.util.regex.Pattern


class ProfileUserFragment : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var full_name: EditText
    private lateinit var address: EditText
    private lateinit var email_: EditText
    private lateinit var dob: EditText
    var profileImageUrl: String? = null
    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mDatabaseReference: DatabaseReference

    private var avLoadingIndicatorView: AVLoadingIndicatorView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_profile_user, container, false)
        //firebase
        mStorage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        imageView = view.findViewById(R.id.userImage)
        full_name = view.findViewById(R.id.regName)
        address = view.findViewById(R.id.regAddress)
        email_ = view.findViewById(R.id.regEmail)
        dob = view.findViewById(R.id.regDOB)
        avLoadingIndicatorView = view.findViewById(R.id.avi)


        view.findViewById<Button>(R.id.update_button).setOnClickListener {


            if (CommanClass.isNetworkAvailable(context)) {
                if (
                        checkErrors(
                                full_name,
                                dob,
                                address
                        )
                ) {
                    avLoadingIndicatorView!!.visibility = View.VISIBLE
                    avLoadingIndicatorView!!.show()
                    saveData(
                            full_name.text.toString(),
                            dob.text.toString(),
                            email_.text.toString(),
                            profileImageUrl.toString(),
                            address.text.toString()
                    )
                } else {
                    Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
                }
            } else
                CommanClass.alertNoConnection(context)
        }




        readUserData()
        return view
    }

    private fun readUserData() {
        val user: FirebaseUser? = mAuth.currentUser
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        if (user != null) {
            mDatabaseReference.child("ConsumersData").child(user.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.getValue(UserDataModel::class.java)!!
                        full_name.setText(userData.name)
                        dob.setText(userData.dob)
                        email_.setText(userData.email)
                        address.setText(userData.address)
                        profileImageUrl= userData.imageUrl
                        showImage(profileImageUrl.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
    private fun showImage(profileImageUrl: String) {

        val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.icon1)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontAnimate()
                .dontTransform()

        activity?.let {
            Glide.with(it)
                    .load(profileImageUrl)
                    .apply(options)
                    .into(imageView)
        }
    }

    private fun checkErrors(
            fullName: EditText,
            dob: EditText,
            address: EditText
    ): Boolean {
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


        if (dob.text.isNullOrEmpty()) {
            dob.error = "Required!"
            return false
        }

        return true
    }

    private fun saveData(fullName: String, dob: String, email: String, url: String, address: String) {

        val mUserData = UserDataModel(fullName, dob, address)
        val user: FirebaseUser? = mAuth.currentUser

        if (user != null) {
            mDatabaseReference.child("ConsumersData").child(user.uid)
                    .setValue(mUserData) //need an object of a
            avLoadingIndicatorView!!.visibility = View.GONE
            avLoadingIndicatorView!!.hide()

        } else {
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
            avLoadingIndicatorView!!.visibility = View.GONE
            avLoadingIndicatorView!!.hide()
        }
    }

}