package com.volcengine.vertcdemo.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedbackType {
    @SerializedName("name")
    private String name;
    @SerializedName("feedback_type_list")
    private List<FeedbackType> feedback_type_list;
    @Expose(serialize = false,deserialize = false)
    private boolean mSelected;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFeedback_type_list(List<FeedbackType> feedback_type_list) {
        this.feedback_type_list = feedback_type_list;
    }

    public List<FeedbackType> getFeedback_type_list() {
        return feedback_type_list;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }

}
