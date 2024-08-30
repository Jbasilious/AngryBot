
plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    }
application.mainClass.set("AngryBot")
group = "org.example"
version = "3.0.0"

val jdaVersion = "5.0.0-beta.16" //



repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation ("org.apache.opennlp:opennlp-tools:2.1.1")
    implementation ("org.mariadb.jdbc:mariadb-java-client:3.1.2")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation ("com.squareup.retrofit2:retrofit:2.10.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.1.0")
    implementation ("me.xdrop:fuzzywuzzy:1.4.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    // Set this to the version of java you want to use,
    // the minimum required for JDA is 1.8
    sourceCompatibility = "16"
}