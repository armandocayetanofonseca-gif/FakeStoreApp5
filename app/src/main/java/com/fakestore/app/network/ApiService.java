package com.fakestore.app.network;

import com.fakestore.app.model.LoginRequest;
import com.fakestore.app.model.LoginResponse;
import com.fakestore.app.model.Product;
import com.fakestore.app.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (POO - Interfaces):
 * Esta también es una interface (igual que OnProductClickListener en
 * ui/). Aquí no escribimos NINGÚN código real, solo declaramos "qué
 * llamadas a la API existen". Retrofit lee esta interface y, por
 * detrás, genera automáticamente una clase que sí sabe cómo armar cada
 * petición HTTP real. Nosotros nunca vemos esa clase generada; solo
 * usamos ApiClient.getApiService() y llamamos a estos métodos como si
 * fueran normales.
 * ---------------------------------------------------------------------
 * NOTA v3 (CRUD - US06/US07/US08):
 * FakeStoreAPI SIMULA las peticiones de escritura (POST/PUT/DELETE):
 * responde 200 OK con el objeto procesado, pero NO lo guarda de verdad
 * en su base de datos (el catálogo general /products sigue igual). Por
 * eso, después de crear/editar/borrar, la app tiene que reflejar el
 * cambio "a mano" en la pantalla (ver ProductFormActivity y
 * ProductDetailActivity), no volviendo a pedir la lista completa.
 * ---------------------------------------------------------------------
 */
public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("users")
    Call<List<User>> getUsers();

    /** US03: catálogo general. */
    @GET("products")
    Call<List<Product>> getProducts();

    /** US04: catálogo filtrado por categoría. */
    @GET("products/category/{category}")
    Call<List<Product>> getProductsByCategory(@Path("category") String category);

    /** US04: lista de categorías disponibles para los filtros. */
    @GET("products/categories")
    Call<List<String>> getCategories();

    /** US05: detalle de producto por id. */
    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") int id);

    /** US06: crear un producto nuevo (simulado por la API). */
    @POST("products")
    Call<Product> createProduct(@Body Product product);

    /** US07: editar un producto existente (simulado por la API). */
    @PUT("products/{id}")
    Call<Product> updateProduct(@Path("id") int id, @Body Product product);

    /** US08: eliminar un producto (simulado por la API; no devuelve contenido). */
    @DELETE("products/{id}")
    Call<Void> deleteProduct(@Path("id") int id);
}
