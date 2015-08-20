package com.github.mjhassanpur.gittrend.api;

import com.google.gson.annotations.SerializedName;

public class Week {

    @SerializedName("w")
    public int week;
    @SerializedName("a")
    public int additions;
    @SerializedName("d")
    public int deletions;
    @SerializedName("c")
    public int commits;
}
