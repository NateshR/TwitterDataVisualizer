package com.nateshr.twitterdatavisualizer.JavaClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by natesh on 2/5/15.
 */
public class Status {

    @SerializedName("text")
    public String text;

    @SerializedName("user")
    public User user;

    @SerializedName("geo")
    public Geo geo;

    @SerializedName("retweeted_status")
    public RetweetedStatus retweetedStatus;

}
