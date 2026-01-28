plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.3.0"
    id("io.freefair.lombok") version "9.2.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "com.knockturnmc"
version = "5.0.1"
tasks.shadowJar { archiveClassifier = "final"; mergeServiceFiles() }
tasks.build { dependsOn(tasks.shadowJar) }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar();
    withJavadocJar()
}

tasks.withType(ProcessResources::class) {
    filteringCharset = Charsets.UTF_8.name()
    val expansion = mapOf(
        "version" to project.version
    )

    inputs.properties(expansion)
    filesMatching("plugin.yml") { expand(expansion) }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
    mavenLocal()
    maven("https://repo.knockturnmc.com/content/repositories/knockturn-public/") {
        name = "knockturnPublic"
    }
}

dependencies {
    // Plugin dependencies
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    //Lombok
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")


    //Dagger
    implementation("com.google.dagger:dagger:2.59")
    annotationProcessor("com.google.dagger:dagger-compiler:2.59")

    implementation("org.apache.commons:commons-text:1.15.0")
}

tasks.runServer {
    val testServerDirectory = file(properties["knockturn.testserver"].toString());
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
    runDirectory = testServerDirectory

    minecraftVersion("1.21.11")
    serverJar(testServerDirectory.resolve("server.jar"))
}

publishing {
    repositories {
        maven("https://repo.knockturnmc.com/content/repositories/knockturn-public-release/") {
            name = "knockturnPublic"
            credentials(PasswordCredentials::class)
        }
    }

    publications.create<MavenPublication>("maven") {
        artifactId = "housepoints"
        from(components["java"])
    }
}

