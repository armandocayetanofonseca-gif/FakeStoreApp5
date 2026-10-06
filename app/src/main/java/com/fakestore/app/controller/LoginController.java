package com.fakestore.app.controller;

import android.content.Context;

import com.fakestore.app.model.LoginRequest;
import com.fakestore.app.model.LoginResponse;
import com.fakestore.app.model.User;
import com.fakestore.app.network.ApiClient;
import com.fakestore.app.session.SessionManager;
import com.fakestore.app.util.NetworkUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (separación Vista / Controlador):
 * Esta clase es el "Controlador" de la pantalla de Login. Contiene
 * TODA la lógica de negocio (revisar la red, armar la petición, leer
 * la respuesta, decidir el rol, guardar la sesión) y NO conoce nada de
 * Android UI: no tiene EditText, Button, TextView, ni "findViewById".
 *
 * La pantalla (LoginActivity, la "Vista") es quien sabe dibujar cosas
 * en la UI. Para que el Controlador pueda "avisarle" a la Vista qué
 * mostrar (un error, un spinner, etc.) sin conocerla directamente, se
 * usa una INTERFACE: LoginView (declarada aquí abajo). LoginActivity
 * IMPLEMENTA esa interface; el Controlador solo conoce la interface,
 * nunca la clase concreta Activity. A este patrón se le conoce como
 * MVP (Model-View-Presenter): el Controlador hace de "Presenter".
 * ---------------------------------------------------------------------
 */
public class LoginController {

    /**
     * Todo lo que el Controlador necesita poder "pedirle" a la pantalla
     * (la Vista). LoginActivity implementa cada uno de estos métodos
     * con código de Android puro (mostrar textos, diálogos, animar).
     */
    public interface LoginView {
        void showNoConnectionModal();

        void hideError();

        void showInvalidCredentialsError();

        void showGenericError();

        void setLoginButtonEnabled(boolean enabled);

        /** Login exitoso: la Vista decide cómo navegar a la siguiente pantalla. */
        void onLoginSuccess(SessionManager.Role role);
    }

    private final Context context;
    private final LoginView view;
    private final SessionManager sessionManager;

    /**
     * NOTA: guardamos "Context" aquí para poder usar NetworkUtils y
     * SessionManager (ambos lo piden). En una app más grande se evitaría
     * guardar el Context de una Activity dentro de un objeto de larga
     * vida (riesgo de fuga de memoria); aquí lo simplificamos porque el
     * Controller vive exactamente lo mismo que la Activity (se crea y
     * se destruye junto con ella).
     */
    public LoginController(Context context, LoginView view) {
        this.context = context;
        this.view = view;
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Intenta iniciar sesión. Esta es la MISMA lógica que antes vivía
     * directamente en LoginActivity; lo único que cambió es que en vez
     * de tocar vistas directamente (tvError.setVisibility(...)), le
     * avisa a "view" (la interface) y la Activity decide cómo pintarlo.
     */
    public void attemptLogin(String username, String password) {
        // Escenario "Login: Sin Conexión" -> detener ANTES de llamar a la API.
        if (!NetworkUtils.isConnected(context)) {
            view.showNoConnectionModal();
            return;
        }

        view.hideError();
        view.setLoginButtonEnabled(false);

        LoginRequest request = new LoginRequest(username, password);
        // NOTA DE CLASE (POO - Interfaces): "new Callback<LoginResponse>() { ... }"
        // es una clase anónima que IMPLEMENTA la interface Callback (la
        // define la librería Retrofit). Le decimos "cuando tengas la
        // respuesta, corre este código", sin necesitar una clase aparte.
        ApiClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                view.setLoginButtonEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    onLoginApiSuccess(response.body().getToken(), username);
                } else if (response.code() == 401) {
                    // No se borra el formulario, solo se muestra la alerta.
                    view.showInvalidCredentialsError();
                } else {
                    view.showGenericError();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                view.setLoginButtonEnabled(true);
                view.showGenericError();
            }
        });
    }

    /**
     * Tras un 200 OK, se recupera la lista de usuarios para localizar el id
     * correspondiente al username autenticado y así decodificar el rol
     * (sección 3). El password NUNCA se solicita ni se guarda en este flujo.
     */
    private void onLoginApiSuccess(String token, String username) {
        ApiClient.getApiService().getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    view.showGenericError();
                    return;
                }

                User matchedUser = null;
                for (User u : response.body()) {
                    if (u.getUsername().equalsIgnoreCase(username)) {
                        matchedUser = u;
                        break;
                    }
                }

                if (matchedUser == null) {
                    view.showGenericError();
                    return;
                }

                SessionManager.Role role = SessionManager.Role.fromUserId(matchedUser.getId());
                sessionManager.saveSession(token, matchedUser.getId(), role);
                view.onLoginSuccess(role);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                view.showGenericError();
            }
        });
    }
}
