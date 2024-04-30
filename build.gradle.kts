plugins {
    kotlin("jvm") version "1.9.23"
}

group = "huffman.encoding"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Jar> {

    manifest {
        attributes["Main-Class"] = "huffman.encoding.MainKt"
    }

}
