package com.example.dinner_selector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

private val CustomLightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = CustomLightColors) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainScreenContent()
                }
            }
        }
    }
}

@Composable
fun FireworkEffect(show: Boolean) {
    if (show) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("fireworks.json"))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = 1
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        )
    }
}

@Composable
fun MainScreenContent() {
    var input by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf<String>()) }
    var counts by remember { mutableStateOf(mutableMapOf<String, Int>()) }
    var currentPick by remember { mutableStateOf("") }
    var isDrawing by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf(listOf<String>()) }
    var winner by remember { mutableStateOf<String?>(null) }
    var showFirework by remember { mutableStateOf(false) }

    LaunchedEffect(winner) {
        if (winner != null) {
            showFirework = true
            delay(3000)
            showFirework = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FireworkEffect(show = showFirework)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(2.dp, shape = RoundedCornerShape(4.dp))
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
                Text(
                    "랜덤 저녁 메뉴 선택기",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("항목 입력") }
                )
                Button(
                    onClick = {
                        if (input.isNotBlank()) {
                            options = options + input
                            counts[input] = 0
                            input = ""
                            winner = null
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("추가")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
            ) {
                items(options) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = {
                                options = options.filterNot { it == item }
                                counts.remove(item)
                            }) {
                                Text("\uD83D\uDDD1 삭제", color = Color.Red)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    isDrawing = true
                    log = emptyList()
                    counts = options.associateWith { 0 }.toMutableMap()
                    winner = null
                },
                enabled = !isDrawing && options.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("랜덤 뽑기")
            }

            if (isDrawing) {
                LaunchedEffect(isDrawing) {
                    while (isDrawing) {
                        delay(500)
                        val pick = options.random()
                        currentPick = pick
                        counts[pick] = counts.getOrDefault(pick, 0) + 1
                        log = log + "$pick → ${counts[pick]}회"

                        if (counts[pick] == 3) {
                            isDrawing = false
                            winner = pick
                            break
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("현재 뽑은 항목: $currentPick")

            Spacer(modifier = Modifier.height(12.dp))

            Text("진행 로그", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(log.size) {
                    Text(log[it], style = MaterialTheme.typography.bodyMedium)
                }
            }

            winner?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "\uD83C\uDF89 최종 선택된 항목: $it",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
