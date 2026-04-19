# CanvasVibe — Contexto del Proyecto para Claude Code

## ¿Qué es CanvasVibe?

Marketplace móvil Android de arte personalizado que conecta artistas colombianos
con compradores. Los productos son cuadros en vinilo, resina epóxica, pintura al
óleo e ilustraciones digitales (temáticas: videojuegos, anime, naturaleza, mascotas).
Precios en pesos colombianos (COP). Mercado objetivo: Colombia, con potencial regional.

---

## Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM (ViewModel + StateFlow) |
| Navegación | Jetpack Navigation Compose (pendiente de agregar) |
| Backend | Firebase (Auth, Firestore, Storage) |
| Build | Gradle con Version Catalog (libs.versions.toml) |
| AGP | 9.1.1 |
| Kotlin | 2.2.10 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 |

### Dependencias ya configuradas (libs.versions.toml)
- Firebase BoM 34.12.0 (gestiona versiones de todas las libs Firebase)
- firebase-auth-ktx, firebase-firestore-ktx, firebase-storage-ktx,
  firebase-messaging-ktx, firebase-analytics-ktx
- kotlinx-coroutines-play-services 1.10.2 (para .await() en Tasks de Firebase)
- Compose BOM 2026.02.01
- lifecycle-runtime-ktx, activity-compose, core-ktx

### Dependencia pendiente de agregar
```toml
# En libs.versions.toml
[versions]
navigationCompose = "2.8.9"

[libraries]
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
```
```kotlin
// En app/build.gradle.kts
implementation(libs.androidx.navigation.compose)
```

---

## Roles del Sistema

| Rol | Constante | Descripción |
|-----|-----------|-------------|
| Comprador | `ROLE_BUYER` | Navega catálogo, compra, rastrea pedidos |
| Vendedor/Artista | `ROLE_SELLER` | Publica obras, gestiona pedidos, ve estadísticas |
| Administrador | `ROLE_ADMIN` | Gestiona usuarios, categorías, reportes globales |

El rol se almacena en Firestore en la colección `users/{uid}.role` y en el
modelo `User.kt`. Después del login, la app debe enrutar al dashboard correspondiente.

---

## Estado Actual del Código

### Archivos ya creados y funcionales

#### `data/model/User.kt`
```kotlin
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "ROLE_BUYER",
    val createdAt: Long = System.currentTimeMillis()
)
```

#### `data/repository/AuthRepository.kt`
- `register(email, password, name, role)` → crea usuario en Firebase Auth +
  guarda en Firestore `users/{uid}`
- `login(email, password)` → autentica y recupera User desde Firestore
- `logout()` / `currentUser()`

#### `ui/auth/LoginViewModel.kt`
- `AuthState`: Idle | Loading | Success(user) | Error(message)
- `login(email, password)`, `register(email, password, name)`, `resetState()`

#### `ui/auth/LoginScreen.kt`
- Pantalla combinada Login + Register (toggle `isRegisterMode`)
- Funcional pero con diseño básico (sin el sistema de diseño final)
- `onLoginSuccess(user)` callback para navegar al dashboard

#### `MainActivity.kt`
- Actualmente solo lanza `LoginScreen` sin navegación real
- **Pendiente:** implementar NavHost con rutas por rol

#### `ui/theme/` (Color.kt, Theme.kt, Type.kt)
- Generados por Android Studio, **pendiente** adaptar al sistema de diseño de CanvasVibe

---

## Sistema de Diseño

### Paleta de colores (Dark Mode por defecto)
```kotlin
// Agregar en Color.kt
val Background     = Color(0xFF0F0F0F)  // Fondo principal
val SurfaceCard    = Color(0xFF1A1A1A)  // Fondo tarjetas y top/bottom bar
val AccentPrimary  = Color(0xFF7C4DFF)  // Morado vibrante — acento principal
val AccentSoft     = Color(0xFFB39DDB)  // Morado suave — acento secundario
val TextPrimary    = Color(0xFFFFFFFF)  // Texto principal
val TextSecondary  = Color(0xFF9E9E9E)  // Texto secundario / placeholders
val Divider        = Color(0xFF2A2A2A)  // Bordes y divisores
val Success        = Color(0xFF4CAF50)
val Warning        = Color(0xFFFF9800)
val Info           = Color(0xFF2196F3)
val Error          = Color(0xFFF44336)
```

### Tipografía (Roboto)
| Uso | Tamaño | Peso |
|-----|--------|------|
| Títulos grandes | 24sp | Bold |
| Títulos de sección | 18sp | Bold |
| Cuerpo principal | 14sp | Regular |
| Texto secundario | 12sp | Regular |
| Etiquetas pequeñas | 10sp | Regular |

### Espaciado y bordes
| Propiedad | Valor |
|-----------|-------|
| Margen horizontal global | 16dp |
| Espaciado entre cards | 12dp |
| Border radius cards | 12dp |
| Border radius botones | 8dp |
| Border radius chips | 20dp |
| Elevación cards | 4dp |
| Altura Top App Bar | 56dp |
| Altura Bottom Nav | 64dp |

### Componentes clave
- **Botón primario:** fondo `#7C4DFF`, texto blanco, 14sp Bold, 48dp alto, full-width
- **Botón outline:** borde 2dp `#7C4DFF`, texto `#7C4DFF`, fondo transparente
- **Card:** fondo `#1A1A1A`, radius 12dp, padding 12dp, sin borde visible
- **Chip/categoría:** fondo del color de la categoría, texto blanco, 11sp, radius 20dp
- **Bottom nav activo:** ícono y label `#7C4DFF`, fondo ovalado `#7C4DFF20`
- **Bottom nav inactivo:** ícono y label `#9E9E9E`

---

## Estructura de Firestore

### Colección `users`
```
users/{uid}
  uid: String
  email: String
  name: String
  role: String          // "ROLE_BUYER" | "ROLE_SELLER" | "ROLE_ADMIN"
  createdAt: Long
```

### Colección `products`
```
products/{productId}
  id: String
  sellerId: String      // uid del artista
  sellerName: String
  title: String
  description: String
  category: String      // "gamer" | "paisajes" | "animales" | "anime" | "abstracto"
  materials: List<String>   // ["vinilo", "resina", "oleo", "digital"]
  sizes: List<String>       // ["30x40", "50x70", "60x90"]
  priceBase: Long       // precio en COP (ej: 185000)
  imageUrls: List<String>   // URLs de Firebase Storage
  rating: Double
  reviewCount: Int
  stock: Int
  elaborationDays: String   // "7-10 días"
  isCustomizable: Boolean
  isActive: Boolean
  createdAt: Long
```

### Colección `orders`
```
orders/{orderId}
  id: String
  buyerId: String
  sellerId: String
  productId: String
  productTitle: String
  productImageUrl: String
  material: String
  size: String
  quantity: Int
  unitPrice: Long
  totalPrice: Long
  status: String   // "PENDING"|"PREPARING"|"SHIPPED"|"DELIVERED"|"CANCELLED"
  createdAt: Long
  updatedAt: Long
```

### Colección `cart` (subcolección del usuario)
```
users/{uid}/cart/{productId}
  productId: String
  title: String
  sellerName: String
  material: String
  size: String
  quantity: Int
  unitPrice: Long
  imageUrl: String
```

### Firebase Storage
```
/products/{sellerId}/{productId}/imagen_1.jpg
/profiles/{uid}/avatar.jpg
```

---

## Pantallas a Implementar — 14 en total

### Navegación pendiente — rutas sugeridas
```kotlin
sealed class Screen(val route: String) {
    object Login           : Screen("login")
    // Comprador
    object BuyerHome       : Screen("buyer/home")
    object ProductDetail   : Screen("buyer/product/{productId}")
    object Cart            : Screen("buyer/cart")
    object OrderTracking   : Screen("buyer/order/{orderId}")
    // Vendedor
    object SellerDashboard : Screen("seller/dashboard")
    object AddProduct      : Screen("seller/add-product")
    object SellerOrders    : Screen("seller/orders")
    object SellerProfile   : Screen("seller/profile")
    // Admin
    object AdminDashboard  : Screen("admin/dashboard")
    object AdminArtists    : Screen("admin/artists")
    object AdminCategories : Screen("admin/categories")
    object AdminReports    : Screen("admin/reports")
}
```

Después del login, enrutar según `user.role`:
- `ROLE_BUYER` → `Screen.BuyerHome`
- `ROLE_SELLER` → `Screen.SellerDashboard`
- `ROLE_ADMIN` → `Screen.AdminDashboard`

---

### Pantallas Compartidas

#### shared_01 — Login / Register (YA EXISTE — refinar diseño)
- Logo CanvasVibe centrado + subtítulo "Marketplace de Arte"
- Campos: correo, contraseña (con toggle ver/ocultar)
- Botón primario: "INGRESAR"
- Divisor "o continúa con" + botón Google (outline)
- Link "¿No tienes cuenta? Regístrate aquí"
- Modo registro: agrega campo nombre + selector de rol

#### shared_02 — Autenticación Biométrica
- Avatar circular del usuario
- "Hola, [Nombre]" 20sp Bold
- Ícono huella 80dp en morado con pulso animado
- Usar `BiometricPrompt` API (datos NO salen del dispositivo)

---

### Rol Comprador — Bottom Nav: [Inicio] [Buscar] [Favoritos] [Pedidos] [Perfil]

#### comprador_01 — Inicio / Catálogo
- Barra búsqueda redondeada
- Chips categorías (scroll horizontal): 🎮 Gamer, 🌿 Paisajes, 🐾 Animales, 🎌 Anime, 🖥 Abstracto
- Card destacado (full-width horizontal)
- Grid 2 columnas "Nuevos Arrivals"

#### comprador_02 — Detalle del Producto
- Galería 16:9 + tira de miniaturas
- Nombre, artista, rating (⭐ amarillo), precio en COP
- Selector de Material (chips seleccionables)
- Selector de Tamaño (chips seleccionables)
- Descripción
- Botones: "♡ Favoritos" (outline) + "Agregar al carrito" (primario)

#### comprador_03 — Carrito de Compras
- Lista items: imagen, nombre, artista, precio, control cantidad [– n +]
- Card resumen: subtotal, envío, descuento, TOTAL en morado
- Botón "Proceder al pago →"

#### comprador_04 — Seguimiento de Pedido
- Chip estado del pedido
- Mapa estilizado placeholder
- Timeline vertical 5 pasos con íconos ✓/→/○

---

### Rol Vendedor — Bottom Nav: [Dashboard] [Pedidos] [Productos] [Estadísticas] [Perfil]

#### vendedor_01 — Dashboard
- Saludo + resumen del día
- Grid KPIs 2×2: ventas mes, pedidos activos, productos, calificación
- Lista 3 pedidos recientes con chips de estado

#### vendedor_02 — Publicar Producto
- Zona carga fotos (borde punteado morado)
- Formulario: nombre, descripción, material (dropdown), tamaños, precio COP,
  tiempo elaboración, stock, toggle "Personalizable"
- Botón "PUBLICAR PRODUCTO"

#### vendedor_03 — Gestión de Pedidos
- Tabs filtro: Todos / Pendiente / Preparando / Enviado / Entregado
- Cards por pedido con botón "Actualizar estado →"

#### vendedor_04 — Perfil del Artista
- Header: avatar circular con borde morado, nombre artístico, especialidad
- Stats: reseñas, calificación, obras, ventas
- Bio (texto)
- Galería 3 columnas (miniaturas)
- Lista configuración con íconos y flechas

---

### Rol Administrador — Bottom Nav: [Dashboard] [Artistas] [Compradores] [Categorías] [Reportes]

#### admin_01 — Dashboard
- KPIs globales: usuarios totales, ventas totales, pedidos activos, artistas verificados
- Gráfica barras (ventas por día de la semana)
- Accesos rápidos 2×2: Gestionar Artistas, Ver Pedidos, Categorías, Reportes

#### admin_02 — Gestión de Artistas
- Búsqueda + tabs: Todos / Verificados / Pendientes / Suspendidos
- Cards artista: avatar, nombre, métricas, chip estado, botones [Ver detalle] [Verificar/Suspender]

#### admin_03 — Gestión de Categorías
- Lista reordenable con toggle activado/desactivado por categoría
- Categorías: 🎮 Gamer (morado), 🌿 Paisajes (verde), 🐾 Animales (naranja),
  🎌 Anime (rojo), 🖥 Abstracto (azul)
- FAB (+) en esquina inferior derecha

#### admin_04 — Reportes
- Filtro período: Hoy / Semana / Mes / Año
- Card ventas totales + variación %
- Gráfica de línea (ventas por día)
- Top 3 productos más vendidos
- Top 3 artistas
- Botones exportar PDF y CSV

---

## Estructura de Carpetas Recomendada

```
app/src/main/java/com/canvasvibe/app/
├── MainActivity.kt               ← punto de entrada, NavHost aquí
├── navigation/
│   └── AppNavigation.kt          ← NavHost + rutas + lógica de rol
├── data/
│   ├── model/
│   │   ├── User.kt               ← YA EXISTE
│   │   ├── Product.kt            ← PENDIENTE
│   │   ├── Order.kt              ← PENDIENTE
│   │   └── CartItem.kt           ← PENDIENTE
│   └── repository/
│       ├── AuthRepository.kt     ← YA EXISTE
│       ├── ProductRepository.kt  ← PENDIENTE
│       ├── OrderRepository.kt    ← PENDIENTE
│       └── CartRepository.kt     ← PENDIENTE
└── ui/
    ├── theme/
    │   ├── Color.kt              ← ACTUALIZAR con paleta CanvasVibe
    │   ├── Theme.kt              ← ACTUALIZAR con dark mode
    │   └── Type.kt               ← ACTUALIZAR con Roboto
    ├── components/               ← PENDIENTE (componentes reutilizables)
    │   ├── ProductCard.kt
    │   ├── CategoryChip.kt
    │   ├── PrimaryButton.kt
    │   └── OrderStatusChip.kt
    ├── auth/
    │   ├── LoginScreen.kt        ← YA EXISTE (refinar diseño)
    │   └── LoginViewModel.kt     ← YA EXISTE
    ├── buyer/
    │   ├── home/
    │   ├── detail/
    │   ├── cart/
    │   └── tracking/
    ├── seller/
    │   ├── dashboard/
    │   ├── addproduct/
    │   ├── orders/
    │   └── profile/
    └── admin/
        ├── dashboard/
        ├── artists/
        ├── categories/
        └── reports/
```

---

## Reglas de Negocio Importantes

1. **Retracto de compra:** el comprador puede cancelar dentro de 5 días hábiles
   si el producto NO fue personalizado exclusivamente para él.
2. **Datos biométricos:** procesados solo localmente con `BiometricPrompt`. Nunca
   se transmiten al servidor.
3. **Pagos:** usar pasarela homologada (PSE / Wompi / MercadoPago Colombia).
   CanvasVibe NUNCA almacena datos de tarjeta — solo tokens del proveedor.
4. **Roles en registro:** por defecto `ROLE_BUYER`. El rol `ROLE_SELLER` requiere
   verificación por admin. El rol `ROLE_ADMIN` solo se asigna manualmente en Firestore.
5. **Precios:** siempre en COP con formato `$185.000 COP` (punto como separador de miles).
6. **Idioma:** toda la interfaz en español colombiano.

---

## Configuración Firebase (ya completada)

- `google-services.json` en `app/` ✅
- package name: `com.canvasvibe.app` ✅
- Firebase Auth (email/password) habilitado ✅
- Firestore habilitado ✅
- Firebase Storage habilitado (plan Blaze) ✅
- Plugin `com.google.gms.google-services` configurado ✅

---

## Orden de Implementación Sugerido

1. **Actualizar tema** (`Color.kt`, `Theme.kt`) con paleta CanvasVibe oscura
2. **Agregar dependencia** Navigation Compose en `libs.versions.toml` y `build.gradle.kts`
3. **Crear `AppNavigation.kt`** con NavHost y ruteo por rol
4. **Actualizar `MainActivity.kt`** para usar el NavHost
5. **Crear modelos** `Product.kt`, `Order.kt`, `CartItem.kt`
6. **Crear componentes reutilizables** en `ui/components/`
7. **Implementar pantallas del Comprador** (4 pantallas — flujo principal del negocio)
8. **Implementar pantallas del Vendedor** (4 pantallas)
9. **Implementar pantallas del Admin** (4 pantallas)
10. **Crear repositorios** `ProductRepository`, `OrderRepository`, `CartRepository`

---

*Documento generado el 16 de abril de 2026 | CanvasVibe v1.0*
