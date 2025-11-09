plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "net.pandette"
version = "1.0-SNAPSHOT"
tasks.shadowJar { archiveClassifier.set("final"); mergeServiceFiles() }

repositories {
    mavenCentral()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.pandette.MagicEraser"
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("net.dv8tion:JDA:6.1.1")
    implementation("com.google.code.gson:gson:2.8.9")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}