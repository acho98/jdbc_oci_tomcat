plugins {
    id("java")
    id("war")
    id("org.gretty") version "4.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("jakarta.servlet:jakarta.servlet-api:5.0.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

gretty {
    contextPath = "/"
    servletContainer = "tomcat10"
}

tasks.test {
    useJUnitPlatform()
}
