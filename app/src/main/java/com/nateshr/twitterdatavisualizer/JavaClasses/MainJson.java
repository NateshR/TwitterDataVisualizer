package com.nateshr.twitterdatavisualizer.JavaClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by natesh on 2/5/15.
 */
public class MainJson {

    @SerializedName("statuses")
    public Status[] statuses;
}
