# CanvasVibe

Marketplace móvil de arte personalizado para conectar artistas colombianos con compradores.

Aplicación Android nativa hecha en Kotlin con Jetpack Compose. Incluye autenticación con biometría, gestión de productos con fotos, carrito de compras, geolocalización para entregas y pagos con ePayco.

---

## ¿Qué hace la app?

CanvasVibe es un proyecto académico que simula un marketplace tipo Etsy pero pensado para arte hecho a mano en Colombia (vinilo, resina epóxica, óleo, ilustración digital). Tiene tres roles bien diferenciados y cada uno tiene su propio panel.

### Para compradores

Buscar obras por categoría, ver detalles, marcar favoritos, agregar al carrito y pagar con tarjeta o PSE a través de ePayco. La app también captura la ubicación GPS para autocompletar la dirección de envío y le hace seguimiento al pedido hasta que llega.

### Para vendedores (artistas)

Publicar obras con fotos tomadas desde la cámara o elegidas de la galería, gestionar inventario (precio, stock, materiales, tamaños), recibir pedidos y actualizar su estado en tiempo real.

### Para administradores

Verificar artistas nuevos, bloquear cuentas problemáticas, editar usuarios, gestionar categorías del catálogo y ver reportes con estadísticas reales de ventas.

---

## Stack tecnológico

| Capa | Herramienta |
|---|---|
| Lenguaje | Kotlin 2.3 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM (ViewModel + StateFlow) |
| Navegación | Jetpack Navigation Compose |
| Backend | Firebase (Auth, Firestore, Storage) |
| Pagos | ePayco con Custom Tabs |
| Ubicación | FusedLocationProvider + Geocoder |
| Imágenes | Coil para carga remota, FileProvider para cámara |
| Build | Gradle con Version Catalog |

---

## Cómo ejecutar el proyecto

### Lo que necesitas

- Android Studio Ladybug o más reciente
- JDK 17 (viene con Android Studio)
- Android SDK 36 (Android 16)
- Un dispositivo físico o emulador con Google Play Services

### Pasos

**1. Clona el repositorio**

```bash
git clone https://github.com/AndresFonseca13/canvasvibe.git
cd canvasvibe
```

**2. Crea los archivos de configuración**

Hay dos archivos que no están en el repositorio por seguridad y tienes que crearlos tú.

**`app/google-services.json`**

Descárgalo desde la consola de Firebase del proyecto `canvasvibe-6fcdd`, pestaña Configuración del proyecto, sección "Tus apps", botón "Descargar google-services.json".

**`local.properties`**

En la raíz del proyecto, crea este archivo con tres líneas:

```properties
sdk.dir=/ruta/a/tu/Android/Sdk
EPAYCO_PUBLIC_KEY=tu_public_key_de_pruebas
EPAYCO_CUST_ID=tu_cust_id_de_pruebas
```

Las llaves de ePayco las obtienes en el dashboard de ePayco, sección Integraciones, opción Llaves API, modo Pruebas.

**3. Abre el proyecto en Android Studio**

File → Open → selecciona la carpeta clonada. Espera a que Gradle sincronice (la primera vez puede tardar varios minutos descargando dependencias).

**4. Compila e instala**

Conecta un celular Android con depuración USB activada (o inicia un emulador con Google Play) y haz click en Run. La primera vez te va a pedir aceptar la depuración en el celular.

---

## Estructura del proyecto

```
canvasvibe/
├── app/
│   └── src/main/
│       ├── java/com/canvasvibe/app/
│       │   ├── data/
│       │   │   ├── model/         # User, Product, Order, CartItem, Category
│       │   │   ├── prefs/         # SharedPreferences para biometría
│       │   │   └── repository/    # Capa de acceso a Firebase
│       │   ├── navigation/        # NavHost y rutas
│       │   ├── payments/          # Configuración e integración de ePayco
│       │   ├── ui/
│       │   │   ├── admin/         # Pantallas del administrador
│       │   │   ├── auth/          # Login, registro, biometría
│       │   │   ├── buyer/         # Pantallas del comprador
│       │   │   ├── seller/        # Pantallas del vendedor
│       │   │   ├── components/    # Composables compartidos
│       │   │   └── theme/         # Colores, tipografía, tema
│       │   ├── util/              # Helpers de cámara y ubicación
│       │   └── MainActivity.kt
│       ├── res/                   # Recursos XML (íconos, strings, file_paths)
│       └── AndroidManifest.xml
├── web/
│   ├── checkout.html              # Bootstrap de ePayco hosteado en GitHub Pages
│   └── return.html                # Página de retorno que dispara el deep link
├── gradle/
│   └── libs.versions.toml         # Versiones centralizadas de dependencias
└── build.gradle.kts
```

---

## Funcionalidades implementadas

- Registro e inicio de sesión con email y contraseña
- Recuperación de contraseña vía email
- Autenticación con huella dactilar (BiometricPrompt)
- Persistencia de la decisión biométrica por usuario
- CRUD completo de productos con subida de imágenes a Firebase Storage
- Captura de fotos desde cámara con FileProvider o desde galería
- Carrito de compras con cantidades editables
- Captura de ubicación GPS y geocoding inverso para autocompletar dirección
- Integración con ePayco vía Custom Tabs y retorno con custom scheme
- Persistencia de referencia de transacción y estado del pago en Firestore
- Seguimiento de pedidos con timeline de estados
- Panel administrativo con CRUD de usuarios y categorías
- Reportes con datos reales calculados desde Firestore
- Navegación por rol después del login

---

## Pruebas de pago con ePayco

El proyecto está configurado en modo de pruebas de ePayco. Para validar el flujo de compra usa estas tarjetas:

| Marca | Número | Vence | CVV | Resultado |
|---|---|---|---|---|
| VISA | 4575 6231 8229 0326 | 12/25 | 123 | Aprobada |
| MASTERCARD | 5170 3944 9037 9427 | 12/25 | 123 | Rechazada |

El modo de pruebas tiene un tope aproximado de un millón de pesos colombianos por transacción.

---

## Notas sobre la entrega

- El archivo `google-services.json` no se versiona por seguridad. Si necesitas las credenciales del proyecto, contacta al autor
- Las llaves de ePayco se manejan vía `local.properties` y se inyectan al `BuildConfig` durante la compilación
- La página intermedia de pago (`web/checkout.html`) se sirve a través de GitHub Pages porque ePayco solo acepta URLs públicas como `response URL`
- El rol de administrador no se puede crear desde la app por seguridad. Para asignarlo hay que cambiar el campo `role` del usuario manualmente en Firestore a `ROLE_ADMIN`

---

## Limitaciones conocidas

- El mapa de seguimiento del pedido es decorativo (no es un mapa real de Google Maps)
- Los botones de exportar reportes a PDF y CSV están preparados visualmente pero la generación de archivos queda como trabajo futuro
- La sección de "Direcciones guardadas" en el perfil del comprador no tiene pantalla todavía
- La confirmación de pago llega como `ref_payco` solamente. La validación rigurosa contra el API de ePayco requiere credenciales server-side fuera del alcance de esta entrega

---

## Autor

**Andrés Felipe Fonseca Ochoa**
Fundación Universitaria Compensar
Proyecto de Desarrollo Móvil Nativo

---

## Licencia

Proyecto académico sin fines comerciales. Las marcas mencionadas (ePayco, Firebase, Google) son propiedad de sus respectivos dueños.
