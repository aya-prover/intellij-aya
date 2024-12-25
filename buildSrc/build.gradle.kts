import java.util.*

plugins { java }
repositories { mavenCentral() }

val rootDir = projectDir.parentFile!!

dependencies {
  val deps = Properties()
  deps.load(rootDir.resolve("gradle/deps.properties").reader())
  val buildUtilVersion = deps.getProperty("version.aya-upstream")
  api("org.aya-prover.upstream", "build-util", buildUtilVersion)
  api("org.aya-prover.upstream", "ij-gk-parser", buildUtilVersion)
}
