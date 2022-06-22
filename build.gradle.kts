plugins {
    java
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("jacoco")
    id("com.github.kt3k.coveralls") version ("2.12.0")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.simpleframework:simple-http:6.0.1")
    implementation("io.undertow:undertow-core:2.2.16.Final")
    implementation("com.samskivert:jmustache:1.15")

    // For the testing package
    implementation("org.hamcrest:hamcrest:2.2")
    implementation("com.googlecode.juniversalchardet:juniversalchardet:1.0.3")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.jmock:jmock-junit4:2.12.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("failed", "standardOut")
    }

    systemProperty("jdk.internal.httpclient.disableHostnameVerification", "true")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(false)
    }
}

coveralls {
    saveAsFile = true
    sendToCoveralls = false
}


publishing {
    publications {
        create<MavenPublication>("molecule") {
            from(components["java"])

            infix fun <T> Property<T>.by(value: T) {
                set(value)
            }

            pom {
                name by project.name
                description by "A web micro-framework for Java"
                url by "https://github.com/testinfected/molecule"

                licenses {
                    license {
                        name by "MIT License"
                        url by "https://opensource.org/licenses/MIT"
                        distribution by "repo"
                    }
                }

                developers {
                    developer {
                        id by "testinfected"
                        name by "Vincent Tencé"
                        organization by "Bee Software"
                        organizationUrl by "http://bee.software"
                    }
                }

                scm {
                    url by "https://github.com/testinfected/molecule"
                    connection by "https://github.com/testinfected/molecule.git"
                    developerConnection by "git@github.com:testinfected/molecule.git"
                }

            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

signing {
    sign(publishing.publications["molecule"])
}