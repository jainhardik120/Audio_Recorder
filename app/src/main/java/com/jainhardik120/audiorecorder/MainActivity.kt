package com.jainhardik120.audiorecorder

import android.annotation.SuppressLint
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioSource
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.documentfile.provider.DocumentFile
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jainhardik120.audiorecorder.ui.theme.AudioRecorderTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar


class MainActivity : ComponentActivity() {
    private var recorder: MediaRecorder? = null

    @SuppressLint("SimpleDateFormat")
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val storageReference = FirebaseStorage.getInstance().reference

        setContent {
            AudioRecorderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    var selectedImageUri by remember {
//                        mutableStateOf<Uri?>(null)
//                    }
//                    var selectedImagesUri by remember {
//                        mutableStateOf<List<Uri>>(emptyList())
//                    }
//                    val singlePickerLauncher = rememberLauncherForActivityResult(
//                        contract = ActivityResultContracts.PickVisualMedia(),
//                        onResult = { uri ->
//                            selectedImageUri = uri
//                        }
//                    )
//                    val multiPickerLauncher = rememberLauncherForActivityResult(
//                        contract = ActivityResultContracts.PickMultipleVisualMedia(),
//                        onResult = { uris ->
//                            selectedImagesUri = uris
//                        }
//                    )
//                    LazyColumn(Modifier.fillMaxSize(), content = {
//                        item {
//                            Row {
//                                Button(onClick = {
//                                    singlePickerLauncher.launch(
//                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                                    )
//                                }) {
//                                    Text(text = "Single Picker")
//                                }
//                                Button(onClick = {
//                                    multiPickerLauncher.launch(
//                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                                    )
//                                }) {
//                                    Text(text = "Multi Picker")
//                                }
//                            }
//                        }
//                        item {
//                            Row {
//                                Button(onClick = {
//                                    selectedImageUri?.let { uri ->
//                                        val fileName = UUID.randomUUID().toString()
//                                        val uploadTask =
//                                            storageReference.child("file/$fileName").putFile(uri)
//                                        uploadTask.addOnSuccessListener {
//                                            storageReference.child("file/$fileName").downloadUrl.addOnSuccessListener {
//                                                println(it)
//                                            }.addOnFailureListener {
//                                                println(it.message)
//                                            }
//                                        }.addOnFailureListener {
//                                            println(it.message ?: "Error")
//                                        }
//                                    }
//                                }) {
//                                    Text(text = "Single Uploader")
//                                }
//                                Button(onClick = {
//
//                                }) {
//                                    Text(text = "Multi Uploader")
//                                }
//                            }
//                        }
//                        item {
//                            AsyncImage(
//                                model = selectedImageUri,
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxWidth(),
//                                contentScale = ContentScale.Crop
//                            )
//                        }
//                        itemsIndexed(selectedImagesUri) { _, item ->
//                            AsyncImage(
//                                model = item,
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxWidth(),
//                                contentScale = ContentScale.Crop
//                            )
//                        }
//
//                    })
                    val scope = rememberCoroutineScope()
                    val permissionState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            android.Manifest.permission.RECORD_AUDIO
                        )
                    )
                    when {
                        permissionState.allPermissionsGranted -> {
                            Column {
                                Button(onClick = {
                                    recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        MediaRecorder(this@MainActivity)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        MediaRecorder()
                                    }.apply {
                                        setAudioSource(AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                        setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
                                        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                                        val currentTimeMillis = Calendar.getInstance().time
                                        val currentDateTime =
                                            dateTimeFormat.format(currentTimeMillis)
                                        val currentDate = currentDateTime.split("_").first()
                                        val currentTime = currentDateTime.split("_").last()
                                        val fileName = "%d_%t".replace("%d", currentDate)
                                            .replace("%t", currentTime)
                                            .replace("%m", currentTimeMillis.time.toString())
                                            .replace(
                                                "%s",
                                                currentTimeMillis.time.div(1000).toString()
                                            )
                                        val file = DocumentFile.fromFile(
                                            this@MainActivity.getExternalFilesDir(null)
                                                ?: this@MainActivity.filesDir
                                        ).createFile("audio/*", "$fileName.m4a")
                                        setOutputFile(
                                            contentResolver.openFileDescriptor(
                                                file!!.uri,
                                                "w"
                                            )?.fileDescriptor
                                        )
                                        runCatching {
                                            prepare()
                                        }
                                        start()
                                    }
                                }) {
                                    Text(text = "Record")
                                }
                                Button(onClick = {
                                    recorder?.apply {
                                        stop()
                                        release()
                                    }
                                    recorder = null
                                }) {
                                    Text(text = "Stop")
                                }
                            }
                        }

                        permissionState.shouldShowRationale -> {
                            Column {
                                Text(text = "Permissions were denied, permissions are required to work")
                                Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                                    Text(text = "Request Permissions")
                                }
                            }
                        }

                        else -> {
                            LaunchedEffect(key1 = permissionState, block = {
                                scope.launch {
                                    permissionState.launchMultiplePermissionRequest()
                                }
                            })

                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }
}
