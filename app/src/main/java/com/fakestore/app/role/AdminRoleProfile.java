package com.fakestore.app.role;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.fakestore.app.R;

/**
 * NOTA DE CLASE (POO - Herencia + Polimorfismo):
 * "extends RoleProfile" significa que AdminRoleProfile HEREDA de
 * RoleProfile: obtiene su "forma" (los 4 métodos abstractos) y está
 * obligada a darles una implementación concreta con @Override.
 * Cuando en ProfileActivity se llama a profile.getEmoji() y "profile"
 * resulta ser un AdminRoleProfile, Java ejecuta ESTA versión y no otra:
 * eso es polimorfismo en acción.
 */
public class AdminRoleProfile extends RoleProfile {

    @Override
    public String getEmoji() {
        return "👑";
    }

    @Override
    public String getLabel() {
        return "Administrador";
    }

    @Override
    public int getAccentColor(Context context) {
        return ContextCompat.getColor(context, R.color.role_admin_accent);
    }

    @Override
    public int getPillBackgroundColor(Context context) {
        return ContextCompat.getColor(context, R.color.role_admin_pill_bg);
    }
}
