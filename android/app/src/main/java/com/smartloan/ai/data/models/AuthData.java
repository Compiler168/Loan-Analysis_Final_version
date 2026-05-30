package com.smartloan.ai.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Auth response containing token and user info.
 */
public class AuthData {

    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    @SerializedName("firebaseCustomToken")
    private String firebaseCustomToken;

    public String getToken() { return token; }
    public String getFirebaseCustomToken() { return firebaseCustomToken; }
    public User getUser() { return user; }
}
