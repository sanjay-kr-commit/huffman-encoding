package huffman.encoding

import java.io.File

fun main() {

    print( "Enter file name : " )
    val file = readln()
    val buffer = File( file ).readText()
    File(
        if ( file.endsWith( ".compressed" ) ) file.removeSuffix( ".compressed") else "$file.compressed"
    ).writeText(
        if ( file.endsWith( ".compressed" ) ) buffer.decodeData() else buffer.encodeData()
    )
}