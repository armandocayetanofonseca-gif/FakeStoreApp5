package com.fakestore.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fakestore.app.R;
import com.fakestore.app.controller.ProductFormController;
import com.fakestore.app.model.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (separación Vista / Controlador):
 * Pantalla de formulario de producto (spec v3 - Gestión CRUD). Esta
 * MISMA pantalla se usa para dos cosas (el spec lo pide así: "diseño
 * idéntico al de creación"):
 *   - US06: crear un producto nuevo (POST /products)
 *   - US07: editar un producto existente (PUT /products/{id})
 *
 * Esta clase es la "Vista": junta el texto crudo de los campos, pinta
 * errores con TextInputLayout.setError(...), muestra el spinner de
 * guardado y navega. TODA la lógica (saber si es "crear" o "editar",
 * validar, armar el Product, llamar a Retrofit) vive en
 * {@link ProductFormController}, que esta Activity implementa a través
 * de ProductFormController.ProductFormView.
 *
 * ---------------------------------------------------------------------
 * SEGURIDAD (muy importante, lo pide el spec explícitamente):
 * "Bloquear acceso a Clientes o Auditores que intenten forzar la vista
 * vía deeplinks" y "cualquier intercepción forzada debe ser bloqueada a
 * nivel de red". Por eso el rol se revisa en DOS lugares:
 *   1) Al abrir la pantalla (onCreate): si no es Admin, ni siquiera se
 *      alcanza a dibujar el formulario, se redirige al catálogo. Esta
 *      primera revisión sigue viviendo en la Vista porque decide algo
 *      100% de UI: si se dibuja el formulario o no.
 *   2) Justo antes de mandar la petición: esa SEGUNDA revisión vive
 *      dentro de ProductFormController.submitForm(), por si alguien
 *      lograra mostrar la pantalla de alguna forma rara.
 * A esto se le llama "seguridad en capas": no confiar en una sola
 * revisión, sino repetirla en cada punto crítico.
 * ---------------------------------------------------------------------
 */
public class ProductFormActivity extends AppCompatActivity implements ProductFormController.ProductFormView {

    // ===== Claves para mandar datos por Intent (extras) =====
    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_CREATE = ProductFormController.MODE_CREATE;
    public static final String MODE_EDIT = ProductFormController.MODE_EDIT;

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_PRICE = "extra_price";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_IMAGE = "extra_image";

    private TextView tvFormTitle;
    private TextInputLayout tilTitle;
    private TextInputLayout tilPrice;
    private TextInputLayout tilDescription;
    private TextInputLayout tilImage;
    private TextInputLayout tilCategory;
    private TextInputEditText etTitle;
    private TextInputEditText etPrice;
    private TextInputEditText etDescription;
    private TextInputEditText etImage;
    private TextInputEditText etCategory;
    private MaterialButton btnSave;
    private View progressSaving;

    private ProductFormController controller;
    private String mode;
    private int editingProductId = -1;

    // ===================== Helpers estáticos para abrir la pantalla =====================

    /** US06: abre el formulario vacío, listo para crear un producto nuevo. */
    public static void startForCreate(Activity activity) {
        Intent intent = new Intent(activity, ProductFormActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_CREATE);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * US07: abre el formulario PRE-CARGADO con los datos del producto.
     * Se usa startActivityForResult porque, al terminar de editar,
     * necesitamos que ProductDetailActivity reciba los datos nuevos de
     * vuelta y actualice lo que se ve en pantalla (ver
     * ProductDetailActivity.onActivityResult).
     */
    public static void startForEdit(Activity activity, int requestCode, Product product) {
        Intent intent = new Intent(activity, ProductFormActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_EDIT);
        intent.putExtra(EXTRA_PRODUCT_ID, product.getId());
        intent.putExtra(EXTRA_TITLE, product.getTitle());
        intent.putExtra(EXTRA_PRICE, product.getPrice());
        intent.putExtra(EXTRA_DESCRIPTION, product.getDescription());
        intent.putExtra(EXTRA_CATEGORY, product.getCategory());
        intent.putExtra(EXTRA_IMAGE, product.getImage());
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = new ProductFormController(this, this);

        // ============ GUARDIA DE SEGURIDAD (va ANTES que todo) ============
        // Si alguien intenta abrir esta pantalla sin ser Admin (por
        // ejemplo forzando el Intent desde afuera, un "deeplink"), lo
        // regresamos al catálogo general de inmediato. Como esto pasa
        // antes de setContentView(), el formulario ni siquiera llega a
        // dibujarse para un usuario sin permiso. Esta primera revisión
        // se queda en la Vista porque decide algo 100% de UI (si se
        // dibuja la pantalla o no); la SEGUNDA revisión (antes de la
        // llamada de red) vive dentro del Controller.
        if (!controller.isCurrentUserAdmin()) {
            Toast.makeText(this, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, CatalogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_product_form);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        tilTitle = findViewById(R.id.tilTitle);
        tilPrice = findViewById(R.id.tilPrice);
        tilDescription = findViewById(R.id.tilDescription);
        tilImage = findViewById(R.id.tilImage);
        tilCategory = findViewById(R.id.tilCategory);
        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etImage = findViewById(R.id.etImage);
        etCategory = findViewById(R.id.etCategory);
        btnSave = findViewById(R.id.btnSave);
        progressSaving = findViewById(R.id.progressSaving);

        findViewById(R.id.btnBack).setOnClickListener(v -> goBackWithTransition());
        btnSave.setOnClickListener(v -> onSaveClicked());

        setupModeFromIntent();
    }

    /** Lee el Intent y decide si esta pantalla se comporta como "crear" o "editar". */
    private void setupModeFromIntent() {
        mode = getIntent().getStringExtra(EXTRA_MODE);
        boolean isEdit = MODE_EDIT.equals(mode);

        tvFormTitle.setText(isEdit ? R.string.form_title_edit : R.string.form_title_create);

        if (isEdit) {
            // US07 "Precarga de Datos": todos los campos ya vienen
            // llenos con la información actual del producto.
            editingProductId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
            etTitle.setText(getIntent().getStringExtra(EXTRA_TITLE));
            etPrice.setText(String.valueOf(getIntent().getDoubleExtra(EXTRA_PRICE, 0)));
            etDescription.setText(getIntent().getStringExtra(EXTRA_DESCRIPTION));
            etImage.setText(getIntent().getStringExtra(EXTRA_IMAGE));
            etCategory.setText(getIntent().getStringExtra(EXTRA_CATEGORY));
        }

        controller.initialize(mode, editingProductId);
    }

    // ===================== Guardar (botón) =====================

    private void onSaveClicked() {
        ProductFormController.ValidationResult result = controller.validateForm(
                getText(etTitle), getText(etPrice), getText(etDescription),
                getText(etImage), getText(etCategory));

        applyValidationResult(result);

        if (!result.valid) {
            return; // Los campos inválidos ya quedaron marcados en rojo.
        }

        controller.submitForm(getText(etTitle), getText(etPrice), getText(etDescription),
                getText(etCategory), getText(etImage));
    }

    /**
     * Traduce el ValidationResult (un simple "paquete" de mensajes de
     * error que armó el Controller) a llamadas de TextInputLayout.setError(...).
     * Material pinta el borde y el texto de error en rojo (#CF6679)
     * automáticamente, sin que tengamos que cambiar colores a mano.
     */
    private void applyValidationResult(ProductFormController.ValidationResult result) {
        tilTitle.setError(result.titleError);
        tilPrice.setError(result.priceError);
        tilDescription.setError(result.descriptionError);
        tilImage.setError(result.imageError);
        tilCategory.setError(result.categoryError);
    }

    private String getText(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    /**
     * "Flujo Exitoso" (US06): alerta de confirmación con el ID nuevo, y
     * al aceptar, limpiar el formulario POR COMPLETO (queda listo para
     * cargar otro producto, sin cerrar la pantalla).
     */
    private void showCreatedDialogAndClearForm(Product createdProduct) {
        String message = String.format(Locale.getDefault(),
                getString(R.string.dialog_product_created_message), createdProduct.getId());

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_product_created_title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_accept, (dialog, which) -> clearForm())
                .show();
    }

    private void clearForm() {
        etTitle.setText("");
        etPrice.setText("");
        etDescription.setText("");
        etImage.setText("");
        etCategory.setText("");
        tilTitle.setError(null);
        tilPrice.setError(null);
        tilDescription.setError(null);
        tilImage.setError(null);
        tilCategory.setError(null);
        etTitle.requestFocus();
    }

    /**
     * "Flujo Exitoso" (US07): mensaje flotante "Producto actualizado
     * (Simulación)", cerrar esta pantalla, y devolver los datos nuevos
     * a ProductDetailActivity (vía setResult) para que los "pinte" en
     * el detalle SIN volver a pedirle nada a la API (recuerda: la API
     * no guarda el cambio de verdad).
     */
    private void onUpdateSuccess(Product updatedProduct) {
        Toast.makeText(this, R.string.toast_product_updated, Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_PRODUCT_ID, editingProductId);
        resultIntent.putExtra(EXTRA_TITLE, updatedProduct.getTitle());
        resultIntent.putExtra(EXTRA_PRICE, updatedProduct.getPrice());
        resultIntent.putExtra(EXTRA_DESCRIPTION, updatedProduct.getDescription());
        resultIntent.putExtra(EXTRA_CATEGORY, updatedProduct.getCategory());
        resultIntent.putExtra(EXTRA_IMAGE, updatedProduct.getImage());
        setResult(Activity.RESULT_OK, resultIntent);

        goBackWithTransition();
    }

    // ===================== Navegación =====================

    private void goBackWithTransition() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // ===================== ProductFormController.ProductFormView =====================

    /**
     * "Gestión de Carga" (US07): mientras se guarda, deshabilitamos el
     * botón (para que no lo puedan tocar 2 veces y mandar la petición
     * duplicada) y mostramos un spinner encima.
     */
    @Override
    public void setSavingState(boolean saving) {
        btnSave.setEnabled(!saving);
        // Mientras carga, bajamos la opacidad del botón (no lo ocultamos
        // por completo) para que el spinner se vea centrado sobre él.
        btnSave.setAlpha(saving ? 0.4f : 1f);
        progressSaving.setVisibility(saving ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProductCreated(Product createdProduct) {
        showCreatedDialogAndClearForm(createdProduct);
    }

    @Override
    public void onProductUpdated(Product updatedProduct) {
        onUpdateSuccess(updatedProduct);
    }

    @Override
    public void onSubmitFailed() {
        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessDenied() {
        Toast.makeText(this, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
        finish();
    }
}
