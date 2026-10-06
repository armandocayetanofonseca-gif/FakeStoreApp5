package com.fakestore.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Clase de utilidad (solo tiene métodos "static", nunca se instancia).
 * Sirve para el escenario "Login: Sin Conexión": antes de llamar a la
 * API, LoginActivity le pregunta a esta clase "¿hay internet?" y, si la
 * respuesta es que no, se detiene ahí mismo (sin loaders infinitos).
 */
public class NetworkUtils {

    private NetworkUtils() {
    }

    @SuppressWarnings("deprecation")
    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        // Android 6.0 (API 23) en adelante usa una API distinta (más
        // nueva) para revisar la conexión; por eso hay 2 caminos.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }
}
