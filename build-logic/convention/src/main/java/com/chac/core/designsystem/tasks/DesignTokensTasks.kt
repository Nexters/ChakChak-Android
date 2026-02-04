package com.chac.core.designsystem.tasks

import groovy.json.JsonSlurper
import org.gradle.api.Project
import java.math.BigDecimal

fun Project.registerDesignTokensTasks(
    designTokensFilePropertyName: String = "designTokensFile",
    tokensRelativePath: String = "src/main/res/raw/design_tokens.json",
    colorOutputRelativePath: String = "src/main/java/com/chac/core/designsystem/ui/theme/Color.kt",
    typeOutputRelativePath: String = "src/main/java/com/chac/core/designsystem/ui/theme/Type.kt",
) {
    val designTokensFileProvider = providers.gradleProperty(designTokensFilePropertyName)
        .map { file(it) }
    val designTokensRawFile = layout.projectDirectory.file(tokensRelativePath).asFile

    val syncTask = tasks.register("syncDesignTokens") {
        group = "design tokens"
        description = "Copy design tokens JSON into core/designsystem raw resources."
        val targetFile = designTokensRawFile
        onlyIf { designTokensFileProvider.isPresent }
        inputs.files(designTokensFileProvider)
        outputs.file(targetFile)
        doLast {
            val designTokensFile = designTokensFileProvider.get()
            if (!designTokensFile.exists()) {
                error("Design tokens file not found at ${designTokensFile.absolutePath}")
            }
            targetFile.parentFile.mkdirs()
            designTokensFile.copyTo(targetFile, overwrite = true)
        }
    }

    tasks.register("generateDesignSystemTokens") {
        group = "design tokens"
        description = "Generate Color.kt and Type.kt from design tokens JSON."
        dependsOn(syncTask)
        val colorFile = layout.projectDirectory.file(colorOutputRelativePath).asFile
        val typeFile = layout.projectDirectory.file(typeOutputRelativePath).asFile
        inputs.file(designTokensRawFile)
        outputs.files(colorFile, typeFile)
        doLast {
            if (!designTokensRawFile.exists()) {
                error(
                    "Design tokens file not found. Provide -P$designTokensFilePropertyName=/path/to/design_tokens.json " +
                        "or create ${designTokensRawFile.absolutePath}",
                )
            }
            val tokens = JsonSlurper().parse(designTokensRawFile) as Map<*, *>
            val colors = tokens["color"] as? Map<*, *> ?: error("Missing color tokens")
            val fonts = tokens["font"] as? Map<*, *> ?: error("Missing font tokens")

            fun toBigDecimal(value: Any?): BigDecimal {
                return when (value) {
                    null -> error("Missing numeric value")
                    is BigDecimal -> value
                    is Int -> value.toBigDecimal()
                    is Long -> value.toBigDecimal()
                    is Double -> value.toBigDecimal()
                    is Float -> value.toBigDecimal()
                    is Number -> value.toDouble().toBigDecimal()
                    is String -> value.toBigDecimal()
                    else -> error("Unsupported numeric type: ${value::class}")
                }
            }

            fun formatSp(value: Any?): String {
                val raw = toBigDecimal(value).stripTrailingZeros().toPlainString()
                return "${raw}.sp"
            }

            fun toArgbHex(colorValue: String): String {
                val hex = colorValue.removePrefix("#").lowercase()
                val argb = when (hex.length) {
                    8 -> hex.substring(6, 8) + hex.substring(0, 6) // RRGGBBAA -> AARRGGBB
                    6 -> "ff$hex"
                    else -> error("Unsupported color format: $colorValue")
                }
                return "0x${argb.uppercase()}"
            }

            fun toPropertyName(tokenKey: String): String {
                val cleaned = tokenKey
                    .replace("#", "")
                    .replace("&", "and")
                val parts = cleaned.split(Regex("[^A-Za-z0-9]+"))
                    .filter { it.isNotBlank() }
                val filtered = parts.filterNot { it.matches(Regex("[0-9a-fA-F]{3,8}")) }
                val hasLetters = filtered.any { it.any { ch -> ch.isLetter() } }
                val normalizedParts = if (filtered.isEmpty() || !hasLetters) parts else filtered
                val base = normalizedParts.joinToString("") { part ->
                    part.lowercase().replaceFirstChar { it.uppercase() }
                }
                val normalized = if (base.isBlank()) "Token" else base
                return if (normalized.first().isDigit()) "Token$normalized" else normalized
            }

            fun uniqueName(name: String, existing: MutableMap<String, Int>): String {
                val count = existing[name] ?: 0
                return if (count == 0) {
                    existing[name] = 1
                    name
                } else {
                    val next = count + 1
                    existing[name] = next
                    "${name}${next}"
                }
            }

            fun canonicalTokenKey(tokenKey: String): String {
                val cleaned = tokenKey.replace("#", "")
                val parts = cleaned.split(Regex("[^A-Za-z0-9]+"))
                    .filter { it.isNotBlank() }
                val filtered = parts.filterNot { it.matches(Regex("[0-9a-fA-F]{3,8}")) }
                val effective = if (filtered.isEmpty()) parts else filtered
                return effective.joinToString("").lowercase()
            }

            fun canonicalPropertyName(name: String): String {
                return name.filter { it.isLetterOrDigit() }.lowercase()
            }

            val colorEntriesInOrder = colors.entries.map { it.key.toString() }
            val colorTokensByCanonical = mutableMapOf<String, MutableList<String>>()
            colorEntriesInOrder.forEach { tokenKey ->
                val canonical = canonicalTokenKey(tokenKey)
                colorTokensByCanonical.getOrPut(canonical) { mutableListOf() }.add(tokenKey)
            }

            val colorLines = run {
                if (!colorFile.exists()) {
                    buildString {
                        appendLine("package com.chac.core.designsystem.ui.theme")
                        appendLine()
                        appendLine("import androidx.compose.ui.graphics.Color")
                        appendLine()
                        appendLine("object ChacColors {")
                        val usedNames = mutableMapOf<String, Int>()
                        colorEntriesInOrder.forEach { tokenKey ->
                            val token = colors[tokenKey] as? Map<*, *>
                                ?: error("Invalid color token: $tokenKey")
                            val value = token["value"] as? String
                                ?: error("Missing color value for: $tokenKey")
                            val baseName = toPropertyName(tokenKey)
                            val propertyName = uniqueName(baseName, usedNames)
                            appendLine("    val $propertyName = Color(${toArgbHex(value)})")
                        }
                        appendLine("}")
                    }
                } else {
                    val lines = colorFile.readLines()
                    val start = lines.indexOfFirst { it.contains("object ChacColors") }
                    if (start == -1) {
                        error("Missing object ChacColors in ${colorFile.absolutePath}")
                    }
                    var balance = 0
                    var end = -1
                    for (i in start until lines.size) {
                        val line = lines[i]
                        balance += line.count { it == '{' }
                        balance -= line.count { it == '}' }
                        if (i > start && balance == 0) {
                            end = i
                            break
                        }
                    }
                    if (end == -1) {
                        error("Unbalanced braces in ${colorFile.absolutePath}")
                    }
                    val usedTokenKeys = mutableSetOf<String>()
                    val usedPropertyNames = mutableMapOf<String, Int>()
                    var valIndent = "    "
                    val output = mutableListOf<String>()
                    output.addAll(lines.subList(0, start + 1))
                    val valRegex = Regex("^\\s*val\\s+(\\w+)\\s*=\\s*Color\\(")
                    for (i in start + 1 until end) {
                        val line = lines[i]
                        val match = valRegex.find(line)
                        if (match != null) {
                            val propertyName = match.groupValues[1]
                            if (valIndent == "    ") {
                                valIndent = line.substringBefore("val")
                            }
                            val canonical = canonicalPropertyName(propertyName)
                            val candidates = colorTokensByCanonical[canonical]
                            val tokenKey = candidates?.firstOrNull { it !in usedTokenKeys }
                            if (tokenKey != null) {
                                usedTokenKeys.add(tokenKey)
                                val token = colors[tokenKey] as? Map<*, *>
                                    ?: error("Invalid color token: $tokenKey")
                                val value = token["value"] as? String
                                    ?: error("Missing color value for: $tokenKey")
                                output.add("${valIndent}val $propertyName = Color(${toArgbHex(value)})")
                                usedPropertyNames[propertyName] = 1
                            }
                        } else {
                            output.add(line)
                        }
                    }
                    val remainingTokenKeys = colorEntriesInOrder.filterNot { it in usedTokenKeys }
                    if (remainingTokenKeys.isNotEmpty()) {
                        if (output.isNotEmpty() && output.last().isNotBlank()) {
                            output.add("")
                        }
                        remainingTokenKeys.forEach { tokenKey ->
                            val token = colors[tokenKey] as? Map<*, *>
                                ?: error("Invalid color token: $tokenKey")
                            val value = token["value"] as? String
                                ?: error("Missing color value for: $tokenKey")
                            val baseName = toPropertyName(tokenKey)
                            val propertyName = uniqueName(baseName, usedPropertyNames)
                            output.add("${valIndent}val $propertyName = Color(${toArgbHex(value)})")
                        }
                    }
                    output.addAll(lines.subList(end, lines.size))
                    output.joinToString("\n")
                }
            }

            fun fontWeightToKotlin(weight: Int): String {
                return when (weight) {
                    100 -> "FontWeight.Thin"
                    200 -> "FontWeight.ExtraLight"
                    300 -> "FontWeight.Light"
                    400 -> "FontWeight.Normal"
                    500 -> "FontWeight.Medium"
                    600 -> "FontWeight.SemiBold"
                    700 -> "FontWeight.Bold"
                    800 -> "FontWeight.ExtraBold"
                    900 -> "FontWeight.Black"
                    else -> "FontWeight(${weight})"
                }
            }

            fun fontFamilyVariable(fontFamilyName: String): String {
                return when (fontFamilyName) {
                    "Pretendard" -> "Pretendard"
                    "Montserrat" -> "Montserrat"
                    else -> error("Unsupported font family: $fontFamilyName")
                }
            }
            val fontEntriesInOrder = fonts.entries.map { it.key.toString() }
            val fontTokensByCanonical = mutableMapOf<String, MutableList<String>>()
            fontEntriesInOrder.forEach { tokenKey ->
                val canonical = canonicalTokenKey(tokenKey)
                fontTokensByCanonical.getOrPut(canonical) { mutableListOf() }.add(tokenKey)
            }

            fun buildTextStyleLines(
                name: String,
                value: Map<*, *>,
                indent: String,
            ): List<String> {
                val fontFamilyName = value["fontFamily"] as? String
                    ?: error("Missing fontFamily for: $name")
                val fontFamily = fontFamilyVariable(fontFamilyName)
                val fontWeightValue = toBigDecimal(value["fontWeight"]).toInt()
                val fontWeight = fontWeightToKotlin(fontWeightValue)
                return listOf(
                    "${indent}val $name = TextStyle(",
                    "${indent}    fontFamily = $fontFamily,",
                    "${indent}    fontWeight = $fontWeight, // $fontWeightValue",
                    "${indent}    fontSize = ${formatSp(value["fontSize"])},",
                    "${indent}    lineHeight = ${formatSp(value["lineHeight"])},",
                    "${indent}    letterSpacing = ${formatSp(value["letterSpacing"])},",
                    "${indent})",
                )
            }

            val typeLines = run {
                if (!typeFile.exists()) {
                    buildString {
                        appendLine("package com.chac.core.designsystem.ui.theme")
                        appendLine()
                        appendLine("import androidx.compose.material3.Typography")
                        appendLine("import androidx.compose.ui.text.TextStyle")
                        appendLine("import androidx.compose.ui.text.font.Font")
                        appendLine("import androidx.compose.ui.text.font.FontFamily")
                        appendLine("import androidx.compose.ui.text.font.FontWeight")
                        appendLine("import androidx.compose.ui.unit.sp")
                        appendLine("import com.chac.core.designsystem.R")
                        appendLine()
                        appendLine("object ChacTextStyles {")
                        val usedNames = mutableMapOf<String, Int>()
                        fontEntriesInOrder.forEachIndexed { index, tokenKey ->
                            val token = fonts[tokenKey] as? Map<*, *>
                                ?: error("Invalid font token: $tokenKey")
                            val value = token["value"] as? Map<*, *>
                                ?: error("Missing font value for: $tokenKey")
                            val baseName = toPropertyName(tokenKey)
                            val propertyName = uniqueName(baseName, usedNames)
                            buildTextStyleLines(propertyName, value, "    ").forEach { appendLine(it) }
                            if (index != fontEntriesInOrder.lastIndex) {
                                appendLine()
                            }
                        }
                        appendLine("}")
                        appendLine()
                        appendLine("val ChacTypography = Typography()")
                    }
                } else {
                    val lines = typeFile.readLines()
                    val start = lines.indexOfFirst { it.contains("object ChacTextStyles") }
                    if (start == -1) {
                        error("Missing object ChacTextStyles in ${typeFile.absolutePath}")
                    }
                    var balance = 0
                    var end = -1
                    for (i in start until lines.size) {
                        val line = lines[i]
                        balance += line.count { it == '{' }
                        balance -= line.count { it == '}' }
                        if (i > start && balance == 0) {
                            end = i
                            break
                        }
                    }
                    if (end == -1) {
                        error("Unbalanced braces in ${typeFile.absolutePath}")
                    }
                    val usedTokenKeys = mutableSetOf<String>()
                    val usedPropertyNames = mutableMapOf<String, Int>()
                    val keptStyleNames = mutableSetOf<String>()
                    var valIndent = "    "
                    val output = mutableListOf<String>()
                    output.addAll(lines.subList(0, start + 1))
                    val valRegex = Regex("^\\s*val\\s+(\\w+)\\s*=\\s*TextStyle\\(")
                    var i = start + 1
                    while (i < end) {
                        val line = lines[i]
                        val match = valRegex.find(line)
                        if (match != null) {
                            val propertyName = match.groupValues[1]
                            if (valIndent == "    ") {
                                valIndent = line.substringBefore("val")
                            }
                            var j = i + 1
                            while (j < end && lines[j].trim() != ")") {
                                j++
                            }
                            val canonical = canonicalPropertyName(propertyName)
                            val candidates = fontTokensByCanonical[canonical]
                            val tokenKey = candidates?.firstOrNull { it !in usedTokenKeys }
                            if (tokenKey != null) {
                                usedTokenKeys.add(tokenKey)
                                val token = fonts[tokenKey] as? Map<*, *>
                                    ?: error("Invalid font token: $tokenKey")
                                val value = token["value"] as? Map<*, *>
                                    ?: error("Missing font value for: $tokenKey")
                                buildTextStyleLines(propertyName, value, valIndent).forEach { output.add(it) }
                                output.add("")
                                usedPropertyNames[propertyName] = 1
                                keptStyleNames.add(propertyName)
                            }
                            i = j + 1
                            if (i < end && lines[i].isBlank()) {
                                i++
                            }
                        } else {
                            output.add(line)
                            i++
                        }
                    }
                    val remainingTokenKeys = fontEntriesInOrder.filterNot { it in usedTokenKeys }
                    if (remainingTokenKeys.isNotEmpty()) {
                        if (output.isNotEmpty() && output.last().isNotBlank()) {
                            output.add("")
                        }
                        remainingTokenKeys.forEach { tokenKey ->
                            val token = fonts[tokenKey] as? Map<*, *>
                                ?: error("Invalid font token: $tokenKey")
                            val value = token["value"] as? Map<*, *>
                                ?: error("Missing font value for: $tokenKey")
                            val baseName = toPropertyName(tokenKey)
                            val propertyName = uniqueName(baseName, usedPropertyNames)
                            buildTextStyleLines(propertyName, value, valIndent).forEach { output.add(it) }
                            output.add("")
                            keptStyleNames.add(propertyName)
                        }
                    }
                    while (output.isNotEmpty() && output.last().isBlank()) {
                        output.removeAt(output.lastIndex)
                    }
                    output.addAll(lines.subList(end, lines.size))
                    output.joinToString("\n")
                }
            }

            fun ensureTrailingBlankLine(content: String): String {
                val trimmed = content.trimEnd()
                return "${trimmed}\n"
            }

            colorFile.writeText(ensureTrailingBlankLine(colorLines))
            typeFile.writeText(ensureTrailingBlankLine(typeLines))
        }
    }
}
