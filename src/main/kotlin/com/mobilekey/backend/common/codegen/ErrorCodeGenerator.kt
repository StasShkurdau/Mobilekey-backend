package com.mobilekey.backend.common.codegen

import com.mobilekey.backend.auth.exception.AuthError
import com.mobilekey.backend.common.exception.ApiError
import com.mobilekey.backend.user.exception.UserError
import java.io.File

/**
 * Generates JSON and TypeScript files with all API error codes.
 *
 * Run via Gradle: ./gradlew generateErrorCodes
 */
object ErrorCodeGenerator {

    private val allErrors: List<ApiError> = listOf(
        *AuthError.entries.toTypedArray(),
        *UserError.entries.toTypedArray(),
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val outputDir = if (args.isNotEmpty()) File(args[0]) else File("generated")
        outputDir.mkdirs()

        generateJson(File(outputDir, "error-codes.json"))
        generateTypeScript(File(outputDir, "error-codes.ts"))

        println("Generated error codes in ${outputDir.absolutePath}")
    }

    private fun generateJson(file: File) {
        val grouped = allErrors.groupBy { it.code.substringBefore(".") }

        val json = buildString {
            appendLine("{")
            grouped.entries.forEachIndexed { groupIndex, (domain, errors) ->
                appendLine("""  "$domain": {""")
                errors.forEachIndexed { errorIndex, error ->
                    val comma = if (errorIndex < errors.size - 1) "," else ""
                    appendLine("""    "${error.code}": { "message": "${error.message}", "httpStatus": ${error.httpStatus.value()} }$comma""")
                }
                val groupComma = if (groupIndex < grouped.size - 1) "," else ""
                appendLine("  }$groupComma")
            }
            appendLine("}")
        }

        file.writeText(json)
        println("  -> ${file.name}")
    }

    private fun generateTypeScript(file: File) {
        val ts = buildString {
            appendLine("// Auto-generated. Do not edit manually.")
            appendLine("// Run: ./gradlew generateErrorCodes")
            appendLine()
            appendLine("export const API_ERRORS = {")
            allErrors.forEach { error ->
                appendLine("""  "${error.code}": "${error.message}",""")
            }
            appendLine("} as const;")
            appendLine()
            appendLine("export type ApiErrorCode = keyof typeof API_ERRORS;")
        }

        file.writeText(ts)
        println("  -> ${file.name}")
    }
}
