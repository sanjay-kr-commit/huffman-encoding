package encoding.huffman.kotlin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    var inputFile by remember { mutableStateOf("") }
    var fileNotFound by remember { mutableStateOf( true) }
    var outputFile by remember { mutableStateOf("") }
    var fileAlreadyExists by remember { mutableStateOf(false) }
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    var bufferSize by remember { mutableStateOf("1") }
    var job : Job? by remember { mutableStateOf(null) }
    var log by remember { mutableStateOf("") }
    var compress by remember { mutableStateOf(true) }

    Window( onCloseRequest = ::exitApplication ) {

        LazyColumn( Modifier.fillMaxSize().padding( 10.dp ) , horizontalAlignment = Alignment.Start , verticalArrangement = Arrangement.Center ) {
            item {
                Text(
                    "Input File : ${
                        if (fileNotFound) "File Not Found" else "File Found"
                    }", fontSize = 30.sp
                )
                TextField(value = inputFile, onValueChange = {
                    inputFile = it
                    fileNotFound = !File(inputFile).exists()
                    outputFile = "$it.huff"
                    fileAlreadyExists = File(outputFile).exists()
                }, modifier = Modifier.fillMaxWidth())
                Text(
                    "Output File ${
                        if (fileAlreadyExists) "File Already Exist" else ""
                    }", fontSize = 30.sp
                )
                TextField(value = outputFile, onValueChange = {
                    outputFile = it
                    fileAlreadyExists = File(outputFile).exists()
                }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Text("Buffer Size : ", fontSize = 20.sp)
                    TextField(value = "$bufferSize mb", onValueChange = {
                        bufferSize = it.filter {
                            it.isDigit()
                        }
                    })
                }
                if ( job == null ) {
                    Button(onClick = {
                        compress = !compress
                    }) {
                        Text(if (compress) "compress" else "decompress")
                    }
                    Button(onClick = {
                        if (!fileAlreadyExists && !fileNotFound) {
                            job = coroutineScope.launch {
                                log = ""
                                if (compress) deflate(
                                    (if (bufferSize.isEmpty() || bufferSize.isBlank()) 1 else bufferSize.filter { it.isDigit() }
                                        .toInt()) * 1024 * 1024,
                                    File(inputFile).inputStream(),
                                    File(outputFile).outputStream(),
                                    {
                                        log = "${log}\n$it"
                                    }
                                )
                                else inflate(
                                    File(inputFile).inputStream(),
                                    File(outputFile).outputStream(),
                                    {
                                        log = "${log}\n$it"
                                    }
                                )
                                job = null
                            }
                        }
                    }) {
                        Text(
                            if (!fileAlreadyExists && !fileNotFound) "Start"
                            else "Check Input File"
                        )
                    }
                }

                Text(log, Modifier.padding(10.dp))

            }
        }
    }

}


