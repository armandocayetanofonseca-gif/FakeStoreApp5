package com.fakestore.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fakestore.app.R;
import com.fakestore.app.controller.ProductDetailController;
import com.fakestore.app.model.Product;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (separación Vista / Controlador):
 * Pantalla de Detalle de Producto (US05), también punto de entrada a
 * Editar (US07) y Eliminar (US08) para el Administrador. Esta clase es
 * la "Vista": pinta los TextViews/ImageView, infla el ViewStub de
 * botones Admin, muestra diálogos y navega. TODA la lógica (pedir el
 * producto a la API, revisar el rol, aplicar la edición local, borrar)
 * vive en {@link ProductDetailController}.
 *
 * Punto clave de seguridad (se mantiene igual que antes, solo que ahora
 * la revisión del rol vive dentro del Controller):
 * "El consumo del endpoint /products/{id} debe validar los permisos de
 * gestión leyendo estrictamente la variable de sesión local, no la
 * respuesta de la API."
 * ---------------------------------------------------------------------
 */
public class ProductDetailActivity extends AppCompatActivity implements ProductDetailController.ProductDetailView {

    private static final String EXTRA_PRODUCT_ID = "extra_product_id";

    // Código para reconocer, en onActivityResult, que la respuesta viene
    // específicamente de la pantalla de "editar producto".
    private static final int REQUEST_CODE_EDIT_PRODUCT = 1001;

    private View progressLoading;
    private View contentDetail;
    private ImageView ivDetailImage;
    private TextView tvDetailCategory;
    private TextView tvDetailTitle;
    private TextView tvDetailPrice;
    private TextView tvDetailDescription;
    private ViewStub stubAdminActions;

    private ProductDetailController controller;

    /**
     * Helper estático para abrir esta pantalla desde cualquier lugar
     * (CatalogActivity) sin tener que armar el Intent a mano cada vez.
     */
    public static void start(Context context, int productId) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        context.startActivity(intent);
        // Si quien nos llamó es una Activity, aplicamos la transición de
        // "avanzar" (desliza desde la derecha). Lo comprobamos con
        // "instanceof" porque "context" podría no ser siempre una Activity.
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        controller = new ProductDetailController(this, this);

        progressLoading = findViewById(R.id.progressLoading);
        contentDetail = findViewById(R.id.contentDetail);
        ivDetailImage = findViewById(R.id.ivDetailImage);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        stubAdminActions = findViewById(R.id.stubAdminActions);

        findViewById(R.id.btnBack).setOnClickListener(v -> goBackWithTransition());

        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId < 0) {
            showUnavailableAndReturn();
            return;
        }

        controller.loadProduct(productId);
    }

    /** Pinta los TextViews/ImageView con los datos de "product". Se reutiliza tras editar (US07). */
    private void renderProductFields(Product product) {
        tvDetailCategory.setText(product.getCategoryDisplay());
        tvDetailTitle.setText(product.getTitle());
        tvDetailPrice.setText(product.getFormattedPrice());
        tvDetailDescription.setText(product.getDescription());

        Glide.with(this)
                .load(product.getImage())
                .into(ivDetailImage);
    }

    /**
     * Aquí está la regla de seguridad más importante de esta pantalla:
     * el rol se lee ÚNICAMENTE a través del Controller (que a su vez
     * lee SessionManager, la variable de sesión LOCAL guardada en el
     * login), nunca de "product" ni de ninguna respuesta de la API.
     */
    private void applyRoleBasedActions() {
        if (!controller.isCurrentUserAdmin()) {
            // REGLA DE SEGURIDAD VISUAL: para Cliente/Auditor NO tocamos
            // el ViewStub en absoluto. Como nunca se llama a
            // stubAdminActions.inflate(), los botones de Editar/Eliminar
            // JAMÁS se crean como objetos View: no es que estén
            // "escondidos" (View.GONE), es que no existen en la
            // jerarquía de vistas. Por eso NO hay ningún "else" aquí.
            return;
        }

        // Solo si es Admin, inflamos el ViewStub: en este momento (y
        // no antes) se crean de verdad los botones Editar/Eliminar.
        View adminActionsView = stubAdminActions.inflate();

        adminActionsView.findViewById(R.id.btnEdit).setOnClickListener(v ->
                ProductFormActivity.startForEdit(this, REQUEST_CODE_EDIT_PRODUCT, controller.getCurrentProduct()));

        adminActionsView.findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());
    }

    // ===================== US07: recibir el resultado de "Editar" =====================

    /**
     * Cuando ProductFormActivity termina de editar, regresa aquí con
     * los datos nuevos (ver ProductFormActivity.onUpdateSuccess). Los
     * usamos para "reflejar los cambios estéticos... localmente", sin
     * volver a llamar a la API (recuerda: no persiste el cambio real).
     * La actualización del objeto en memoria (con los setters que
     * validan) ahora la hace el Controller (applyLocalEdit).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_PRODUCT && resultCode == Activity.RESULT_OK && data != null) {
            controller.applyLocalEdit(
                    data.getStringExtra(ProductFormActivity.EXTRA_TITLE),
                    data.getDoubleExtra(ProductFormActivity.EXTRA_PRICE, controller.getCurrentProduct().getPrice()),
                    data.getStringExtra(ProductFormActivity.EXTRA_DESCRIPTION),
                    data.getStringExtra(ProductFormActivity.EXTRA_CATEGORY),
                    data.getStringExtra(ProductFormActivity.EXTRA_IMAGE)
            );
        }
    }

    // ===================== US08: Eliminar =====================

    /** "Confirmación Obligatoria": nunca se borra con un solo toque. */
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> controller.deleteProduct())
                .show();
        // Si el usuario toca "Cancelar", el diálogo simplemente se
        // cierra (dialog.dismiss()) y AQUÍ NO SE HACE NINGUNA PETICIÓN
        // HTTP: controller.deleteProduct() solo se llama desde el botón positivo.
    }

    /** "Flujo Exitoso" (US08): redirige automáticamente al catálogo GENERAL (sin filtro activo). */
    private void returnToGeneralCatalog() {
        Intent intent = new Intent(this, CatalogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    /**
     * "Manejo de Errores (404/Network en Detalle)": muestra la alerta
     * "Producto no disponible" y, al aceptar, regresa automáticamente
     * al catálogo (finish() vuelve a CatalogActivity, que ya estaba
     * debajo en la pila de navegación).
     */
    private void showUnavailableAndReturn() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.product_unavailable_title)
                .setMessage(R.string.product_unavailable_message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_accept, (dialog, which) -> goBackWithTransition())
                .show();
    }

    /** Regresa al Catálogo con la animación "hacia atrás" (entra desde la izquierda). */
    private void goBackWithTransition() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // ===================== ProductDetailController.ProductDetailView =====================

    @Override
    public void onProductLoaded(Product product) {
        // Crossfade: el spinner se desvanece mientras el detalle del
        // producto aparece con fade-in, en vez de un cambio brusco.
        progressLoading.animate().alpha(0f).setDuration(200)
                .withEndAction(() -> progressLoading.setVisibility(View.GONE)).start();

        contentDetail.setAlpha(0f);
        contentDetail.setVisibility(View.VISIBLE);
        contentDetail.animate().alpha(1f).setDuration(300).start();

        renderProductFields(product);

        applyRoleBasedActions();
    }

    @Override
    public void onProductUnavailable() {
        showUnavailableAndReturn();
    }

    @Override
    public void onProductUpdatedLocally(Product product) {
        renderProductFields(product);
    }

    @Override
    public void onProductDeleted() {
        Toast.makeText(this, R.string.toast_product_deleted, Toast.LENGTH_SHORT).show();
        returnToGeneralCatalog();
    }

    @Override
    public void onDeleteFailed() {
        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessDenied() {
        Toast.makeText(this, R.string.access_denied_message, Toast.LENGTH_SHORT).show();
    }
}
