package es.tendencias.app.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receptor de eventos de Android para el Widget de tendencias.
 * Vinculado en AndroidManifest.xml a 'android.appwidget.action.APPWIDGET_UPDATE'.
 */
class TendenciasWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TendenciasGlanceWidget()
}
