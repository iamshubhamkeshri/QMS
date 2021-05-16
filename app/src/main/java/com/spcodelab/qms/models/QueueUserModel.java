package com.spcodelab.qms.models;

public class QueueUserModel {
    String firmName, firmUID, serviceType, location, address, firmLogoUrl, regDate, regTime, status, avgServiceTime, myToken, rating, purposeOfVisit;

    public QueueUserModel(String firmName, String firmUID, String serviceType, String location, String address, String firmLogoUrl, String regDate,
                          String regTime, String status, String avgServiceTime , String myToken, String purposeOfVisit, String rating) {
        this.firmName = firmName;
        this.firmUID = firmUID;
        this.serviceType = serviceType;
        this.location = location;
        this.address = address;
        this.firmLogoUrl = firmLogoUrl;
        this.regDate = regDate;
        this.regTime = regTime;
        this.status = status;
        this.avgServiceTime = avgServiceTime;
        this.myToken = myToken;
        this.purposeOfVisit = purposeOfVisit;
        this.rating=rating;
    }

    public QueueUserModel() {
        //required to receive data
    }

    public String getFirmName() {
        return firmName;
    }

    public String getFirmUID() {
        return firmUID;
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

    public String getFirmLogoUrl() {
        return firmLogoUrl;
    }

    public String getRegDate() {
        return regDate;
    }

    public String getRegTime() {
        return regTime;
    }

    public String getStatus() { return status; }

    public String getAvgServiceTime() {
        return avgServiceTime;
    }

    public String getMyToken() {
        return myToken;
    }

    public String getRating() { return rating; }

    public String getPurposeOfVisit() { return purposeOfVisit; }
}
