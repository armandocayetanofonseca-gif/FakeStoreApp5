package com.fakestore.app.controller;

import android.content.Context;

import com.fakestore.app.model.Product;
import com.fakestore.app.network.ApiClient;
import com.fakestore.app.session.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controlador del Catálogo (US03/US04/US06). Decide QUÉ pedirle a la
 * API (todos los productos o filtrados por categoría) y mantiene el
 * dato de "cuál es la categoría activa ahora mismo" — eso es estado de
 * negocio, no de pantalla, por eso vive aquí y no en la Activity.
 */
public class CatalogController {

    public interface CatalogView {
        void showLoading();

        void showContent();

        void showError();

        /** "Gestión de Memoria en Filtros": limpiar antes de pintar lo nuevo. */
        void clearProductList();

        void onProductsLoaded(List<Product> products);

        void onCategoriesLoaded(List<String> categories);
    }

    private final Context context;
    private final CatalogView view;
    private final SessionManager sessionManager;

    // "null" significa "sin filtro" = catálogo general (/products).
    private String currentCategory = null;

    public CatalogController(Context context, CatalogView view) {
        this.context = context;
        this.view = view;
        this.sessionManager = new SessionManager(context);
    }

    /** US06: el botón "+" (crear producto) solo debe existir para el Administrador. */
    public boolean isCurrentUserAdmin() {
        return sessionManager.getRole() == SessionManager.Role.ADMIN;
    }

    public void loadCategories() {
        ApiClient.getApiService().getCategories().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return; // si fallan las categorías, igual queda "Ver todos"
                }
                view.onCategoriesLoaded(response.body());
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                // Silencioso: el catálogo general sigue funcionando aunque
                // no se puedan cargar las categorías.
            }
        });
    }

    /**
     * Carga el catálogo. Si "category" es null, pide /products (todos);
     * si no, pide /products/category/{category} (filtrado). Guarda
     * "category" como la categoría activa, para que retryLastLoad()
     * sepa qué repetir si el usuario toca "Reintentar".
     */
    public void loadProducts(String category) {
        currentCategory = category;
        view.showLoading();

        // "Gestión de Memoria en Filtros": limpiamos el arreglo ANTES de
        // que llegue la respuesta nueva, para que nunca se vean mezclados
        // productos de la categoría anterior con la nueva.
        view.clearProductList();

        Call<List<Product>> call = (category == null)
                ? ApiClient.getApiService().getProducts()
                : ApiClient.getApiService().getProductsByCategory(category);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    view.showContent();
                    view.onProductsLoaded(response.body());
                } else {
                    view.showError();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                view.showError();
            }
        });
    }

    /** Botón "Reintentar": repite la última categoría (o "todos") que falló. */
    public void retryLastLoad() {
        loadProducts(currentCategory);
    }
}
