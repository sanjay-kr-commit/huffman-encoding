package huffman.encoding

data class Node(
    val char: Char? = null,
    var freq : Int = -1,
    var left : Node? = null,
    var right : Node? = null
) {

    val serialize : String
        get() = StringBuilder()
            .apply {
                _serialiseTree_(this)
            }.toString()

    override fun toString(): String {
        return serialize
    }

    companion object {

        val String.deserializeHuffmanTree : Node
            get() {
                deserializerIndexer = 0
                return _deserializeTree_()
            }

        private var deserializerIndexer : Int = 0
        private fun String._deserializeTree_() : Node {
            if ( get(deserializerIndexer) == '1' ) {
                deserializerIndexer++
                return Node( char = get(deserializerIndexer++) )
            }
            return Node().apply {
                deserializerIndexer++
                left = _deserializeTree_()
                right = _deserializeTree_()
            }
        }

    }

    private fun Node._serialiseTree_(buffer : StringBuilder ) {
        char?.let {
            buffer.append( "1" )
            buffer.append( it )
            return
        }
        buffer.append( "0" )
        left?._serialiseTree_( buffer )
        right?._serialiseTree_( buffer )
    }

}