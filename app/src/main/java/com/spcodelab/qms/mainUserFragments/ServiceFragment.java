package com.spcodelab.qms.mainUserFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spcodelab.qms.R;
import com.spcodelab.qms.models.UserDataModel;

import org.w3c.dom.Text;

public class ServiceFragment extends Fragment {

    TextView Welcome_msg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_services, container, false);


        Welcome_msg= view.findViewById(R.id.Welcome_user);
        FirebaseDatabase.getInstance().getReference().child("ConsumersData").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserDataModel user = dataSnapshot.getValue(UserDataModel.class);
                //assert user != null;
                if (user != null) {
                   Welcome_msg.setText("Welcome,\n" + user.name);
                } else {
                    Welcome_msg.setText("Welcome,\nBuddy");
                }
                Animation animation = new AlphaAnimation(0, 1);
                animation.setDuration(3500);
                Welcome_msg.setAnimation(animation);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // Inflate the layout for this fragment
        return view;
    }

}