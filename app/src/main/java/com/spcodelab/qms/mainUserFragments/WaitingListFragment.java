package com.spcodelab.qms.mainUserFragments;

import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
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
import com.spcodelab.qms.models.PartnerRatingModel;
import com.spcodelab.qms.models.QueueStatusModel;
import com.spcodelab.qms.models.QueueUserModel;
import com.wang.avi.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.spcodelab.qms.CommanClass.alertNoConnection;
import static com.spcodelab.qms.CommanClass.isNetworkAvailable;

public class WaitingListFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    String currentUserUID;
    String canceledTokens;
    int currentToken, totalToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        recyclerView = view.findViewById(R.id.waitingList);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        if (isNetworkAvailable(getContext())) {
            currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            fetchQuery();
        } else
            alertNoConnection(getContext());
        return view;
    }

    private void fetchQuery() {
        Query query = FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID).orderByChild("regDate");

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
                holder.address.setText(model.getAddress() + " ( " + model.getLocation() + " )");
                holder.purpose.setText("Purpose: " + model.getPurposeOfVisit());
                holder.currentStatus.setText("Current Status\n" + model.getStatus());


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
                holder.regDate.setText("Reg Date\n" + model.getRegDate());
                holder.regTime.setText("Reg. Time\n" + model.getRegTime());


                if (model.getStatus().equals("Completed") || model.getStatus().equals("Canceled")) {
                    holder.hiddenLayout.setVisibility(View.GONE);
                    holder.button.setText("RATE NOW");
                    holder.feedback.setVisibility(View.VISIBLE);
                    if (!model.getRating().equals("0")) {
                        holder.ratingBar.setEnabled(false);
                        holder.button.setVisibility(View.GONE);
                        holder.ratingBar.setRating(Float.parseFloat(model.getRating()));
                    }
                }


                holder.myToken.setText(model.getMyToken());
                FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(model.getFirmUID())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    QueueStatusModel queueStatusModel = snapshot.getValue(QueueStatusModel.class);
                                    canceledTokens = queueStatusModel.getCanceledToken();
                                    totalToken = Integer.parseInt(queueStatusModel.getTotalToken());
                                    if (queueStatusModel.getCurrentToken().equals("NA")) {
                                        currentToken = totalToken;
                                    } else {
                                        currentToken = Integer.parseInt(queueStatusModel.getCurrentToken());
                                    }
                                    int expected_time = (Integer.parseInt(model.getMyToken()) - currentToken) * Integer.parseInt(model.getAvgServiceTime());
                                    if (expected_time > 0) {
                                        holder.expectedTime.setText("Your Turn In -\n" + expected_time + " min");
                                    } else if (expected_time == 0) {
                                        holder.expectedTime.setText("Its Your Turn");
                                    } else {
                                        holder.expectedTime.setText("Your Turn Is -\n" + "Missed");
                                    }
                                    holder.totalToken.setText(queueStatusModel.getTotalToken());
                                    holder.currentToken.setText(queueStatusModel.getCurrentToken());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }

                        });


                holder.button.setOnClickListener(v -> {
                    if (holder.button.getText().equals("CANCEL VISIT")) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Title")
                                .setMessage("Do you really want to cancel visit at " + model.getFirmName() + " ?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        FirebaseDatabase.getInstance().getReference().child("QueueUser")
                                                .child(currentUserUID).child(model.getFirmUID()).child("status").setValue("Canceled");

                                        FirebaseDatabase.getInstance().getReference().child("QueuePartner").child(model.getFirmUID())
                                                .child(model.getRegDate()).child(currentUserUID).child("status").setValue("Canceled");

                                        if (canceledTokens.equals("null")) {
                                            canceledTokens = null;
                                            canceledTokens = model.getMyToken();
                                        } else {
                                            canceledTokens = canceledTokens + "," + model.getMyToken();
                                        }

                                        //updating current token as NA if this token is last token
                                        if (currentToken == Integer.parseInt(model.getMyToken())) {
                                            //FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(model.getFirmUID()).child("currentToken").setValue("NA");

                                            List<Integer> intCanceledTokenList = new ArrayList<Integer>();
                                            if (!canceledTokens.equals("null")) {
                                                String[] canceledTokenList = canceledTokens.split("\\s*,\\s*");
                                                for (String s : canceledTokenList)
                                                    intCanceledTokenList.add(Integer.valueOf(s));
                                            }


                                            if (currentToken != totalToken) {
                                                for (int i = currentToken + 1; i <= totalToken; i++) {

                                                    if (!intCanceledTokenList.contains(i)) {
                                                        FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(model.getFirmUID()).child("currentToken").
                                                                setValue(String.valueOf(i));
                                                        break;
                                                    }
                                                }
                                            } else {
                                                FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(model.getFirmUID()).child("currentToken").
                                                        setValue("NA");
                                            }
                                        }

                                        FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(model.getFirmUID())
                                                .child("canceledToken").setValue(canceledTokens);

                                        Toast.makeText(getContext(), "Your Visit is Canceled", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();

                    } else if (holder.button.getText().equals("RATE NOW")) {
                        if (holder.ratingBar.getRating() != 0 && model.getRating().equals("0")) {
                            holder.button.setEnabled(false);
                            FirebaseDatabase.getInstance().getReference().child("PartnerRating").child(model.getFirmUID())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                PartnerRatingModel rModel = snapshot.getValue(PartnerRatingModel.class);
                                                double rating = Double.parseDouble(rModel.getRating());
                                                int ratedBy = Integer.parseInt(rModel.getRatedBy());
                                                rating = ((rating * ratedBy) + holder.ratingBar.getRating()) / (ratedBy + 1);
                                                ratedBy = ratedBy + 1;
                                                PartnerRatingModel rUpdateModel = new PartnerRatingModel(String.valueOf(rating), String.valueOf(ratedBy));
                                                FirebaseDatabase.getInstance().getReference().child("PartnerRating").child(model.getFirmUID()).setValue(rUpdateModel);
                                                FirebaseDatabase.getInstance().getReference().child("QueueUser").child(currentUserUID).child(model.getFirmUID()).child("rating").setValue(String.valueOf(rating));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
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
        public TextView purpose;
        public TextView expectedTime;
        public TextView currentStatus;
        public TextView regTime;
        public TextView regDate;
        public ImageView firmImage;
        private final TextView currentToken;
        private final TextView totalToken;
        public TextView myToken;
        public Button button;
        public LinearLayout feedback;
        public RatingBar ratingBar;
        public ConstraintLayout hiddenLayout;


        public QueueHolder(@NonNull View itemView) {
            super(itemView);
            firmName = itemView.findViewById(R.id.firmName);
            serviceType = itemView.findViewById(R.id.serviceType);
            address = itemView.findViewById(R.id.address);
            purpose = itemView.findViewById(R.id.purpose);
            expectedTime = itemView.findViewById(R.id.expectedTime);
            currentStatus = itemView.findViewById(R.id.currentStatus);
            regTime = itemView.findViewById(R.id.regTime);
            regDate = itemView.findViewById(R.id.regDate);
            hiddenLayout = itemView.findViewById(R.id.cl_center);
            firmImage = itemView.findViewById(R.id.firmLogo);
            currentToken = itemView.findViewById(R.id.currentToken);
            totalToken = itemView.findViewById(R.id.totalTokens);
            myToken = itemView.findViewById(R.id.myToken);
            button = itemView.findViewById(R.id.button);
            feedback = itemView.findViewById(R.id.feedback);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }
}