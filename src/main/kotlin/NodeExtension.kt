package huffman.encoding

private var deserializerIndex : Int = 0

private fun Array<Byte>.deserialize() : Node {
    if ( get(deserializerIndex++) == 1.toByte() ) return Node( get(deserializerIndex++) )
    return Node().apply {
        left = deserialize()
        right = deserialize()
    }
}

val Array<Byte>.deserialize : Node
get() {
    deserializerIndex = 0
    return deserialize()
}