package com.spcodelab.qms.models;

public class PartnerRatingModel {
    String rating,ratedBy;

    public PartnerRatingModel(String rating, String ratedBy) {
        this.rating = rating;
        this.ratedBy = ratedBy;
    }

    public PartnerRatingModel() {
        //requires to receive data
    }

    public String getRating() {
        return rating;
    }

    public String getRatedBy() {
        return ratedBy;
    }
}
