package com.github.mjhassanpur.gittrend.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Contributor {

    @SerializedName("total")
    public int commits;
    public List<Week> weeks;
    public User author;
}
