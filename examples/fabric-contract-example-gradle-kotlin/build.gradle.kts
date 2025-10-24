/*
 * SPDX-License-Identifier: Apache-2.0
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("com.gradleup.shadow") version "9.2.2"
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
}



version = "0.0.1"

dependencies {
    implementation("org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:2.5.7")
    implementation("org.json:json:20250517")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
           
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

tasks {
    "shadowJar"(ShadowJar::class) {
        archiveBaseName = "chaincode"
        archiveVersion = ""
        archiveClassifier = ""
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
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
