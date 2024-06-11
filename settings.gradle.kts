// global build settings for the LbjExamples project

rootProject.name = "LbjExamples"

dependencyResolutionManagement {
    repositories {
        //mavenLocal() // to find libraries installed locally
        mavenCentral() // to find libraries released to the Maven Central repository
        //maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") } // to find public snapshots of LWJGL
        //maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") } // to find libraries staged but not yet released
        //maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") } // to find public snapshots of libraries
    }
}

// subprojects:
include("apps")
include("docs")
include("kotlin-apps")
