package encoding.huffman.kotlin

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

val ByteArray.toTypedArray : Array<Byte> get() = Array( size ) { 0.toByte() }.apply {
    for ( index in this@toTypedArray.indices ) this[index] = this@toTypedArray[index]
}

val Array<Byte>.toByteArray : ByteArray get() = ByteArray( size ).apply {
    for ( index in this@toByteArray.indices ) this[index] = this@toByteArray[index]
}
val InputStream.huffmanTable: HashMap<Int, Int>
    get() =
        HashMap<Int, Int>().also { table ->
            var byte: Int
            while (this.read().also { byte = it } != -1) {
                table[byte] = table.getOrDefault(byte, 0) + 1
            }
            this.close()
        }


val HashMap<Int, Int>.huffmanTree: HuffmanNode?
    get() {
        val queue =
            PriorityQueue<HuffmanNode> { a, b ->
                a.freq.compareTo(b.freq)
            }
        for ((k, v) in this) {
            queue.add(HuffmanNode(null, null, k, v))
        }
        while (queue.size > 1) {
            val a = queue.poll()
            val b = queue.poll()
            queue.add(HuffmanNode(a, b, -1, a.freq + b.freq))
        }
        return queue.poll()
    }

fun HashMap<Int, String>.huffmanPathTableHelper(
    node: HuffmanNode,
    path: String,
) {
    if (node.code != -1) {
        put(node.code, path)
        return
    }
    huffmanPathTableHelper(node.left!!, "${path}0")
    huffmanPathTableHelper(node.right!!, "${path}1")
}

val HuffmanNode.huffmanPathTable: HashMap<Int, String>
    get() =
        HashMap<Int, String>().also { it.huffmanPathTableHelper(this, "") }

fun InputStream.compressedByte( huffmanPathTable : HashMap<Int, String> ) : ByteArray {
    val list = ArrayList<Byte>()
    var byte : Int = 0
    var count = 0
    use {
        var code : Int
        while ( it.read().also { code = it } != -1 ) {
            huffmanPathTable[code]!!.forEach { bit ->
                byte = byte shl 1
                byte = byte or (bit - '0')
                count++
                if ( count == 8 ) {
                    list.add( byte.toByte() )
                    count %= 8
                }
            }
        }
    }
    if ( count != 0 ) {
        byte = byte shl (8-count)
        list.add( byte.toByte() )
    } else count = 8
    list.add( 0 , count.toByte() )
    return list.toByteArray()
}



fun deflate(
    bufferSize : Int,
    inputFile : InputStream,
    outputFile : OutputStream ,
    log : (Any) -> Unit = {
//        println( it )
    }
) {
    var uncompressedSize = 0L
    var compressedSize = 0L
    var count = 1
    val buffer = ByteArray(bufferSize)
    var byteRead = 0
    DataOutputStream(outputFile).use { outStream ->
        DataInputStream(inputFile).use { inStream ->
            outStream.writeInt(bufferSize)
            log( "Buffer Size : $bufferSize bytes" )
            while (inStream.read(buffer).also { byteRead = it } != -1) {
                log( "Deflating Segment ${count++}" )
                log( "Initial : $byteRead" )
                if ( byteRead != bufferSize ) buffer[byteRead] = -1
                uncompressedSize += byteRead.toLong()
                val huffmanTree = buffer.inputStream()
                    .huffmanTable.huffmanTree!!
                val huffmanPathTable = huffmanTree.huffmanPathTable
                // write tree
                val treeSize = huffmanTree.serialize.let {
                    outStream.writeInt( it.size )
                    outStream.write( it.toByteArray )
                    it.size
                }
                log( "Tree Size   : $treeSize" )
                val inputStream = object : InputStream() {
                    var i = 0
                    override fun read(): Int {
                        return if ( i == byteRead ) -1
                        else buffer[i++].toInt() and 0xff
                    }
                }
                inputStream.compressedByte(huffmanPathTable).let {
                    compressedSize += it.size + treeSize + 8
                    log( "content Size   : ${it.size}" )
                    log( "Final   : ${it.size+treeSize}" )
                    outStream.writeInt( it.size )
                    outStream.write( it  )
                }
            }
            log( "Uncompressed      : $uncompressedSize bytes" )
            log( "compressed        : $compressedSize bytes" )
            log( "Total Segments    : ${count-1}" )
            log( "Compression Rate  : ${100 - ((compressedSize*100)/uncompressedSize)} %" )
        }
    }
}


fun InputStream.decompressedBytes(
    huffmanNode : HuffmanNode,
    lastByte : Int,
    bufferSize : Int
) : ByteArray {
    var i = 0
    val buffer = ByteArray( bufferSize )
    val lastUsedBits = read()
    var node = huffmanNode
    var byte : Int
    var count = 2
    while ( read().also { byte = it } != -1 ) {
        var bit = 128
        val repeat = if ( count == lastByte ) lastUsedBits else 8
        repeat( repeat ) {
            node = if ( (byte and bit) == 0 ) {
                node.left!!
            } else node.right!!
            if (node.code != -1 ) {
                buffer[i++] = node.code.toByte()
//                buffer.add( node.code.toByte())
                node = huffmanNode
            }
            bit = bit shr 1
        }
        count++
    }
    if ( i < bufferSize )
        return buffer.copyOfRange( 0 , i )
    return buffer
}


fun inflate(
    inputFile : InputStream,
    outputFile : OutputStream ,
    log : (Any) -> Unit = {
//        println( it )
    }
) : Unit = DataInputStream(inputFile).use { inStream ->
    DataOutputStream(outputFile).use { outStream ->
        // read BufferSize
        val bufferSize = inStream.readInt()
        log( "Buffer Size : $bufferSize bytes" )
        var count = 1
        while ( inStream.available() > 0 ) {
            log( "Inflating segment : ${count++}" )
            // tree size
            val huffmanTreeSize = inStream.readInt()
            // tree content
            val huffmanTree = inStream.readNBytes(huffmanTreeSize)
                .toTypedArray().deserializeHuffmanNode!!
            val content = inStream.readInt()
            val compressedBit = inStream.readNBytes(content)
            log( "initial : ${huffmanTreeSize+1+content+4}" )
            outStream.write(
            compressedBit.inputStream().decompressedBytes(
                    huffmanTree , compressedBit.size , bufferSize
                ).also {
                    log( "final : ${it.size}" )
                }
            )
        }
    }
}



