package com.example.terminalai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object WebSearch {

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun search(query: String): String {
        val ptResult = searchWikipediaPT(query)
        if (ptResult.isNotBlank()) return ptResult

        val enQuery = translateToEnglish(query)
        val enResult = searchWikipediaEN(enQuery)
        if (enResult.isNotBlank()) return enResult

        return ""
    }

    private fun searchWikipediaPT(query: String): String {
        return try {
            val clean = query.replace("?", "").trim()
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = URL("https://pt.wikipedia.org/w/api.php?action=opensearch&search=$encoded&limit=1&format=json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "TerminalAI/1.0 (Android)")

            if (conn.responseCode != 200) { conn.disconnect(); return "" }
            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val searchJson = org.json.JSONArray(response)
            val urls = searchJson.optJSONArray(3) ?: return ""
            if (urls.length() == 0) return ""
            val articleUrl = urls.getString(0)
            val articleTitle = URLEncoder.encode(articleUrl.substringAfterLast("/"), "UTF-8")

            val summaryUrl = URL("https://pt.wikipedia.org/api/rest_v1/page/summary/$articleTitle")
            val summaryConn = summaryUrl.openConnection() as HttpURLConnection
            summaryConn.requestMethod = "GET"
            summaryConn.connectTimeout = 5000
            summaryConn.readTimeout = 5000
            summaryConn.setRequestProperty("User-Agent", "TerminalAI/1.0 (Android; gabriel@example.com)")
            summaryConn.setRequestProperty("Accept", "application/json")

            if (summaryConn.responseCode != 200) { summaryConn.disconnect(); return "" }
            val summaryResponse = summaryConn.inputStream.bufferedReader().readText()
            summaryConn.disconnect()

            JSONObject(summaryResponse).optString("extract", "").take(600)
        } catch (e: Exception) { "" }
    }

    private fun searchWikipediaEN(query: String): String {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://en.wikipedia.org/w/api.php?action=opensearch&search=$encoded&limit=1&format=json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "TerminalAI/1.0 (Android)")

            if (conn.responseCode != 200) { conn.disconnect(); return "" }
            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val searchJson = org.json.JSONArray(response)
            val urls = searchJson.optJSONArray(3) ?: return ""
            if (urls.length() == 0) return ""
            val articleUrl = urls.getString(0)
            val articleTitle = articleUrl.substringAfterLast("/")

            val summaryUrl = URL("https://en.wikipedia.org/api/rest_v1/page/summary/$articleTitle")
            val summaryConn = summaryUrl.openConnection() as HttpURLConnection
            summaryConn.requestMethod = "GET"
            summaryConn.connectTimeout = 5000
            summaryConn.readTimeout = 5000
            summaryConn.setRequestProperty("User-Agent", "TerminalAI/1.0 (Android; gabriel@example.com)")
            summaryConn.setRequestProperty("Accept", "application/json")

            if (summaryConn.responseCode != 200) { summaryConn.disconnect(); return "" }
            val summaryResponse = summaryConn.inputStream.bufferedReader().readText()
            summaryConn.disconnect()

            JSONObject(summaryResponse).optString("extract", "").take(600)
        } catch (e: Exception) { "" }
    }

    private fun translateToEnglish(query: String): String {
        return query
            .replace("maior país", "largest country")
            .replace("menor país", "smallest country")
            .replace("capital de", "capital of")
            .replace("presidente de", "president of")
            .replace("população de", "population of")
            .replace("ponto mais profundo", "deepest point")
            .replace("altura de", "height of")
            .replace("?", "")
            .trim()
    }

    fun isFactualQuestion(query: String): Boolean {
        val factualKeywords = listOf(
            "quantos", "quantas", "qual", "quais", "quando", "onde",
            "quem", "como se chama", "o que é", "o que são",
            "qual é", "qual a", "quanto", "distância", "população",
            "capital", "presidente", "altura", "tamanho", "velocidade",
            "temperatura", "ano", "data", "nasceu", "morreu", "fundado",
            "maior", "menor", "mais", "menos", "melhor", "pior"
        )
        val lower = query.lowercase()
        return factualKeywords.any { lower.contains(it) }
    }
}