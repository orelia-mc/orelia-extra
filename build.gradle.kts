plugins {
    id("java")
    id("com.gradleup.shadow") version "9.5.1"
    id("maven-publish")
}

group = "rpg"
version = "1.0.12"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    // TEMP DEV LOOP: prefers locally-published orelia-core/orelia-world (./gradlew
    // publishToMavenLocal in those repos) over jitpack, so in-flight changes are picked up
    // without a push. Remove this line before merging - production builds should resolve
    // from jitpack only.
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    // Resolves orelia-core/orelia-world straight from their GitHub repos, same as
    // orelia-world does for orelia-core.
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    // VaultAPI's POM pulls in an old org.bukkit:bukkit:1.13.1 as a transitive dependency,
    // which conflicts with the org.bukkit:bukkit capability paper-api provides - exclude it.
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    // orelia-extra only ever calls into orelia-core/orelia-world through their published
    // rpg.api interfaces (Bukkit ServicesManager) or generic rpg.core.* infrastructure -
    // never gameplay-module internals directly.
    compileOnly("com.github.orelia-mc:orelia-core:main-SNAPSHOT")
    compileOnly("com.github.orelia-mc:orelia-world:main-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set("orelia-extra")
    }
    build {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    test {
        useJUnitPlatform()
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// Publishes to mavenLocal under the same coordinates jitpack normally resolves
// (com.github.orelia-mc:orelia-extra:main-SNAPSHOT), so a future orelia-debug plugin can pick
// up local changes during development without waiting on a push. Temporary dev-loop aid only -
// production builds still resolve this dependency from jitpack.io.
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.orelia-mc"
            artifactId = "orelia-extra"
            version = "main-SNAPSHOT"
            from(components["java"])
        }
    }
}
