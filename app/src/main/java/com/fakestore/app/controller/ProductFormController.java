package com.fakestore.app.controller;

import android.content.Context;

import com.fakestore.app.R;
import com.fakestore.app.model.Product;
import com.fakestore.app.network.ApiClient;
import com.fakestore.app.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controlador del formulario de Crear/Editar producto (US06/US07).
 * Valida los campos, arma el objeto Product y decide si debe llamar a
 * createProduct o updateProduct según el modo. La Activity nunca toca
 * Retrofit directamente; solo le pasa el texto crudo de los campos y
 * reacciona a los callbacks de ProductFormView.
 */
public class ProductFormController {

    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";

    public interface ProductFormView {
        void setSavingState(boolean saving);

        void onProductCreated(Product createdProduct);

        void onProductUpdated(Product updatedProduct);

        void onSubmitFailed();

        void onAccessDenied();
    }

    /**
     * Resultado de validar el formulario. NO es un modelo de datos de
     * la app (como User o Product), es solo un "paquete" temporal con
     * los mensajes de error de cada campo; por eso no sigue la regla
     * de "setters que validan" — aquí no hay nada que validar, ya es
     * el resultado de la validación.
     */
    public static class ValidationResult {
        public final boolean valid;
        public final String titleError;
        public final String priceError;
        public final String descriptionError;
        public final String imageError;
        public final String categoryError;

        ValidationResult(boolean valid, String titleError, String priceError,
                          String descriptionError, String imageError, String categoryError) {
            this.valid = valid;
            this.titleError = titleError;
            this.priceError = priceError;
            this.descriptionError = descriptionError;
            this.imageError = imageError;
            this.categoryError = categoryError;
        }
    }

    private final Context context;
    private final ProductFormView view;
    private final SessionManager sessionManager;

    private String mode;
    private int editingProductId = -1;

    public ProductFormController(Context context, ProductFormView view) {
        this.context = context;
        this.view = view;
        this.sessionManager = new SessionManager(context);
    }

    /** Se llama una vez, al abrir la pantalla, para que el Controller sepa si crea o edita. */
    public void initialize(String mode, int editingProductId) {
        this.mode = mode;
        this.editingProductId = editingProductId;
    }

    public boolean isCurrentUserAdmin() {
        return sessionManager.getRole() == SessionManager.Role.ADMIN;
    }

    /**
     * Validación Frontend (US06): revisa que ningún campo obligatorio
     * esté vacío y que el precio sea estrictamente numérico. Regresa
     * un ValidationResult con el mensaje de error de CADA campo (o
     * null si ese campo está bien), para que la Vista decida cómo
     * mostrarlo (en este proyecto, con TextInputLayout.setError()).
     */
    public ValidationResult validateForm(String title, String priceText, String description,
                                          String image, String category) {
        boolean valid = true;
        String titleError = null;
        String priceError = null;
        String descriptionError = null;
        String imageError = null;
        String categoryError = null;

        if (isBlank(title)) {
            titleError = context.getString(R.string.error_field_required);
            valid = false;
        }

        if (isBlank(priceText)) {
            priceError = context.getString(R.string.error_field_required);
            valid = false;
        } else if (!isNumeric(priceText)) {
            priceError = context.getString(R.string.error_price_invalid);
            valid = false;
        }

        if (isBlank(description)) {
            descriptionError = context.getString(R.string.error_field_required);
            valid = false;
        }

        if (isBlank(image)) {
            imageError = context.getString(R.string.error_field_required);
            valid = false;
        }

        if (isBlank(category)) {
            categoryError = context.getString(R.string.error_field_required);
            valid = false;
        }

        return new ValidationResult(valid, titleError, priceError, descriptionError, imageError, categoryError);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /** "Estrictamente numérico": intentamos convertir el texto a número; si truena, no es válido. */
    private boolean isNumeric(String text) {
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Arma el Product y decide si crea o edita. Se asume que el
     * formulario YA pasó validateForm() con resultado válido (la
     * Vista no debería llamar esto si validateForm().valid es false).
     */
    public void submitForm(String title, String priceText, String description, String category, String image) {
        // Segunda revisión de seguridad (ver nota de la clase en
        // ProductFormActivity): aunque ya se filtró al abrir la
        // pantalla, lo volvemos a comprobar justo antes de tocar la red.
        if (!isCurrentUserAdmin()) {
            view.onAccessDenied();
            return;
        }

        double price = Double.parseDouble(priceText); // ya validado en validateForm()
        int id = (MODE_EDIT.equals(mode) && editingProductId > 0) ? editingProductId : 0;
        Product product = new Product(id, title, price, description, category, image);

        view.setSavingState(true);

        if (MODE_EDIT.equals(mode)) {
            updateProduct(product);
        } else {
            createProduct(product);
        }
    }

    // ===================== US06: Crear =====================

    private void createProduct(Product product) {
        ApiClient.getApiService().createProduct(product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                view.setSavingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    view.onProductCreated(response.body());
                } else {
                    view.onSubmitFailed();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                view.setSavingState(false);
                view.onSubmitFailed();
            }
        });
    }

    // ===================== US07: Editar =====================

    private void updateProduct(Product product) {
        ApiClient.getApiService().updateProduct(editingProductId, product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                view.setSavingState(false);
                if (response.isSuccessful()) {
                    view.onProductUpdated(product);
                } else {
                    view.onSubmitFailed();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                view.setSavingState(false);
                view.onSubmitFailed();
            }
        });
    }
}
