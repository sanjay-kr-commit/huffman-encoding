package huffman.encoding

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

val Array<Byte>.toHuffmanTable : Map<Byte,Int> get() = HashMap<Byte,Int>().apply {
    this@toHuffmanTable.forEach { byte ->
        this[byte] = this[byte]?.plus(1) ?: 1
    }
}

val Map<Byte,Int>.toHuffmanTree : Node get() {
    val queue = PriorityQueue<Node>( Comparator.comparingInt {
        node -> node.freq
    } )
    // push table to queue
    forEach { ( byte , freq ) ->
        queue.add( Node( byte , freq ) )
    }
    while ( queue.size > 1 ) {
        val node1 = queue.poll()
        val node2 = queue.poll()
        queue.add( Node().apply {
            freq = node1.freq + node2.freq
            left = node1
            right = node2
        } )
    }
    return queue.poll()
}

val Array<Byte>.toHuffmanTree : Node get() = toHuffmanTable.toHuffmanTree
val ByteArray.toHuffmanTree : Node get() = toTypedArray.toHuffmanTree

fun Node?.cacheHuffmanBitPath(
    cache : HashMap<Byte,String> = hashMapOf() ,
    path : String = ""
) : Map<Byte,String> = this?.run {
    if ( isByteInitialized ) cache[byte] = path
    else {
        left.cacheHuffmanBitPath( cache , "${path}0" )
        right.cacheHuffmanBitPath( cache , "${path}1")
    }
    cache
} ?: cache

val Array<Byte>.deflate : Array<Byte> get() {

    println( "Deflating" )
    println( "Initial Byte Count : $size" )

    val huffmanTree : Node = toHuffmanTree
    val cachedBitPath : Map<Byte,String> = huffmanTree.cacheHuffmanBitPath()
    val serializedTree : Array<Byte> = huffmanTree.serialize
    val serializedTreeSize : Int = serializedTree.size
    val deflatedBuffer = ArrayList<Byte>( size+serializedTreeSize+3 )
    val encodedBuffer = ArrayList<Byte>( size )
    var bitCount : Int = 0

    println( "Huffman Tree : ${serializedTree.toList()}" )
    println( "Byte Tree Path :$cachedBitPath" )

    // largest tree can be serialized is 255*255+254 i.e. 65279
    if ( serializedTreeSize > 65279 ) throw Exception( """
        The Serialized Tree Is Too Big To Write
        Expected : size < 65280
        Given : $serializedTreeSize
    """.trimIndent() )

    // in order to store how big is tree we need to add its length to the start of the tree
    // 255 mean all 8 bits set to 1 , unsigned byte max value
    deflatedBuffer.add( (serializedTreeSize/255).toByte() )
    deflatedBuffer.add( (serializedTreeSize%255).toByte() )

    // write tree to buffer
    deflatedBuffer.addAll( serializedTree )

    // encode bytes
    var encodedByte : Int = 0
    forEach { byte ->
        cachedBitPath[byte]?.forEach { direction ->
            encodedByte = encodedByte shl 1
            encodedByte = direction-'0' or encodedByte
            bitCount++
            if ( bitCount == 8 ) {
                encodedBuffer.add( encodedByte.toByte() )
                encodedByte = 0
                bitCount = 0
            }
        }
    }
    if ( bitCount > 0 && encodedByte > 0 ){
        encodedByte = encodedByte shl (8-bitCount)
        encodedBuffer.add( encodedByte.toByte() )
    }

    // write incomplete byte count
    deflatedBuffer.add( (bitCount%8).toByte() )
    // write encoded Buffer
    deflatedBuffer.addAll( encodedBuffer )

    println( "Final Byte Count : ${deflatedBuffer.size}" )

    return deflatedBuffer.toTypedArray()
}

val ByteArray.deflate : ByteArray
    get() = toTypedArray.deflate.toByteArray

val Array<Byte>.inflate : Array<Byte> get() {

    println( "Inflating" )
    println( "Initial Byte Count : $size" )

    var index = 0
    val serializedTreeSize = 255*this[index++].toInt() + this[index++].toInt()
    val deserializedTree = Arrays.copyOfRange( this , index , index+serializedTreeSize )
        .also {
            println( "Huffman Tree : ${it.toList()}" )
        }
        .deserialize
    index += serializedTreeSize
    val incompleteBit : Int = this[index++].toInt()
    val inflatedBuffer = ArrayList<Byte>( size )
    val reverseBits : (Int) -> Int = {
        var reversed = 0
        var copy = it
        repeat( 8 ) {
            reversed = reversed shl 1
            reversed = reversed or ( copy and 1)
            copy = copy shr 1
        }
        reversed
    }

    var node = deserializedTree
    for ( i in index until size-1 ) {
       var encodedPath = reverseBits( this[i].toInt() )
        repeat( 8 ) {
            val direction = encodedPath and 1
            encodedPath = encodedPath shr 1
            node = if ( direction == 1 ) node.right!! else node.left!!
            if ( node.isByteInitialized ) {
                inflatedBuffer.add( node.byte )
                node = deserializedTree
            }
        }
    }

    var encodedPath = reverseBits( this[size-1].toInt() )
    repeat( if ( incompleteBit == 0 ) 8 else incompleteBit ) {
        val direction = encodedPath and 1
        encodedPath = encodedPath shr 1
        node = if ( direction == 1 ) node.right!! else node.left!!
        if ( node.isByteInitialized ) {
            inflatedBuffer.add( node.byte )
            node = deserializedTree
        }
    }

    println( "Final Byte Count : ${inflatedBuffer.size}" )

    return inflatedBuffer.toTypedArray()
}

val ByteArray.inflate : ByteArray
    get() = toTypedArray.inflate.toByteArray