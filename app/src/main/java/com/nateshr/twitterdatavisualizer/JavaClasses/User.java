package com.nateshr.twitterdatavisualizer.JavaClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by natesh on 2/5/15.
 */
public class User {

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;
}
