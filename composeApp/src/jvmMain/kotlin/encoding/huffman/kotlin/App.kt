package encoding.huffman.kotlin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

fun main() = application {

    var mainWindow by remember { mutableStateOf( true ) }
    val log = remember { mutableStateListOf("") }
    val job: MutableState<Job?> = remember { mutableStateOf(null) }
    val outputFile : MutableState<String> = remember { mutableStateOf("") }

    val inputFile = remember { mutableStateOf("") }
    val fileNotFound = remember { mutableStateOf(true) }
    val fileAlreadyExists = remember { mutableStateOf(false) }
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val bufferSize = remember { mutableStateOf("1") }
    val compress = remember { mutableStateOf(true) }

    Window( onCloseRequest = ::exitApplication , title = "Huffman Encoding" ) {
        Box( modifier = Modifier.fillMaxSize().padding( 10.dp )  , contentAlignment = Alignment.Center ) {
            AnimatedVisibility( visible = mainWindow  , modifier = Modifier.fillMaxSize()) {
                  MainWindow(
                    {
                        mainWindow = !mainWindow
                    },
                    log,
                    job,
                    outputFile,
                    inputFile,
                    fileNotFound,
                    fileAlreadyExists,
                    coroutineScope,
                    bufferSize,
                    compress
                )
            }

            AnimatedVisibility( visible = !mainWindow , modifier = Modifier.fillMaxSize() ) {
                LogWindow(
                    {
                        mainWindow = !mainWindow
                    }, log, job, outputFile
                )
            }
        }
    }
}

@Composable
fun MainWindow(
    changeWindow : () -> Unit = {},
    log : MutableList<String>,
    job: MutableState<Job?>,
    outputFile : MutableState<String> ,
    inputFile : MutableState<String> ,
    fileNotFound : MutableState<Boolean> ,
    fileAlreadyExists : MutableState<Boolean> ,
    coroutineScope : CoroutineScope ,
    bufferSize : MutableState<String> ,
    compress : MutableState<Boolean> ,
) {

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        bottomBar = {
            Row {
                if ( job.value == null ) Button(onClick = {
                    compress.value = !compress.value
                }, modifier = Modifier.padding( horizontal = 16.dp )) {
                    Text(if (compress.value) "compress" else "decompress")
                }
                Button(onClick = {
                    if (!fileAlreadyExists.value && !fileNotFound.value) {
                        job.value?.let {
                            it.cancel()
                            job.value = null
                            File(outputFile.value).delete()
                        } ?: run {
                            job.value = coroutineScope.launch {
                                changeWindow()
                                log.clear()
                                if (compress.value) deflate(
                                    (if (bufferSize.value.isEmpty() || bufferSize.value.isBlank()) 1 else bufferSize.value.filter { it.isDigit() }
                                        .toInt()) * 1024 * 1024,
                                    File(inputFile.value).inputStream(),
                                    File(outputFile.value).outputStream(),
                                    {
                                        log.add(it.toString() )
                                    }
                                )
                                else inflate(
                                    File(inputFile.value).inputStream(),
                                    File(outputFile.value).outputStream(),
                                    {
                                        log.add(it.toString() )
                                    }
                                )
                                job.value = null
                            }
                        }
                    }
                }) {
                    job.value?.let {
                        Text( "Cancel" )
                    } ?: Text(
                        if (!fileAlreadyExists.value && !fileNotFound.value) "Start"
                        else "Check Input File"
                    )
                }
                Button(onClick = {
                    changeWindow()
                } , modifier = Modifier.padding(horizontal = 16.dp )) {
                    Text( "Show Log" )
                }

            }
        }
    ) {
        Column(
            Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "Input File : ${
                    if (fileNotFound.value) "File Not Found" else "File Found"
                }", fontSize = 30.sp ,
                color = if ( fileNotFound.value ) Color.Red else Color.Black
            )
            TextField(value = inputFile.value, onValueChange = {
                inputFile.value = it
                fileNotFound.value = !File(inputFile.value).exists()
                outputFile.value = "$it.huff"
                fileAlreadyExists.value = File(outputFile.value).exists()
            }, modifier = Modifier.fillMaxWidth())
            Text(
                "Output File ${
                    if (fileAlreadyExists.value) "File Already Exist" else ""
                }", fontSize = 30.sp ,
                color = if ( fileAlreadyExists.value ) Color.Red else Color.Black
            )
            TextField(value = outputFile.value, onValueChange = {
                outputFile.value = it
                fileAlreadyExists.value = File(outputFile.value).exists()
            }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp) , verticalAlignment = Alignment.CenterVertically) {
                Text("Buffer Size : ", fontSize = 20.sp)
                TextField(value = "${bufferSize.value} mb", onValueChange = {
                    bufferSize.value = it.filter {
                        it.isDigit()
                    }
                })
            }
        }
    }
}


@Composable
fun LogWindow(
    changeWindow : () -> Unit = {},
    log : MutableList<String>,
    job: MutableState<Job?>,
    outputFile : MutableState<String>,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
        bottomBar = {
            Button( onClick = {
                job.value?.let {
                    it.cancel()
                    job.value = null
                    File(outputFile.value).delete()
                }
                changeWindow()
            } ) {
                Text(
                    job.value?.let {
                        "Cancel"
                    } ?: "Back"
                )
            }
        }
    ) {

        LazyColumn (
            modifier = Modifier.fillMaxSize().padding(10.dp)
                .padding( it ) ,
        ) {
            log.forEach {
                item {
                    Text( it )
                }
            }
        }
    }
}