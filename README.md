# Material Switch

Material 3 Switch accurately implemented to its design spec in Compose Multiplatform.
Intended to be a drop-in replacement for Jetpack Compose's Material 3 Switch.

I made this because I generally dislike the Compose Material 3 Switch due to lacking
multiple animations and color states.

### Dependency

Works on all platforms that Compose multiplatform is available on
`implementation("io.github.t895:materialswitch:0.1.4")`

### Demo

![A Jetpack Compose Material 3 Switch, this repository's implementation, and an Android View Material 3 Switch being checked/unchecked and enabled/disabled](https://github.com/user-attachments/assets/4683ff6d-5f11-4fba-b71d-e611d89765a4)

### Build platform artifacts

#### Android aar

- Run `./gradlew :switch:assembleRelease`
- Output: `/switch/build/outputs/aar/switch-release.aar`

#### JVM jar

- Run `./gradlew :switch:jvmJar`
- Output: `/switch/build/libs/switch-jvm-1.0.jar`

#### iOS Framework

- Run `./gradlew :switch:linkReleaseFrameworkIosArm64`
- Output: `/switch/build/bin/iosArm64/releaseFramework/switch.framework`
