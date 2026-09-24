package es.tendencias.app.model

/**
 * Número de tendencias a mostrar en el widget.
 */
enum class TrendCountOption(val value: Int?, val label: String) {
    ONE(1, "1"),
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    AUTO(null, "Automático según el espacio")
}

/**
 * Formato del texto del título de la tendencia.
 */
enum class TextFormatOption(val label: String, val description: String) {
    FULL("Completo", "Muestra el título original de la tendencia"),
    SHORT_PHRASE("Frase corta", "Adapta o recorta el texto para que sea más compacto"),
    ONE_WORD("Una palabra", "Muestra únicamente la primera palabra relevante"),
    TWO_WORDS("Dos palabras", "Muestra únicamente las dos primeras palabras"),
    ONE_LINE("Una línea", "Limita la tendencia a una única línea con elipsis")
}

/**
 * Manejo de imágenes (únicamente imágenes válidas provistas por Google Trends RSS).
 */
enum class ImageOption(val label: String, val description: String) {
    NONE("Sin imágenes", "No muestra imágenes, aprovechando el espacio para texto"),
    VALID_ONLY("Solo imagen válida", "Muestra la imagen únicamente si la fuente la proporciona"),
    IMAGE_AND_TEXT("Imagen + texto", "Prioriza miniatura junto al texto si existe imagen")
}

/**
 * Densidad de espaciado del widget.
 */
enum class WidgetDensity(val label: String, val paddingDp: Int) {
    COMPACT("Compacto", 6),
    NORMAL("Normal", 10),
    SPACIOUS("Amplio", 14),
    AUTO("Automático", 8)
}

/**
 * Intervalo de actualización periódica.
 */
enum class UpdateInterval(val label: String, val minutes: Long) {
    MANUAL("Manual", 0L),
    MIN_15("15 minutos (según disponibilidad del SO)", 15L),
    MIN_30("30 minutos (recomendado en Android)", 30L),
    HOUR_1("1 hora", 60L),
    HOUR_2("2 horas", 120L)
}

/**
 * Acción ejecutada al tocar una tendencia dentro del widget.
 */
enum class WidgetClickAction(val label: String) {
    OPEN_GOOGLE_TRENDS("Abrir tendencia en Google Trends"),
    OPEN_ASSOCIATED_NEWS("Abrir noticia asociada si existe"),
    SEARCH_WEB("Buscar tendencia en Google"),
    NONE("No hacer nada")
}

/**
 * Estilo de fondo del widget.
 */
enum class WidgetBackgroundStyle(val label: String) {
    TRANSPARENT("Transparente"),
    LIGHT("Claro"),
    DARK("Oscuro"),
    SYSTEM("Adaptado al sistema")
}

/**
 * Configuración completa y persistente del widget de tendencias.
 */
data class WidgetConfig(
    val trendCount: TrendCountOption = TrendCountOption.AUTO,
    val textFormat: TextFormatOption = TextFormatOption.FULL,
    val imageOption: ImageOption = ImageOption.VALID_ONLY,
    val density: WidgetDensity = WidgetDensity.AUTO,
    val updateInterval: UpdateInterval = UpdateInterval.MIN_30,
    val showRanking: Boolean = true,
    val showVolume: Boolean = true,
    val showCategory: Boolean = true,
    val showLastUpdated: Boolean = true,
    val showManualRefresh: Boolean = true,
    val clickAction: WidgetClickAction = WidgetClickAction.SEARCH_WEB,
    val backgroundStyle: WidgetBackgroundStyle = WidgetBackgroundStyle.SYSTEM,
    val autoAdaptSize: Boolean = true
)
