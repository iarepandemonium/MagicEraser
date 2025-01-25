plugins {
    java
    `jvm-test-suite`
    id("io.freefair.lombok") version "8.12"
}

group = "net.pandette"
version = "1.0.0-SNAPSHOT"

allprojects {
    tasks {
        withType<JavaCompile> { options.encoding = Charsets.UTF_8.name() }
        withType<Javadoc> { options.encoding = Charsets.UTF_8.name() }
        withType<ProcessResources> { filteringCharset = Charsets.UTF_8.name() }
        test { useJUnitPlatform() }
        withType<Jar> {
            manifest.attributes(
                "Main-Class" to "net.pandette.MagicEraser",
                "Class-Path" to configurations.runtimeClasspath.map {
                    it.joinToString(" ") { f ->
                        "${project.properties["sirenhunts.libpath"] ?: "libs"}/${f.name}"
                    }
                }
            )
        }
        register<Sync>("copyDependencies") {
            from(configurations.runtimeClasspath)
            into(project.layout.buildDirectory.file("libs/dependencies"))
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.2.2")
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
}