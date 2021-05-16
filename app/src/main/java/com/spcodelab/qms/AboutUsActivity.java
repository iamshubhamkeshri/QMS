package com.spcodelab.qms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.Objects;

public class AboutUsActivity extends AppCompatActivity {

    private WebView mWebview;
    FloatingActionButton rohitL, shubhL,maroofL, ujjwalL;
    ExpandableLayout faqs_Expand;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);

        Toolbar toolbar = findViewById(R.id.toolbar_attempt);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.setTitle("About Us");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        rohitL = findViewById(R.id.rohitLinkedIn);
        maroofL =findViewById(R.id.maroofLinkedIN);
        shubhL=findViewById(R.id.shubhLinkedIn);
        ujjwalL = findViewById(R.id.ujjwalLinkedin);

        rohitL.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://www.linkedin.com/in/rohit-tiwari-200115152/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        maroofL.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://www.linkedin.com/in/mohd-maroof-a74b4316b/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        ujjwalL.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://www.linkedin.com/in/ujjawal-bhusal-8479b31a9/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        ujjwalL.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://www.linkedin.com/in/ujjawal-bhusal-8479b31a9/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        shubhL.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://www.linkedin.com/in/ujjawal-bhusal-8479b31a9/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });




        RatingBar ratingBar = findViewById(R.id.rating);
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("App Rating").
                child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ratingBar.setRating(Float.parseFloat(snapshot.getValue().toString()) );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        findViewById(R.id.rate).setOnClickListener(v -> {
            String rate= String.valueOf(ratingBar.getRating());
            databaseReference.setValue(rate);
            Toast.makeText(AboutUsActivity.this, "Rated Successfully - "+rate, Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.faqs).setOnClickListener(v -> {
            faqs_Expand=findViewById(R.id.faqs_expand);
            faqs_Expand.toggle();
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
