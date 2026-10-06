package com.fakestore.app.ui;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fakestore.app.R;
import com.fakestore.app.controller.LoginController;
import com.fakestore.app.session.SessionManager;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (separación Vista / Controlador):
 * Esta clase es la "Vista" de la pantalla de Login. El profesor pidió
 * que las "pestañas" de estilos/UI quedaran separadas de los
 * controladores, así que ahora LoginActivity SOLO hace cosas de
 * Android puro: busca vistas (findViewById), lee lo que el usuario
 * escribió, muestra errores/diálogos, anima y navega entre pantallas.
 *
 * TODA la lógica de negocio (revisar conexión, llamar a la API, decidir
 * el rol, guardar la sesión) se movió a {@link LoginController}, que
 * NO conoce nada de Android UI. Para que el Controller pueda "avisarle"
 * a esta Activity qué mostrar sin conocerla directamente, esta clase
 * IMPLEMENTA la interface interna LoginController.LoginView: cada
 * método de esa interface es una orden que el Controller da y que
 * aquí se traduce en código de Android (mostrar un texto, un diálogo,
 * habilitar un botón, etc.).
 *
 * El comportamiento es EXACTAMENTE el mismo que antes (mismos
 * escenarios US01/US02, mismas animaciones); solo cambió "quién hace
 * qué" dentro del código.
 * ---------------------------------------------------------------------
 */
public class LoginActivity extends AppCompatActivity implements LoginController.LoginView {

    private EditText etUsername;
    private EditText etPassword;
    private TextView tvError;
    private Button btnLogin;

    private LoginController controller;

    /** Color de alerta definido en la sección 1: #CF6679 */
    private static final int COLOR_ERROR = Color.parseColor("#CF6679");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // El Controller se crea aquí y recibe "this" como su LoginView:
        // así sabe a quién avisarle cuando algo pase.
        controller = new LoginController(this, this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvError = findViewById(R.id.tvError);
        btnLogin = findViewById(R.id.btnLogin);

        tvError.setTextColor(COLOR_ERROR);
        tvError.setVisibility(TextView.INVISIBLE);

        // La Vista ya NO sabe cómo se hace login: solo junta el texto
        // de los campos y se lo pasa al Controller.
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();
            controller.attemptLogin(username, password);
        });

        animateEntrance();
    }

    /**
     * Animación de entrada: cada elemento aparece con un pequeño retraso
     * (delay) respecto al anterior, dando un efecto "en cascada" en vez
     * de que todo aparezca de golpe. Se hace con ViewPropertyAnimator
     * (view.animate()...), que es la forma más simple de animar en
     * Android: se le dice a la vista a dónde tiene que llegar y ella
     * sola calcula los pasos intermedios.
     */
    private void animateEntrance() {
        // Views que se van a animar, en el orden en que deben aparecer.
        View[] views = {
                findViewById(R.id.logoCircle),
                findViewById(R.id.tvAppTitle),
                findViewById(R.id.tvSubtitle),
                findViewById(R.id.tilUsername),
                findViewById(R.id.tilPassword),
                findViewById(R.id.btnLogin)
        };

        long baseDelay = 0L;
        for (View view : views) {
            // Estado inicial: invisible y un poco más abajo de su
            // posición real.
            view.setAlpha(0f);
            view.setTranslationY(40f);

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(baseDelay)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // Cada elemento empieza 60ms después que el anterior ->
            // efecto "cascada".
            baseDelay += 60L;
        }
    }

    /**
     * Animación de "shake" (temblor horizontal): se usa cuando el login
     * falla, como retroalimentación visual de "algo salió mal", además
     * del mensaje de texto en rojo.
     */
    private void shake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                view, "translationX",
                0f, -20f, 20f, -16f, 16f, -8f, 8f, 0f
        );
        animator.setDuration(400);
        animator.start();
    }

    // ===================== LoginController.LoginView =====================
    // Cada uno de estos métodos es una "orden" que el Controller manda;
    // aquí solo se traduce en código de Android. Nada de red ni de
    // SessionManager vive en esta clase.

    @Override
    public void showNoConnectionModal() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_no_connection_title))
                .setMessage(getString(R.string.dialog_no_connection_message))
                .setPositiveButton(R.string.dialog_accept, (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
        // No se invoca ninguna llamada de red ni loader: se detiene aquí mismo.
    }

    @Override
    public void hideError() {
        tvError.setVisibility(TextView.INVISIBLE);
    }

    @Override
    public void showInvalidCredentialsError() {
        tvError.setText(R.string.error_invalid_credentials);
        tvError.setVisibility(TextView.VISIBLE);
        shake(etPassword);
        // El formulario NO se limpia, permitiendo al usuario corregir.
    }

    @Override
    public void showGenericError() {
        tvError.setText(R.string.error_generic);
        tvError.setVisibility(TextView.VISIBLE);
    }

    @Override
    public void setLoginButtonEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
    }

    @Override
    public void onLoginSuccess(SessionManager.Role role) {
        // Las 3 pantallas por rol se unificaron en una sola "Mi Perfil";
        // ProfileActivity lee el rol guardado en SessionManager y ajusta
        // únicamente la insignia (ícono/etiqueta/color de acento).
        Intent intent = new Intent(this, ProfileActivity.class);
        // Reinicia la pila de navegación por completo (sección 4).
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Transición: el login se desvanece y el perfil aparece con fade
        // (se siente como un "arranque limpio" de la sesión).
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    /**
     * Bloqueo de Retroceso (sección 4): si el usuario llega a Login tras un
     * logout, no debe poder retroceder al catálogo cacheado. Como la pila ya
     * fue reiniciada por completo (FLAG_ACTIVITY_CLEAR_TASK), el back nativo
     * en esta pantalla solo puede llevar a segundo plano/salir de la app.
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
