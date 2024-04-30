package huffman.encoding

val ByteArray.toTypedArray : Array<Byte> get() = Array( size ) { 0.toByte() }.apply {
    for ( index in this@toTypedArray.indices ) this[index] = this@toTypedArray[index]
}

val Array<Byte>.toByteArray : ByteArray get() = ByteArray( size ).apply {
    for ( index in this@toByteArray.indices ) this[index] = this@toByteArray[index]
}