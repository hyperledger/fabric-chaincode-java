import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("java")
}

group = "org.hyperledger.fabric-chaincode-java"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compile("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.0.0-SNAPSHOT")
    testCompile("junit:junit:4.12")
}

tasks {
    named<ShadowJar>("shadowJar") {
        baseName = "chaincode"
        version = null
        classifier = null
        manifest {
            attributes(mapOf("Main-Class" to "org.hyperledger.fabric.example.SimpleAsset"))
        }
    }
}