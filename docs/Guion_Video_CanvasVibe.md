# Guion del video — CanvasVibe (5 minutos)

**Tema:** Desarrollo de interfaces — explicación técnica de layouts, drawables, estilos y values.
**Autor:** Andrés Felipe Fonseca Ochoa
**Universidad:** Fundación Universitaria Compensar
**Duración objetivo:** 5 minutos

> Indicaciones: el guion está dividido en bloques con tiempo. Donde dice **[mostrar archivo X]**, abre ese archivo en pantalla. Las **negritas** son énfasis. Los `...` son pausas breves.

---

## INTRODUCCIÓN — 0:00 a 0:25

Hola, mi nombre es **Andrés Felipe Fonseca Ochoa**. En este video voy a explicar **cómo está construida la interfaz** de **CanvasVibe**, una aplicación móvil Android para vender arte personalizado.

Voy a recorrer cómo organicé los **layouts**, los **drawables**, los **estilos** y los archivos **values**, mostrando código real del proyecto.

---

## DEL XML CLÁSICO A JETPACK COMPOSE — 0:25 a 1:00

Antes de entrar en detalles, una aclaración importante: en Android moderno, los layouts ya **no se construyen con archivos XML** como antes. Se usa **Jetpack Compose**, que es la forma oficial recomendada por Google desde 2021.

En el proyecto sí quedan archivos XML, pero **solo los esenciales**: el `AndroidManifest.xml`, el archivo `themes.xml`, los `colors.xml` y `strings.xml` dentro de `values`, y los `drawable` del ícono de la app.

Las pantallas en sí se escriben en **Kotlin con funciones llamadas Composables**. En lugar de tener un `activity_main.xml`, tengo funciones como `BuyerHomeScreen` o `CartScreen` que describen directamente lo que se debe dibujar.

---

## LAYOUTS EN COMPOSE — 1:00 a 1:50

**[mostrar BuyerHomeScreen.kt]**

Aquí está el equivalente moderno de los layouts. Donde antes usaba `LinearLayout` ahora uso **Column** —apila elementos verticalmente— o **Row** —los pone en horizontal.

Por ejemplo, en este archivo, la pantalla principal del comprador es una **Column** que contiene la barra superior, el contenido scrollable, y la barra inferior de navegación. Cada hijo se ubica en orden, igual que un `LinearLayout` con `orientation="vertical"`.

El equivalente de `FrameLayout` es **Box**, que apila elementos uno encima del otro. Lo uso por ejemplo en la imagen del producto, donde sobre un fondo con gradiente coloco varios círculos decorativos.

Para listas largas, en lugar de `RecyclerView` con su Adapter y ViewHolder, uso **LazyColumn**. **[mostrar SellerProductsScreen.kt]** Esta pantalla del vendedor lista todos sus productos: cada `item` se renderiza solo cuando entra en pantalla, lo que mantiene el rendimiento aunque haya cientos de productos.

Los **Modifiers** son lo que en XML serían atributos como `padding`, `margin`, `background` o `width`. Aquí los encadeno con punto: `.fillMaxWidth().padding(16.dp).background(SurfaceDark)`.

---

## DRAWABLES — 1:50 a 2:30

**[mostrar carpeta res/drawable]**

Los drawables que sí existen como XML en el proyecto son los del **ícono del launcher**: `ic_launcher_foreground.xml` y `ic_launcher_background.xml`. Son **vectores** — gráficos definidos con paths matemáticos que escalan sin perder calidad en cualquier resolución.

**[mostrar ic_launcher_foreground.xml]**

Pueden ver aquí el `<vector>` con sus `<path>` y un gradiente lineal definido en XML. Esto es lo que aparece en el cajón de aplicaciones del celular.

Para los **íconos dentro de la app** —corazones, lupa, carrito, etc.— uso la librería **Material Icons** de Compose. En lugar de meter un drawable XML por cada ícono, simplemente escribo `Icons.Filled.Favorite` o `Icons.Filled.ShoppingCart` y Compose se encarga del resto.

**[mostrar ProductDetailScreen.kt - HeroImage]**

Y para fondos con efectos visuales, como el gradiente morado de la imagen del producto, no uso un drawable XML sino un **Brush** dibujado en código: `Brush.linearGradient(0f to Color(0xFF292929), 0.5f to Primary, 1f to SurfaceDark)`. Es más flexible porque puedo cambiar colores y stops según el estado de la pantalla.

---

## VALUES: COLORS, STRINGS, THEMES — 2:30 a 3:25

**[mostrar res/values/strings.xml]**

En la carpeta `values` tengo los archivos clásicos. El `strings.xml` define el nombre de la app que se ve en el launcher: `CanvasVibe`.

**[mostrar res/values/themes.xml]**

El `themes.xml` define el tema base de la `Activity` con `Theme.Material.Light.NoActionBar`. Esto es lo que Android lee antes de que Compose tome el control.

**[mostrar res/values/colors.xml]**

El `colors.xml` mantiene los colores históricos, pero la **paleta real de la app** la defino en Kotlin para poder usarla directamente desde los Composables.

**[mostrar ui/theme/Color.kt]**

Aquí declaro las variables: `Background`, `SurfaceDark`, `Primary` —el morado vibrante—, `PrimaryAccent`, `TextPrimary`, `TextSecondary`, `BorderSubtle` y `ErrorRed`. Todas son `Color` de Compose con valores hexadecimales.

**[mostrar ui/theme/Theme.kt]**

Y el `Theme.kt` es el equivalente moderno de un `style` XML: aquí construyo un `darkColorScheme` de **Material 3** y lo envuelvo en una función `CanvasVibeTheme` que se aplica a toda la app. Cualquier Composable puede leer estos colores con `MaterialTheme.colorScheme.primary`.

---

## ESTILOS Y COMPONENTES REUTILIZABLES — 3:25 a 4:15

En XML clásico se usan archivos `<style>` para no repetir atributos. En Compose el equivalente son **Composables reutilizables**.

**[mostrar BuyerBottomNav.kt]**

Por ejemplo, este archivo define la **barra inferior del comprador**: con cuatro tabs —Inicio, Explorar, Carrito, Perfil—. Recibe el índice seleccionado y un callback. Está usada en cinco pantallas distintas sin repetir código.

Hice lo mismo con **SellerBottomNav** y **AdminBottomNav** —cada rol tiene su propia barra— y con tarjetas, chips, badges y botones. **[mostrar SellerBottomNav.kt brevemente]**

Para los estilos visuales más finos —radio de bordes, padding, alturas— sigo un **sistema consistente**: las tarjetas tienen 14dp de radio, los botones primarios miden 48 a 54dp de alto, los chips son ovalados con radio de 999, y el padding horizontal global es de 16dp. Todo esto está documentado en mi sistema de diseño y aplicado uniformemente.

---

## MANIFEST Y CONFIGURACIÓN — 4:15 a 4:40

**[mostrar AndroidManifest.xml]**

El **AndroidManifest** declara los permisos que necesita la app —`USE_BIOMETRIC` para huella y rostro, e `INTERNET` para conectarse a Firebase—, registra la `MainActivity` como punto de entrada, y aplica el tema `Theme.CanvasVibe`.

**[mostrar MainActivity.kt]**

En la `MainActivity` activo `enableEdgeToEdge` para que la app aproveche toda la pantalla, envuelvo el contenido en `safeDrawingPadding` para respetar la barra de estado y el notch del celular, y dentro pongo el árbol de Composables que define toda la interfaz.

---

## CIERRE — 4:40 a 5:00

En resumen: la interfaz de **CanvasVibe** combina **archivos XML mínimos** —manifest, ícono y values— con **17 pantallas escritas completamente en Jetpack Compose**. El resultado es **menos código repetido**, un sistema de diseño coherente y una experiencia consistente en los tres roles del marketplace.

Eso es todo. **Gracias por su atención.**
