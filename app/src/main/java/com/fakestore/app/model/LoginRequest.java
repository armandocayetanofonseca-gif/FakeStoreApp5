package com.fakestore.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Cuerpo (body) que se manda en el POST /auth/login.
 *
 * El password SÍ viaja aquí (es indispensable para poder autenticar),
 * pero solo vive dentro de este objeto temporal, mientras se arma la
 * petición. Nunca se guarda en SessionManager ni en ningún otro lado
 * de la app: en cuanto Retrofit termina de mandar la petición, este
 * objeto ya no se vuelve a usar.
 */
public class LoginRequest {

    @SerializedName("username")
    private final String username;

    @SerializedName("password")
    private final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
