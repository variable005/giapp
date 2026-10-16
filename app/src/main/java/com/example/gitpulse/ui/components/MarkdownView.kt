package com.example.gitpulse.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/** One image reference from markdown, with a badge heuristic for tiny status shields. */
data class MdImage(val alt: String, val url: String, val isBadge: Boolean)

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Blockquote(val text: String, val isWarning: Boolean) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletList(val items: List<String>) : MarkdownBlock()
    object Divider : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class ImageRow(val images: List<MdImage>) : MarkdownBlock()
}

@Composable
fun MarkdownView(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) {
        runCatching { parseMarkdown(markdown) }.getOrElse {
            // Never take a screen down over an exotic README — fall back to raw text.
            listOf(MarkdownBlock.Paragraph(text = markdown.take(6000)))
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> MarkdownHeader(block)
                is MarkdownBlock.Paragraph -> MarkdownParagraph(block)
                is MarkdownBlock.Blockquote -> MarkdownBlockquote(block)
                is MarkdownBlock.CodeBlock -> MarkdownCodeBlock(block)
                is MarkdownBlock.BulletList -> MarkdownBulletList(block)
                is MarkdownBlock.Divider -> HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
                is MarkdownBlock.Table -> MarkdownTable(block)
                is MarkdownBlock.ImageRow -> MarkdownImageRow(block)
            }
        }
    }
}

@Composable
private fun MarkdownHeader(header: MarkdownBlock.Header) {
    val typography = MaterialTheme.typography
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    when (header.level) {
        1 -> Text(
            text = header.text,
            style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = onSurface
        )
        2 -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = header.text,
                style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = primary
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 1.dp
            )
        }
        3 -> Text(
            text = header.text,
            style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = onSurface
        )
        else -> Text(
            text = header.text,
            style = typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MarkdownParagraph(paragraph: MarkdownBlock.Paragraph) {
    val primary = MaterialTheme.colorScheme.primary
    val codeBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val codeColor = MaterialTheme.colorScheme.onSurface

    val annotated = remember(paragraph.text) {
        buildInlineMarkdown(paragraph.text, primary, codeBg, codeColor)
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun MarkdownBlockquote(quote: MarkdownBlock.Blockquote) {
    val containerColor = if (quote.isWarning) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val accentColor = if (quote.isWarning) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = if (quote.isWarning) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
        } else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (quote.isWarning) {
                    Text(
                        text = "WARNING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MarkdownCodeBlock(codeBlock: MarkdownBlock.CodeBlock) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (codeBlock.language.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = codeBlock.language.lowercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Text(
                    text = codeBlock.code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MarkdownBulletList(bulletList: MarkdownBlock.BulletList) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        bulletList.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MarkdownTable(table: MarkdownBlock.Table) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Headers Row
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    table.headers.forEach { h ->
                        Text(
                            text = h,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.widthIn(min = 90.dp)
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                // Rows
                table.rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { cell ->
                            Text(
                                text = cell,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.widthIn(min = 90.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkdownImageRow(row: MarkdownBlock.ImageRow) {
    val images = row.images
    if (images.size == 1 && !images[0].isBadge) {
        // Full-width screenshot inside a soft frame.
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = images[0].url,
                contentDescription = images[0].alt.ifBlank { null },
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
            )
        }
    } else {
        // Badge rows flow at their natural small size.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            images.forEach { image ->
                AsyncImage(
                    model = image.url,
                    contentDescription = image.alt.ifBlank { null },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(24.dp)
                        .widthIn(max = 240.dp)
                )
            }
        }
    }
}

private val MdImageToken = Regex("""!\[([^\]]*)\]\(\s*([^)\s]+?)(?:\s+"[^"]*")?\s*\)""")
private val MdLinkToken = Regex("""\[([^\]]*)\]\(\s*[^)\s]*\s*\)""")
private val HtmlImageToken = Regex("""<img[^>]*\ssrc\s*=\s*["'][^"']+["'][^>]*>""", RegexOption.IGNORE_CASE)

private fun isBadgeUrl(url: String): Boolean =
    url.contains("shields.io") || url.contains("badge", ignoreCase = true) ||
        url.contains("badgen") || url.endsWith(".svg", ignoreCase = true)

private fun extractImages(line: String): List<MdImage> {
    val images = mutableListOf<MdImage>()
    MdImageToken.findAll(line).forEach { m ->
        val url = m.groupValues[2]
        images.add(MdImage(alt = m.groupValues[1], url = url, isBadge = isBadgeUrl(url)))
    }
    HtmlImageToken.findAll(line).forEach { m ->
        val url = m.groupValues[1]
        images.add(MdImage(alt = "", url = url, isBadge = isBadgeUrl(url)))
    }
    return images
}

private fun stripImages(line: String): String =
    MdImageToken.replace(HtmlImageToken.replace(line, ""), "").replace("&nbsp;", " ").trim()

/** Returns the images when a line contains nothing but images, badges or
 *  link-wrapped badges; null means the line should render as text. */
private fun parseImagesOnlyLine(line: String): List<MdImage>? {
    if (line.isBlank()) return null
    val images = extractImages(line)
    if (images.isEmpty()) return null
    var remainder = stripImages(line)
    remainder = MdLinkToken.replace(remainder, "").trim()
    return if (remainder.isEmpty()) images else null
}

internal fun parseMarkdown(raw: String): List<MarkdownBlock> {
    val lines = raw.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        when {
            // Image / badge rows (consecutive badge lines merge into one row)
            parseImagesOnlyLine(line) != null -> {
                val images = mutableListOf<MdImage>()
                while (i < lines.size) {
                    val lineImages = parseImagesOnlyLine(lines[i]) ?: break
                    if (lineImages.size == 1 && !lineImages[0].isBadge) {
                        // Standalone screenshot: own full-width row, never glued to badges.
                        if (images.isEmpty()) {
                            images.add(lineImages[0])
                            i++
                        }
                        break
                    }
                    images.addAll(lineImages)
                    i++
                }
                blocks.add(MarkdownBlock.ImageRow(images))
            }

            // Code block
            line.trimStart().startsWith("```") -> {
                val lang = line.trimStart().removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.CodeBlock(language = lang, code = codeLines.joinToString("\n")))
                i++
            }

            // Divider
            line.trim() in listOf("---", "***", "___") -> {
                blocks.add(MarkdownBlock.Divider)
                i++
            }

            // Headers
            line.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(level = 1, text = line.removePrefix("# ").trim()))
                i++
            }
            line.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(level = 2, text = line.removePrefix("## ").trim()))
                i++
            }
            line.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(level = 3, text = line.removePrefix("### ").trim()))
                i++
            }
            line.startsWith("#### ") -> {
                blocks.add(MarkdownBlock.Header(level = 4, text = line.removePrefix("#### ").trim()))
                i++
            }

            // Blockquotes
            line.trimStart().startsWith("> ") || line.trimStart() == ">" -> {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && (lines[i].trimStart().startsWith("> ") || lines[i].trimStart() == ">")) {
                    quoteLines.add(lines[i].trimStart().removePrefix(">").trim())
                    i++
                }
                val fullQuote = quoteLines.filter { it.isNotBlank() }.joinToString(" ")
                val isWarning = fullQuote.contains("WARNING", ignoreCase = true)
                blocks.add(MarkdownBlock.Blockquote(text = fullQuote, isWarning = isWarning))
            }

            // Bullet list
            line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                val items = mutableListOf<String>()
                while (i < lines.size && (lines[i].trimStart().startsWith("- ") || lines[i].trimStart().startsWith("* "))) {
                    val itemText = lines[i].trimStart().removePrefix("- ").removePrefix("* ").trim()
                    items.add(itemText)
                    i++
                }
                blocks.add(MarkdownBlock.BulletList(items = items))
            }

            // Table
            line.contains("|") && line.trim().startsWith("|") && line.trim().endsWith("|") -> {
                val headers = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                i++
                if (i < lines.size && lines[i].contains("---")) {
                    i++ // Skip delimiter line
                }
                val rows = mutableListOf<List<String>>()
                while (i < lines.size && lines[i].contains("|") && lines[i].trim().startsWith("|")) {
                    val cells = lines[i].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    rows.add(cells)
                    i++
                }
                blocks.add(MarkdownBlock.Table(headers = headers, rows = rows))
            }

            // Empty line
            line.isBlank() -> {
                i++
            }

            // Paragraph
            else -> {
                val paraLines = mutableListOf<String>()
                while (i < lines.size && lines[i].isNotBlank() &&
                    !lines[i].startsWith("#") &&
                    !lines[i].startsWith("```") &&
                    !lines[i].startsWith(">") &&
                    !lines[i].startsWith("- ") &&
                    !lines[i].startsWith("* ") &&
                    lines[i].trim() !in listOf("---", "***")
                ) {
                    paraLines.add(lines[i].trim())
                    i++
                }
                if (paraLines.isEmpty()) {
                    // Defensive: odd line shapes (e.g. "#tag", "##### deep heading",
                    // "###### x") must never stall the parser — take the line as plain
                    // text and advance so the loop always makes progress.
                    paraLines.add(lines[i].trim())
                    i++
                }
                val rawParagraph = paraLines.joinToString(" ")
                val inlineImages = extractImages(rawParagraph)
                val textOnly = stripImages(rawParagraph)
                if (textOnly.isNotBlank()) {
                    blocks.add(MarkdownBlock.Paragraph(text = textOnly))
                }
                if (inlineImages.isNotEmpty()) {
                    blocks.add(MarkdownBlock.ImageRow(inlineImages))
                }
            }
        }
    }

    return blocks
}

private fun buildInlineMarkdown(
    text: String,
    primaryColor: Color,
    codeBgColor: Color,
    codeTextColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldStart = text.indexOf("**", cursor)
            val codeStart = text.indexOf("`", cursor)

            val nextIndex = when {
                boldStart != -1 && codeStart != -1 -> minOf(boldStart, codeStart)
                boldStart != -1 -> boldStart
                codeStart != -1 -> codeStart
                else -> -1
            }

            if (nextIndex == -1) {
                append(text.substring(cursor))
                break
            }

            // Append regular text before token
            if (nextIndex > cursor) {
                append(text.substring(cursor, nextIndex))
            }

            if (nextIndex == boldStart) {
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    val boldText = text.substring(boldStart + 2, boldEnd)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldText)
                    pop()
                    cursor = boldEnd + 2
                } else {
                    append("**")
                    cursor = boldStart + 2
                }
            } else {
                val codeEnd = text.indexOf("`", codeStart + 1)
                if (codeEnd != -1) {
                    val codeContent = text.substring(codeStart + 1, codeEnd)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBgColor,
                            color = codeTextColor
                        )
                    )
                    append(" $codeContent ")
                    pop()
                    cursor = codeEnd + 1
                } else {
                    append("`")
                    cursor = codeStart + 1
                }
            }
        }
    }
}
