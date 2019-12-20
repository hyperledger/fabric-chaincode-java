/*
 * SPDX-License-Identifier: Apache-2.0
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("com.github.johnrengelman.shadow") version "2.0.3"
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
}



version = "0.0.1"

java {
 sourceCompatibility = JavaVersion.VERSION_1_8
}


dependencies {
    implementation("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:1.4.4")
    implementation("org.json:json:20180813")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
           
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven {
        setUrl("https://jitpack.io")
    }
    maven {
        setUrl("https://hyperledger.jfrog.io/hyperledger/fabric-maven")
    }
}

tasks {
    "shadowJar"(ShadowJar::class) {
        baseName = "chaincode"
        version = null
        classifier = null
        manifest {
            attributes(mapOf("Main-Class" to "org.hyperledger.fabric.contract.ContractRouter"))
        }
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
