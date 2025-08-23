// Gradle script to build the "docs" subproject of LbjExamples

plugins {
    java // for sourceSets
}

sourceSets.main {
    resources {
        srcDir("en") // for NetBeans access
    }
}
