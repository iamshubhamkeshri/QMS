package com.spcodelab.qms;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spcodelab.qms.models.PartnerDataModel;
import com.spcodelab.qms.models.PartnerRatingModel;
import com.spcodelab.qms.models.QueuePartnerModel;
import com.spcodelab.qms.models.QueueStatusModel;
import com.spcodelab.qms.models.QueueUserModel;
import com.spcodelab.qms.models.UserDataModel;
import com.wang.avi.AVLoadingIndicatorView;

import org.angmarch.views.NiceSpinner;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.spcodelab.qms.CommanClass.alertNoConnection;
import static com.spcodelab.qms.CommanClass.isNetworkAvailable;


public class AvailServices extends AppCompatActivity {
    private final ArrayList<String> Location_List = new ArrayList<>();
    String location_selected, service_Type;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    String currentUserUID;
    String date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avail_sertvices);

        service_Type = getIntent().getExtras().get("Service").toString();

        currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (isNetworkAvailable(this)) {
            loadLoactionArray();
        } else
            alertNoConnection(this);

        //current date
        date = new SimpleDateFormat("yyyy-M-dd", Locale.getDefault()).format(new Date());


        Toolbar toolbar = findViewById(R.id.toolbar_attempt);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.setTitle(service_Type);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        avLoadingIndicatorView = findViewById(R.id.avi);
        recyclerView = findViewById(R.id.servicePartners);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        NiceSpinner niceSpinner = findViewById(R.id.avail_services_location);
        niceSpinner.setOnClickListener(v -> niceSpinner.attachDataSource(Location_List));

        niceSpinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            location_selected = parent.getItemAtPosition(position).toString();

            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            avLoadingIndicatorView.show();
            if (isNetworkAvailable(this)) {
                fetchQuery();
            } else
                alertNoConnection(this);

            firebaseRecyclerAdapter.startListening();

        });
    }


    private void loadLoactionArray() {

        //addListenerForSingleValueEvent
        FirebaseDatabase.getInstance().getReference().child("ServiceableLocation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Location_List.add("Delhi");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Location_List.add(snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchQuery() {
        Query query = FirebaseDatabase.getInstance().getReference().child("ServicesAtLocation").
                child(location_selected).child(service_Type);

        FirebaseRecyclerOptions<PartnerDataModel> options =
                new FirebaseRecyclerOptions.Builder<PartnerDataModel>()
                        .setQuery(query, PartnerDataModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PartnerDataModel, ServiceHolder>(options) {
            @NotNull
            @Override
            public ServiceHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_service_provider, parent, false);

                return new ServiceHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull ServiceHolder holder, final int position, @NotNull PartnerDataModel model) {
                holder.firmName.setText(model.getFirmName());
                holder.address.setText(model.getAddress());

                FirebaseDatabase.getInstance().getReference().child("PartnerRating").child(model.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            PartnerRatingModel ratingModel = snapshot.getValue(PartnerRatingModel.class);
                            if (!ratingModel.getRating().equals("0")) {
                                holder.rating.setText(ratingModel.getRating());
                            } else {
                                holder.rating.setText("NA");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                RequestOptions options = new RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.ic_shop)
                        .error(R.drawable.noimage)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH)
                        .dontAnimate()
                        .dontTransform();


                Glide.with(AvailServices.this)
                        .load(model.getImageUrl())
                        .apply(options)
                        .into(holder.firmImage);


                holder.joinQueue.setOnClickListener(v ->
                        FirebaseDatabase.getInstance().getReference().child("QueuePartner").child(Objects.requireNonNull(model.getUid()))
                                .child(date).child(currentUserUID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(AvailServices.this, "Your Already In Queue\nThanks", Toast.LENGTH_LONG).show();
                                } else {
                                    ProcessJoinQueue(model);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        }));


                FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(model.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            QueueStatusModel qModel = snapshot.getValue(QueueStatusModel.class);
                            holder.totalToken.setText(qModel.getTotalToken());
                            holder.currentToken.setText(qModel.getCurrentToken());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        avLoadingIndicatorView.setVisibility(View.GONE);
        avLoadingIndicatorView.hide();
    }

    private void ProcessJoinQueue(PartnerDataModel pModel) {
        FirebaseDatabase.getInstance().getReference().child("ConsumersData").
                child(currentUserUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    UserDataModel uModel = snapshot.getValue(UserDataModel.class);
                    JoinQueue(pModel, uModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //current time
    private String getCurrentTime() {
        String delegate = "hh:mm aaa";
        return (String) DateFormat.format(delegate, Calendar.getInstance().getTime());
    }

    private void JoinQueue(PartnerDataModel pModel, UserDataModel uModel) {

        final int PERMISSION_REQUEST_CAMERA = 0;


        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.popup_join_queues, null);
        final AlertDialog dialog = myDialog.create();
        dialog.setView(view);
        dialog.setCancelable(false);

        LinearLayout linearLayout = view.findViewById(R.id.layout);
        ImageView closeBtn = view.findViewById(R.id.close);
        ImageView scannerLogo = view.findViewById(R.id.imageView);
        TextView titleCard = view.findViewById(R.id.title_card);
        TextView status = view.findViewById(R.id.status);
        TextInputEditText purposeVisit = view.findViewById(R.id.purpose_text);
        Button submit = view.findViewById(R.id.button);
        CodeScannerView scannerView = view.findViewById(R.id.scanner_view);
        titleCard.setText("Please Scan QR Code\n" + "Available at " + pModel.getFirmName() + "\nto get Token Number");


        CodeScanner mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            if (result.getText().equals(pModel.getUid())) {
                scannerView.setVisibility(View.GONE);
                status.setVisibility(View.VISIBLE);
                status.setTextColor(getResources().getColor(R.color.successColor));
                status.setText(R.string.QR_verification_successful);
                submit.setText(R.string.get_token);
                submit.setEnabled(true);
                scannerLogo.setVisibility(View.GONE);
                purposeVisit.setVisibility(View.VISIBLE);
                mCodeScanner.releaseResources();
            } else {
                mCodeScanner.releaseResources();
                status.setVisibility(View.VISIBLE);
                scannerView.setVisibility(View.VISIBLE);
                submit.setEnabled(true);
            }

        }));


        submit.setOnClickListener(view1 -> {
            if (submit.getText().equals("SCAN")) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission is already available, start camera preview
                    if (status.getVisibility() == View.VISIBLE) {
                        status.setVisibility(View.GONE);
                    }
                    mCodeScanner.startPreview();
                    scannerLogo.setVisibility(View.GONE);
                    scannerView.setVisibility(View.VISIBLE);
                    submit.setEnabled(false);
                } else {
                    // Permission has not been granted and must be requested.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {
                        //Ask permission again
                        Snackbar.make(linearLayout, R.string.camera_access_required,
                                Snackbar.LENGTH_INDEFINITE).setAction("OK", view2 -> {
                            // Request the permission
                            ActivityCompat.requestPermissions(AvailServices.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    PERMISSION_REQUEST_CAMERA);
                        }).show();

                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                        Snackbar.make(linearLayout, "Give camera permission from app setting", Snackbar.LENGTH_SHORT).show();
                    }
                }
            } else {
                String purpose_text = purposeVisit.getText().toString();
                if (TextUtils.isEmpty(purpose_text)) {
                    purposeVisit.setError("Required!");
                } else {
                    FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(pModel.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int queueSize;

                                    if (snapshot.exists()) {
                                        QueueStatusModel queueStatusModel = snapshot.getValue(QueueStatusModel.class);
                                        queueSize = Integer.parseInt(queueStatusModel.getTotalToken());

                                        //setting queue for partner
                                        QueuePartnerModel queuePartnerModel = new QueuePartnerModel(uModel.name, uModel.email, purpose_text, uModel.imageUrl,
                                                getCurrentTime(), "In Queue", String.valueOf(queueSize + 1), currentUserUID);

                                        FirebaseDatabase.getInstance().getReference().child("QueuePartner").child(pModel.getUid()).child(date).child(currentUserUID).setValue(queuePartnerModel);


                                        //setting queue for user
                                        QueueUserModel queueUserModel = new QueueUserModel(pModel.getFirmName(), pModel.getUid(), pModel.getServiceType(),
                                                pModel.getLocation(), pModel.getAddress(), pModel.getImageUrl(), date, getCurrentTime(), "In Queue",
                                                pModel.getAverageServiceTime(), String.valueOf(queueSize + 1), purpose_text, "0");

                                        FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID)
                                                .child(pModel.getUid()).setValue(queueUserModel);


                                        //updating total queue size
                                        FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(pModel.getUid())
                                                .child("totalToken").setValue(String.valueOf(queueSize + 1));

                                        //updating current Token if No one is available to available
                                        if (queueStatusModel.getCurrentToken().equals("NA")) {
                                            FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(pModel.getUid())
                                                    .child("currentToken").setValue(String.valueOf(queueSize + 1));
                                        }


                                        Toast.makeText(AvailServices.this, "Token Generated Successfully\n     Wait For Your Turn", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(AvailServices.this, "Token Generation Failed", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }


        });

        dialog.show();
        closeBtn.setOnClickListener(view12 -> dialog.dismiss());
    }

    static class ServiceHolder extends RecyclerView.ViewHolder {
        public TextView firmName;
        public TextView rating;
        public TextView address;
        public ImageView firmImage;
        public TextView currentToken;
        public TextView totalToken;
        public Button joinQueue;

        public ServiceHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.text_address);
            rating = itemView.findViewById(R.id.text_Partner_rating);
            firmName = itemView.findViewById(R.id.text_Service_provider_name);
            firmImage = itemView.findViewById(R.id.image_firm);
            joinQueue = itemView.findViewById(R.id.JoinQueue);
            currentToken = itemView.findViewById(R.id.currentToken);
            totalToken = itemView.findViewById(R.id.totalTokens);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //firebaseRecyclerAdapter.stopListening();
    }
}