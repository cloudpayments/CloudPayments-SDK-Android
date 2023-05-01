import java.io.File
import java.util.Properties
import java.io.InputStreamReader
import java.io.FileInputStream
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

object Common {
    fun Project.getLocalProperty(key: String, file: String = "gradle.properties"): String {
        val properties = Properties()
        val localProperties = File(this.rootDir, file)
        if (localProperties.isFile) {
            InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8)
                .use { reader ->
                    properties.load(reader)
                }
        } else error("File $file not found")

        return properties.getProperty(key)
    }

    fun Project.getGitShortName(): String {
        val stdout = ByteArrayOutputStream()

        try {
            exec {
                setCommandLine("git rev-parse --short HEAD")
                standardOutput = stdout
            }
        } catch (ignored: Exception) {
            // no-op
        }

        return stdout.toString().trim()
    }

    fun Project.getGitCurrentBranch(): String {
        val stdout = ByteArrayOutputStream()

        try {
            exec {
                setCommandLine("git rev-parse --abbrev-ref HEAD")
                standardOutput = stdout
            }
        } catch (ignored: Exception) {
            // no-op
        }

        return stdout.toString().trim()
    }
}