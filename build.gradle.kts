plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register<Exec>("buildSite") {
    dependsOn(":composeApp:wasmJsBrowserDistribution")
    commandLine("sh", "-c", "rm -rf docs/* && cp -r composeApp/build/dist/wasmJs/productionExecutable/* docs/. && cp docs/index.html docs/404.html")
}
