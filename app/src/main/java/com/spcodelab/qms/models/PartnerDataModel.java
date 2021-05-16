package com.spcodelab.qms.models;

public class PartnerDataModel {
    public String firmName, serviceType, location, address, email, imageUrl, uid, averageServiceTime;

    public PartnerDataModel(String firmName, String serviceType, String location, String address,
                            String email, String imageUrl, String uid, String averageServiceTime) {
        this.firmName = firmName;
        this.serviceType = serviceType;
        this.location = location;
        this.address = address;
        this.email = email;
        this.imageUrl = imageUrl;
        this.uid = uid;
        this.averageServiceTime = averageServiceTime;
    }


    public PartnerDataModel() {
        //needed to receive data
    }

    public String getFirmName() {
        return firmName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getLocation() {
        return location;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getAverageServiceTime() {
        return averageServiceTime;
    }
}
