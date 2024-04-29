package huffman.encoding

import huffman.encoding.Node.Companion.deserializeHuffmanTree
import java.util.*

val String.buildCharacterTable : Map<Char,Int>
    get() {
        return HashMap<Char,Int>().apply {
            this@buildCharacterTable.forEach {
                this[it] = this[it]?.plus(1) ?: 1
            }
        }
    }

val Map<Char,Int>.buildHuffmanTree : Node
    get() {
        val queue = PriorityQueue<Node>(
            Comparator.comparingInt { i ->
                i.freq
            }
        )
        forEach { (char, count) ->
            queue.add(Node(char,count))
        }
        while (queue.size > 1 ) {
            val left = queue.poll()
            val right = queue.poll()
            queue.add(
                Node(
                freq = left.freq + right.freq,
                left = left ,
                right = right
            )
            )
        }
        return queue.poll()
    }

fun Node?.huffmanCharPath(path : String = "", map : HashMap<Char,String> = hashMapOf() ) : Map<Char,String> {
    if ( this == null ) return map
    char?.let {
        map[char] = path
    }
    left.huffmanCharPath( path + "0" , map )
    return right.huffmanCharPath( path + "1" , map )
}

fun String.encodeData() : String = StringBuilder().let { encodedBuffer ->

    println( "Document Char Count : $length" )

    val huffmanTree = buildCharacterTable
        .also {
            println( "huffman table : $it" )
        }
        .buildHuffmanTree
        .also {
            println( "Huffman Tree $it" )
        }

    huffmanTree
        .serialize
        .let {
            var logBaseSize = 0
            var sizeCopy = it.length
            while ( sizeCopy > 0 ) {
                logBaseSize++
                sizeCopy /= 10
            }
            // append int size
            encodedBuffer.append( logBaseSize )
            // append heap size
            encodedBuffer.append( it.length )
            // append huffman heap
            encodedBuffer.append( it )
        }

    StringBuilder().let { encodedData ->
        var bitCount = 0
        val huffmanCharPath = huffmanTree
            .huffmanCharPath()

        println( "Huffman Encoded Char Path : $huffmanCharPath" )

        var c = 0
        this.forEach { char ->
            huffmanCharPath[char]?.forEach {
                c = c shl 1
                c = c or it-'0'
                bitCount++
                if ( bitCount == 8 ) {
                    encodedData.append( c.toChar() )
                    c = 0
                    bitCount = 0
                }
            }
        }

        if ( bitCount > 0 ) {
           c = c shl (8-bitCount)
            encodedData.append( c.toChar() )
        }

        // append trash bit
        encodedBuffer.append( (8-bitCount)%8 )
        // append compressed data
        encodedBuffer.append( encodedData )

    }

    println( "Encoded Char Count : ${encodedBuffer.length}" )

    encodedBuffer
}.toString()

fun String.decodeData() : String {
    var index = 0
    var logBaseSize = this[index++] - '0'
    var heapSize = 0
    while ( logBaseSize > 0 ) {
        heapSize *= 10
        heapSize += this[index++] - '0'
        logBaseSize--
    }
    val heap = substring( index , index+heapSize )
    val trashBit = this[ index+heapSize ] - '0'
    val encodedData = substring( index+heapSize+1 )
    val huffmanTree = heap.deserializeHuffmanTree
    val decodedBuffer = StringBuilder()

    val reverseBitOrder : (Int) -> Int = { it ->
        var original = it
        var reversed = 0
        repeat( 8 ) {
            reversed = reversed shl 1
            reversed = reversed or (original and 1)
            original = original shr 1
        }
        reversed
    }

    var node : Node? = huffmanTree
    for ( i in 0 until encodedData.length-1 ) {
        var reversedBit = reverseBitOrder( encodedData[i] - 0.toChar() )
        repeat( 8 ) {
            val path = reversedBit and 1
            reversedBit = reversedBit shr 1
            node = if ( path == 1 ) node!!.right else node!!.left
            if ( node!!.char != null ) {
                decodedBuffer.append( node!!.char )
                node = huffmanTree
            }
        }
    }

    var reversedBit = reverseBitOrder( encodedData[encodedData.length-1] - 0.toChar() )
    repeat(
        8-trashBit
    ) {
        val path = reversedBit and 1
        reversedBit = reversedBit shr 1
        node = if ( path == 1 ) node!!.right else node!!.left
        if ( node!!.char != null ) {
            decodedBuffer.append( node!!.char )
            node = huffmanTree
        }
    }

    return decodedBuffer.toString()
}
