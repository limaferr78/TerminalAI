package com.example.terminalai

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream

class AIManager(private val context: Context) {
    private val engine = AiChat.getInferenceEngine(context)
    private var isInitialized = false

    suspend fun initializeModel(): Boolean {
        return try {
            val modelFile = copyModelFromAssets()

            engine.state.first {
                it is InferenceEngine.State.Initialized ||
                        it is InferenceEngine.State.Error
            }

            if (engine.state.value is InferenceEngine.State.Error) return false

            engine.loadModel(modelFile.absolutePath)
            engine.setSystemPrompt(
                "You are a factual assistant. When given web context, use ONLY that context to answer. Always respond in Brazilian Portuguese."
            )
            isInitialized = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun extractSearchTerm(question: String): String {
        return try {
            val prompt = "<|user|>What is the main topic/subject of this question? Reply with ONLY a Wikipedia article title in English, 1-4 words: \"$question\"</s>\n<|assistant|>"

            var result = ""
            engine.sendUserPrompt(prompt, 20).collect { token ->
                result += token
            }
            android.util.Log.d("AIManager", "Search term extracted: $result")
            result.trim().lines().first().trim()
        } catch (e: Exception) {
            question
        }
    }

    fun generateResponseStream(userMessage: String): Flow<String> {
        return flow {
            val isFactual = WebSearch.isFactualQuestion(userMessage)
            val isOnline = WebSearch.isOnline(context)

            var finalPrompt = userMessage

            if (isFactual && isOnline) {
                emit("[🌐 Buscando na web...]\n")

                // Usa o modelo para extrair o termo de busca
                val searchTerm = extractSearchTerm(userMessage)

                val searchResult = withContext(Dispatchers.IO) {
                    WebSearch.search(searchTerm)
                }
                if (searchResult.isNotBlank()) {
                    finalPrompt = """Web context: $searchResult

Question: $userMessage

Answer in Portuguese based on the context above."""
                    emit("[✓ Contexto encontrado]\n\n")
                } else {
                    emit("[✗ Sem resultado, usando conhecimento próprio]\n\n")
                }
            }

            engine.sendUserPrompt(finalPrompt, 512).collect { token ->
                emit(token)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun cleanup() {
        engine.cleanUp()
    }

    private fun copyModelFromAssets(): File {
        val modelName = "qwen2.5-1.5b-instruct-q4_k_m.gguf"
        val outputFile = File(context.filesDir, modelName)
        if (outputFile.exists()) return outputFile
        context.assets.open(modelName).use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        return outputFile
    }
}