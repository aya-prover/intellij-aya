import java.util.*

plugins {
  java
  groovy
}

repositories {
  mavenCentral()
}

val rootDir = projectDir.parentFile!!

dependencies {
  val deps = Properties()
  deps.load(rootDir.resolve("gradle/deps.properties").reader())
  api("org.aya-prover.upstream", "build-util", deps.getProperty("version.aya-upstream"))
}
