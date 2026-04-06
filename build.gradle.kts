plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.backend"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Source: https://mvnrepository.com/artifact/info.picocli/picocli
    implementation("info.picocli:picocli:4.7.6")
    // Source: https://mvnrepository.com/artifact/com.github.javaparser/javaparser-core
    implementation("com.github.javaparser:javaparser-core:3.28.0")
    // Source: https://mvnrepository.com/artifact/org.apache.lucene/lucene-core
    implementation("org.apache.lucene:lucene-core:10.4.0")
    // AI Layer
    implementation(platform("ai.djl:bom:0.36.0"))
    // Source: https://mvnrepository.com/artifact/ai.djl/api
    implementation("ai.djl:api")
    // Source: https://mvnrepository.com/artifact/ai.djl.onnxruntime/onnxruntime-engine
    implementation("ai.djl.onnxruntime:onnxruntime-engine")
    // Source: https://mvnrepository.com/artifact/ai.djl.huggingface/tokenizers
    implementation("ai.djl.huggingface:tokenizers")
    // MANIFEST configuration
    implementation("junit:junit:3.8.2")
}
tasks.test {
    useJUnitPlatform()
}

/*
* JAR configurations to avoid overwriting of manifests.
* */
tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveBaseName.set("cbase")
    archiveVersion.set("1.0.0v")
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "com.backend.CBase"
    }
}