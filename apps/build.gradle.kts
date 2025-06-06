// Gradle script to build and run the "apps" subproject of LbjExamples

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    application // to build JVM applications
    checkstyle  // to analyze Java sourcecode for style violations
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
}

val javaVersion = JavaVersion.current()

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass = "com.github.stephengold.lbjexamples.AppChooser"
}
tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}

tasks.withType<JavaCompile>().all { // Java compile-time options:
    options.compilerArgs.add("-Xdiags:verbose")
    if (javaVersion.isCompatibleWith(JavaVersion.VERSION_20)) {
        // Suppress warnings that source value 8 is obsolete.
        options.compilerArgs.add("-Xlint:-options")
    }
    options.compilerArgs.add("-Xlint:unchecked")
    options.encoding = "UTF-8"
    options.isDeprecation = true // to provide detailed deprecation warnings
    if (javaVersion.isCompatibleWith(JavaVersion.VERSION_1_10)) {
        options.release = 8
    }
}

val fs = System.getProperty("file.separator")

// Register tasks to run specific applications:

tasks.register<JavaExec>("AppChooser") {
    description = "Runs the AppChooser app."
    mainClass = "com.github.stephengold.lbjexamples.AppChooser"
}

// physics console apps (no graphics)
tasks.register<JavaExec>("HelloLibbulletjme") {
    description = "Runs the HelloLibbulletjme console app."
    mainClass = "com.github.stephengold.lbjexamples.apps.console.HelloLibbulletjme"
}
tasks.register<JavaExec>("HelloVehicle0") {
    description = "Runs the HelloVehicle0 console app."
    mainClass = "com.github.stephengold.lbjexamples.apps.console.HelloVehicle0"
}
tasks.register<JavaExec>("SpeedTest0") {
    args("0")
    mainClass = "com.github.stephengold.lbjexamples.apps.console.SpeedTest"
}
tasks.register<JavaExec>("SpeedTest1") {
    args("1")
    mainClass = "com.github.stephengold.lbjexamples.apps.console.SpeedTest"
}
tasks.register<JavaExec>("SpeedTest2") {
    args("2")
    mainClass = "com.github.stephengold.lbjexamples.apps.console.SpeedTest"
}

// physics tutorial apps (very simple)
tasks.register<JavaExec>("HelloCcd") {
    description = "Runs the HelloCcd tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloCcd"
}
tasks.register<JavaExec>("HelloCharacter") {
    description = "Runs the HelloCharacter tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloCharacter"
}
tasks.register<JavaExec>("HelloCloth") {
    description = "Runs the HelloCloth tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloCloth"
}
tasks.register<JavaExec>("HelloClothRigid") {
    description = "Runs the HelloClothRigid tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloClothRigid"
}
tasks.register<JavaExec>("HelloContactResponse") {
    description = "Runs the HelloContactResponse tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloContactResponse"
}
tasks.register<JavaExec>("HelloCustomShape") {
    description = "Runs the HelloCustomShape tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloCustomShape"
}
tasks.register<JavaExec>("HelloDamping") {
    description = "Runs the HelloDamping tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloDamping"
}
tasks.register<JavaExec>("HelloDeactivation") {
    description = "Runs the HelloDeactivation tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloDeactivation"
}
tasks.register<JavaExec>("HelloDoor") {
    description = "Runs the HelloDoor tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloDoor"
}
tasks.register<JavaExec>("HelloDoubleEnded") {
    description = "Runs the HelloDoubleEnded tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloDoubleEnded"
}
tasks.register<JavaExec>("HelloGhost") {
    description = "Runs the HelloGhost tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloGhost"
}
tasks.register<JavaExec>("HelloJoint") {
    description = "Runs the HelloJoint tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloJoint"
}
tasks.register<JavaExec>("HelloKinematics") {
    description = "Runs the HelloKinematics tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloKinematics"
}
tasks.register<JavaExec>("HelloLimit") {
    description = "Runs the HelloLimit tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloLimit"
}
tasks.register<JavaExec>("HelloMadMallet") {
    description = "Runs the HelloMadMallet tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloMadMallet"
}
tasks.register<JavaExec>("HelloMassDistribution") {
    description = "Runs the HelloMassDistribution tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloMassDistribution"
}
tasks.register<JavaExec>("HelloMinkowski") {
    description = "Runs the HelloMinkowski tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloMinkowski"
}
tasks.register<JavaExec>("HelloMotor") {
    description = "Runs the HelloMotor tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloMotor"
}
tasks.register<JavaExec>("HelloNewHinge") {
    description = "Runs the HelloNewHinge tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloNewHinge"
}
tasks.register<JavaExec>("HelloNonUniformGravity") {
    description = "Runs the HelloNonUniformGravity tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloNonUniformGravity"
}
tasks.register<JavaExec>("HelloPin") {
    description = "Runs the HelloPin tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloPin"
}
tasks.register<JavaExec>("HelloRigidBody") {
    description = "Runs the HelloRigidBody tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloRigidBody"
}
tasks.register<JavaExec>("HelloServo") {
    description = "Runs the HelloServo tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloServo"
}
tasks.register<JavaExec>("HelloSoftBody") {
    description = "Runs the HelloSoftBody tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloSoftBody"
}
tasks.register<JavaExec>("HelloSoftRope") {
    description = "Runs the HelloSoftRope tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloSoftRope"
}
tasks.register<JavaExec>("HelloSoftSoft") {
    description = "Runs the HelloSoftSoft tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloSoftSoft"
}
tasks.register<JavaExec>("HelloSport") {
    description = "Runs the HelloSport tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloSport"
}
tasks.register<JavaExec>("HelloSpring") {
    description = "Runs the HelloSpring tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloSpring"
}
tasks.register<JavaExec>("HelloStaticBody") {
    description = "Runs the HelloStaticBody tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloStaticBody"
}
tasks.register<JavaExec>("HelloVehicle") {
    description = "Runs the HelloVehicle tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloVehicle"
}
tasks.register<JavaExec>("HelloWalk") {
    description = "Runs the HelloWalk tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloWalk"
}
tasks.register<JavaExec>("HelloWind") {
    description = "Runs the HelloWind tutorial app."
    mainClass = "com.github.stephengold.lbjexamples.apps.HelloWind"
}

val os = DefaultNativePlatform.getCurrentOperatingSystem()
val includeLinux = os.isLinux
val includeMacOsX = os.isMacOsX
val includeWindows = os.isWindows
val enableNativeAccess = JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)

tasks.withType<JavaExec>().all { // JVM runtime options:
    if (os.isMacOsX) {
        jvmArgs("-XstartOnFirstThread") // required for GLFW on macOS
    }
    classpath = sourceSets.main.get().runtimeClasspath
    enableAssertions = true
    if (enableNativeAccess) {
        jvmArgs("--enable-native-access=ALL-UNNAMED") // suppress System::load() warning
    }
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

    implementation(libs.jsnaploader) // HelloLibbulletjme and HelloVehicle0 apps need access
    implementation(libs.sport)
}