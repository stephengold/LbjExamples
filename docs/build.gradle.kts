// Gradle script to build the "docs" subproject of LbjExamples

plugins {
    `java`
}

sourceSets.main {
    java {
        srcDir("en") // for IDE access (no Java there)
    }
}
