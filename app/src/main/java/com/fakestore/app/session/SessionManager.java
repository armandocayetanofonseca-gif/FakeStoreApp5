package com.fakestore.app.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (POO - Encapsulamiento):
 * "prefs" es PRIVATE y "final": nadie de afuera puede tocar
 * SharedPreferences directamente, solo a través de los métodos públicos
 * de esta clase (saveSession, getToken, getRole, clearSessionDeep...).
 * Eso evita que otra parte de la app guarde datos de sesión de forma
 * inconsistente o se le olvide guardar alguno de los 3 valores juntos.
 * ---------------------------------------------------------------------
 * Maneja la persistencia LOCAL de la sesión (token, id de usuario, rol)
 * usando SharedPreferences. Esta es la "variable de sesión local" que
 * menciona el spec: ProductDetailActivity lee el rol de AQUÍ, nunca de
 * la respuesta de la API.
 * ---------------------------------------------------------------------
 */
public class SessionManager {

    private static final String PREFS_NAME = "fakestore_session_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "user_role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Guarda los 3 datos de sesión juntos, en una sola operación. */
    public void saveSession(String token, int userId, Role role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_ROLE, role.name())
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public Role getRole() {
        String stored = prefs.getString(KEY_ROLE, null);
        return stored != null ? Role.valueOf(stored) : null;
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    /**
     * Logout: Limpieza Profunda.
     * Elimina token de SharedPreferences, y también limpia el carrito
     * local (CartLocalStorage), dejando la sesión completamente vacía.
     */
    public void clearSessionDeep(Context context) {
        prefs.edit().clear().apply();
        CartLocalStorage.clear(context);
    }

    /**
     * NOTA DE CLASE (enum, no clase "normal"):
     * Un enum es una lista fija y cerrada de valores posibles (aquí,
     * los 3 roles que existen en la app). Java garantiza que "Role"
     * SOLO puede valer ADMIN, AUDITOR o CLIENT, nunca otra cosa: eso
     * hace innecesario validar "¿el texto que llegó es un rol válido?"
     * en cualquier otro lado del código.
     */
    public enum Role {
        ADMIN,
        AUDITOR,
        CLIENT;

        /**
         * Sección 3 del spec: el rol se decide en código a partir del ID
         * del usuario devuelto tras un login exitoso (200 OK).
         *   IDs 1 y 2  -> Administrador
         *   ID 3       -> Auditor
         *   Resto      -> Cliente
         */
        public static Role fromUserId(int userId) {
            if (userId == 1 || userId == 2) {
                return ADMIN;
            } else if (userId == 3) {
                return AUDITOR;
            } else {
                return CLIENT;
            }
        }
    }
}
