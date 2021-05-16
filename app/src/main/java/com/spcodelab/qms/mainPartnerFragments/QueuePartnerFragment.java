package com.spcodelab.qms.mainPartnerFragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.spcodelab.qms.models.QueuePartnerModel;
import com.spcodelab.qms.models.QueueStatusModel;
import com.wang.avi.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.spcodelab.qms.CommanClass.alertNoConnection;
import static com.spcodelab.qms.CommanClass.isNetworkAvailable;


public class QueuePartnerFragment extends Fragment {

    String currentPartnerUID;
    CalendarView calendarView;
    Button openCalender;
    TextView totalToken;
    TextView currentToken;
    String canceledTokens;
    int intTotalToken, intCurrentToken;
    LinearLayout calenderLayout;
    TextView reset;

    String date;

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private AVLoadingIndicatorView avLoadingIndicatorView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_queue_partner, container, false);

        //current date
        date = new SimpleDateFormat("yyyy-M-dd", Locale.getDefault()).format(new Date());

        totalToken = view.findViewById(R.id.totalTokens);
        currentToken = view.findViewById(R.id.currentToken);


        openCalender = view.findViewById(R.id.selectedDate);
        calenderLayout = view.findViewById(R.id.calenderLayout);
        openCalender.setText(date);
        calendarView = view.findViewById(R.id.calender);

        openCalender.setOnClickListener(v -> {
            if (calenderLayout.getVisibility() == View.GONE) {
                calenderLayout.setVisibility(View.VISIBLE);
            } else {
                calenderLayout.setVisibility(View.GONE);
            }
        });


        recyclerView = view.findViewById(R.id.partnerQueue);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        if (isNetworkAvailable(getContext())) {
            currentPartnerUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            getQueueStatus();
            fetchQuery(date);

        } else
            alertNoConnection(getContext());

        Calendar calendar = Calendar.getInstance();

        calendarView.setMaxDate(calendar.getTimeInMillis());
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selecdtedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            openCalender.setText(selecdtedDate);
            calenderLayout.setVisibility(View.GONE);
            fetchQuery(selecdtedDate.trim());
            firebaseRecyclerAdapter.startListening();

        });

        reset = view.findViewById(R.id.resetQueue);
        reset.setOnClickListener(v -> {
            //setting Queue Status
            new AlertDialog.Builder(getContext())
                    .setTitle("Title")
                    .setMessage("Do you really want to Reset Queue?\nIs this end of the day?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            QueueStatusModel mQueueStatusData = new QueueStatusModel("0", "NA", "null");
                            FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(currentPartnerUID)
                                    .setValue(mQueueStatusData);
                            Toast.makeText(getContext(), "Queue Reseted", Toast.LENGTH_SHORT).show();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();

        });


        return view;
    }


    private void getQueueStatus() {
        FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(currentPartnerUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    QueueStatusModel qModel = snapshot.getValue(QueueStatusModel.class);
                    totalToken.setText(qModel.getTotalToken());
                    currentToken.setText(qModel.getCurrentToken());
                    intTotalToken = Integer.parseInt(qModel.getTotalToken());
                    if (qModel.getCurrentToken().equals("NA")) {
                        intCurrentToken = intTotalToken;
                    } else {
                        intCurrentToken = Integer.parseInt(qModel.getCurrentToken());
                    }
                    canceledTokens = qModel.getCanceledToken();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchQuery(String onDate) {
        Query query = FirebaseDatabase.getInstance().getReference().child("QueuePartner").child(currentPartnerUID).child(onDate).orderByChild("customerTokenNumber");

        FirebaseRecyclerOptions<QueuePartnerModel> options =
                new FirebaseRecyclerOptions.Builder<QueuePartnerModel>()
                        .setQuery(query, QueuePartnerModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<QueuePartnerModel, QueueHolder>(options) {
            @NotNull
            @Override
            public QueueHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_partner_queue, parent, false);

                return new QueueHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NotNull QueueHolder holder, final int position, @NotNull QueuePartnerModel model) {

                holder.cusName.setText(model.getCustomerName());
                holder.cusEmail.setText(model.getEmail());
                holder.purposeOfVisit.setText("Purpose : " + model.getPurpose());


                RequestOptions options = new RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.ic_shop)
                        .error(R.drawable.noimage)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH)
                        .dontAnimate()
                        .dontTransform();


                Glide.with(getContext())
                        .load(model.getCustomerImage())
                        .apply(options)
                        .into(holder.cusImage);
                holder.cusToken.setText(model.getCustomerTokenNumber());
                holder.regTime.setText("Reg. Time\n" + model.getRegTime());
                holder.status.setText("Current Status\n" + model.getStatus());


                if (model.getStatus().equals("Completed") || model.getStatus().equals("Canceled")) {
                    holder.hideLayout.setVisibility(View.GONE);
                }

                holder.cancel.setOnClickListener(v -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Title")
                            .setMessage("Do you really want to cancel visit of " + model.getCustomerName() + " ?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    FirebaseDatabase.getInstance().getReference().child("QueueUser")
                                            .child(model.getCustomerUID()).child(currentPartnerUID).child("status").setValue("Canceled");

                                    FirebaseDatabase.getInstance().getReference().child("QueuePartner").child(currentPartnerUID)
                                            .child(onDate).child(model.getCustomerUID()).child("status").setValue("Canceled");

                                    if (canceledTokens.equals("null")) {
                                        canceledTokens = null;
                                        canceledTokens = model.getCustomerTokenNumber();
                                    } else {
                                        canceledTokens = canceledTokens + "," + model.getCustomerTokenNumber();
                                    }

                                    //updating current token as NA if this token is last token
                                    if (intCurrentToken == Integer.parseInt(model.getCustomerTokenNumber())) {
                                        //FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(currentPartnerUID).child("currentToken").setValue("NA");
                                        updateCurrentToken();
                                    }

                                    FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(currentPartnerUID)
                                            .child("canceledToken").setValue(canceledTokens);
                                    Toast.makeText(getContext(), "Visit canceled of : " + model.getCustomerName(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();

                });

                holder.complete.setOnClickListener(v -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Title")
                            .setMessage("Do you really want to mark visit as complete for " + model.getCustomerName() + " ?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {

                                if (intCurrentToken == Integer.parseInt(model.getCustomerTokenNumber())) {
                                    FirebaseDatabase.getInstance().getReference().child("QueueUser")
                                            .child(model.getCustomerUID()).child(currentPartnerUID).child("status").setValue("Completed");

                                    FirebaseDatabase.getInstance().getReference().child("QueuePartner").child(currentPartnerUID)
                                            .child(onDate).child(model.getCustomerUID()).child("status").setValue("Completed");

                                    updateCurrentToken();

                                } else {
                                    Toast.makeText(getContext(), "This is not Current User", Toast.LENGTH_SHORT).show();
                                }

                            })
                            .setNegativeButton(android.R.string.no, null).show();
                });

                holder.notify.setOnClickListener(v -> {
                    FirebaseDatabase.getInstance().getReference().child("AlertUser").child(model.getCustomerUID()).setValue("1");
                    Toast.makeText(getContext(), "User Notified", Toast.LENGTH_SHORT).show();
                });


            }


        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);

//        avLoadingIndicatorView.setVisibility(View.GONE);
//        avLoadingIndicatorView.hide();
    }

    private void updateCurrentToken() {
        List<Integer> intCanceledTokenList = new ArrayList<Integer>();
        if (!canceledTokens.equals("null")) {
            String[] canceledTokenList = canceledTokens.split("\\s*,\\s*");
            for (String s : canceledTokenList) intCanceledTokenList.add(Integer.valueOf(s));
        }


        if (intCurrentToken != intTotalToken) {
            for (int i = intCurrentToken + 1; i <= intTotalToken; i++) {

                if (!intCanceledTokenList.contains(i)) {
                    FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(currentPartnerUID).child("currentToken").
                            setValue(String.valueOf(i));
                    break;
                }
            }
        } else {
            FirebaseDatabase.getInstance().getReference().child("QueueStatus").child(currentPartnerUID).child("currentToken").
                    setValue("NA");
        }


    }

    static class QueueHolder extends RecyclerView.ViewHolder {
        public TextView cusName;
        public TextView cusEmail;
        public TextView purposeOfVisit;
        public ImageView cusImage;
        public TextView cusToken;
        public TextView regTime;
        public TextView status;
        public Button complete;
        public Button cancel;
        public Button notify;
        public LinearLayout hideLayout;


        public QueueHolder(@NonNull View itemView) {
            super(itemView);
            cusName = itemView.findViewById(R.id.cusName);
            cusEmail = itemView.findViewById(R.id.cusEmail);
            purposeOfVisit = itemView.findViewById(R.id.cusPurpose);
            cusImage = itemView.findViewById(R.id.cusImage);
            cusToken = itemView.findViewById(R.id.cusCurrentToken);
            regTime = itemView.findViewById(R.id.cusRegTime);
            status = itemView.findViewById(R.id.cusCurrentStatus);
            complete = itemView.findViewById(R.id.cusComplete);
            cancel = itemView.findViewById(R.id.cusCancel);
            notify = itemView.findViewById(R.id.cusNotify);
            hideLayout = itemView.findViewById(R.id.hideLayout);
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