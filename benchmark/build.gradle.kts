/*
 * This file was generated by the Gradle 'init' task.
 *
 * This is a general purpose Gradle build.
 * Learn more about Gradle by exploring our samples at https://docs.gradle.org/8.0.2/samples
 * This project uses @Incubating APIs which are subject to change.
 */
plugins {
    id("java")
}

repositories {
    mavenCentral()
    mavenLocal()
}
tasks.withType(JavaCompile::class.java) { options.compilerArgs.add("--enable-preview")}

tasks.withType(JavaExec::class.java) {jvmArgs!!.add("--enable-preview")}