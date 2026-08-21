dependencies {
    // Domain types form part of core's public API.
    api(project(":domain"))

    // Utilities are internal to core's implementation.
    implementation(project(":utils"))
}
