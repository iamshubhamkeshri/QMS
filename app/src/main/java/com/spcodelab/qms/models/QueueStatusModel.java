package com.spcodelab.qms.models;

public class QueueStatusModel {
    String totalToken,currentToken,canceledToken;

    public QueueStatusModel(String totalToken, String currentToken, String canceledToken) {
        this.totalToken = totalToken;
        this.currentToken = currentToken;
        this.canceledToken = canceledToken;
    }

    public QueueStatusModel() {
        //Required to receive data
    }

    public String getTotalToken() {
        return totalToken;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public String getCanceledToken() {
        return canceledToken;
    }
}
