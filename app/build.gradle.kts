import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.cybersync.nodd"
    compileSdk = 36

    signingConfigs {
        // One shared key for test builds, so an APK from any computer or from GitHub Actions
        // installs over the previous one. Not for the Play Store: that needs your own private upload key.
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.cybersync.nodd"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// The plan document lives in docs/nodd-concept.html, the same file that is published as the web page.
// This task wraps it in a standalone page that uses the bundled fonts, so it opens offline in the app.
abstract class BuildPlanPage : DefaultTask() {
    @get:InputFile
    abstract val source: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun build() {
        val body = source.get().asFile.readText()
            .replace(Regex("""<link[^>]*fonts\.(googleapis|gstatic)\.com[^>]*>\s*"""), "")
        val fonts = listOf(
            Triple("Bricolage Grotesque", "BricolageGrotesque.ttf", "200 800"),
            Triple("Figtree", "Figtree.ttf", "300 900"),
            Triple("JetBrains Mono", "JetBrainsMono.ttf", "100 800"),
        )
        val page = buildString {
            appendLine("<!doctype html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine("<meta charset=\"utf-8\">")
            appendLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            appendLine("<style>")
            fonts.forEach { (family, file, weights) ->
                appendLine("@font-face{font-family:\"$family\";src:url(\"../fonts/$file\") format(\"truetype\");font-weight:$weights;font-display:block}")
            }
            appendLine("[hidden]{display:none!important}")
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine(body)
            appendLine("</body>")
            appendLine("</html>")
        }
        val dir = outputDir.get().dir("plan").asFile
        dir.mkdirs()
        dir.resolve("index.html").writeText(page)
    }
}

val buildPlanPage = tasks.register<BuildPlanPage>("buildPlanPage") {
    source.set(rootProject.layout.projectDirectory.file("docs/nodd-concept.html"))
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(buildPlanPage, BuildPlanPage::outputDir)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    testImplementation("junit:junit:4.13.2")
}
