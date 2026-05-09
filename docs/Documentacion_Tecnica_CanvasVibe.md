# CanvasVibe — Documentación Técnica

**Marketplace de Arte Personalizado para Android**
Versión 1.0 · Mayo 2026
Universidad: Fundación Universitaria Compensar
Autor: Andrés Felipe Fonseca Ochoa

---

## 1. Propósito de este documento

Este documento complementa el *Documento de Requerimientos de Software v1.0*. Mientras aquel define **qué** debe hacer CanvasVibe, este explica **cómo está construida** la aplicación: cómo se organiza el código, qué tecnologías usa, cómo se conecta con la base de datos, qué pantallas existen y cómo navegan entre sí.

El objetivo es que cualquier persona que reciba el proyecto pueda entender la estructura general sin necesidad de leer línea por línea, y que sirva de guía rápida para mantener o ampliar la app más adelante.

---

## 2. Visión general

CanvasVibe es una aplicación móvil nativa para Android que conecta artistas independientes con compradores de arte personalizado en Colombia. La app se ejecuta como una sola aplicación instalada en el celular, pero internamente se divide en tres experiencias distintas según el rol del usuario:

- **Comprador** — explora el catálogo, agrega productos al carrito, paga y hace seguimiento del pedido.
- **Vendedor (Artista)** — publica obras, gestiona sus pedidos, edita su perfil público.
- **Administrador** — supervisa la plataforma, verifica artistas, gestiona categorías y revisa reportes.

Toda la información (usuarios, productos, pedidos, categorías) se guarda en la nube usando Firebase, lo que permite que los datos estén siempre sincronizados entre dispositivos y que el negocio escale sin necesidad de mantener servidores propios.

---

## 3. Stack tecnológico

| Componente | Tecnología | ¿Para qué sirve? |
|---|---|---|
| Lenguaje | Kotlin 2.3 | Lenguaje oficial moderno de Android, más seguro y conciso que Java. |
| Interfaz de usuario | Jetpack Compose + Material 3 | Forma declarativa de construir pantallas (describes lo que quieres ver y Android lo dibuja). |
| Arquitectura | MVVM (Model–View–ViewModel) | Separa la lógica de negocio de la interfaz para que el código sea fácil de mantener y probar. |
| Navegación | Navigation Compose 2.9.7 | Maneja el cambio entre pantallas y los parámetros que se pasan entre ellas. |
| Imágenes remotas | Coil 2.7 | Descarga, cachea y muestra imágenes desde URLs (ej: fotos de productos en Firebase Storage). |
| Autenticación | Firebase Auth | Maneja el registro, login y sesiones de usuario sin necesidad de implementar servidores. |
| Base de datos | Firebase Firestore | Almacena todos los datos del negocio (usuarios, productos, pedidos, categorías). Sincroniza en tiempo real. |
| Almacenamiento de archivos | Firebase Storage | Guarda las imágenes que suben los vendedores. |
| Mensajería push | Firebase Cloud Messaging (FCM) | Permite enviar notificaciones (preparado, no activado en v1.0). |
| Biometría | AndroidX Biometric 1.1 | Permite ingreso con huella o rostro usando los sensores del propio celular. |
| Build | Gradle 9.4 + Version Catalog | Sistema que compila el proyecto y maneja todas las librerías. |

**Versiones mínimas y objetivo:**
- Android mínimo soportado: 8.0 (API 26) — cubre alrededor del 95% de dispositivos Android en Colombia.
- Android objetivo: 14 (API 36) — la app está optimizada para la versión más reciente.

---

## 4. Arquitectura general

La aplicación sigue el patrón **MVVM (Model–View–ViewModel)** organizado en capas:

**1. Capa de presentación (UI)**
Las pantallas (`Screens`) están escritas en Jetpack Compose. Cada pantalla solo se preocupa por **dibujar lo que ve el usuario** y reaccionar a sus toques. No contiene lógica de negocio.

**2. ViewModel**
Es el intermediario entre la pantalla y los datos. Cada pantalla compleja tiene un ViewModel asociado que:
- Recibe acciones del usuario (clic, escribir, etc.).
- Pide datos a los repositorios.
- Expone el estado actual de la pantalla mediante `StateFlow` (un flujo reactivo de Kotlin).

Cuando el ViewModel actualiza el estado, la pantalla se redibuja automáticamente.

**3. Capa de datos (Repositorios)**
Los repositorios encapsulan el acceso a Firebase. La pantalla nunca habla con Firebase directamente — siempre lo hace a través de un repositorio. Esto permite cambiar la fuente de datos en el futuro sin tocar la UI.

Existen los siguientes repositorios:

- `AuthRepository` — registro, login, logout, usuario actual.
- `ProductRepository` — consultas y cambios sobre productos.
- `OrderRepository` — pedidos.
- `CartRepository` — carrito por usuario.
- `CategoryRepository` — categorías del catálogo.
- `StorageRepository` — subir imágenes.

**4. Modelos**
Son clases simples (data classes de Kotlin) que representan las entidades del negocio: `User`, `Product`, `Order`, `CartItem`, `Category`. Son lo que viaja entre Firebase y la app.

**Flujo típico de una acción:**
> Usuario toca un producto en el catálogo → la pantalla notifica al ViewModel → el ViewModel pide el producto al `ProductRepository` → el repositorio consulta Firestore → los datos vuelven al ViewModel → el ViewModel actualiza su `StateFlow` → la pantalla se redibuja con los datos.

---

## 5. Estructura del proyecto

El código está organizado por funcionalidad, no por tipo de archivo. Esto significa que todo lo relacionado con (por ejemplo) el carrito está junto, en lugar de tener todas las pantallas en una carpeta y todos los ViewModels en otra.

```
app/src/main/java/com/canvasvibe/app/
│
├── MainActivity.kt           ← Punto de entrada de la app
│
├── navigation/               ← Define todas las rutas y la navegación
│   ├── Screen.kt             ← Lista de pantallas y sus identificadores
│   └── AppNavigation.kt      ← Mapa de qué pantalla mostrar para cada ruta
│
├── data/                     ← Acceso a datos
│   ├── model/                ← Estructuras de datos (User, Product, Order, etc.)
│   ├── repository/           ← Lógica para hablar con Firebase
│   └── prefs/                ← Preferencias locales (ej: biometría)
│
└── ui/                       ← Todas las pantallas e interfaz
    ├── theme/                ← Colores, tipografías, tema oscuro
    ├── auth/                 ← Login, registro, biometría
    ├── buyer/                ← Pantallas del comprador
    │   ├── home/             (catálogo)
    │   ├── detail/           (detalle de producto)
    │   ├── cart/             (carrito)
    │   ├── tracking/         (seguimiento)
    │   ├── profile/          (perfil del comprador)
    │   └── components/       (componentes compartidos del comprador)
    ├── seller/               ← Pantallas del vendedor
    │   ├── dashboard/
    │   ├── products/         (lista con CRUD)
    │   ├── addproduct/       (formulario crear/editar)
    │   ├── orders/
    │   ├── profile/
    │   └── components/
    └── admin/                ← Pantallas de administrador
        ├── dashboard/
        ├── artists/
        ├── buyers/
        ├── categories/
        ├── reports/
        └── components/
```

Cada subcarpeta de pantalla contiene típicamente:
- `XxxScreen.kt` — la interfaz visual.
- `XxxViewModel.kt` — el cerebro que maneja datos y acciones.

---

## 6. Estructura de datos en Firestore

Firebase Firestore organiza la información en **colecciones** (similar a tablas) y **documentos** (similar a filas). CanvasVibe usa cuatro colecciones principales:

### users
Cada documento representa un usuario registrado. El identificador del documento es el `uid` que asigna Firebase Auth automáticamente.

| Campo | Tipo | Descripción |
|---|---|---|
| `uid` | string | Identificador único |
| `email` | string | Correo de la cuenta |
| `name` | string | Nombre visible |
| `role` | string | `ROLE_BUYER` / `ROLE_SELLER` / `ROLE_ADMIN` |
| `artistStatus` | string (opcional) | Estado de verificación: `VERIFICADO`, `PENDIENTE`, `SUSPENDIDO` |
| `buyerStatus` | string (opcional) | `ACTIVO` o `BLOQUEADO` |
| `createdAt` | número | Fecha de creación en milisegundos |

### products
Cada producto publicado por un vendedor.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | string | Identificador del producto |
| `sellerId` | string | uid del vendedor |
| `sellerName` | string | Nombre del vendedor |
| `title` | string | Nombre del producto |
| `description` | string | Descripción larga |
| `category` | string | Slug de la categoría (ej: "gamer") |
| `materials` | array | Lista de materiales disponibles |
| `sizes` | array | Tamaños disponibles |
| `priceBase` | número | Precio base en pesos colombianos |
| `imageUrls` | array | URLs de las imágenes en Storage |
| `rating` | número | Promedio de calificaciones |
| `reviewCount` | número | Cantidad de reseñas |
| `stock` | número | Unidades disponibles |
| `elaborationDays` | string | Tiempo estimado (ej: "7-10 días") |
| `isCustomizable` | booleano | Permite variaciones |
| `isActive` | booleano | Producto visible en catálogo |
| `createdAt` | número | Fecha de creación |

### orders
Cada pedido realizado.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | string | ID del pedido |
| `buyerId` / `buyerName` | string | Comprador |
| `sellerId` / `sellerName` | string | Vendedor |
| `productId` / `productTitle` / `productImageUrl` | string | Producto comprado |
| `material`, `size`, `quantity` | varios | Variante elegida |
| `unitPrice`, `totalPrice` | número | Precios |
| `status` | string | `PENDING` / `PREPARING` / `SHIPPED` / `DELIVERED` / `CANCELLED` |
| `createdAt`, `updatedAt` | número | Fechas |

### categories
Categorías del catálogo (gamer, paisajes, animales, anime, abstracto, etc.).

| Campo | Tipo | Descripción |
|---|---|---|
| `id` / `slug` | string | Identificador interno |
| `name` | string | Nombre visible |
| `emoji` | string | Emoji que la representa |
| `colorHex` | string | Color asociado (ej: "#7C4DFF") |
| `productCount` | número | Cantidad de productos (calculado) |
| `order` | número | Orden de visualización |
| `isActive` | booleano | Si aparece en el catálogo |

### Firebase Storage
Las imágenes se guardan en estas rutas:

- `products/{sellerId}/{productId}/imagen_{n}.jpg` — fotos de productos.
- `profiles/{uid}/avatar.jpg` — avatares de usuarios (preparado, no activado).

---

## 7. Roles y reglas de negocio

### Cómo se asigna el rol
- Por defecto, todo registro nuevo se crea como `ROLE_BUYER`.
- En el formulario de registro el usuario puede elegir `ROLE_SELLER`. Cuando lo hace, queda en estado `PENDIENTE` hasta que un administrador lo verifique.
- El rol `ROLE_ADMIN` **nunca** se puede asignar desde la app. Solo se asigna manualmente cambiando el campo `role` directamente en la consola de Firestore. Esta es una decisión de seguridad: evita que alguien pueda escalar privilegios desde la propia app.

### Después del login
Al iniciar sesión, la app lee el `role` del usuario desde Firestore y lo redirige al dashboard correspondiente:

- `ROLE_BUYER` → BuyerHome (catálogo).
- `ROLE_SELLER` → SellerDashboard.
- `ROLE_ADMIN` → AdminDashboard.

### Biometría
La autenticación biométrica usa la API `BiometricPrompt` de Android, lo que significa que **los datos biométricos nunca salen del dispositivo**. La app solo recibe un "sí, es el dueño del celular" o "no". Esto cumple con la Ley 1581 sobre datos sensibles.

La decisión del usuario (activar o "ahora no") se guarda localmente en `SharedPreferences` por usuario. Si la activa, la próxima vez que inicie sesión podrá entrar con huella sin volver a escribir contraseña. Si dice "ahora no", la app no le vuelve a preguntar al hacer login (puede activarla más tarde desde el perfil).

---

## 8. Pantallas implementadas

### Autenticación
1. **Login / Registro** — formulario con correo, contraseña y, en modo registro, nombre y selector de rol (Comprador / Vendedor).
2. **Biometría** — pantalla intermedia entre el login y el dashboard que pregunta si quiere activar huella/rostro. Se muestra una sola vez por usuario.

### Comprador (5 pantallas)
3. **Inicio / Catálogo** — barra de búsqueda, chips de categorías cargados desde Firestore, lista de productos con tarjeta destacada arriba.
4. **Detalle de producto** — galería, descripción, selector de material y tamaño, precio en COP, botón de favoritos y botón "Agregar al carrito".
5. **Carrito** — lista de productos agregados, control de cantidades, resumen de subtotal/comisión/total, botón "Pagar ahora".
6. **Seguimiento de pedido** — estado actual, mapa placeholder, timeline narrativo del pedido.
7. **Perfil del comprador** — header con avatar y nombre, estadísticas (pedidos, favoritos), lista de configuración (mis pedidos, favoritos, direcciones, métodos de pago, notificaciones), toggle de biometría, botón cerrar sesión.

### Vendedor (5 pantallas)
8. **Dashboard del vendedor** — KPI "Ventas de hoy" calculado en tiempo real, tarjeta semanal placeholder, actividad ("X pedidos listos para despacho", "Y pagos por confirmar"), pedidos recientes, botón "Publicar nuevo producto".
9. **Lista de productos** — todos los productos del vendedor con miniatura, precio, stock con `[− n +]`, badge Activo/Oculto y acciones por producto: **Editar**, **Inhabilitar/Activar**, **Eliminar** (con confirmación).
10. **Agregar / Editar producto** — formulario con selector múltiple de fotos (PhotoPicker, hasta 3), categoría (cargada desde Firestore), nombre, descripción, precio, stock, tiempo de elaboración, materiales y tamaños como chips, switch personalizable. En modo edición carga los datos existentes.
11. **Gestión de pedidos** — tabs Activos / Entregados, lista con cliente, timeline narrativo según estado y botón que avanza el pedido al siguiente estado en Firestore.
12. **Perfil del artista** — datos del vendedor, plan y comisión, reputación, seguidores, configuración, cerrar sesión.

### Administrador (5 pantallas)
13. **Dashboard administrativo** — KPIs reales desde Firestore (usuarios totales, ventas totales, pedidos activos, vendedores), gráfica de barras semanales, accesos rápidos, botón cerrar sesión.
14. **Gestión de artistas** — búsqueda, tabs Todos/Verificados/Pendientes/Suspendidos, cards con estadísticas reales (rating, ventas, ingresos generados), acciones Verificar/Suspender/Reactivar que actualizan `users.{uid}.artistStatus`.
15. **Gestión de compradores** — listado de usuarios con `role=ROLE_BUYER` ordenados por gasto descendente, tabs Todos/Activos/Bloqueados, acción Bloquear/Desbloquear.
16. **Gestión de categorías** — lista en vivo desde Firestore, toggle ON/OFF que persiste, botón "+ Nueva categoría" que abre un diálogo (nombre, emoji, color), botón eliminar, botón "Restaurar 5 por defecto" como escape hatch.
17. **Reportes** — filtro de período (Hoy/Semana/Mes/Año), total de ventas con variación porcentual vs período anterior, gráfica de líneas dinámica (franjas horarias / días / semanas / meses), Top 3 productos y Top 3 artistas calculados en vivo, botones Exportar PDF/CSV (preparados).

**Total: 17 pantallas funcionales conectadas a Firebase.**

---

## 9. Sistema de diseño

La app usa una identidad visual consistente en todas las pantallas, en modo oscuro por defecto (no hay modo claro en v1.0).

**Paleta de colores:**

| Uso | Color | Hex |
|---|---|---|
| Fondo principal | Negro grafito | `#0F0F0F` |
| Tarjetas y barras | Gris muy oscuro | `#1A1A1A` |
| Acento principal | Morado vibrante | `#7C4DFF` |
| Acento suave | Lavanda | `#B39DDB` |
| Texto principal | Blanco | `#FFFFFF` |
| Texto secundario | Gris claro | `#9E9E9E` |
| Bordes/divisores | Gris muy oscuro | `#2A2A2A` |
| Éxito | Verde | `#4CAF50` |
| Advertencia | Naranja | `#FF9800` |
| Error | Rojo | `#F44336` |

**Tipografía:** Roboto (default de Android).
- Títulos grandes 24sp Bold · Títulos de sección 18sp · Cuerpo 14sp · Texto secundario 12sp · Etiquetas 10–11sp.

**Espaciado y bordes:**
- Padding horizontal 16dp.
- Radio de bordes: tarjetas 12–14dp, botones 10dp, chips/pills 999dp, inputs 10dp.
- Altura de top bar 56dp, bottom nav 64dp, botones primarios 48–54dp.

**Componentes reutilizables creados:**
- `BuyerBottomNav` — barra inferior del comprador (Inicio · Explorar · Carrito · Perfil).
- `SellerBottomNav` — barra inferior del vendedor (Inicio · Productos · Pedidos · Perfil).
- `AdminBottomNav` — barra inferior del admin (Dashboard · Artistas · Compradores · Categorías · Reportes).

Los tres siguen el mismo patrón: ítem activo en morado con fondo translúcido, ítem inactivo en gris.

---

## 10. Navegación y enrutamiento

La navegación entre pantallas usa **Navigation Compose**. Cada pantalla tiene una ruta única definida en el archivo `Screen.kt`. Por ejemplo:

- `login` → pantalla de inicio de sesión.
- `buyer/home` → catálogo del comprador.
- `buyer/product/{productId}` → detalle de un producto específico.
- `seller/products` → lista de productos del vendedor.
- `admin/categories` → gestión de categorías.

Cuando el usuario toca un botón, la app llama a `navController.navigate("ruta")`. Si la ruta requiere parámetros (como el ID de un producto), se incluyen en la URL.

El archivo `AppNavigation.kt` actúa como un **mapa central**: cada ruta tiene un bloque que dice qué pantalla mostrar y qué datos pasarle.

**Decisiones de navegación:**
- Después del logout se navega al login con `popUpTo(0)` para limpiar todo el historial — el usuario no puede regresar al dashboard con el botón "atrás".
- La pantalla de biometría se salta automáticamente si el usuario ya tomó una decisión previa (guardada en SharedPreferences).

---

## 11. Persistencia local

Aparte de Firebase, la app guarda algunos datos pequeños directamente en el celular usando **SharedPreferences** (almacenamiento clave-valor de Android):

- **Decisión sobre biometría por usuario** — recuerda si el usuario activó la huella o dijo "ahora no", para no preguntar de nuevo en cada login.

Se eligió SharedPreferences (en lugar de bases de datos locales como Room) porque la cantidad de información es mínima y no requiere consultas complejas.

---

## 12. Seguridad

**Autenticación**
- Firebase Auth maneja contraseñas con hashing internamente. La app nunca recibe la contraseña en texto plano después del registro.
- Las sesiones se mantienen automáticamente con tokens; el SDK de Firebase se encarga de renovarlos.

**Reglas de Firestore**
La seguridad real de los datos no está en la app, sino en las **reglas de Firestore**. Sin reglas correctas, cualquiera podría leer o escribir desde fuera de la app. Las reglas mínimas configuradas son:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth != null;
    }
    match /products/{id} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    match /orders/{id} {
      allow read, write: if request.auth != null;
    }
    match /categories/{id} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

**Lectura pública** en productos y categorías (para que los compradores puedan navegar sin autenticarse, si en el futuro se quiere). **Escritura** solo si el usuario está autenticado.

**Datos biométricos**
Procesados localmente con `BiometricPrompt`. No se transmiten al servidor. La app solo guarda la *decisión* del usuario (activado / no activado), nunca la huella en sí.

**Pagos**
La app no almacena ningún dato de tarjetas. La integración con la pasarela (PSE / Wompi / MercadoPago) maneja todo lo financiero. CanvasVibe solo guarda los tokens que devuelve la pasarela.

**Comunicación**
Toda la comunicación entre la app y Firebase usa HTTPS / TLS por defecto.

---

## 13. Instalación y ejecución

### Requisitos
- Android Studio Hedgehog o superior.
- JDK 17 (incluido en Android Studio).
- Una cuenta de Firebase con un proyecto creado.
- El archivo `google-services.json` colocado en `app/` (ya incluido en el repositorio).

### Compilar y ejecutar en emulador
1. Abrir el proyecto en Android Studio.
2. Esperar a que Gradle sincronice (descarga librerías la primera vez).
3. Crear un emulador con Android 8.0+ desde Device Manager.
4. Pulsar el botón Run (▶️).

### Ejecutar en celular físico
1. Activar **Modo desarrollador** en el celular (tocar 7 veces "Número de compilación" en Ajustes → Acerca del teléfono).
2. Activar **Depuración USB** dentro de Opciones de desarrollador.
3. Conectar el celular con cable de datos.
4. Aceptar el popup que aparece en el celular ("Permitir depuración USB").
5. Verificar que aparece con `adb devices` desde la terminal.
6. Seleccionar el dispositivo en Android Studio y pulsar Run.

### Configuración de Firebase necesaria
- **Authentication** → habilitar "Email/Password".
- **Firestore Database** → crear en modo producción y aplicar las reglas mostradas arriba.
- **Storage** → habilitar (requiere plan Blaze).

### Crear un usuario administrador
Por seguridad, los administradores no se crean desde la app. El procedimiento es:

1. Registrarse normalmente desde la app con un correo (queda como `ROLE_BUYER`).
2. Ir a **Firebase Console → Firestore → users**, abrir el documento creado.
3. Cambiar el campo `role` de `ROLE_BUYER` a `ROLE_ADMIN`.
4. Cerrar sesión y volver a entrar.

---

## 14. Estado actual (mayo 2026)

**Implementado y funcional:**
- Sistema de autenticación con email/contraseña.
- Autenticación biométrica con persistencia de decisión.
- Tres roles diferenciados con dashboards independientes.
- Catálogo dinámico con categorías cargadas desde Firestore.
- CRUD completo de productos para vendedores (crear, editar, activar/inhabilitar, eliminar, ajustar stock).
- Gestión de pedidos con avance de estados.
- Panel administrativo completo: dashboard con KPIs reales, gestión de artistas, gestión de compradores, gestión de categorías con seed automático, reportes con cálculos en vivo.
- Sistema de diseño consistente en todas las pantallas.
- Manejo de errores con banners de feedback al usuario.
- Carga de imágenes a Firebase Storage con compresión vía PhotoPicker.

**Preparado pero no activo:**
- Notificaciones push (FCM configurado, faltan triggers).
- Exportación PDF / CSV de reportes (botones presentes, lógica pendiente).
- Pasarela de pago real (botón "Pagar ahora" presente, integración pendiente).

**No implementado (fuera de alcance v1.0):**
- Sistema de reseñas y calificaciones desde la app.
- Sistema de favoritos persistente.
- Recuperación de contraseña por correo.
- Verificación de correo al registrarse.
- Modo claro.
- Versión web para administradores.

---

## 15. Decisiones técnicas destacadas

**¿Por qué Jetpack Compose y no XML?**
Compose es la dirección oficial de Google desde 2021. Permite escribir interfaces más rápido, con menos código repetido y con mejor rendimiento. Cualquier desarrollador Android nuevo se forma ya en Compose.

**¿Por qué Firebase y no un backend propio?**
Para una v1.0 con un solo desarrollador, Firebase elimina meses de trabajo: autenticación, base de datos, almacenamiento de archivos y mensajería en una sola plataforma. La sincronización en tiempo real (cuando el admin cambia una categoría, el comprador la ve cambiar inmediatamente) es prácticamente gratis. La contraparte es un costo mensual una vez la app crezca, pero es predecible.

**¿Por qué guardar el rol en Firestore y no en el token de Firebase?**
Firebase soporta "custom claims" para guardar el rol en el token, pero requieren un servidor propio para asignarlos. Guardarlo en Firestore es más simple, accesible desde la app y suficiente para v1.0. Las **Firestore Security Rules** se encargan de que un comprador no pueda modificar productos aunque conozca la estructura.

**¿Por qué soft-delete en lugar de borrado real?**
Cuando un vendedor "inhabilita" un producto, no se borra: se cambia `isActive = false`. Esto preserva el histórico para los pedidos pasados (que aún apuntan a ese producto). El botón "Eliminar" sí hace borrado real, con confirmación.

**¿Por qué semilla automática de categorías?**
La primera vez que la app arranca, si la colección `categories` está vacía, se crean las 5 categorías base automáticamente. Esto evita que el primer admin tenga que configurarlas manualmente y que la app se vea vacía la primera vez que se instala.

---

## 16. Glosario rápido

| Término | Significado en este proyecto |
|---|---|
| Composable | Una función de Kotlin que dibuja parte de la pantalla (botón, lista, etc.). |
| StateFlow | Un canal por donde el ViewModel emite el estado actual. La pantalla "escucha" y se redibuja cuando cambia. |
| Repository | Clase que sabe cómo hablar con Firebase. Aísla la lógica de datos del resto de la app. |
| ViewModel | El cerebro de una pantalla. Guarda su estado y maneja sus acciones. |
| ROLE_BUYER / SELLER / ADMIN | Los tres tipos de usuario del sistema. |
| Soft-delete | Marcar como inactivo en lugar de borrar. |
| Seed | Datos iniciales que se cargan automáticamente la primera vez. |
| Top bar / Bottom nav | Barra superior con título / barra inferior con tabs. |

---

## 17. Historial del documento

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| 1.0 | Mayo 2026 | Andrés Felipe Fonseca Ochoa | Documentación técnica inicial complementaria al documento de requerimientos v1.0. |
