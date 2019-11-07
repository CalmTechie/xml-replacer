import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

group = "ru.korolev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("commons-cli:commons-cli:1.4")
    implementation("org.jdom:jdom2:2.0.6")
    

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}