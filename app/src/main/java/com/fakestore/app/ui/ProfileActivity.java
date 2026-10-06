package com.fakestore.app.ui;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fakestore.app.R;
import com.fakestore.app.controller.ProfileController;
import com.fakestore.app.model.User;
import com.fakestore.app.role.RoleProfile;
import com.fakestore.app.session.LogoutHelper;
import com.fakestore.app.session.SessionManager;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (separación Vista / Controlador):
 * Pantalla única "Mi Perfil", compartida por los 3 roles. Esta clase es
 * la "Vista": pinta los TextViews, resuelve la insignia por rol (usando
 * el polimorfismo de RoleProfile, que SÍ sigue viviendo aquí porque es
 * 100% visual: decide colores/íconos, no hace llamadas de red) y
 * reacciona a lo que el {@link ProfileController} le avisa a través de
 * la interface ProfileController.ProfileView.
 *
 * La llamada a la API (getUsers, buscar al usuario actual) ya NO vive
 * aquí: se movió a ProfileController.loadOwnProfile().
 *
 * REGLA DE NEGOCIO: el campo password nunca se solicita ni se muestra aquí
 * (User.java no lo define, ver modelo).
 * ---------------------------------------------------------------------
 */
public class ProfileActivity extends AppCompatActivity implements ProfileController.ProfileView {

    private ProfileController controller;

    private TextView tvAvatarInitial;
    private TextView tvRoleBadgeIcon;
    private TextView tvFullName;
    private View pillRole;
    private TextView tvRolePillIcon;
    private TextView tvRolePillLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        controller = new ProfileController(this, this);

        tvAvatarInitial = findViewById(R.id.tvAvatarInitial);
        tvRoleBadgeIcon = findViewById(R.id.tvRoleBadgeIcon);
        tvFullName = findViewById(R.id.tvFullName);
        pillRole = findViewById(R.id.pillRole);
        tvRolePillIcon = findViewById(R.id.tvRolePillIcon);
        tvRolePillLabel = findViewById(R.id.tvRolePillLabel);

        ImageButton btnLogoutIcon = findViewById(R.id.btnLogoutIcon);
        View btnLogout = findViewById(R.id.btnLogout);
        View btnGoToCatalog = findViewById(R.id.btnGoToCatalog);
        btnLogoutIcon.setOnClickListener(v -> LogoutHelper.performLogout(this));
        btnLogout.setOnClickListener(v -> LogoutHelper.performLogout(this));
        // Los 3 roles pueden entrar al catálogo; lo que cambia por rol
        // es lo que ven DENTRO del detalle de cada producto (US05).
        btnGoToCatalog.setOnClickListener(v -> {
            startActivity(new Intent(this, CatalogActivity.class));
            // Transición: el catálogo entra deslizándose desde la derecha.
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        applyRoleStyling(controller.getCurrentRole());
        controller.loadOwnProfile();
    }

    /**
     * Ajusta ícono, etiqueta y color de acento según el rol.
     * ProfileActivity YA NO decide "si es admin haz esto, si es auditor haz
     * lo otro": le pide los datos a "profile" y confía en que cada subclase
     * de RoleProfile sabe responder por sí misma -> POLIMORFISMO real.
     */
    private void applyRoleStyling(SessionManager.Role role) {
        RoleProfile profile = RoleProfile.from(role);

        int accentColor = profile.getAccentColor(this);
        int pillBgColor = profile.getPillBackgroundColor(this);

        // Fondo del círculo del avatar tintado con el color de acento (tenue).
        tintDrawableBackground(tvAvatarInitial, pillBgColor);
        tintDrawableBackground(pillRole, pillBgColor);

        tvAvatarInitial.setTextColor(accentColor);
        tvRolePillLabel.setTextColor(accentColor);

        tvRoleBadgeIcon.setText(profile.getEmoji());
        tvRolePillIcon.setText(profile.getEmoji());
        tvRolePillLabel.setText(profile.getLabel());
    }

    private void tintDrawableBackground(View view, int color) {
        GradientDrawable bg = (GradientDrawable) view.getBackground().mutate();
        bg.setColor(color);
        view.setBackground(bg);
    }

    private void bindRow(int includeId, String icon, String label, String value) {
        View row = findViewById(includeId);
        ((TextView) row.findViewById(R.id.rowIcon)).setText(icon);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        ((TextView) row.findViewById(R.id.rowValue)).setText(value);
    }

    // ===================== ProfileController.ProfileView =====================

    @Override
    public void onProfileLoaded(User user) {
        String fullName = user.getName().getFullName();
        tvFullName.setText(fullName);
        tvAvatarInitial.setText(String.valueOf(Character.toUpperCase(fullName.charAt(0))));

        bindRow(R.id.rowUserId, "🪪", "ID de Usuario", "#" + user.getId());
        bindRow(R.id.rowUsername, "👤", "Usuario", user.getUsername());
        bindRow(R.id.rowEmail, "✉️", "Correo Electrónico", user.getEmail());
        bindRow(R.id.rowPhone, "📞", "Teléfono", user.getPhone());
        bindRow(R.id.rowAddress, "📍", "Dirección", user.getAddress().getFullAddress());
    }

    @Override
    public void onProfileLoadFailed() {
        // Alerta de error visual delegada a la capa de vista (color #CF6679).
        // (Mismo comportamiento silencioso que antes: la pantalla no se rompe,
        // simplemente los campos quedan con su valor por defecto del layout.)
    }
}
