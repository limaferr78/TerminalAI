package com.example.terminalai.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.terminalai.AIManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch

// Cores do terminal
val Green = Color(0xFF00FF00)
val DimGreen = Color(0xFF00AA00)
val White = Color(0xFFCCCCCC)
val Gray = Color(0xFF666666)
val Yellow = Color(0xFFFFFF00)
val Background = Color(0xFF0C0C0C)
val DividerColor = Color(0xFF222222)

data class Message(
    val role: String, // "user" ou "ai"
    val content: String,
    val isStreaming: Boolean = false
)

@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val aiManager = remember { AIManager(context) }
    var isModelLoaded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var streamingText by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Carregando modelo...") }

    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isModelLoaded = aiManager.initializeModel()
        statusText = if (isModelLoaded) "Modelo carregado. Digite sua pergunta." else "Erro ao carregar modelo."
    }

    LaunchedEffect(messages, streamingText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(
                "╔══════════════════════════════╗",
                color = DimGreen, fontFamily = FontFamily.Monospace, fontSize = 13.sp
            )
            Text(
                "║     TERMINAL AI  v1.0        ║",
                color = Green, fontFamily = FontFamily.Monospace, fontSize = 13.sp
            )
            Text(
                "╚══════════════════════════════╝",
                color = DimGreen, fontFamily = FontFamily.Monospace, fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "─── $statusText",
                color = Gray, fontFamily = FontFamily.Monospace, fontSize = 12.sp
            )
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Área de mensagens
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            messages.forEach { message ->
                if (message.role == "user") {
                    // Mensagem do usuário
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Text("C:\\> ", color = Green, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        Text(message.content, color = White, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    // Resposta da IA — formata parágrafos
                    val paragraphs = message.content.trim().split("\n")
                    paragraphs.forEach { paragraph ->
                        if (paragraph.isBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            Text(
                                paragraph,
                                color = White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("─".repeat(40), color = DividerColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Texto sendo gerado (streaming)
            if (streamingText.isNotEmpty()) {
                val paragraphs = streamingText.trim().split("\n")
                paragraphs.forEach { paragraph ->
                    if (paragraph.isBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        Text(
                            paragraph,
                            color = White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                // Cursor piscante
                Text("█", color = Green, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }

            if (isLoading && streamingText.isEmpty()) {
                Text("█ processando...", color = DimGreen, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }

        // Input
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                "C:\\> ",
                color = Green,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
            TextField(
                value = input,
                onValueChange = { if (!isLoading) input = it },
                enabled = isModelLoaded && !isLoading,
                placeholder = {
                    Text(
                        if (!isModelLoaded) "Carregando..." else "Digite aqui...",
                        color = Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Green,
                    focusedTextColor = Green,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    cursorColor = Green,
                    disabledContainerColor = Color.Transparent,
                    disabledTextColor = Gray
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (input.isNotBlank() && !isLoading) {
                            val userInput = input
                            input = ""
                            keyboardController?.hide()
                            isLoading = true
                            streamingText = ""

                            messages = messages + Message("user", userInput)

                            coroutineScope.launch(Dispatchers.IO) {
                                val buffer = StringBuilder()
                                aiManager.generateResponseStream(userInput)
                                    .catch { e ->
                                        withContext(Dispatchers.Main) {
                                            messages = messages + Message("ai", "Erro: ${e.message}")
                                            streamingText = ""
                                            isLoading = false
                                        }
                                    }
                                    .collect { token ->
                                        buffer.append(token)
                                        withContext(Dispatchers.Main) {
                                            streamingText = buffer.toString()
                                        }
                                    }
                                withContext(Dispatchers.Main) {
                                    messages = messages + Message("ai", buffer.toString())
                                    streamingText = ""
                                    isLoading = false
                                }
                            }
                        }
                    }
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}