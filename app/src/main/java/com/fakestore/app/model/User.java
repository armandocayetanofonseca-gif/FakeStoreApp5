package com.fakestore.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de usuario mapeado desde https://fakestoreapi.com/users
 *
 * ---------------------------------------------------------------------
 * REGLA DE NEGOCIO CRÍTICA (seguridad):
 * Está ESTRICTAMENTE PROHIBIDO recuperar, almacenar en estado global o
 * renderizar el campo "password". Por eso este modelo NO define un campo
 * password. Aunque la API devuelva esa propiedad en el JSON, Gson
 * simplemente la ignora porque no existe un campo mapeado para
 * deserializarla, evitando que el dato llegue a memoria/estado de la app.
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (POO - Encapsulamiento):
 * Igual que en Product.java: los campos son PRIVATE, los SETTERS validan
 * antes de guardar, y algunos GETTERS regresan el dato con formato listo
 * para mostrarse en pantalla.
 * ---------------------------------------------------------------------
 */
public class User {

    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("name")
    private Name name;

    @SerializedName("address")
    private Address address;

    @SerializedName("phone")
    private String phone;

    // NOTA: NO existe "private String password;" a propósito (ver regla arriba).

    public User(int id, String email, String username, Name name, Address address, String phone) {
        setId(id);
        setEmail(email);
        setUsername(username);
        this.name = name;
        this.address = address;
        setPhone(phone);
    }

    // ===================== GETTERS =====================

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public Name getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    // ===================== SETTERS QUE VALIDAN =====================

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id de usuario no puede ser negativo");
        }
        this.id = id;
    }

    /** Validación simple: un email debe contener "@" (no es una validación perfecta, pero atrapa el error obvio). */
    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El email no es válido: " + email);
        }
        this.email = email;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El username no puede estar vacío");
        }
        this.username = username;
    }

    public void setPhone(String phone) {
        this.phone = (phone == null) ? "" : phone;
    }

    /** name: { firstname, lastname } */
    public static class Name {
        @SerializedName("firstname")
        private String firstname;
        @SerializedName("lastname")
        private String lastname;

        public Name(String firstname, String lastname) {
            setFirstname(firstname);
            setLastname(lastname);
        }

        public String getFirstname() {
            return firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setFirstname(String firstname) {
            this.firstname = (firstname == null) ? "" : firstname;
        }

        public void setLastname(String lastname) {
            this.lastname = (lastname == null) ? "" : lastname;
        }

        /** Getter CON FORMATO: junta nombre + apellido listos para mostrar. */
        public String getFullName() {
            return (firstname + " " + lastname).trim();
        }
    }

    /** address: { city, street, number, zipcode, geolocation } */
    public static class Address {
        @SerializedName("city")
        private String city;
        @SerializedName("street")
        private String street;
        @SerializedName("number")
        private int number;
        @SerializedName("zipcode")
        private String zipcode;
        @SerializedName("geolocation")
        private Geolocation geolocation;

        public Address(String city, String street, int number, String zipcode, Geolocation geolocation) {
            this.city = city;
            this.street = street;
            setNumber(number);
            this.zipcode = zipcode;
            this.geolocation = geolocation;
        }

        public String getCity() {
            return city;
        }

        public String getStreet() {
            return street;
        }

        public int getNumber() {
            return number;
        }

        public String getZipcode() {
            return zipcode;
        }

        public Geolocation getGeolocation() {
            return geolocation;
        }

        /** El número de la calle no puede ser negativo. */
        public void setNumber(int number) {
            if (number < 0) {
                throw new IllegalArgumentException("El número de la dirección no puede ser negativo");
            }
            this.number = number;
        }

        /** Getter CON FORMATO: arma la dirección completa lista para mostrar. */
        public String getFullAddress() {
            return street + " " + number + ", " + city + " (" + zipcode + ")";
        }
    }

    /** geolocation: { lat, long } */
    public static class Geolocation {
        @SerializedName("lat")
        private String lat;
        @SerializedName("long")
        private String lng;

        public Geolocation(String lat, String lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public String getLat() {
            return lat;
        }

        public String getLng() {
            return lng;
        }
    }
}
