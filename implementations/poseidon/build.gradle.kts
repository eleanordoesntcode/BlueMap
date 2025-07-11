plugins {
	java
	`java-library`
	id("com.diffplug.spotless") version "6.1.2"
	id ("com.github.node-gradle.node") version "3.0.1"
	id ("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.bluecolored.bluemap.bukkit"
version = System.getProperty("bluemap.version") ?: "?" // set by BlueMapCore

val javaTarget = 8
java {
	sourceCompatibility = JavaVersion.toVersion(javaTarget)
	targetCompatibility = JavaVersion.toVersion(javaTarget)
}

repositories {
	mavenCentral()
	maven {
		setUrl("https://libraries.minecraft.net")
	}
	maven {
		setUrl("https://jitpack.io")
	}
	maven {
		setUrl("https://oss.sonatype.org/content/repositories/snapshots")
	}
	maven {
		setUrl("https://repository.johnymuffin.com/repository/maven-public")
	}
		maven {
		setUrl("https://repository.johnymuffin.com/repository/maven-snapshots")
	}
}

dependencies {
	api ("de.bluecolored.bluemap.common:BlueMapCommon") {
		//exclude dependencies provided by bukkit
		exclude( group = "com.google.guava", module = "guava" )
		exclude( group = "com.google.code.gson", module = "gson" )
	}

	shadow ("com.legacyminecraft.poseidon:poseidon-craftbukkit:1.1.8")
	implementation ("org.bstats:bstats-bukkit:2.2.1")

	testImplementation ("org.junit.jupiter:junit-jupiter:5.8.2")
	testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

spotless {
	java {
		target ("src/*/java/**/*.java")

		licenseHeaderFile("../../HEADER")
		indentWithSpaces()
		trimTrailingWhitespace()
	}
}

tasks.withType(JavaCompile::class).configureEach {
	options.apply {
		encoding = "utf-8"
	}
}

tasks.withType(AbstractArchiveTask::class).configureEach {
	isReproducibleFileOrder = true
	isPreserveFileTimestamps = false
}

tasks.test {
	useJUnitPlatform()
}

tasks.processResources {
	from("src/main/resources") {
		include("plugin.yml")
		duplicatesStrategy = DuplicatesStrategy.INCLUDE

		expand (
			"version" to project.version
		)
	}
}

tasks.shadowJar {
	destinationDirectory.set(file("../../build/release"))
	archiveFileName.set("BlueMap-${project.version}-${project.name}.jar")

	//relocate ("com.flowpowered.math", "de.bluecolored.shadow.flowpowered.math") //DON"T relocate this, because the API depends on it
	relocate ("com.typesafe.config", "de.bluecolored.shadow.typesafe.config")
	relocate ("net.querz.nbt", "de.bluecolored.shadow.querz.nbt")
	relocate ("org.spongepowered.configurate", "de.bluecolored.shadow.configurate")
	relocate ("org.bstats", "de.bluecolored.shadow.bstats")
	relocate ("com.mojang.brigadier", "de.bluecolored.shadow.mojang.brigadier")
	relocate ("com.github.benmanes.caffeine", "de.bluecolored.shadow.benmanes.caffeine")
	relocate ("org.aopalliance", "de.bluecolored.shadow.aopalliance")
	relocate ("javax.inject", "de.bluecolored.shadow.javax.inject")
	relocate ("org.checkerframework", "de.bluecolored.shadow.checkerframework")
	relocate ("org.codehaus", "de.bluecolored.shadow.codehaus")
	relocate ("io.leangen.geantyref", "de.bluecolored.shadow.geantyref")
	relocate ("io.airlift", "de.bluecolored.shadow.airlift")

	relocate ("com.google.errorprone", "de.bluecolored.shadow.google.errorprone")
	relocate ("com.google.inject", "de.bluecolored.shadow.google.inject")

	relocate ("org.apache.commons.dbcp2", "de.bluecolored.shadow.apache.commons.dbcp2")
	relocate ("org.apache.commons.io", "de.bluecolored.shadow.apache.commons.io")
	relocate ("org.apache.commons.lang3", "de.bluecolored.shadow.apache.commons.lang3")
	relocate ("org.apache.commons.logging", "de.bluecolored.shadow.apache.commons.logging")
	relocate ("org.apache.commons.pool2", "de.bluecolored.shadow.apache.commons.pool2")
}

tasks.register("release") {
	dependsOn(tasks.shadowJar)
}