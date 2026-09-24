package es.tendencias.app.model

/**
 * Noticia vinculada a una tendencia de Google Trends.
 */
data class NewsItem(
    val title: String,
    val source: String,
    val url: String,
    val snippet: String? = null,
    val pictureUrl: String? = null
)

/**
 * Modelo de datos representativo de una tendencia oficial de Google Trends para España.
 */
data class Trend(
    val id: String,
    val title: String,
    val rank: Int = 0,
    val approxTraffic: String? = null,
    val pubDate: String? = null,
    val pictureUrl: String? = null,
    val pictureSource: String? = null,
    val newsItems: List<NewsItem> = emptyList(),
    val sourceUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
