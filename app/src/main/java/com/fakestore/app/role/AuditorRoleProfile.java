package com.fakestore.app.role;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.fakestore.app.R;

/**
 * NOTA DE CLASE (POO - Herencia + Polimorfismo):
 * Misma idea que AdminRoleProfile: hereda de RoleProfile y sobrescribe
 * (@Override) cada método abstracto con la versión que le corresponde
 * al rol Auditor.
 */
public class AuditorRoleProfile extends RoleProfile {

    @Override
    public String getEmoji() {
        return "⭐";
    }

    @Override
    public String getLabel() {
        return "Auditor";
    }

    @Override
    public int getAccentColor(Context context) {
        return ContextCompat.getColor(context, R.color.role_auditor_accent);
    }

    @Override
    public int getPillBackgroundColor(Context context) {
        return ContextCompat.getColor(context, R.color.role_auditor_pill_bg);
    }
}
