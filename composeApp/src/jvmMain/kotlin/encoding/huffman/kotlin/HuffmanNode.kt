package encoding.huffman.kotlin


data class HuffmanNode(
    var left: HuffmanNode? = null ,
    var right: HuffmanNode? = null,
    var code: Int = -1,
    var freq: Int = 0,
) {

    override fun toString(): String {
        return "code:$code left:{$left} right:{$right}"
    }

    private fun ArrayList<Byte>.deflatePath() : ArrayList<Byte> = ArrayList<Byte>().apply {
        var count = 0
        var i = 0
        val one = 1.toByte()
        while ( i < this@deflatePath.size ) {
            if ( this@deflatePath[i] == one ) {
                add( count.toByte() )
                i++
                add( this@deflatePath[i] )
                count = -1
            }
            i++
            count++
        }
    }

    val serialize: Array<Byte>
        get() =
            ArrayList<Byte>()
                .apply {
                    serializeNode(this)
                }
                .deflatePath()
                .toTypedArray()

    private fun HuffmanNode.serializeNode(serializedNode: ArrayList<Byte>) {
        if (code != -1) {
            serializedNode.add(1)
            serializedNode.add( (code-128).toByte())
            return
        }
        serializedNode.add(0)
        left?.serializeNode(serializedNode)
        right?.serializeNode(serializedNode)
    }

    companion object {

        private  data class Counter( var counter : Int = 0 )
        private fun deserializeNode( serializedByteStream : ArrayList<Byte> , counter : Counter ) : HuffmanNode {
            if ( serializedByteStream[counter.counter++] == 1.toByte() ) return HuffmanNode( code = (serializedByteStream[counter.counter++]+128))
            return HuffmanNode().apply {
                left = deserializeNode( serializedByteStream , counter )
                right = deserializeNode( serializedByteStream , counter )
            }
        }

        private fun inflatePath( deflatedTreePathStream : Array<Byte> ) : ArrayList<Byte> = ArrayList<Byte>().apply {
            for ( i in 0 until deflatedTreePathStream.size ) {
                if ( i%2 == 0 ) {
                    repeat( deflatedTreePathStream[i].toInt() ) {
                        add(0)
                    }
                    add(1)
                } else add( deflatedTreePathStream[i] )
            }
        }

        fun deserializeNode( serializedByteStream : Array<Byte> ) : HuffmanNode?
        = deserializeNode(
            inflatePath(serializedByteStream)
            , Counter() )

    }

}

val Array<Byte>.deserializeHuffmanNode: HuffmanNode?
    get() = HuffmanNode.deserializeNode( this )