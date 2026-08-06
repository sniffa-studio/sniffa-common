plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.5"
}

group = "studio.sniffa"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val grpcVersion = "1.73.0"
val protobufVersion = "4.31.1"

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.18.2")

    // api(...): the generated gRPC stub/message types are part of this library's public surface -
    // consumers (sniffa-backend, sniffa-discord) reference them directly (EventInfo, Winner, ...).
    api("io.grpc:grpc-stub:$grpcVersion")
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    // Generated stub code references javax.annotation.Generated, removed from the JDK itself since 11.
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
