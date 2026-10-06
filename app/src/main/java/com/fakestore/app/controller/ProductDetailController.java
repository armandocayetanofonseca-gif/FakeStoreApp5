package com.fakestore.app.controller;

import android.content.Context;

import com.fakestore.app.model.Product;
import com.fakestore.app.network.ApiClient;
import com.fakestore.app.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controlador del Detalle de Producto. Guarda el producto actual como
 * estado propio (igual que CatalogController guarda la categoría
 * activa) y resuelve las 3 historias de usuario de esta pantalla:
 * ver el detalle (US05), aplicar una edición ya hecha en otra pantalla
 * (US07) y borrar el producto (US08).
 *
 * Ninguno de estos métodos muestra un Toast, un AlertDialog ni navega
 * entre Activities: eso es trabajo 100% de ProductDetailActivity (la
 * Vista), a través de los callbacks de ProductDetailView.
 */
public class ProductDetailController {

    public interface ProductDetailView {
        void onProductLoaded(Product product);

        void onProductUnavailable();

        /** US07: el producto ya se actualizó en memoria, hay que repintar la UI. */
        void onProductUpdatedLocally(Product product);

        void onProductDeleted();

        void onDeleteFailed();

        void onAccessDenied();
    }

    private final Context context;
    private final ProductDetailView view;
    private final SessionManager sessionManager;

    // Estado propio del Controller: el producto que se está mostrando
    // ahora mismo. Editar/Eliminar lo usan sin tener que volver a
    // pedirlo ni pasarlo de un lado a otro.
    private Product currentProduct;

    public ProductDetailController(Context context, ProductDetailView view) {
        this.context = context;
        this.view = view;
        this.sessionManager = new SessionManager(context);
    }

    public Product getCurrentProduct() {
        return currentProduct;
    }

    /**
     * Punto clave de seguridad (US05/US07/US08): el rol se lee
     * ÚNICAMENTE de SessionManager (variable de sesión LOCAL guardada
     * en el login), nunca de "currentProduct" ni de ninguna respuesta
     * de la API.
     */
    public boolean isCurrentUserAdmin() {
        return sessionManager.getRole() == SessionManager.Role.ADMIN;
    }

    // ===================== US05: cargar el detalle =====================

    public void loadProduct(int productId) {
        ApiClient.getApiService().getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                // "Manejo de Errores (404/Network en Detalle)": si la API
                // no encuentra el producto, cae aquí también (response
                // no exitoso), no solo en onFailure.
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    view.onProductLoaded(currentProduct);
                } else {
                    view.onProductUnavailable();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                view.onProductUnavailable();
            }
        });
    }

    // ===================== US07: aplicar edición local =====================

    /**
     * Actualiza currentProduct con lo que el usuario guardó en
     * ProductFormActivity. Usamos los setters (que validan, igual que
     * en cualquier otro lado del proyecto) en vez de tocar los campos
     * directamente.
     */
    public void applyLocalEdit(String title, double price, String description, String category, String image) {
        currentProduct.setTitle(title);
        currentProduct.setPrice(price);
        currentProduct.setDescription(description);
        currentProduct.setCategory(category);
        currentProduct.setImage(image);
        view.onProductUpdatedLocally(currentProduct);
    }

    // ===================== US08: eliminar =====================

    public void deleteProduct() {
        // Segunda revisión de seguridad (defensa en capas), igual que
        // en ProductFormController: no confiamos solo en que el botón
        // "Eliminar" estuviera visible.
        if (!isCurrentUserAdmin()) {
            view.onAccessDenied();
            return;
        }

        ApiClient.getApiService().deleteProduct(currentProduct.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    view.onProductDeleted();
                } else {
                    view.onDeleteFailed();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                view.onDeleteFailed();
            }
        });
    }
}
