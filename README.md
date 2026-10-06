# FakeStore — Sistema Móvil (Java / Android Studio)

Implementación en Java de las especificaciones de UX/UI recibidas de **Armando Fonseca** (Project Manager) para el equipo **Rafa, Omar, Angel, Chiquilín, Choster y Jairo**.

Este README cubre **v1 (Login + Perfil por rol) y v2 (Catálogo + Filtros + Detalle por rol, US01–US05)**.

## Cómo abrirlo en Android Studio

1. Descomprime/copia la carpeta `FakeStoreApp/`.
2. Android Studio → **File > Open...** → selecciona `FakeStoreApp/`.
3. Sincroniza Gradle (si pide regenerar el wrapper, acepta).
4. Run ▶ sobre un emulador o dispositivo (minSdk 26 / Android 8.0+).

## Notas de clase (POO) — dónde encontrar cada pilar

Como parte de la materia ya vimos POO básica (encapsulamiento, herencia, polimorfismo, interfaces). Aquí está mapeado dónde aparece cada uno en el código, con comentarios "NOTA DE CLASE" en los archivos:

| Pilar de POO | Dónde se ve | Archivo(s) |
|---|---|---|
| **Encapsulamiento** | Campos `private`, setters que **validan** (ej. precio no negativo, email con "@"), getters que **dan formato** (ej. `getFormattedPrice()`, `getFullAddress()`) | `model/Product.java`, `model/User.java`, `session/SessionManager.java` |
| **Herencia** | `AdminRoleProfile`, `AuditorRoleProfile`, `ClientRoleProfile` extienden (`extends`) la clase abstracta `RoleProfile` | `role/*.java` |
| **Polimorfismo** | `ProfileActivity` llama `profile.getEmoji()` sin saber si "profile" es Admin/Auditor/Cliente; cada subclase responde distinto | `role/RoleProfile.java` + `ui/ProfileActivity.java` |
| **Interfaces** | `OnProductClickListener` (interface propia del proyecto) y `Callback<T>` (de Retrofit, implementada con clases anónimas / lambdas) | `ui/OnProductClickListener.java`, `ui/ProductAdapter.java`, `ui/LoginActivity.java` |

### Sobre la duda del profesor ("la clase padre no era necesaria")

Está respondida directamente como comentario en `role/RoleProfile.java`. Resumen: es cierto que nunca hacemos `new RoleProfile()` (no se puede, es `abstract`), pero **por eso mismo** es necesaria — existe como el "tipo común" que hace posible el polimorfismo, no como algo que se instancia. Decidimos **conservarla**; si prefieres la posición del profesor, el comentario en ese archivo explica exactamente qué cambiaría (volver a un if/else por rol en `ProfileActivity`).

### Sobre "setters validan, getters dan formato"

Aplicado en `Product.java` y `User.java`:
- **Setters que validan**: `setPrice()` rechaza precios negativos, `setTitle()`/`setCategory()` rechazan vacíos, `setEmail()` exige que tenga "@", etc. Si el dato es inválido, lanzan `IllegalArgumentException`.
- **Getters con formato**: `getFormattedPrice()` regresa `"$29.99"` en vez del `double` crudo; `getCategoryDisplay()` regresa la categoría con mayúscula inicial; `getFullName()` y `getFullAddress()` (en `User.java`) arman el string final listo para pantalla.

## v2: Catálogo, Filtros y Detalle por Rol (US03–US05)

### Catálogo (`CatalogActivity` + `ProductAdapter`)
- **RecyclerView** con `GridLayoutManager` (2 columnas) — vistas reciclables, no se crea una vista por producto.
- **Glide** descarga las imágenes en background thread (no bloquea la UI).
- Cada tarjeta muestra: imagen, título y precio (obligatorio por spec).
- **Filtros por categoría** como "pills" (equivalen a Chips), cargados desde `/products/categories`; no ocultan el catálogo, solo cambian qué se ve.
- **Gestión de memoria**: antes de pintar una categoría nueva, se limpia el arreglo anterior (`adapter.clearImmediately()`) para no mezclar datos.
- Estados: spinner circular centrado mientras carga; mensaje + botón "Reintentar" si falla la red.

### Detalle de producto (`ProductDetailActivity`)
- Consume `/products/{id}`.
- **Regla de seguridad clave**: los permisos de gestión (botones Editar/Eliminar) se deciden leyendo `SessionManager.getRole()` — la variable de sesión **local** guardada en el login — nunca la respuesta de la API.
- Para Administrador (IDs 1, 2): se **infla un `ViewStub`** con los botones Editar/Eliminar (accent `#BB86FC`).
- Para Cliente/Auditor: el `ViewStub` **nunca se infla**, así que los botones no existen de verdad en la jerarquía de vistas (no es lo mismo que `View.GONE` sobre botones ya creados — así lo exige el spec).
- Error 404/red: alerta "Producto no disponible" → al aceptar, regresa automáticamente al catálogo.

## v3: Gestión CRUD de Inventario — solo Admin (US06–US08)

FakeStoreAPI **simula** las escrituras (POST/PUT/DELETE): responde 200 OK con el objeto procesado, pero no lo guarda de verdad (el catálogo general no cambia). Por eso, después de crear/editar/borrar, la app refleja el cambio "a mano" en pantalla, sin volver a pedirle la lista completa a la API.

### Crear producto (US06) — `ProductFormActivity` en modo `create`
- Botón "+" flotante en el catálogo, visible **solo para Admin** (mismo patrón `ViewStub` que el detalle: para Cliente/Auditor el botón no se infla, no existe en la jerarquía).
- Formulario con Título, Precio, Descripción, URL de Imagen y Categoría.
- Validación local antes de mandar la petición: ningún campo vacío, precio estrictamente numérico. Los campos inválidos se marcan en rojo (`#CF6679`) vía `TextInputLayout.setError()`.
- Al tener éxito: alerta con el ID nuevo generado → al aceptar, se limpia el formulario completo (queda listo para cargar otro producto).

### Editar producto (US07) — `ProductFormActivity` en modo `edit`
- Se abre desde el botón "Editar" del detalle, con el mismo diseño de formulario, **pre-cargado** con los datos actuales.
- Al presionar Guardar: el botón se deshabilita y aparece un spinner (evita doble envío) hasta que responde la API.
- Al tener éxito: mensaje flotante "Producto actualizado (Simulación)", se cierra el formulario y `ProductDetailActivity` recibe los datos nuevos por `onActivityResult` para repintarlos localmente (sin volver a llamar a la API).

### Eliminar producto (US08)
- Botón "Eliminar" con **énfasis destructivo** (rojo sólido, distinto del morado de "Editar").
- **Confirmación obligatoria**: `AlertDialog` "¿Estás seguro de eliminar este producto?". Si se cancela, no se dispara ninguna petición HTTP.
- Al confirmar y tener éxito: mensaje flotante + redirección automática al catálogo general (sin filtro activo).

### Seguridad (aplicada en 2 capas, no solo una)
- **Capa 1 — al abrir la pantalla**: `ProductFormActivity.onCreate()` revisa el rol *antes* de `setContentView()`; si no es Admin, redirige al catálogo sin llegar a dibujar el formulario (cubre el caso de "forzar la vista vía deeplinks").
- **Capa 2 — justo antes de la petición de red**: tanto `ProductFormActivity` (guardar) como `ProductDetailActivity` (eliminar) vuelven a comprobar el rol inmediatamente antes de llamar a `createProduct`/`updateProduct`/`deleteProduct`, por si la capa 1 se saltara de alguna forma. En ambos casos el rol se lee de `SessionManager` (sesión local), nunca de la API.
- El botón "Eliminar" (y el "+" de crear) **no se instancian ocultos**: se excluyen dinámicamente de la jerarquía con `ViewStub`, igual que Editar/Eliminar en el detalle.

## Estructura del código

| Área | Archivo(s) |
|---|---|
| Modelos (con setters que validan, getters con formato) | `model/User.java`, `model/Product.java`, `model/LoginRequest.java`, `model/LoginResponse.java` |
| Red (Retrofit) | `network/ApiService.java`, `network/ApiClient.java` |
| Polimorfismo de roles | `role/RoleProfile.java` (abstracta) + `role/AdminRoleProfile.java`, `role/AuditorRoleProfile.java`, `role/ClientRoleProfile.java` |
| Sesión / logout | `session/SessionManager.java`, `session/LogoutHelper.java`, `session/CartLocalStorage.java` |
| Login | `ui/LoginActivity.java`, `util/NetworkUtils.java` |
| Perfil unificado (3 roles) | `ui/ProfileActivity.java` |
| Catálogo + filtros + FAB "crear" (solo Admin) | `ui/CatalogActivity.java`, `ui/ProductAdapter.java`, `ui/OnProductClickListener.java` (interface) |
| Detalle de producto + Editar/Eliminar (solo Admin) | `ui/ProductDetailActivity.java` |
| Formulario crear/editar producto (CRUD, solo Admin) | `ui/ProductFormActivity.java` |

## Regla de negocio crítica

`User.java` **no define un campo `password`**. Aunque el JSON de `https://fakestoreapi.com/users` lo incluya, Gson lo ignora al no existir un campo mapeado — el dato nunca llega a memoria ni a ningún estado de la app.

## Pendiente / fuera de alcance

- Los botones "Editar" y "Eliminar" del Admin están conectados (muestran un `Toast`) pero no implementan la lógica real de edición/borrado contra la API — el spec v2 no la pide explícitamente, solo la visibilidad condicionada por rol.
- El carrito de compras (`CartLocalStorage`) solo existe para cumplir la limpieza en logout; no guarda productos todavía.
