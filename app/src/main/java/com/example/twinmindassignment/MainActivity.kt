package com.example.twinmindassignment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }

        setContent {
            TwinMindUI()
        }
    }
}

@Composable
fun TwinMindUI() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "dashboard") {
        composable("dashboard") { DashboardUI(navController) }
        composable("record") { RecordingUI(navController) }
        composable("summary") { SummaryUI() }
    }
}

/* ---------------- DASHBOARD UI ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardUI(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meetings") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("record")
            }) {
                Icon(Icons.Default.Mic, contentDescription = "Record")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items((1..5).toList()) { index ->
                Card(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("summary")
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Meeting $index", fontWeight = FontWeight.Bold)
                        Text("Tap to view summary")
                    }
                }
            }
        }
    }
}

/* ---------------- RECORDING UI ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingUI(navController: NavHostController) {
    var isRecording by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("Stopped") }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            seconds++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recording") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%02d:%02d", seconds / 60, seconds % 60),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(status, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isRecording = !isRecording
                    status = if (isRecording) "Recording..." else "Stopped"

                    val intent = Intent(
                        navController.context,
                        RecordingService::class.java
                    )

                    intent.action = if (isRecording)
                        RecordingService.ACTION_START
                    else
                        RecordingService.ACTION_STOP

                    navController.context.startService(intent)

                },
                modifier = Modifier.size(120.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Finish")
            }
        }
    }
}

/* ---------------- SUMMARY UI ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryUI() {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("transcripts", Context.MODE_PRIVATE)

    var transcript by remember {
        mutableStateOf(
            prefs.getString("latest_transcript", null)
        )
    }

    // Re-check transcript every time screen opens
    LaunchedEffect(Unit) {
        transcript = prefs.getString("latest_transcript", null)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Summary") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            if (transcript == null) {
                Text("Generating summary...")
            } else {

                Text("Transcript", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(transcript!!)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Action Items", fontWeight = FontWeight.Bold)
                Text("• Review transcript\n• Generate summary")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Key Points", fontWeight = FontWeight.Bold)
                Text("• Transcription completed\n• Ready for summary generation")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DashboardUIPreview() {
    DashboardUI(navController = rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun RecordingUIPreview() {
    RecordingUI(navController = rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SummaryUIPreview() {
    SummaryUI()
}

