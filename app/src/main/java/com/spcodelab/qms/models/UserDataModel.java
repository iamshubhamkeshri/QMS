package com.spcodelab.qms.models;

import androidx.annotation.Keep;

@Keep
public class UserDataModel {
    public String name, dob, email, imageUrl,address;

    public UserDataModel(String name, String dob, String email, String imageUrl, String address) {
        this.name = name;
        this.dob = dob;
        this.email = email;
        this.imageUrl = imageUrl;
        this.address = address;
    }


    public UserDataModel() {
        //This is needed for getting data
    }
}
