plugins {
    java
    `maven-publish`
}

apply(plugin = "java")
group="com.knockturnmc"
version = "4.0.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType(Javadoc::class) {
    options.encoding = Charsets.UTF_8.name()
    (options as StandardJavadocDocletOptions).links("https://lynxplay.dev/ktp/1.17.1-R0.1-SNAPSHOT/")
}

tasks.withType(ProcessResources::class) {
    filteringCharset = Charsets.UTF_8.name()
}

repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Plugin dependencies
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")

    //Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")

    //Dagger
    implementation("com.google.dagger:dagger:2.39.1")
    annotationProcessor("com.google.dagger:dagger-compiler:2.39.1")

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

