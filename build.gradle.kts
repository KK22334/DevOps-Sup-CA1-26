println("Enabling Java library plugin")

subprojects {
    // Java Library provides explicit API and implementation dependency scopes.
    apply(plugin = "java-library")
}
