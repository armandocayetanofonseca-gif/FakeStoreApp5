package com.fakestore.app.controller;

import android.content.Context;

import com.fakestore.app.model.User;
import com.fakestore.app.network.ApiClient;
import com.fakestore.app.session.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controlador de la pantalla "Mi Perfil". Su única responsabilidad de
 * negocio es: pedirle a la API la lista de usuarios y localizar al que
 * tiene el id de la sesión actual. Nada de esto toca Android UI; eso
 * lo hace ProfileActivity (la Vista) a través de ProfileView.
 */
public class ProfileController {

    public interface ProfileView {
        void onProfileLoaded(User user);

        void onProfileLoadFailed();
    }

    private final Context context;
    private final ProfileView view;
    private final SessionManager sessionManager;

    public ProfileController(Context context, ProfileView view) {
        this.context = context;
        this.view = view;
        this.sessionManager = new SessionManager(context);
    }

    /** El rol decide la insignia (ver RoleProfile); se expone tal cual desde la sesión local. */
    public SessionManager.Role getCurrentRole() {
        return sessionManager.getRole();
    }

    /** El usuario ve únicamente SU propio perfil, sin importar el rol. */
    public void loadOwnProfile() {
        int currentUserId = sessionManager.getUserId();

        ApiClient.getApiService().getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    view.onProfileLoadFailed();
                    return;
                }
                for (User user : response.body()) {
                    if (user.getId() == currentUserId) {
                        view.onProfileLoaded(user);
                        return;
                    }
                }
                view.onProfileLoadFailed();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                view.onProfileLoadFailed();
            }
        });
    }
}
