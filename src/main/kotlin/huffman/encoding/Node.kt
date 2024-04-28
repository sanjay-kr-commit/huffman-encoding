package huffman.encoding

data class Node(
    val char: Char? = null,
    var freq : Int = -1,
    var left : Node? = null,
    var right : Node? = null
) {

    val nodeCount : Int
        get() = _nodeCount_()

    private fun _nodeCount_(node : Node? = this, level : Int = 0 ) : Int =
        node?.let {
            var current = 1
            repeat( level ) { current *= 2 }
            val left = _nodeCount_( node.left , level+1 )
            val right = _nodeCount_( node.right , level+1 )
            current + if ( left > right ) left else right
        } ?: 0

    val toHeap : Array<Char?>
        get() = _toHeap_( this).trimNull()

    private fun Array<Char?>.trimNull() : Array<Char?> {
        var i = size-1
        while ( i > 0 && this[i] == null ) i--
        val array = Array<Char?>(i+1) { null }
        for ( j in 0 .. i ) array[j] = this[j]
        return array
    }

    private fun _toHeap_(
        node : Node? = this,
        heap: Array<Char?> = Array( _nodeCount_() ) { null },
        level : Int = 0
    ) : Array<Char?> {
        if ( node == null ) return heap
        heap[level] = node.char
        _toHeap_( node.left , heap , (level*2)+1 )
        return _toHeap_( node.right , heap, (level*2)+2 )
    }

    override fun toString(): String {
        return _toHeap_()
            ._encodeHeapString_()
    }

    companion object {

        private fun Array<Char?>._encodeHeapString_() : String =
            StringBuffer().apply {
                this@_encodeHeapString_.forEach {
                    append(
                        it ?: 0.toChar()
                    )
                }
            }.toString()

        fun compressedString( heapBuffer : Array<Char?> ) = heapBuffer._encodeHeapString_()

        private fun Array<Char?>._decodeHeapString_(
            index : Int = 0
        ) : Node? {
            if ( index >= size ) return null
            return Node(
                char = this[index]
            ).apply {
                left = this@_decodeHeapString_._decodeHeapString_( index * 2 + 1 )
                right = this@_decodeHeapString_._decodeHeapString_( index * 2 + 2 )
            }
        }

        fun String.toHuffmanTree() : Node {
            val charArray = Array<Char?>(this.length) { null }
            for ( i in indices) charArray[i] = if ( this[i] == 0.toChar() ) null else this[i]
            return charArray._decodeHeapString_()!!
        }

    }

}