package com.fakestore.app.session;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.fakestore.app.ui.LoginActivity;

/**
 * "Logout: Limpieza Profunda":
 * 1. Muestra un micro-loader de 0.5s.
 * 2. Limpia SessionManager (token/id/rol) y el carrito local.
 * 3. Redirige a Login reiniciando la pila de navegación por completo
 *    (con esto, el botón "Atrás" ya no puede regresar al catálogo o al
 *    perfil cacheados).
 */
public class LogoutHelper {

    private static final long MICRO_LOADER_DURATION_MS = 500L;

    private LogoutHelper() {
    }

    public static void performLogout(Activity activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Cerrando sesión...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // postDelayed: espera 500ms (el "micro-loader") antes de
        // ejecutar el bloque de limpieza + navegación.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager sessionManager = new SessionManager(activity);
            sessionManager.clearSessionDeep(activity);

            progressDialog.dismiss();

            Intent intent = new Intent(activity, LoginActivity.class);
            // Estas 2 flags juntas destruyen todo el historial de
            // pantallas anteriores y empiezan una pila nueva desde Login.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            // Transición fade: sensación de "cerrar y reiniciar" la sesión.
            activity.overridePendingTransition(com.fakestore.app.R.anim.fade_in, com.fakestore.app.R.anim.fade_out);
            activity.finish();
        }, MICRO_LOADER_DURATION_MS);
    }
}
