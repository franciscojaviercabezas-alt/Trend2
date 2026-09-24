package es.tendencias.app.data

import android.util.Xml
import es.tendencias.app.model.NewsItem
import es.tendencias.app.model.Trend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

interface TrendsRepository {
    fun getTrendsStream(): Flow<List<Trend>>
    suspend fun refreshTrends(): Result<List<Trend>>
}

/**
 * Conexión nativa directa con el feed RSS oficial de Google Trends para España (geo=ES).
 * No utiliza ninguna API ficticia ni datos simulados.
 */
class TrendsRepositoryImpl : TrendsRepository {

    private val _trends = MutableStateFlow<List<Trend>>(emptyList())
    override fun getTrendsStream(): Flow<List<Trend>> = _trends.asStateFlow()

    private val feedUrls = listOf(
        "https://trends.google.com/trending/rss?geo=ES",
        "https://trends.google.com/trends/trendingsearches/daily/rss?geo=ES"
    )

    override suspend fun refreshTrends(): Result<List<Trend>> {
        return withContext(Dispatchers.IO) {
            var lastException: Exception? = null

            for (urlString in feedUrls) {
                var connection: HttpURLConnection? = null
                try {
                    val url = URL(urlString)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 10000
                        readTimeout = 10000
                        setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        )
                        setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*")
                        setRequestProperty("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    }

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.use { inputStream ->
                            val parsedTrends = parseGoogleTrendsRss(inputStream)
                            if (parsedTrends.isNotEmpty()) {
                                _trends.value = parsedTrends
                                return@withContext Result.success(parsedTrends)
                            }
                        }
                    }
                } catch (e: Exception) {
                    lastException = e
                } finally {
                    connection?.disconnect()
                }
            }

            Result.failure(lastException ?: Exception("No se pudieron cargar las tendencias de Google Trends España"))
        }
    }

    private fun parseGoogleTrendsRss(inputStream: InputStream): List<Trend> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(inputStream, "UTF-8")

        val trends = mutableListOf<Trend>()
        var eventType = parser.eventType
        var currentRank = 1

        var inItem = false
        var currentTitle = ""
        var currentTraffic: String? = null
        var currentPubDate: String? = null
        var currentPicture: String? = null
        var currentPictureSource: String? = null
        val currentNews = mutableListOf<NewsItem>()

        var inNewsItem = false
        var newsTitle = ""
        var newsSnippet: String? = null
        var newsUrl = ""
        var newsSource = ""
        var newsPic: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "item" -> {
                            inItem = true
                            currentTitle = ""
                            currentTraffic = null
                            currentPubDate = null
                            currentPicture = null
                            currentPictureSource = null
                            currentNews.clear()
                        }
                        "news_item" -> {
                            inNewsItem = true
                            newsTitle = ""
                            newsSnippet = null
                            newsUrl = ""
                            newsSource = ""
                            newsPic = null
                        }
                        "title" -> if (inItem && !inNewsItem) currentTitle = parser.nextText().trim()
                        "approx_traffic" -> if (inItem) {
                            val raw = parser.nextText().trim()
                            currentTraffic = if (raw.isNotEmpty()) "+$raw búsquedas" else null
                        }
                        "pubDate" -> if (inItem) currentPubDate = parser.nextText().trim()
                        "picture" -> if (inItem && !inNewsItem) currentPicture = parser.nextText().trim()
                        "picture_source" -> if (inItem) currentPictureSource = parser.nextText().trim()
                        "news_item_title" -> if (inNewsItem) newsTitle = parser.nextText().trim()
                        "news_item_snippet" -> if (inNewsItem) newsSnippet = parser.nextText().trim()
                        "news_item_url" -> if (inNewsItem) newsUrl = parser.nextText().trim()
                        "news_item_source" -> if (inNewsItem) newsSource = parser.nextText().trim()
                        "news_item_picture" -> if (inNewsItem) newsPic = parser.nextText().trim()
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "news_item" -> {
                            if (newsTitle.isNotBlank()) {
                                currentNews.add(
                                    NewsItem(
                                        title = newsTitle,
                                        source = newsSource,
                                        url = newsUrl,
                                        snippet = newsSnippet,
                                        pictureUrl = newsPic
                                    )
                                )
                            }
                            inNewsItem = false
                        }
                        "item" -> {
                            if (currentTitle.isNotBlank()) {
                                trends.add(
                                    Trend(
                                        id = "trend-es-$currentRank-${currentTitle.hashCode()}",
                                        title = currentTitle,
                                        rank = currentRank,
                                        approxTraffic = currentTraffic,
                                        pubDate = currentPubDate,
                                        pictureUrl = currentPicture,
                                        pictureSource = currentPictureSource,
                                        newsItems = currentNews.toList(),
                                        sourceUrl = "https://www.google.es/search?q=${java.net.URLEncoder.encode(currentTitle, "UTF-8")}"
                                    )
                                )
                                currentRank++
                            }
                            inItem = false
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return trends
    }
}
