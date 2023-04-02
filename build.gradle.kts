plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

apply(plugin = "java")
group="com.knockturnmc"
version = "4.0.0-SNAPSHOT"
tasks.shadowJar { archiveClassifier.set("final"); mergeServiceFiles() }

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType(Javadoc::class) {
    options.encoding = Charsets.UTF_8.name()
    (options as StandardJavadocDocletOptions).links("https://lynxplay.dev/ktp/1.19.4-R0.1-SNAPSHOT/")
}
tasks.withType(ProcessResources::class) {
    filteringCharset = Charsets.UTF_8.name()
}

repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
    mavenCentral()
    mavenLocal()
    maven("https://repo.knockturnmc.com/content/repositories/knockturn-public/") {
        name = "knockturnPublic"
    }
}

dependencies {
    // Plugin dependencies
    compileOnly("dev.lynxplay.ktp:ktp-api:1.19.4-R0.1-SNAPSHOT")

    //Lombok
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    //Dagger
    implementation("com.google.dagger:dagger:2.39.1")
    annotationProcessor("com.google.dagger:dagger-compiler:2.39.1")

    implementation("org.apache.commons:commons-text:1.10.0")


}

publishing {
    repositories {
        maven("https://repo.knockturnmc.com/content/repositories/knockturn-public/") {
            name="knockturnPublic"
            credentials(PasswordCredentials::class)
        }
    }

    publications.create<MavenPublication>("maven") {
        artifactId = "housepoints"
        from(components["java"])

    }
}

