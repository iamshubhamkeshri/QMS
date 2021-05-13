package com.spcodelab.qms.mainUserFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spcodelab.qms.R;
import com.spcodelab.qms.models.PartnerDataModel;
import com.spcodelab.qms.models.QueueUserModel;
import com.wang.avi.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.spcodelab.qms.CommanClass.alertNoConnection;
import static com.spcodelab.qms.CommanClass.isNetworkAvailable;

public class WaitingListFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    String currentUserUID;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view=inflater.inflate(R.layout.fragment_waiting_list, container, false);

        recyclerView = view.findViewById(R.id.waitingList);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if (isNetworkAvailable(getContext())) {
            currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            //fetchQuery();
            //firebaseRecyclerAdapter.startListening();
        } else
            alertNoConnection(getContext());



        return view;
    }

    private void fetchQuery() {
        Query query = FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID);

//        FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//               if (snapshot.exists()){
//                   Toast.makeText(getContext(),snapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
//               }else{
//                   Toast.makeText(getContext(), "not found", Toast.LENGTH_SHORT).show();
//               }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


        FirebaseRecyclerOptions<QueueUserModel> options =
                new FirebaseRecyclerOptions.Builder<QueueUserModel>()
                        .setQuery(query, QueueUserModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<QueueUserModel, QueueHolder>(options) {
            @NotNull
            @Override
            public QueueHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_user_queue, parent, false);

                return new QueueHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull    QueueHolder holder, final int position, @NotNull QueueUserModel model) {
                holder.firmName.setText(model.getFirmName());
                holder.address.setText(model.getAddress());

                holder.firmName.setText(model.getFirmName());
                holder.serviceType.setText(model.getServiceType());
                holder.address.setText(model.getAddress()+" ("+model.getLocation()+" )");


                RequestOptions options = new RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.ic_shop)
                        .error(R.drawable.noimage)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH)
                        .dontAnimate()
                        .dontTransform();


                Glide.with(getContext())
                        .load(model.getFirmLogoUrl())
                        .apply(options)
                        .into(holder.firmImage);
                holder.regDate.setText("Reg Date\n"+model.getRegDate());
                holder.regTime.setText("Reg. Time\n"+model.getRegTime());

                int expected_time=(Integer.parseInt(model.getMyToken())-Integer.parseInt(model.getCurrentToken()))*Integer.parseInt(model.getAvgServiceTime());
                holder.expectedTime.setText("\n"+ expected_time);
                holder.currentStatus.setText("Current Status\n"+model.getStatus());
                if(model.getStatus().equals("Completed")||model.getStatus().equals("Canceled")||model.getStatus().equals("Suspended")){
                    holder.tokenLayout.setVisibility(View.GONE);
                    holder.expectedTime.setVisibility(View.GONE);
                    holder.refresh.setVisibility(View.GONE);
                    holder.cancelVisit.setText("RATE");
                    holder.feedback.setVisibility(View.GONE);
                }
                holder.myToken.setText(model.getMyToken());
                holder.currentToken.setText(model.getCurrentToken());
                holder.totalToken.setText(model.getTotalToken());
                holder.refresh.setOnClickListener(v -> {
                    FirebaseDatabase.getInstance().getReference().child("ServiceAtLocation")
                            .child(model.getLocation()).child(model.getServiceType()).child(model.getFirmUID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PartnerDataModel partnerDataModel=snapshot.getValue(PartnerDataModel.class);
                                int expected_time=(Integer.parseInt(model.getMyToken())-Integer.parseInt(partnerDataModel.getCurrentToken()))*Integer.parseInt(model.getAvgServiceTime());
                                holder.expectedTime.setText("Your Turn In\n"+ expected_time);
                                holder.totalToken.setText(partnerDataModel.getTotalToken());
                                holder.currentToken.setText(partnerDataModel.getCurrentToken());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });


                holder.cancelVisit.setOnClickListener(v ->{
                    if(holder.cancelVisit.getText().equals("CANCEL VISIT")){
                        FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID).child("status").setValue("Canceled");
                    }else{
                        FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID)
                                .child("rating").setValue(String.valueOf(holder.ratingBar.getRating()));
                    }

                });



            }

        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);

//        avLoadingIndicatorView.setVisibility(View.GONE);
//        avLoadingIndicatorView.hide();
    }

    static class QueueHolder extends RecyclerView.ViewHolder {
        public TextView firmName;
        public TextView serviceType;
        public TextView address;
        public TextView expectedTime;
        public TextView currentStatus;
        public TextView regTime;
        public TextView regDate;
        public ImageView refresh;
        public ImageView firmImage;
        private final TextView currentToken;
        private final TextView totalToken;
        public TextView myToken;
        public Button cancelVisit;
        public LinearLayout tokenLayout;
        public LinearLayout feedback;
        public RatingBar ratingBar;
        public ConstraintLayout hiddenLayout;


        public QueueHolder(@NonNull View itemView) {
            super(itemView);
            firmName = itemView.findViewById(R.id.firmName);
            serviceType = itemView.findViewById(R.id.serviceType);
            address = itemView.findViewById(R.id.address);
            expectedTime = itemView.findViewById(R.id.expectedTime);
            currentStatus = itemView.findViewById(R.id.currentStatus);
            regTime = itemView.findViewById(R.id.regTime);
            regDate = itemView.findViewById(R.id.regDate);
            hiddenLayout =itemView.findViewById(R.id.cl_center);
            firmImage = itemView.findViewById(R.id.firmLogo);
            currentToken = itemView.findViewById(R.id.currentToken);
            totalToken = itemView.findViewById(R.id.totalTokens);
            myToken = itemView.findViewById(R.id.myToken);
            cancelVisit = itemView.findViewById(R.id.cancel);
            tokenLayout = itemView.findViewById(R.id.hide);
            feedback = itemView.findViewById(R.id.feedback);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID);

//        FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID).child("10PUbloYl6PZ96HNMPEEo7PbNO62").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//               if (snapshot.exists()){
//                   QueueUserModel queueUserModel=snapshot.getValue(QueueUserModel.class);
//                   Toast.makeText(getContext(),queueUserModel.getFirmName(), Toast.LENGTH_SHORT).show();
//               }else{
//                   Toast.makeText(getContext(), "not found", Toast.LENGTH_SHORT).show();
//               }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


        FirebaseRecyclerOptions<QueueUserModel> options =
                new FirebaseRecyclerOptions.Builder<QueueUserModel>()
                        .setQuery(query, QueueUserModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<QueueUserModel, QueueHolder>(options) {
            @NotNull
            @Override
            public QueueHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_user_queue, parent, false);

                return new QueueHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull QueueHolder holder, final int position, @NotNull QueueUserModel model) {
                holder.firmName.setText(model.getFirmName());
                holder.address.setText(model.getAddress());

                holder.firmName.setText(model.getFirmName());
                holder.serviceType.setText(model.getServiceType());
                holder.address.setText(model.getAddress()+" ("+model.getLocation()+" )");


                RequestOptions options = new RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.ic_shop)
                        .error(R.drawable.noimage)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH)
                        .dontAnimate()
                        .dontTransform();


                Glide.with(getContext())
                        .load(model.getFirmLogoUrl())
                        .apply(options)
                        .into(holder.firmImage);
                holder.regDate.setText("Reg Date\n"+model.getRegDate());
                holder.regTime.setText("Reg. Time\n"+model.getRegTime());



                int expected_time=(Integer.parseInt(model.getMyToken())-Integer.parseInt(model.getCurrentToken()))*Integer.parseInt(model.getAvgServiceTime());
                holder.expectedTime.setText("\n"+ expected_time);
                holder.currentStatus.setText("Current Status\n"+model.getStatus());
                if(model.getStatus().equals("Completed")||model.getStatus().equals("Canceled")||model.getStatus().equals("Suspended")){
                    holder.tokenLayout.setVisibility(View.GONE);
                    holder.expectedTime.setVisibility(View.GONE);
                    holder.refresh.setVisibility(View.GONE);
                    holder.cancelVisit.setText("RATE");
                    holder.feedback.setVisibility(View.GONE);
                }
                holder.myToken.setText(model.getMyToken());
                holder.currentToken.setText(model.getCurrentToken());
                holder.totalToken.setText(model.getTotalToken());
                holder.refresh.setOnClickListener(v -> {
                    FirebaseDatabase.getInstance().getReference().child("ServiceAtLocation")
                            .child(model.getLocation()).child(model.getServiceType()).child(model.getFirmUID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PartnerDataModel partnerDataModel=snapshot.getValue(PartnerDataModel.class);
                                int expected_time=(Integer.parseInt(model.getMyToken())-Integer.parseInt(partnerDataModel.getCurrentToken()))*Integer.parseInt(model.getAvgServiceTime());
                                holder.expectedTime.setText("Your Turn In\n"+ expected_time);
                                holder.totalToken.setText(partnerDataModel.getTotalToken());
                                holder.currentToken.setText(partnerDataModel.getCurrentToken());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });


                holder.cancelVisit.setOnClickListener(v ->{
                    if(holder.cancelVisit.getText().equals("VIEW MORE")){
                        holder.hiddenLayout.setVisibility(View.VISIBLE);

                    }else if(holder.cancelVisit.getText().equals("CANCLE VISIT")){
                        FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID).child("status").setValue("Canceled");
                    }else{
                        FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID)
                                .child("rating").setValue(String.valueOf(holder.ratingBar.getRating()));
                    }

                });



            }

        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
        Toast.makeText(getContext(), "bye", Toast.LENGTH_SHORT).show();
    }
}