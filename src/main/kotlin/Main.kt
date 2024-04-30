package huffman.encoding

import java.io.File

fun main() {

    print( "Enter file name : " )
    val file = readln()
    val buffer = File( file ).readBytes()
    File(
        if ( file.endsWith( ".huffman_encoding" ) ) file.removeSuffix( ".huffman_encoding") else "$file.huffman_encoding"
    ).writeBytes(
        if ( file.endsWith( ".huffman_encoding" ) ) buffer.inflate else buffer.deflate
    )

}

