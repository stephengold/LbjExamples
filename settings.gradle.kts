// global build settings shared by all LbjExamples subprojects

rootProject.name = "LbjExamples"

dependencyResolutionManagement {
    repositories {
        //mavenLocal() // to find libraries installed locally
        mavenCentral() // to find libraries released to the Maven Central repository
        maven {
            name = "Central Portal Snapshots"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

// subprojects:
include("apps")
include("docs")
include("kotlin-apps")
