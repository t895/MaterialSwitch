# Switch

Material 3 Switch accurately implemented to its design spec in Compose Multiplatform.
Intended to be a drop-in replacement for Jetpack Compose's Material 3 Switch.

I made this because I generally dislike the Compose Material 3 Switch due to lacking
multiple animations and color states.

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
