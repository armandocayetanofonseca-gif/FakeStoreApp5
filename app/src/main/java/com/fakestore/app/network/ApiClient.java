package com.fakestore.app.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (POO - Encapsulamiento / patrón Singleton):
 * El constructor es PRIVATE: nadie puede hacer "new ApiClient()" desde
 * afuera. La única forma de obtener el ApiService es a través del
 * método estático getApiService(), que crea el objeto Retrofit UNA sola
 * vez (la primera vez que se llama) y lo reutiliza siempre después
 * (por eso "apiService" es "static": pertenece a la clase, no a un
 * objeto en particular). Esto evita crear conexiones repetidas sin
 * necesidad.
 * ---------------------------------------------------------------------
 */
public class ApiClient {

    private static final String BASE_URL = "https://fakestoreapi.com/";

    // "static" = un solo ApiService compartido por toda la app.
    private static ApiService apiService;

    // Constructor privado: esta clase no se instancia, solo se usa por
    // sus métodos estáticos.
    private ApiClient() {
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
