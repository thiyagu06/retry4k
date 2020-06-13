plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.71"
    jacoco
    id("io.gitlab.arturbosch.detekt") version "1.4.0"
    `java-library`
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.3")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:+")
    testRuntimeOnly("net.bytebuddy:byte-buddy:1.10.6")
}

tasks {

    named<Task>("check") {
        dependsOn(named<Task>("jacocoTestReport"))
    }

    named<Test>("test") {
        useJUnitPlatform()
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "13"
    }

    withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
            xml.destination = file("$buildDir/reports/jacoco/jacocoTestReport.xml")
            html.destination = file("$buildDir/reports/jacoco")
        }

        classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                exclude("com/thiyagu06/retry4k/RetryStrategy.class")
                exclude()
            }
        )
    }
}

