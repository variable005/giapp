package com.example.gitpulse.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards against regressions in the README markdown parser — most importantly the
 * historical stall on hash lines without a following space ("#tag", "##### x"),
 * which used to spin the parser forever and freeze the app on repo READMEs.
 */
class MarkdownViewParsingTest {

    @Test
    fun parserMakesProgressOnHashLinesWithoutSpace() {
        val markdown = buildString {
            appendLine("###### deep heading without a known level")
            appendLine("#tag")
            appendLine("### regular heading")
            appendLine("plain paragraph text")
        }

        val blocks = parseMarkdown(markdown)

        // The key contract: parsing terminates and keeps every line.
        assertTrue(blocks.isNotEmpty())
        assertTrue(blocks.any { it is MarkdownBlock.Header && it.text == "regular heading" })
        assertTrue(blocks.any { it is MarkdownBlock.Paragraph && it.text.contains("#tag") })
    }

    @Test
    fun badgesMergeIntoOneSmallRowAndFlagsBadgeUrls() {
        val markdown = buildString {
            appendLine("[![Build](https://img.shields.io/badge/build-passing-green)](https://example.com/ci)")
            appendLine("![Coverage](https://img.shields.io/badge/coverage-97%25-blue)")
        }

        val rows = parseMarkdown(markdown).filterIsInstance<MarkdownBlock.ImageRow>()

        assertEquals(1, rows.size)
        assertEquals(2, rows.first().images.size)
        assertTrue(rows.first().images.all { it.isBadge })
    }

    @Test
    fun screenshotLinesGetTheirOwnFullWidthRow() {
        val markdown = buildString {
            appendLine("![Build](https://img.shields.io/badge/build-passing-green)")
            appendLine("![hero](docs/hero.png)")
        }

        val rows = parseMarkdown(markdown).filterIsInstance<MarkdownBlock.ImageRow>()

        assertEquals(2, rows.size)
        assertTrue(rows[0].images.all { it.isBadge })
        assertFalse(rows[1].images.first().isBadge)
    }

    @Test
    fun inlineImagesAreExtractedFromParagraphs() {
        val markdown = "Here is a shot ![screen](docs/screen.png) of the app."

        val blocks = parseMarkdown(markdown)

        val paragraph = blocks.filterIsInstance<MarkdownBlock.Paragraph>().first()
        assertFalse(paragraph.text.contains("docs/screen.png"))
        assertTrue(blocks.filterIsInstance<MarkdownBlock.ImageRow>().isNotEmpty())
    }
}
