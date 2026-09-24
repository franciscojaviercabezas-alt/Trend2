package es.tendencias.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.glance.appwidget.updateAll
import es.tendencias.app.model.*
import es.tendencias.app.widget.TendenciasGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface WidgetPreferencesRepository {
    fun getWidgetConfigStream(): Flow<WidgetConfig>
    fun getWidgetConfig(): WidgetConfig
    suspend fun saveWidgetConfig(config: WidgetConfig)
}

class WidgetPreferencesRepositoryImpl(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : WidgetPreferencesRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "es_tendencias_widget_preferences",
        Context.MODE_PRIVATE
    )

    private val _configFlow = MutableStateFlow(readFromPrefs())

    override fun getWidgetConfigStream(): Flow<WidgetConfig> = _configFlow.asStateFlow()

    override fun getWidgetConfig(): WidgetConfig = _configFlow.value

    override suspend fun saveWidgetConfig(config: WidgetConfig) {
        prefs.edit().apply {
            putString("KEY_TREND_COUNT", config.trendCount.name)
            putString("KEY_TEXT_FORMAT", config.textFormat.name)
            putString("KEY_IMAGE_OPTION", config.imageOption.name)
            putString("KEY_DENSITY", config.density.name)
            putString("KEY_UPDATE_INTERVAL", config.updateInterval.name)
            putBoolean("KEY_SHOW_RANKING", config.showRanking)
            putBoolean("KEY_SHOW_VOLUME", config.showVolume)
            putBoolean("KEY_SHOW_CATEGORY", config.showCategory)
            putBoolean("KEY_SHOW_LAST_UPDATED", config.showLastUpdated)
            putBoolean("KEY_SHOW_MANUAL_REFRESH", config.showManualRefresh)
            putString("KEY_CLICK_ACTION", config.clickAction.name)
            putString("KEY_BACKGROUND_STYLE", config.backgroundStyle.name)
            putBoolean("KEY_AUTO_ADAPT_SIZE", config.autoAdaptSize)
            apply()
        }

        _configFlow.value = config

        // Solicita a Jetpack Glance la actualización de todos los widgets colocados en la pantalla de inicio
        scope.launch {
            try {
                TendenciasGlanceWidget().updateAll(context)
            } catch (e: Exception) {
                // Registro seguro si el widget aún no está añadido a la pantalla de inicio
            }
        }
    }

    private fun readFromPrefs(): WidgetConfig {
        val trendCount = try {
            TrendCountOption.valueOf(prefs.getString("KEY_TREND_COUNT", TrendCountOption.AUTO.name) ?: TrendCountOption.AUTO.name)
        } catch (_: Exception) { TrendCountOption.AUTO }

        val textFormat = try {
            TextFormatOption.valueOf(prefs.getString("KEY_TEXT_FORMAT", TextFormatOption.FULL.name) ?: TextFormatOption.FULL.name)
        } catch (_: Exception) { TextFormatOption.FULL }

        val imageOption = try {
            ImageOption.valueOf(prefs.getString("KEY_IMAGE_OPTION", ImageOption.VALID_ONLY.name) ?: ImageOption.VALID_ONLY.name)
        } catch (_: Exception) { ImageOption.VALID_ONLY }

        val density = try {
            WidgetDensity.valueOf(prefs.getString("KEY_DENSITY", WidgetDensity.AUTO.name) ?: WidgetDensity.AUTO.name)
        } catch (_: Exception) { WidgetDensity.AUTO }

        val updateInterval = try {
            UpdateInterval.valueOf(prefs.getString("KEY_UPDATE_INTERVAL", UpdateInterval.MIN_30.name) ?: UpdateInterval.MIN_30.name)
        } catch (_: Exception) { UpdateInterval.MIN_30 }

        val clickAction = try {
            WidgetClickAction.valueOf(prefs.getString("KEY_CLICK_ACTION", WidgetClickAction.SEARCH_WEB.name) ?: WidgetClickAction.SEARCH_WEB.name)
        } catch (_: Exception) { WidgetClickAction.SEARCH_WEB }

        val backgroundStyle = try {
            WidgetBackgroundStyle.valueOf(prefs.getString("KEY_BACKGROUND_STYLE", WidgetBackgroundStyle.SYSTEM.name) ?: WidgetBackgroundStyle.SYSTEM.name)
        } catch (_: Exception) { WidgetBackgroundStyle.SYSTEM }

        return WidgetConfig(
            trendCount = trendCount,
            textFormat = textFormat,
            imageOption = imageOption,
            density = density,
            updateInterval = updateInterval,
            showRanking = prefs.getBoolean("KEY_SHOW_RANKING", true),
            showVolume = prefs.getBoolean("KEY_SHOW_VOLUME", true),
            showCategory = prefs.getBoolean("KEY_SHOW_CATEGORY", true),
            showLastUpdated = prefs.getBoolean("KEY_SHOW_LAST_UPDATED", true),
            showManualRefresh = prefs.getBoolean("KEY_SHOW_MANUAL_REFRESH", true),
            clickAction = clickAction,
            backgroundStyle = backgroundStyle,
            autoAdaptSize = prefs.getBoolean("KEY_AUTO_ADAPT_SIZE", true)
        )
    }
}
