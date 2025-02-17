plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
application {
    mainClass.set("SearchApp")
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
dependencies {
    // Apache Lucene Core
    implementation("org.apache.lucene:lucene-core:8.11.4")

    // Lucene Common Analyzers
    implementation("org.apache.lucene:lucene-analyzers-common:8.11.4")

    // Lucene Query Parser
    implementation("org.apache.lucene:lucene-queryparser:8.11.4")

    implementation("org.apache.lucene:lucene-suggest:8.11.4")

    // Jackson Databind для работы с JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")

    implementation("org.apache.commons:commons-text:1.13.0")

}


tasks.test {
    useJUnitPlatform()
}