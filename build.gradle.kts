import io.papermc.hangarpublishplugin.HangarPublishTask
import io.papermc.hangarpublishplugin.model.Platforms
import xyz.jpenilla.resourcefactory.bukkit.Permission.Default
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    `java-library`
    `maven-publish`
    idea
    id("com.palantir.git-version") version "4.1.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.0"
    id("com.gradleup.shadow") version "9.0.0-rc1"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
group = "org.kitteh"
version = "4-SNAPSHOT" + '+' + details.gitHash
description = "VanishNoPacket-Refined"

repositories {
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.helpch.at/releases")
    maven("https://nexus.scarsz.me/content/groups/public/")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://jitpack.io")
}

dependencies {

    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.essentialsx:EssentialsX:2.21.2") {
        exclude("org.spigotmc")
    }
    testImplementation("net.essentialsx:EssentialsX:2.21.2") {
        exclude("org.spigotmc")
    }
    compileOnly("com.github.milkbowl:vaultapi:1.7") {
        exclude("org.bukkit")
    }

    compileOnly("net.luckperms:api:5.5")
    testImplementation("net.luckperms:api:5.5")

    compileOnly("me.clip:placeholderapi:2.11.6") {
        exclude("me.clip.placeholderapi.libs.kyori")
        exclude("net.kyori")
    }
    compileOnly("com.discordsrv:discordsrv:1.29.0") {
        exclude("github.scarsz.discordsrv.dependencies.kyori")
        exclude("net.kyori")
    }
    compileOnly("xyz.jpenilla:squaremap-api:1.3.7")
}

java.sourceCompatibility = JavaVersion.VERSION_21

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
    shadowJar {
        minimize()
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isDeprecation = true
    }
    withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    runServer {
        minecraftVersion("1.21.11")
        downloadPlugins {
            url("https://download.luckperms.net/1609/bukkit/loader/LuckPerms-Bukkit-5.5.20.jar")
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}


hangarPublish {
    publications.register("plugin") {
        version = project.version.toString()
        channel.set("Snapshot")
        id.set("VanishNoPacket-Refined")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                val versions: List<String> =
                    (property("paperVersion") as String).split(",").map { it.trim() }
                platformVersions.set(versions)

                dependencies {
                    hangar("Essentials") {
                        required.set(false)
                    }
                    hangar("PlaceholderAPI") {
                        required.set(false)
                    }
                    url("Vault", "https://www.spigotmc.org/resources/vault.34315/") {
                        required.set(false)
                    }
                    hangar("squaremap") {
                        required.set(false)
                    }
                    url("LuckPerms", "https://luckperms.net/download") {
                        required.set(false)
                    }
                }
            }
        }
    }
}

tasks.withType(HangarPublishTask::class).configureEach {
    dependsOn(tasks.named("jar"))
    dependsOn(tasks.named("shadowJar"))
}

paperPluginYaml {
    name = "VanishNoPacket"
    main = "org.kitteh.vanish.VanishPlugin"
    authors.add("Matt \"MBax\" Baxter")
    authors.add("Lexie \"Tech\" Malina")
    authors.add("Fendi (forked)")
    apiVersion = "1.21.11"
    version = project.version.toString()
    description = "Vanish for the high speed admin"
    foliaSupported = true

    dependencies {
        server("Essentials", PaperPluginYaml.Load.BEFORE, false)
        server("PlaceholderAPI", PaperPluginYaml.Load.BEFORE, false)
        server("Vault", PaperPluginYaml.Load.BEFORE, false)
        server("squaremap", PaperPluginYaml.Load.BEFORE, false)
        server("LuckPerms", PaperPluginYaml.Load.BEFORE, false)
    }

    permissions {
        register("vanish.vanish") {
            description.set("Allows use of vanish commands.")
            default.set(Default.OP)
        }
        register("vanish.joinvanished") {
            description.set("Allows joining vanished when join.vanish-on-join is enabled.")
            default.set(Default.FALSE)
        }
        register("vanish.joinwithoutannounce") {
            description.set("Allows joining without a public join announcement.")
            default.set(Default.FALSE)
        }
        register("vanish.see") {
            description.set("Allows seeing vanished players.")
            default.set(Default.OP)
        }
        register("vanish.statusupdates") {
            description.set("Allows receiving vanish status updates.")
            default.set(Default.OP)
        }
        register("vanish.list") {
            description.set("Allows listing vanished players.")
            default.set(Default.OP)
        }
        register("vanish.reload") {
            description.set("Allows reloading VanishNoPacket configuration.")
            default.set(Default.OP)
        }
        register("vanish.fakeannounce") {
            description.set("Allows fake join and fake quit announcements.")
            default.set(Default.OP)
        }
    }
}
