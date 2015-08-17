package com.github.mjhassanpur.gittrend.api;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("login")
    public String name;
    @SerializedName("avatar_url")
    public String avatarUrl;
    @SerializedName("html_url")
    public String htmlUrl;
}
