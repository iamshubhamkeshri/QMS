package com.spcodelab.qms.models;

public class QueuePartnerModel {
    String customerName,email,purpose,customerImage,regTime,status,customerTokenNumber,customerUID;

    public QueuePartnerModel(String customerName, String email, String purpose, String customerImage, String regTime, String status, String customerTokenNumber, String customerUID) {
        this.customerName = customerName;
        this.email = email;
        this.purpose = purpose;
        this.customerImage = customerImage;
        this.regTime = regTime;
        this.status = status;
        this.customerTokenNumber = customerTokenNumber;
        this.customerUID = customerUID;
    }

    public QueuePartnerModel() {
        //required to receive data
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getEmail() {
        return email;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getCustomerImage() {
        return customerImage;
    }

    public String getRegTime() {
        return regTime;
    }

    public String getStatus() {
        return status;
    }

    public String getCustomerTokenNumber() {
        return customerTokenNumber;
    }

    public String getCustomerUID() {
        return customerUID;
    }
}
