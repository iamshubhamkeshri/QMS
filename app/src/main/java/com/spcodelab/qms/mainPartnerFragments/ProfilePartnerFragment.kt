package com.spcodelab.qms.mainPartnerFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.spcodelab.qms.CommanClass.alertNoConnection
import com.spcodelab.qms.CommanClass.isNetworkAvailable
import com.spcodelab.qms.R
import com.spcodelab.qms.models.PartnerDataModel
import com.wang.avi.AVLoadingIndicatorView
import java.util.regex.Pattern

class ProfilePartnerFragment : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var firm_name: EditText
    private lateinit var address: EditText
    private lateinit var email_: TextView
    private lateinit var avgSeviceTime: EditText
    private lateinit var location_: TextView
    private lateinit var serviceType: TextView
    private lateinit var rating: TextView
    private lateinit var ratedBy: TextView
    private lateinit var currentToken: TextView
    private lateinit var totalToken: TextView

    var PartnerDataLocation: String? = null
    var profileImageUrl: String? = null
    var uid: String? = null

    //firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mDatabaseReference: DatabaseReference

    private var avLoadingIndicatorView: AVLoadingIndicatorView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_partner, container, false)

        //firebase
        mStorage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        imageView = view.findViewById(R.id.userImage)
        firm_name = view.findViewById(R.id.regName)
        rating = view.findViewById(R.id.rating)
        ratedBy = view.findViewById(R.id.ratedby)
        currentToken = view.findViewById(R.id.currentToken)
        totalToken = view.findViewById(R.id.totalTokens)
        address = view.findViewById(R.id.regAddress)
        email_ = view.findViewById(R.id.regEmail)
        avgSeviceTime = view.findViewById(R.id.regAverageServiceTime)
        location_ = view.findViewById(R.id.regLocation)
        serviceType = view.findViewById(R.id.regServiceType)
        avLoadingIndicatorView = view.findViewById(R.id.avi)



        view.findViewById<Button>(R.id.update_button).setOnClickListener {


            if (isNetworkAvailable(context)) {
                if (
                        checkErrors(
                                firm_name,
                                address,
                                avgSeviceTime
                        )
                ) {
                    avLoadingIndicatorView!!.visibility = View.VISIBLE
                    avLoadingIndicatorView!!.show()
                    saveData(
                            firm_name.text.toString(),
                            serviceType.text.toString(),
                            location_.toString(),
                            address.text.toString(),
                            avgSeviceTime.text.toString(),
                            email_.text.toString(),
                            profileImageUrl.toString(),

                            )
                } else {
                    Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
                }
            } else
                alertNoConnection(context)
        }




        readUserData()
        return view
    }

    private fun readUserData() {
        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            FirebaseDatabase.getInstance().reference.child("PartnersData").child(user.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        PartnerDataLocation = snapshot.value.toString()
                        FirebaseDatabase.getInstance().getReference(PartnerDataLocation!!).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val userData = snapshot.getValue(PartnerDataModel::class.java)!!
                                    firm_name.setText(userData.firmName)
                                    address.setText(userData.address)
                                    email_.text = userData.email
                                    avgSeviceTime.setText(userData.averageServiceTime)
                                    location_.text = userData.location
                                    serviceType.text = userData.serviceType
                                    ratedBy.text = userData.ratedBy
                                    rating.text = userData.rating
                                    currentToken.text = userData.currentToken
                                    totalToken.text = userData.totalToken


                                    profileImageUrl = userData.imageUrl
                                    uid = userData.uid


                                    showImage(profileImageUrl.toString())
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
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
                .error(R.drawable.noimage)
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
            firm_name: EditText,
            address: EditText,
            avgServiceTime: EditText
    ): Boolean {
        if (firm_name.text.isNullOrEmpty()) {
            firm_name.error = "Required!"
            return false
        }
        if (!firm_name.text.matches(Pattern.compile("^[a-zA-Z\\s]+").toRegex())) {
            firm_name.error = "Invalid name"
            return false
        }
        if (address.text.isNullOrEmpty()) {
            address.error = "Required!"
            return false
        }


        if (avgServiceTime.text.isNullOrEmpty()) {
            avgServiceTime.error = "Required!"
            return false
        }

        return true
    }

    private fun saveData(firmName: String, serviceType: String, location: String, address: String, averageServiceTime: String, email: String, url: String) {

        val mUserData = PartnerDataModel(firmName, serviceType, location, address, email, url, uid, averageServiceTime, currentToken.text.toString(), totalToken.text.toString(), rating.text.toString(), ratedBy.text.toString())
        val user: FirebaseUser? = mAuth.currentUser

        if (user != null) {
            FirebaseDatabase.getInstance().getReference(PartnerDataLocation!!)
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