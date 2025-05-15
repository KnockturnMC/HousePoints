group = "com.knockturnmc"
version = "4.0.1"
tasks.shadowJar { archiveClassifier = "final"; mergeServiceFiles() }
tasks.build { dependsOn(tasks.shadowJar) }

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.freefair.lombok") version "8.4"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
    withSourcesJar();
    withJavadocJar()
}

tasks.withType(Javadoc::class) {
    options.encoding = Charsets.UTF_8.name()
    (options as StandardJavadocDocletOptions).links("https://lynxplay.dev/ktp/1.19.4-R0.1-SNAPSHOT/")
}
tasks.withType(ProcessResources::class) {
    filteringCharset = Charsets.UTF_8.name()
}

tasks.processResources {
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
}

dependencies {
    // Plugin dependencies
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    //Dagger
    implementation("com.google.dagger:dagger:2.50")
    annotationProcessor("com.google.dagger:dagger-compiler:2.50")

    implementation("org.apache.commons:commons-text:1.11.0")
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

