package com.fakestore.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de un producto del catálogo (viene de https://fakestoreapi.com/products).
 *
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (POO - Encapsulamiento):
 * El profesor comentó que "los setters necesitan validar y los getters
 * dan formato". Aplicamos esa regla aquí:
 *   - Los SETTERS revisan que el dato tenga sentido antes de guardarlo
 *     (ej. que el precio no sea negativo). Si el dato es inválido,
 *     lanzamos una excepción en vez de guardar basura.
 *   - Los GETTERS no solo regresan el dato "crudo": algunos lo dan ya
 *     formateado y listo para mostrarse en pantalla (ej. el precio con
 *     signo de $ y 2 decimales).
 * Esto es "encapsulamiento": los campos son PRIVATE (nadie de afuera los
 * toca directamente) y solo se puede entrar/salir por los métodos
 * públicos (setters/getters), que controlan que todo quede consistente.
 * ---------------------------------------------------------------------
 */
public class Product {

    // Campos privados: nadie fuera de esta clase puede modificarlos
    // directamente (encapsulamiento). Ya NO son "final" porque ahora
    // se pueden cambiar después de creado el objeto, pero SOLO a través
    // de los setters (que validan).
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("price")
    private double price;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("image")
    private String image;

    /**
     * Constructor: arma un Product de una sola vez.
     * En vez de asignar los campos directamente (this.price = price),
     * llamamos a los SETTERS, así la validación se aplica también aquí
     * y no hay forma de crear un Product con datos inválidos.
     */
    public Product(int id, String title, double price, String description, String category, String image) {
        setId(id);
        setTitle(title);
        setPrice(price);
        setDescription(description);
        setCategory(category);
        setImage(image);
    }

    // ===================== GETTERS =====================

    public int getId() {
        return id;
    }

    /** Getter simple: regresa el título tal cual está guardado. */
    public String getTitle() {
        return title;
    }

    /** Getter "crudo": el precio como número, útil para hacer cálculos. */
    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Getter "crudo" de la categoría: regresa el texto EXACTO que manda
     * la API (ej. "men's clothing"). Lo necesitamos así, sin tocar, para
     * poder usarlo en la URL del filtro /products/category/{cat}.
     */
    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }

    // ================= GETTERS CON FORMATO =================
    // (regla del profesor: "los getters dan formato")

    /** Getter CON FORMATO: precio listo para mostrar en pantalla, ej: "$29.99". */
    public String getFormattedPrice() {
        return String.format(java.util.Locale.US, "$%.2f", price);
    }

    /**
     * Getter CON FORMATO: categoría con la primera letra en mayúscula,
     * para que se vea bonita en la UI (ej. "men's clothing" -> "Men's clothing").
     * Este SÍ es para mostrar en pantalla; para filtrar usa getCategory().
     */
    public String getCategoryDisplay() {
        if (category == null || category.isEmpty()) {
            return category;
        }
        return Character.toUpperCase(category.charAt(0)) + category.substring(1);
    }

    // ===================== SETTERS QUE VALIDAN =====================

    /** El id de un producto nunca debería ser negativo. */
    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id del producto no puede ser negativo");
        }
        this.id = id;
    }

    /** El título no puede venir vacío ni nulo. */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título del producto no puede estar vacío");
        }
        this.title = title;
    }

    /** El precio no puede ser negativo (no existen precios negativos). */
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.price = price;
    }

    /** La descripción puede venir vacía, pero nunca nula (evitamos null por seguridad). */
    public void setDescription(String description) {
        this.description = (description == null) ? "" : description;
    }

    /** La categoría no puede venir vacía ni nula. */
    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }
        this.category = category;
    }

    /** La imagen puede venir vacía (usamos un placeholder), pero nunca nula. */
    public void setImage(String image) {
        this.image = (image == null) ? "" : image;
    }
}
