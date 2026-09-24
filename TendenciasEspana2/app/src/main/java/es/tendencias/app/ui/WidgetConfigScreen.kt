package es.tendencias.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.tendencias.app.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    currentConfig: WidgetConfig,
    onSaveConfig: (WidgetConfig) -> Unit,
    onNavigateBack: () -> Unit
) {
    var config by remember { mutableStateOf(currentConfig) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Configuración del Widget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pantalla de inicio · Jetpack Glance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onSaveConfig(config)
                        showSavedSnackbar = true
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar configuración")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        onSaveConfig(config)
                        showSavedSnackbar = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aplicar al Widget")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Nota informativa
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "El widget es redimensionable (2x2, 4x2, 2x4, 4x4) y se alimenta exclusivamente del feed RSS oficial de Google Trends España.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 1. Número de tendencias
            ConfigSection(title = "1. Número de tendencias") {
                val options = TrendCountOption.values()
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { config = config.copy(trendCount = option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.trendCount == option,
                            onClick = { config = config.copy(trendCount = option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 2. Formato del texto
            ConfigSection(title = "2. Formato del texto") {
                TextFormatOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { config = config.copy(textFormat = option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.textFormat == option,
                            onClick = { config = config.copy(textFormat = option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = option.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = option.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            // 3. Imágenes
            ConfigSection(title = "3. Imágenes (Fuente oficial)") {
                ImageOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { config = config.copy(imageOption = option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.imageOption == option,
                            onClick = { config = config.copy(imageOption = option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = option.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = option.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            // 4. Densidad y redimensionamiento
            ConfigSection(title = "4. Densidad del widget (2x2, 2x4, 4x2, 4x4)") {
                WidgetDensity.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { config = config.copy(density = option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.density == option,
                            onClick = { config = config.copy(density = option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 5. Frecuencia de actualización
            ConfigSection(title = "5. Frecuencia de actualización en segundo plano") {
                UpdateInterval.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { config = config.copy(updateInterval = option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.updateInterval == option,
                            onClick = { config = config.copy(updateInterval = option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    text = "* Nota de Android: La gestión de batería de Android (Doze mode) puede limitar refrescos menores a 30 minutos.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 6. Ranking (#1, #2...)
            ConfigSection(title = "6. Posición en ranking") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Mostrar posición (#1, #2...)", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = config.showRanking,
                        onCheckedChange = { config = config.copy(showRanking = it) }
                    )
                }
            }

            // 7. Información adicional
            ConfigSection(title = "7. Información adicional") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Volumen de búsquedas (si está disponible)", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = config.showVolume,
                        onCheckedChange = { config = config.copy(showVolume = it) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Categoría o contexto", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = config.showCategory,
                        onCheckedChange = { config = config.copy(showCategory = it) }
                    )
                }
            }

            // 8. Fecha y hora de actualización
            ConfigSection(title = "8. Fecha y hora de actualización") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Mostrar fecha/hora de actualización", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Aparece en tamaño pequeño y discreto en la esquina", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = config.showLastUpdated,
                        onCheckedChange = { config = config.copy(showLastUpdated = it) }
                    )
                }
            }

            // 9. Acción al tocar una tendencia
            ConfigSection(title = "9. Acción al tocar una tendencia") {
                WidgetClickAction.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { config = config.copy(clickAction = option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.clickAction == option,
                            onClick = { config = config.copy(clickAction = option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 10. Fondo del widget
            ConfigSection(title = "10. Estilo de fondo") {
                WidgetBackgroundStyle.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { config = config.copy(backgroundStyle = option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.backgroundStyle == option,
                            onClick = { config = config.copy(backgroundStyle = option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 11. Adaptación automática
            ConfigSection(title = "11. Adaptación automática al espacio") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Adaptar contenido automáticamente", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Reorganiza y reduce la información de forma inteligente cuando el widget tenga poco espacio en la pantalla de inicio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = config.autoAdaptSize,
                        onCheckedChange = { config = config.copy(autoAdaptSize = it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ConfigSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
