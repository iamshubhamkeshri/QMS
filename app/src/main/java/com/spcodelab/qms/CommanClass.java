package com.spcodelab.qms;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;


public class CommanClass {
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String UserType = "userType";


    /*method to handle network connection**/
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    public static void alertNoConnection(Context context) {

        Dialog builder = new Dialog(context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(dialogInterface -> {
            //nothing;
        });


        Toasty.info(context, "No/slow Internet Connection", Toasty.LENGTH_LONG).show();

        ImageView imageView = new ImageView(context);
        Glide.with(context).load(R.drawable.internetissue).into(imageView);
        //imageView.setImageResource(R.drawable.nowifi);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                400,
                500));
        builder.show();
    }



    public static void checkForUserType(MyCallback myCallback, String UID) {

        final String[] UserType = new String[1];
        FirebaseDatabase.getInstance().getReference().child("UserType").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (UID!=null){
                    UserType[0] = snapshot.getValue(String.class);
                    myCallback.onCallback(UserType[0]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public interface MyCallback {
        void onCallback(String value);
    }

}


