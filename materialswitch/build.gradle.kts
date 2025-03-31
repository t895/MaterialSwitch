import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
}

val libName = "materialswitch"
val libPackage = "com.t895.$libName"
val packageVersion = "0.1.3"

group = libPackage
version = packageVersion

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = libName
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }
}

android {
    namespace = libPackage
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.t895",
        artifactId = libName,
        version = packageVersion,
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set(libName)
        description.set("Material 3 Switch accurately implemented to its design spec in Compose Multiplatform")
        inceptionYear.set("2024")
        url.set("https://github.com/t895/MaterialSwitch")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/mit")
            }
        }

        // Specify developer information
        developers {
            developer {
                id.set("t895")
                name.set("Charles Lombardo")
                email.set("clombardo169@gmail.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/t895/$libName")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}
