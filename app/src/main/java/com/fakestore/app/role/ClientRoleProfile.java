package com.fakestore.app.role;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.fakestore.app.R;

/**
 * NOTA DE CLASE (POO - Herencia + Polimorfismo):
 * Tercera y última hija de RoleProfile. Con las 3 subclases completas
 * (Admin, Auditor, Cliente), ProfileActivity nunca necesita preguntar
 * "¿qué rol es?" con if/else: simplemente confía en que "profile"
 * (sea quien sea) sabe responder por sí mismo.
 */
public class ClientRoleProfile extends RoleProfile {

    @Override
    public String getEmoji() {
        return "🛒";
    }

    @Override
    public String getLabel() {
        return "Cliente";
    }

    @Override
    public int getAccentColor(Context context) {
        return ContextCompat.getColor(context, R.color.role_client_accent);
    }

    @Override
    public int getPillBackgroundColor(Context context) {
        return ContextCompat.getColor(context, R.color.role_client_pill_bg);
    }
}
