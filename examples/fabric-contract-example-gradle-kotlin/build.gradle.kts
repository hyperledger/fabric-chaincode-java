/*
 * SPDX-License-Identifier: Apache-2.0
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
}



version = "0.0.1"

dependencies {
    implementation("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.5.2")
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
           
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
    maven {
        setUrl("https://hyperledger.jfrog.io/hyperledger/fabric-maven")
    }
}

tasks {
    "shadowJar"(ShadowJar::class) {
        archiveBaseName = "chaincode"
        archiveVersion = ""
        archiveClassifier = ""
        mergeServiceFiles()
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
