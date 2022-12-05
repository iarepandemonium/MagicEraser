plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"
tasks.shadowJar { archiveClassifier.set("final"); mergeServiceFiles() }

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("net.dv8tion:JDA:5.0.0-alpha.22")
    implementation("com.google.code.gson:gson:2.8.9")
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}