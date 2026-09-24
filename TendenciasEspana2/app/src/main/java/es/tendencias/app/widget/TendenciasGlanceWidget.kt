package es.tendencias.app.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import es.tendencias.app.MainActivity
import es.tendencias.app.data.TrendsRepositoryImpl
import es.tendencias.app.data.WidgetPreferencesRepositoryImpl
import es.tendencias.app.model.*
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Widget nativo de Jetpack Glance completamente configurable y redimensionable (2x2, 2x4, 4x2, 4x4).
 * Conectado en exclusiva con el feed oficial de Google Trends España (geo=ES).
 */
class TendenciasGlanceWidget : GlanceAppWidget() {

    companion object {
        private val SIZE_2X2 = DpSize(120.dp, 120.dp)
        private val SIZE_4X2 = DpSize(260.dp, 120.dp)
        private val SIZE_2X4 = DpSize(120.dp, 260.dp)
        private val SIZE_4X4 = DpSize(260.dp, 260.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SIZE_2X2, SIZE_4X2, SIZE_2X4, SIZE_4X4)
    )

    private val repository = TrendsRepositoryImpl()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefsRepo = WidgetPreferencesRepositoryImpl(context)
        val config = prefsRepo.getWidgetConfig()

        // Obtiene las tendencias reales directamente del feed oficial
        val allTrends = repository.getTrendsStream().firstOrNull() ?: emptyList()

        provideContent {
            GlanceTheme {
                WidgetRoot(
                    trends = allTrends,
                    config = config,
                    context = context
                )
            }
        }
    }

    @Composable
    private fun WidgetRoot(
        trends: List<Trend>,
        config: WidgetConfig,
        context: Context
    ) {
        val size = LocalSize.current
        val isCompactWidth = size.width < 180.dp
        val isCompactHeight = size.height < 180.dp

        // Determina el número de tendencias según configuración o adaptación inteligente
        val maxItems = when {
            config.autoAdaptSize || config.trendCount.value == null -> when {
                isCompactWidth && isCompactHeight -> 2 // 2x2: 2 tendencias
                !isCompactWidth && isCompactHeight -> 3 // 4x2: 3 tendencias
                isCompactWidth && !isCompactHeight -> 5 // 2x4: 5 tendencias llenando la columna
                else -> 6 // 4x4: 6 tendencias
            }
            else -> config.trendCount.value!!
        }

        val displayedTrends = trends.take(maxItems)
        val isSingleTrend = displayedTrends.size == 1

        // Densidad de relleno ultra-eficiente para aprovechar los bordes
        val contentPadding = when (config.density) {
            WidgetDensity.COMPACT -> 4.dp
            WidgetDensity.NORMAL -> 6.dp
            WidgetDensity.SPACIOUS -> 8.dp
            WidgetDensity.AUTO -> if (isCompactWidth || isCompactHeight) 4.dp else 7.dp
        }

        val containerModifier = GlanceModifier
            .fillMaxSize()
            .padding(contentPadding)

        Column(
            modifier = containerModifier
        ) {
            // Cabecera ultra-compacta (ocupa el mínimo espacio vertical)
            WidgetHeader(
                isCompactWidth = isCompactWidth,
                isCompactHeight = isCompactHeight,
                config = config
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            if (displayedTrends.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Conectando a Google Trends...",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                }
            } else if (isSingleTrend) {
                // Layout destacado para 1 sola tendencia (aprovecha todo el espacio)
                SingleTrendSpotlight(
                    trend = displayedTrends.first(),
                    config = config,
                    isCompactWidth = isCompactWidth,
                    isCompactHeight = isCompactHeight
                )
            } else {
                // Lista compacta y uniforme para múltiples tendencias
                LazyColumn(
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    items(displayedTrends) { trend ->
                        TrendRowItem(
                            trend = trend,
                            config = config,
                            isCompactWidth = isCompactWidth
                        )
                    }
                }
            }

            // Pie discreto de sincronización (sólo en widgets no pequeños si está habilitado)
            if (config.showLastUpdated && !(isCompactWidth && isCompactHeight)) {
                Spacer(modifier = GlanceModifier.height(1.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    Text(
                        text = "Sync $timeStr · Google Trends",
                        style = TextStyle(
                            fontSize = 8.sp,
                            color = GlanceTheme.colors.outline
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun WidgetHeader(
        isCompactWidth: Boolean,
        isCompactHeight: Boolean,
        config: WidgetConfig
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCompactWidth) "Tendencias" else "Tendencias España",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompactWidth) 10.sp else 11.sp,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            // En tamaños pequeños 2x2, el timestamp va directo en la cabecera si está activo
            if (isCompactWidth && isCompactHeight && config.showLastUpdated) {
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                Text(
                    text = timeStr,
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = GlanceTheme.colors.outline
                    )
                )
            } else if (!isCompactWidth) {
                Text(
                    text = "Google Trends",
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = GlanceTheme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Botón de actualización manual discreto (↻) independiente de la acción al tocar
            if (config.showManualRefresh) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = "↻",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    ),
                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshTrendsActionCallback>())
                )
            }
        }
    }

    @Composable
    private fun SingleTrendSpotlight(
        trend: Trend,
        config: WidgetConfig,
        isCompactWidth: Boolean,
        isCompactHeight: Boolean
    ) {
        val formattedTitle = formatTrendTitle(trend.title, config.textFormat)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (config.showRanking) {
                Text(
                    text = "#1 EN ESPAÑA",
                    style = TextStyle(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = formattedTitle,
                style = TextStyle(
                    fontSize = if (isCompactWidth && isCompactHeight) 12.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = if (config.textFormat == TextFormatOption.ONE_LINE) 1 else if (isCompactHeight) 3 else 4,
                modifier = GlanceModifier.fillMaxWidth()
            )

            if (config.showVolume && trend.approxTraffic != null) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = trend.approxTraffic,
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.outline
                    )
                )
            }
        }
    }

    @Composable
    private fun TrendRowItem(
        trend: Trend,
        config: WidgetConfig,
        isCompactWidth: Boolean
    ) {
        val formattedTitle = formatTrendTitle(trend.title, config.textFormat)

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición en ranking
            if (config.showRanking) {
                Text(
                    text = "#${trend.rank}",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCompactWidth) 9.sp else 10.sp,
                        color = GlanceTheme.colors.primary
                    ),
                    modifier = GlanceModifier.width(if (isCompactWidth) 18.dp else 22.dp)
                )
            }

            // Título de la tendencia (ocupa todo el ancho restante si no hay imagen)
            Text(
                text = formattedTitle,
                style = TextStyle(
                    fontSize = if (isCompactWidth) 10.sp else 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = if (config.textFormat == TextFormatOption.ONE_LINE) 1 else if (config.textFormat == TextFormatOption.FULL) 3 else 2,
                modifier = GlanceModifier.defaultWeight()
            )

            // Volumen de búsquedas si está activado, disponible y hay espacio
            if (config.showVolume && !isCompactWidth && trend.approxTraffic != null) {
                Spacer(modifier = GlanceModifier.width(3.dp))
                Text(
                    text = trend.approxTraffic.replace(" búsquedas", ""),
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = GlanceTheme.colors.outline
                    )
                )
            }
        }
    }

    private fun formatTrendTitle(rawTitle: String, format: TextFormatOption): String {
        // COMPLETO: utilizar exactamente el título original recibido de la fuente.
        // No aplicar ninguna función que lo reduzca a una, dos o tres palabras.
        // No hacer split, take, substring ni ninguna otra transformación que reduzca el número de palabras.
        if (format == TextFormatOption.FULL) {
            return rawTitle
        }

        // UNA LÍNEA: mantener el título completo y limitar únicamente su representación visual a una línea.
        // NO reducir el contenido previamente a una o dos palabras.
        if (format == TextFormatOption.ONE_LINE) {
            return rawTitle
        }

        val words = rawTitle.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.isEmpty()) return rawTitle

        return when (format) {
            TextFormatOption.FULL -> rawTitle
            TextFormatOption.ONE_LINE -> rawTitle
            TextFormatOption.ONE_WORD -> {
                // Mostrar exactamente la primera palabra
                words.first()
            }
            TextFormatOption.TWO_WORDS -> {
                // Mostrar exactamente las dos primeras palabras
                if (words.size <= 2) rawTitle.trim() else "${words[0]} ${words[1]}"
            }
            TextFormatOption.SHORT_PHRASE -> {
                // Puede reducirse el texto para hacerlo más compacto, pero debe poder contener más de dos palabras.
                if (words.size <= 3) rawTitle.trim()
                else if (words.size > 4) "${words.take(4).joinToString(" ")}..."
                else words.joinToString(" ")
            }
        }
    }
}

/**
 * Callback ejecutado al pulsar el botón de actualización manual (↻) en el widget.
 * Solicita una nueva lectura de tendencias directamente al repositorio abstracto,
 * sin acoplarse a Google Trends ni a una fuente concreta, actualizando el widget con los nuevos datos.
 */
class RefreshTrendsActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repository: es.tendencias.app.data.TrendsRepository = TrendsRepositoryImpl()
        repository.refreshTrends()
        TendenciasGlanceWidget().update(context, glanceId)
    }
}

