package com.spcodelab.qms.models;

public class PartnerDataModel {
    public String firmName, serviceType, location, address, email, imageUrl, uid, averageServiceTime,currentToken,totalToken,rating,ratedBy;

    public PartnerDataModel(String firmName, String serviceType, String location, String address, String email, String imageUrl,
                            String uid, String averageServiceTime,String currentToken, String totalToken,String rating, String ratedBy) {
        this.firmName = firmName;
        this.serviceType = serviceType;
        this.location = location;
        this.address = address;
        this.email = email;
        this.imageUrl = imageUrl;
        this.uid = uid;
        this.averageServiceTime = averageServiceTime;
        this.currentToken = currentToken;
        this.totalToken = totalToken;
        this.rating = rating;
        this.ratedBy = ratedBy;
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

    public String getCurrentToken() {
        return currentToken;
    }

    public String getTotalToken() {
        return totalToken;
    }

    public String getRating() {
        return rating;
    }

    public String getRatedBy() {
        return ratedBy;
    }
}
