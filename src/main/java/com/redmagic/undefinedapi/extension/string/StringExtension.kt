package com.redmagic.undefinedapi.extension.string

import com.redmagic.undefinedapi.utils.RGBUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.md_5.bungee.api.ChatColor
import java.util.regex.Matcher
import java.util.regex.Pattern

private val miniMessage = MiniMessage.miniMessage()

/**
 * Converts the given string to small text.
 *
 * @return The converted string in small text.
 */
fun String.toSmallText(): String {
    return uppercase()
        .replace("Q", "ꞯ")
        .replace("W", "ᴡ")
        .replace("E", "ᴇ")
        .replace("R", "ʀ")
        .replace("T", "ᴛ")
        .replace("Y", "ʏ")
        .replace("U", "ᴜ")
        .replace("I", "ɪ")
        .replace("O", "ᴏ")
        .replace("P", "ᴘ")
        .replace("A", "ᴀ")
        .replace("S", "ѕ")
        .replace("D", "ᴅ")
        .replace("F", "ꜰ")
        .replace("G", "ɢ")
        .replace("H", "ʜ")
        .replace("J", "ᴊ")
        .replace("K", "ᴋ")
        .replace("L", "ʟ")
        .replace("Z", "ᴢ")
        .replace("X", "x")
        .replace("C", "ᴄ")
        .replace("V", "ᴠ")
        .replace("B", "ʙ")
        .replace("N", "ɴ")
        .replace("M", "ᴍ")
}

fun String.toComponent(): Component = miniMessage.deserialize(this)

private val replacements = mapOf(
    "<BLACK>" to ChatColor.BLACK.toString(),
    "<dark_blue>" to ChatColor.DARK_BLUE.toString(),
    "<dark_green>" to ChatColor.DARK_GREEN.toString(),
    "<dark_aqua>" to ChatColor.DARK_AQUA.toString(),
    "<dark_red>" to ChatColor.DARK_RED.toString(),
    "<dark_purple>" to ChatColor.DARK_PURPLE.toString(),
    "<gold>" to ChatColor.GOLD.toString(),
    "<gray>" to ChatColor.GRAY.toString(),
    "<dark_gray>" to ChatColor.DARK_GRAY.toString(),
    "<blue>" to ChatColor.BLUE.toString(),
    "<green>" to ChatColor.GREEN.toString(),
    "<aqua>" to ChatColor.AQUA.toString(),
    "<red>" to ChatColor.RED.toString(),
    "<light_purple>" to ChatColor.LIGHT_PURPLE.toString(),
    "<yellow>" to ChatColor.YELLOW.toString(),
    "<white>" to ChatColor.WHITE.toString(),
    "<magic>" to ChatColor.MAGIC.toString(),
    "<bold>" to ChatColor.BOLD.toString(),
    "<strikethrough>" to ChatColor.STRIKETHROUGH.toString(),
    "<underline>" to ChatColor.UNDERLINE.toString(),
    "<italic>" to ChatColor.ITALIC.toString(),
    "<reset>" to ChatColor.RESET.toString()
)

private val hexPattern: Regex = """<#([A-Fa-f0-9]{6})>""".toRegex()
private val rgbPattern: Regex = """<rgb\((\d{1,3}),(\d{1,3}),(\d{1,3})\)>""".toRegex()
fun String.translateColor(): String =
    replacements.entries.fold(this) { acc, (old, new) -> acc.replace(old, new, ignoreCase = true) }
        .let { originalString ->
            rgbPattern.findAll(originalString).fold(originalString) { str, matchResult ->
                val (r, g, b) = matchResult.destructured.toList().map { it.toInt() }
                val replacement = "<#${RGBUtil.rgbToHex(r, g, b)}>"
                str.replace(matchResult.value, replacement)
            }
        }
        .let { newString ->
            hexPattern.findAll(newString).fold(StringBuilder(newString)) { builder, matchResult ->
                val (hex) = matchResult.destructured
                val replacement = buildString {
                    append(ChatColor.COLOR_CHAR, "x")
                    hex.chunked(2) { append(ChatColor.COLOR_CHAR, it) }
                }
                val range = matchResult.range
                builder.replace(range.first, range.last + 1, replacement)
            }.toString()
        }

