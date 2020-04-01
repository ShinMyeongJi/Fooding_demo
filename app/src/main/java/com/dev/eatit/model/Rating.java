package com.dev.eatit.model;

public class Rating {
    private String userPhone;
    private String foodId;
    private String rateVlaue;
    private String comment;

    public Rating() {
    }

    public Rating(String userPhone, String foodId, String rateVlaue, String comment) {
        this.userPhone = userPhone;
        this.foodId = foodId;
        this.rateVlaue = rateVlaue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getRateVlaue() {
        return rateVlaue;
    }

    public void setRateVlaue(String rateVlaue) {
        this.rateVlaue = rateVlaue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
