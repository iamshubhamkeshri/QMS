package com.spcodelab.qms.models;

public class QueueUserModel {
    String firmName, firmUID, serviceType, location, address, firmLogoUrl, regDate, regTime, status, avgServiceTime, currentToken, myToken, totalToken, rating, purposeOfVisit;

    public QueueUserModel(String firmName, String firmUID, String serviceType, String location, String address, String firmLogoUrl, String regDate,
                          String regTime, String status, String avgServiceTime, String currentToken, String myToken, String totalToken, String purposeOfVisit) {
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
        this.currentToken = currentToken;
        this.myToken = myToken;
        this.totalToken = totalToken;
        this.purposeOfVisit = purposeOfVisit;
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

    public String getStatus() {
        return status;
    }

    public String getAvgServiceTime() {
        return avgServiceTime;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public String getMyToken() {
        return myToken;
    }

    public String getTotalToken() {
        return totalToken;
    }

    public String getRating() {
        return rating;
    }

    public String getPurposeOfVisit() { return purposeOfVisit; }
}
