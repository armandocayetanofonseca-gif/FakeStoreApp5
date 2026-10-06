package com.fakestore.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fakestore.app.R;
import com.fakestore.app.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (Requisito técnico US03 - "Vistas Reciclables"):
 * En vez de crear una vista (item_product.xml) por CADA producto de la
 * lista (lo cual saturaría la memoria si hay 100+ productos),
 * RecyclerView "recicla" un puñado de vistas visibles en pantalla y
 * las va reutilizando/rellenando con datos distintos mientras el
 * usuario hace scroll. El Adapter es quien le dice a RecyclerView cómo
 * armar cada vista (onCreateViewHolder) y cómo llenarla con datos
 * (onBindViewHolder).
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (POO - Encapsulamiento):
 * La lista "products" es PRIVATE: nadie de afuera puede hacer
 * adapter.products.clear() directamente. Solo se puede modificar a
 * través del método público submitList(), que además se encarga de
 * avisarle a RecyclerView que los datos cambiaron.
 * ---------------------------------------------------------------------
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    // Lista privada: el "estado" real vive aquí, encapsulado.
    private final List<Product> products = new ArrayList<>();

    // Interface (contrato) que se dispara cuando tocan un producto.
    private final OnProductClickListener listener;

    // Guarda cuál fue la última posición que ya se animó, para no
    // repetir la animación de entrada cada vez que el usuario hace
    // scroll hacia arriba y hacia abajo sobre las mismas tarjetas.
    private int lastAnimatedPosition = -1;

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    /**
     * Reemplaza TODA la lista actual por una nueva.
     *
     * Requisito US04 "Gestión de Memoria en Filtros": limpiamos primero
     * (products.clear()) ANTES de agregar los nuevos datos, para nunca
     * mezclar productos de la categoría anterior con la nueva y evitar
     * un layout "sucio" (con productos repetidos o de la categoría vieja).
     */
    public void submitList(List<Product> newProducts) {
        products.clear();
        if (newProducts != null) {
            products.addAll(newProducts);
        }
        // Nueva lista -> reiniciamos el conteo de animación para que
        // las tarjetas vuelvan a aparecer en cascada.
        lastAnimatedPosition = -1;
        // Le avisamos a RecyclerView que TODO cambió, para que se
        // redibuje desde cero con la lista nueva.
        notifyDataSetChanged();
    }

    /** Vacía la lista sin esperar respuesta nueva (se usa justo antes de cada llamada a la API). */
    public void clearImmediately() {
        products.clear();
        lastAnimatedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
        animateItemEntrance(holder.itemView, position);
    }

    /**
     * Animación de entrada de cada tarjeta: aparece con fade + un
     * pequeño deslizamiento hacia arriba. Solo se anima la PRIMERA vez
     * que se ve cada posición (si ya se animó, "lastAnimatedPosition"
     * lo recuerda y no se repite al hacer scroll).
     */
    private void animateItemEntrance(View itemView, int position) {
        if (position > lastAnimatedPosition) {
            itemView.setAlpha(0f);
            itemView.setTranslationY(60f);
            itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(position * 40L) // efecto cascada según la posición
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            lastAnimatedPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    /**
     * ViewHolder: guarda las referencias a las vistas de UNA tarjeta
     * (imagen, título, precio) para no tener que buscar
     * (findViewById) cada vez que RecyclerView reutiliza esta vista.
     */
    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivImage;
        private final TextView tvTitle;
        private final TextView tvPrice;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivProductImage);
            tvTitle = itemView.findViewById(R.id.tvProductTitle);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
        }

        void bind(Product product, OnProductClickListener listener) {
            tvTitle.setText(product.getTitle());
            // Getter CON FORMATO: ya viene como "$29.99", no hay que
            // armar el string aquí.
            tvPrice.setText(product.getFormattedPrice());

            // Requisito US03 "Imágenes Asíncronas": Glide descarga la
            // imagen en un hilo de fondo (background thread) y la
            // muestra sola cuando esté lista, sin trabar la UI.
            Glide.with(itemView.getContext())
                    .load(product.getImage())
                    .into(ivImage);

            // Al tocar la tarjeta, avisamos por la interface (el
            // Adapter no sabe qué pasa después, solo "avisa").
            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }
}
