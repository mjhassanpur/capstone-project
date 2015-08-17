package com.github.mjhassanpur.gittrend.api;

import com.google.gson.annotations.SerializedName;

public class Repository {

    public String name;
    @SerializedName("full_name")
    public String fullName;
    public User owner;
    @SerializedName("html_url")
    public String htmlUrl;
    public String description;
    @SerializedName("stargazers_count")
    public int stars;
    public String language;
    @SerializedName("forks_count")
    public int forks;
}
