plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "com.google.guava", name = "guava", "25.0-jre"){
        because("Outdated")
    }
    implementation(group = "org.immutables", name = "immutables", "+"){
        because("Up-To-Date")
    }
}
