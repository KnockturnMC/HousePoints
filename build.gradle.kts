plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.freefair.lombok") version "8.4"
}

group="com.knockturnmc"
version = "5.0.1-SNAPSHOT"
tasks.shadowJar { archiveClassifier = "final"; mergeServiceFiles() }
tasks.build { dependsOn(tasks.shadowJar) }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
    withSourcesJar();
    withJavadocJar()
}

tasks.withType(ProcessResources::class) {
    filteringCharset = Charsets.UTF_8.name()
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
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    //Lombok
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")


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

