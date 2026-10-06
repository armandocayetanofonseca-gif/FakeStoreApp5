package com.fakestore.app.model;

import com.google.gson.annotations.SerializedName;

/** Respuesta del POST /auth/login: solo trae el token de sesión. */
public class LoginResponse {

    @SerializedName("token")
    private final String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
