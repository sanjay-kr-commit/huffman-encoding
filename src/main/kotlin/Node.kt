package huffman.encoding

import kotlin.properties.Delegates

class Node {

    var byte by Delegates.notNull<Byte>()
    var freq : Int = 0
    var left : Node? = null
    var right : Node? = null

    val isByteInitialized : Boolean get() = try {
        byte.toInt()
        true
    } catch ( _: Exception ) { false }

    val serialize : Array<Byte>
        get() = ArrayList<Byte>().apply {
            serializeNode( this )
        }.toTypedArray()

    private fun Node.serializeNode(
        serializedNode : ArrayList<Byte>
    ) {
        if ( isByteInitialized ) {
            serializedNode.add( 1 )
            serializedNode.add( byte )
            return
        }
        serializedNode.add( 0 )
        left?.serializeNode(serializedNode )
        right?.serializeNode(serializedNode )
    }

    constructor ()
    constructor (byte : Byte) {
        this.byte = byte
    }
    constructor (byte : Byte , freq : Int) {
        this.byte = byte
        this.freq = freq
    }

}