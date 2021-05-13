package com.spcodelab.qms.mainPartnerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.spcodelab.qms.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class QueuePartnerFragment extends Fragment {

    String currentUID, date;
    RecyclerView recyclerView;
    CalendarView calendarView;
    Button openCalender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_queue_partner, container, false);

        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //current date
        date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());

        calendarView = view.findViewById(R.id.calander);
        openCalender = view.findViewById(R.id.selectedDate);
        openCalender.setText(date);
        openCalender.setOnClickListener(v -> {
            if (calendarView.getVisibility() == View.GONE) {
                calendarView.setVisibility(View.VISIBLE);
            } else {
                calendarView.setVisibility(View.GONE);
            }
        });

        //openCalender.setText(String.valueOf((int) calendarView.getDate()));
        recyclerView = view.findViewById(R.id.partnerQueue);


        return view;
    }
}