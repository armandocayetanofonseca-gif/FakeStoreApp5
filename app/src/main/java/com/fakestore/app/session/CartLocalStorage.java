package com.fakestore.app.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Carrito local del rol Cliente. Se limpia por completo durante el
 * logout ("Limpieza Profunda"), junto con el resto de la sesión.
 *
 * NOTA: esta clase todavía no guarda productos de verdad (no forma
 * parte del spec v2); solo existe el método clear() para cumplir con
 * la regla de "limpiar carrito local" al cerrar sesión.
 */
public class CartLocalStorage {

    private static final String CART_PREFS_NAME = "fakestore_cart_prefs";

    private CartLocalStorage() {
    }

    public static void clear(Context context) {
        SharedPreferences cartPrefs =
                context.getSharedPreferences(CART_PREFS_NAME, Context.MODE_PRIVATE);
        cartPrefs.edit().clear().apply();
    }
}
