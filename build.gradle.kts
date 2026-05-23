import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val kotlinVersion = "2.3.21"
val ktorVersion = "3.5.0"

plugins {
    kotlin("multiplatform") version "2.3.21"
    id("org.asciidoctor.jvm.convert") version "4.0.5"
}

group = "de.oio"
version = "1.0"

repositories {
    mavenCentral()
}

defaultTasks("jsBrowserProductionWebpack", "asciidoctor")

kotlin {
    jvmToolchain(17)

    jvm()

    js(IR) {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-server-compression:$ktorVersion")
                implementation("io.ktor:ktor-server-cors:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:1.5.32")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
                implementation(npm("konva", "10.3.0"))
            }
        }
    }
}

tasks.named<AsciidoctorTask>("asciidoctor") {
    baseDirFollowsSourceDir()
    outputOptions {
        backends("html5")
        setOutputDir(layout.buildDirectory.dir("distributions"))
    }
}

tasks.named<KotlinWebpack>("jsBrowserProductionWebpack") {
    sourceMaps = false
}

tasks.named<Jar>("jvmJar") {
    val webpackTask = tasks.named<KotlinWebpack>("jsBrowserProductionWebpack")

    dependsOn(webpackTask)
    from(webpackTask.flatMap { it.outputDirectory.file(it.mainOutputFileName) })
}

tasks.register("stage") {
    dependsOn(tasks.named("jvmJar"), tasks.named("jsBrowserProductionWebpack"), tasks.named("asciidoctor"))
}

tasks.register<JavaExec>("run") {
    dependsOn(tasks.named("jvmJar"), tasks.named("asciidoctor"))
    classpath(tasks.named<Jar>("jvmJar"), configurations.named("jvmRuntimeClasspath"))
    mainClass.set("ServerKt")
}
