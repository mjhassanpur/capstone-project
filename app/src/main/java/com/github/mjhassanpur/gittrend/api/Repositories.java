package com.github.mjhassanpur.gittrend.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Repositories {

    @SerializedName("items")
    public List<Repository> repositories;
}
