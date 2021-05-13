package com.spcodelab.qms.mainPartnerFragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.spcodelab.qms.R;

import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class QrFragment extends Fragment {
    ImageView Qr;
    Bitmap bitmap;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qr, container, false);

        Qr = view.findViewById(R.id.qr);

        String currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        QRGEncoder qrgEncoder = new QRGEncoder(currentUserUID, null, QRGContents.Type.TEXT, 150);

        bitmap = qrgEncoder.getBitmap();
        Qr.setImageBitmap(bitmap);

        return view;

    }


}