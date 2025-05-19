// Gradle script to build the "kotlin-apps" subproject of LbjExamples

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    application // to build JVM applications
    alias(libs.plugins.kotlin.jvm) // to compile Kotlin
}

application {
    mainClass = "com.github.stephengold.lbjexamples.ktapps.console.HelloLibbulletjmeKt"
}
tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}

val fs = System.getProperty("file.separator")

// Register tasks to run specific applications:

// physics console apps (no graphics)
tasks.register<JavaExec>("HelloLibbulletjme") {
    description = "Runs the Kotlin version of the HelloLibbulletjme console app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.console.HelloLibbulletjmeKt"
}
tasks.register<JavaExec>("HelloVehicle0") {
    description = "Runs the Kotlin version of the HelloVehicle0 console app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.console.HelloVehicle0Kt"
}

// physics tutorial apps (very simple)
tasks.register<JavaExec>("HelloCcd") {
    description = "Runs the Kotlin version of the HelloCcd tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloCcdKt"
}
tasks.register<JavaExec>("HelloCharacter") {
    description = "Runs the Kotlin version of the HelloCharacter tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloCharacterKt"
}
tasks.register<JavaExec>("HelloCloth") {
    description = "Runs the Kotlin version of the HelloCloth tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloClothKt"
}
tasks.register<JavaExec>("HelloClothRigid") {
    description = "Runs the Kotlin version of the HelloClothRigid tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloClothRigidKt"
}
tasks.register<JavaExec>("HelloContactResponse") {
    description = "Runs the Kotlin version of the HelloContactResponse tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloContactResponseKt"
}
tasks.register<JavaExec>("HelloDamping") {
    description = "Runs the Kotlin version of the HelloDamping tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloDampingKt"
}
tasks.register<JavaExec>("HelloDeactivation") {
    description = "Runs the Kotlin version of the HelloDeactivation tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloDeactivationKt"
}
tasks.register<JavaExec>("HelloDoubleEnded") {
    description = "Runs the Kotlin version of the HelloDoubleEnded tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloDoubleEndedKt"
}
tasks.register<JavaExec>("HelloGhost") {
    description = "Runs the Kotlin version of the HelloGhost tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloGhostKt"
}
tasks.register<JavaExec>("HelloJoint") {
    description = "Runs the Kotlin version of the HelloJoint tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloJointKt"
}
tasks.register<JavaExec>("HelloKinematics") {
    description = "Runs the Kotlin version of the HelloKinematics tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloKinematicsKt"
}
tasks.register<JavaExec>("HelloLimit") {
    description = "Runs the Kotlin version of the HelloLimit tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloLimitKt"
}
tasks.register<JavaExec>("HelloMotor") {
    description = "Runs the Kotlin version of the HelloMotor tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloMotorKt"
}
tasks.register<JavaExec>("HelloNonUniformGravity") {
    description = "Runs the Kotlin version of the HelloNonUniformGravity tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloNonUniformGravityKt"
}
tasks.register<JavaExec>("HelloMadMallet") {
    description = "Runs the Kotlin version of the HelloMadMallet tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloMadMalletKt"
}
tasks.register<JavaExec>("HelloMassDistribution") {
    description = "Runs the Kotlin version of the HelloMassDistribution tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloMassDistributionKt"
}
tasks.register<JavaExec>("HelloPin") {
    description = "Runs the Kotlin version of the HelloPin tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloPinKt"
}
tasks.register<JavaExec>("HelloRigidBody") {
    description = "Runs the Kotlin version of the HelloRigidBody tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloRigidBodyKt"
}
tasks.register<JavaExec>("HelloServo") {
    description = "Runs the Kotlin version of the HelloServo tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloServoKt"
}
tasks.register<JavaExec>("HelloSoftBody") {
    description = "Runs the Kotlin version of the HelloSoftBody tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloSoftBodyKt"
}
tasks.register<JavaExec>("HelloSoftRope") {
    description = "Runs the Kotlin version of the HelloSoftRope tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloSoftRopeKt"
}
tasks.register<JavaExec>("HelloSoftSoft") {
    description = "Runs the Kotlin version of the HelloSoftSoft tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloSoftSoftKt"
}
tasks.register<JavaExec>("HelloSport") {
    description = "Runs the Kotlin version of the HelloSport tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloSportKt"
}
tasks.register<JavaExec>("HelloSpring") {
    description = "Runs the Kotlin version of the HelloSpring tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloSpringKt"
}
tasks.register<JavaExec>("HelloStaticBody") {
    description = "Runs the Kotlin version of the HelloStaticBody tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloStaticBodyKt"
}
tasks.register<JavaExec>("HelloVehicle") {
    description = "Runs the Kotlin version of the HelloVehicle tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloVehicleKt"
}
tasks.register<JavaExec>("HelloWalk") {
    description = "Runs the Kotlin version of the HelloWalk tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.ktapps.HelloWalkKt"
}

val os = DefaultNativePlatform.getCurrentOperatingSystem()
val includeLinux = os.isLinux()
val includeMacOsX = os.isMacOsX()
val includeWindows = os.isWindows()

tasks.withType<JavaExec>().all { // JVM runtime options:
    if (os.isMacOsX()) {
        jvmArgs("-XstartOnFirstThread") // required for GLFW on macOS
    }
    classpath = sourceSets.main.get().getRuntimeClasspath()
    enableAssertions = true
    jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=10")
}

// which BFT (build flavor + type) of native physics libraries to include:
val bft = providers.gradleProperty("bft").get()

dependencies {
    if (includeLinux) {
        runtimeOnly(variantOf(libs.libbulletjme.linux64){classifier(bft)})
        runtimeOnly(variantOf(libs.lwjgl){classifier("natives-linux")})
        runtimeOnly(variantOf(libs.lwjgl.assimp){classifier("natives-linux")})
        runtimeOnly(variantOf(libs.lwjgl.glfw){classifier("natives-linux")})
        runtimeOnly(variantOf(libs.lwjgl.opengl){classifier("natives-linux")})

        runtimeOnly(variantOf(libs.libbulletjme.linuxarm32hf){classifier(bft)})
        runtimeOnly(variantOf(libs.lwjgl){classifier("natives-linux-arm32")})
        runtimeOnly(variantOf(libs.lwjgl.assimp){classifier("natives-linux-arm32")})
        runtimeOnly(variantOf(libs.lwjgl.glfw){classifier("natives-linux-arm32")})
        runtimeOnly(variantOf(libs.lwjgl.opengl){classifier("natives-linux-arm32")})

        runtimeOnly(variantOf(libs.libbulletjme.linuxarm64){classifier(bft)})
        runtimeOnly(variantOf(libs.lwjgl){classifier("natives-linux-arm64")})
        runtimeOnly(variantOf(libs.lwjgl.assimp){classifier("natives-linux-arm64")})
        runtimeOnly(variantOf(libs.lwjgl.glfw){classifier("natives-linux-arm64")})
        runtimeOnly(variantOf(libs.lwjgl.opengl){classifier("natives-linux-arm64")})
    }

    if (includeMacOsX) {
        runtimeOnly(variantOf(libs.libbulletjme.macosx64){classifier(bft)})
        runtimeOnly(variantOf(libs.lwjgl){classifier("natives-macos")})
        runtimeOnly(variantOf(libs.lwjgl.assimp){classifier("natives-macos")})
        runtimeOnly(variantOf(libs.lwjgl.glfw){classifier("natives-macos")})
        runtimeOnly(variantOf(libs.lwjgl.opengl){classifier("natives-macos")})

        runtimeOnly(variantOf(libs.libbulletjme.macosxarm64){classifier(bft)})
        runtimeOnly(variantOf(libs.lwjgl){classifier("natives-macos-arm64")})
        runtimeOnly(variantOf(libs.lwjgl.assimp){classifier("natives-macos-arm64")})
        runtimeOnly(variantOf(libs.lwjgl.glfw){classifier("natives-macos-arm64")})
        runtimeOnly(variantOf(libs.lwjgl.opengl){classifier("natives-macos-arm64")})
    }

    if (includeWindows) {
        runtimeOnly(variantOf(libs.libbulletjme.windows64){classifier(bft)})
        runtimeOnly(variantOf(libs.lwjgl){classifier("natives-windows")})
        runtimeOnly(variantOf(libs.lwjgl.assimp){classifier("natives-windows")})
        runtimeOnly(variantOf(libs.lwjgl.glfw){classifier("natives-windows")})
        runtimeOnly(variantOf(libs.lwjgl.opengl){classifier("natives-windows")})
    }

    implementation(libs.jsnaploader)
    implementation(libs.sport)
}
