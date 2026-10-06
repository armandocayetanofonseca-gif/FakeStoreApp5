package com.fakestore.app.ui;

import com.fakestore.app.model.Product;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (POO - Interfaces):
 * Una interface es un "contrato": solo declara QUÉ métodos debe tener
 * quien la implemente, pero no dice CÓMO deben funcionar por dentro.
 *
 * Aquí la usamos para que ProductAdapter (quien dibuja cada tarjeta de
 * producto en el RecyclerView) pueda avisarle a CatalogActivity "oye,
 * tocaron este producto" sin que el Adapter necesite saber nada de
 * CatalogActivity ni de cómo se abre la pantalla de detalle. El Adapter
 * solo conoce la interface (el contrato); CatalogActivity es quien la
 * implementa y decide qué hacer cuando se cumple ese contrato.
 *
 * Esto es exactamente el mismo patrón que usa Retrofit con su interface
 * Callback<T> (ver ApiService/LoginActivity): la librería no sabe qué
 * vamos a hacer con la respuesta, solo sabe que existe un método
 * onResponse()/onFailure() que va a llamar cuando termine.
 * ---------------------------------------------------------------------
 */
public interface OnProductClickListener {

    /**
     * Se llama cuando el usuario toca una tarjeta de producto.
     * @param product el producto que fue tocado
     */
    void onProductClick(Product product);
}
