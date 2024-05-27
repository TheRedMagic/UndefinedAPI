plugins {
    `java-library`
    java
    `maven-publish`
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.dokka") version "1.9.20"
}

apply(plugin = "maven-publish")
val versionVar = "0.4.44"
val groupIdVar = "com.redmagic"
val artifactIdVar = "UndefinedAPI"

version = groupIdVar

publishing {
    repositories {
        maven {
            name = "repo.undefinedcreation.com"
            url = uri("https://repo.undefinedcreation.com/repo")
            credentials(PasswordCredentials::class) {
                username = System.getenv("MAVEN_NAME")
                password = System.getenv("MAVEN_SECRET")
            }
        }
    }

    publications {
        register<MavenPublication>("maven") {
            groupId = groupIdVar
            artifactId = artifactIdVar
            version = versionVar
            from(components["java"])
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

allprojects {

    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = "com.redmagic"
    version = versionVar

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.reflections:reflections:0.9.11")
        implementation("net.kyori:adventure-platform-bukkit:4.3.2")
        implementation("net.kyori:adventure-text-minimessage:4.16.0")
        implementation("org.json:json:20171018")
    }

}



dependencies {
    implementation(project(":common"))
    implementation(project(":v1_20_4"))
    implementation(project(":api"))
}

tasks {


    shadowJar {
        archiveFileName.set("UndefinedAPI-shadow-${versionVar}.jar")
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }


}


kotlin{
    jvmToolchain(17)
}
