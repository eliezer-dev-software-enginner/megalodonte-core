plugins { `java-library`; `maven-publish` }
group = "megalodonte"
version = "1.0.0-beta"
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
    withSourcesJar()
}
tasks.withType<JavaCompile>().configureEach { options.release.set(17); options.encoding = "UTF-8" }
sourceSets.test {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
tasks.register<JavaExec>("contractTest") {
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("megalodonte.contracts.CoreContractTest")
}
tasks.test {
    dependsOn("contractTest")
    // Gradle 9 fails when this lifecycle task has no JUnit tests. The property
    // does not exist in Gradle 8.13, used by AGP 8.12, so configure it only
    // when the running Gradle exposes it.
    javaClass.methods.firstOrNull {
        it.name == "getFailOnNoDiscoveredTests" && it.parameterCount == 0
    }?.invoke(this)?.let { property ->
        @Suppress("UNCHECKED_CAST")
        (property as org.gradle.api.provider.Property<Boolean>).set(false)
    }
}
tasks.check { dependsOn("contractTest") }
val verifyBoundary = tasks.register("verifyPortableBoundary") {
    inputs.files(sourceSets.main.get().allJava)
    dependsOn(tasks.compileJava)
    doLast {
        sourceSets.main.get().allJava.forEach { file ->
            check(!Regex("(?m)^\\s*(import|requires)\\s+(transitive\\s+)?(javafx|android|java\\.awt|java\\.desktop|org\\.slf4j)[.;]").containsMatchIn(file.readText())) {
                "Platform dependency in shared core: $file"
            }
        }
        sourceSets.main.get().output.classesDirs.asFileTree.matching { include("**/*.class") }.forEach { file ->
            val bytes = file.readBytes()
            val major = ((bytes[6].toInt() and 255) shl 8) or (bytes[7].toInt() and 255)
            check(major <= 61) { "Shared core bytecode exceeds Java 17: $file" }
        }
    }
}
tasks.check { dependsOn(verifyBoundary) }
publishing { publications { create<MavenPublication>("mavenJava") { from(components["java"]) } } }
