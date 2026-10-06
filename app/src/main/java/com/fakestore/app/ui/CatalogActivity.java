package com.fakestore.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fakestore.app.R;
import com.fakestore.app.controller.CatalogController;
import com.fakestore.app.model.Product;

import java.util.List;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (separación Vista / Controlador):
 * Pantalla de Catálogo (US03 y US04 del spec v2). Esta clase es la
 * "Vista": arma el RecyclerView, las pills de filtro y los 3 estados
 * de pantalla (cargando/contenido/error), pero YA NO llama a Retrofit
 * directamente ni guarda "cuál es la categoría activa" como lógica de
 * negocio — eso vive en {@link CatalogController}, que esta Activity
 * implementa a través de CatalogController.CatalogView.
 *
 * "selectedPillView" SÍ se queda aquí: es puro estado visual (qué pill
 * está resaltada ahora mismo), no una regla de negocio.
 * ---------------------------------------------------------------------
 */
public class CatalogActivity extends AppCompatActivity implements CatalogController.CatalogView {

    private RecyclerView recyclerProducts;
    private ProgressBar progressLoading;
    private View layoutError;
    private LinearLayout llFilters;

    private ProductAdapter adapter;
    private CatalogController controller;

    // Guardamos referencia a la pill seleccionada para poder
    // "des-resaltarla" cuando el usuario elija otra. Esto es estado
    // puramente visual, por eso se queda en la Vista.
    private TextView selectedPillView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        controller = new CatalogController(this, this);

        recyclerProducts = findViewById(R.id.recyclerProducts);
        progressLoading = findViewById(R.id.progressLoading);
        layoutError = findViewById(R.id.layoutError);
        llFilters = findViewById(R.id.llFilters);

        findViewById(R.id.btnBack).setOnClickListener(v -> goBackWithTransition());
        findViewById(R.id.btnRetry).setOnClickListener(v -> controller.retryLastLoad());

        setupRecyclerView();
        addAllFilterPill(); // la pill "Ver todos" siempre existe, va primero
        controller.loadCategories(); // luego se agregan las categorías de la API
        controller.loadProducts(null); // al entrar, se ve el catálogo general
        setupAddProductFabIfAdmin(); // US06: botón "+" solo para Admin
    }

    /**
     * US06: el botón para crear un producto solo debe existir para el
     * Administrador. Igual que en ProductDetailActivity, el rol se lee
     * (a través del Controller, que a su vez lee SessionManager, la
     * sesión LOCAL) y, si no es Admin, el ViewStub simplemente nunca se
     * infla: el botón no llega a crearse.
     */
    private void setupAddProductFabIfAdmin() {
        if (!controller.isCurrentUserAdmin()) {
            return;
        }

        ViewStub stubFab = findViewById(R.id.stubAddProductFab);
        View fab = stubFab.inflate();
        fab.setOnClickListener(v -> ProductFormActivity.startForCreate(this));
    }

    /**
     * RecyclerView en cuadrícula de 2 columnas (más cómodo para tarjetas
     * con imagen que una lista vertical simple).
     */
    private void setupRecyclerView() {
        adapter = new ProductAdapter(product -> {
            // Esto es una IMPLEMENTACIÓN de la interface
            // OnProductClickListener usando una expresión lambda: es la
            // forma corta de escribir "new OnProductClickListener() {
            // onProductClick(product) { ... } }".
            ProductDetailActivity.start(this, product.getId());
        });
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setAdapter(adapter);
    }

    // ===================== Filtros (US04) =====================

    /** La pill "Ver todos" siempre está presente y quita cualquier filtro activo. */
    private void addAllFilterPill() {
        TextView pill = inflateFilterPill(getString(R.string.filter_all));
        pill.setOnClickListener(v -> selectCategory(null, pill));
        llFilters.addView(pill);
        selectPillVisual(pill); // seleccionada por defecto
    }

    private TextView inflateFilterPill(String text) {
        TextView pill = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.item_filter_pill, llFilters, false);
        pill.setText(text);
        return pill;
    }

    /**
     * Se llama cuando el usuario toca una pill (categoría o "Ver todos").
     * "category" puede ser null (= sin filtro = catálogo general).
     */
    private void selectCategory(String category, TextView pillView) {
        selectPillVisual(pillView);
        controller.loadProducts(category);
    }

    /** Cambia el estilo visual: resalta la pill tocada y regresa la anterior a su estado normal. */
    private void selectPillVisual(TextView newSelection) {
        if (selectedPillView != null) {
            selectedPillView.setBackgroundResource(R.drawable.bg_filter_pill_unselected);
            selectedPillView.setTextColor(getColor(R.color.filter_pill_unselected_text));
        }
        newSelection.setBackgroundResource(R.drawable.bg_filter_pill_selected);
        newSelection.setTextColor(getColor(R.color.filter_pill_selected_text));
        selectedPillView = newSelection;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    // ===================== CatalogController.CatalogView =====================

    @Override
    public void showLoading() {
        progressLoading.setAlpha(1f);
        progressLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
    }

    /**
     * Cuando llegan los productos, en vez de solo "aparecer" de golpe,
     * el RecyclerView hace un fade-in suave (crossfade) mientras el
     * spinner se desvanece: se siente como una transición, no como un
     * cambio brusco de pantalla.
     */
    @Override
    public void showContent() {
        progressLoading.animate().alpha(0f).setDuration(200)
                .withEndAction(() -> progressLoading.setVisibility(View.GONE)).start();
        layoutError.setVisibility(View.GONE);

        recyclerProducts.setAlpha(0f);
        recyclerProducts.animate().alpha(1f).setDuration(250).start();
    }

    @Override
    public void showError() {
        progressLoading.setVisibility(View.GONE);
        progressLoading.setAlpha(1f); // por si venía de un fade-out anterior
        layoutError.setAlpha(0f);
        layoutError.setVisibility(View.VISIBLE);
        layoutError.animate().alpha(1f).setDuration(250).start();
    }

    @Override
    public void clearProductList() {
        adapter.clearImmediately();
    }

    @Override
    public void onProductsLoaded(List<Product> products) {
        adapter.submitList(products);
    }

    @Override
    public void onCategoriesLoaded(List<String> categories) {
        for (String category : categories) {
            TextView pill = inflateFilterPill(capitalize(category));
            pill.setOnClickListener(v -> selectCategory(category, pill));
            llFilters.addView(pill);
        }
    }

    /** Regresa al Perfil con la animación "hacia atrás" (entra desde la izquierda). */
    private void goBackWithTransition() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        // También aplicamos la transición cuando el usuario usa el
        // botón/gesto "Atrás" del sistema, no solo nuestro ícono.
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
