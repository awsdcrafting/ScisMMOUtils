package eu.scisneromam.mc.scismmoutils.utils

import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*


/**
 * Project: DiscordBot
 * Initially created by scisneromam on 10.02.2019.
 * @author scisneromam
 * ---------------------------------------------------------------------
 * Copyright © 2019 | scisneromam | All rights reserved.
 */


val ZERO_WIDTH_SPACE = "\u200B"

fun StringBuilder.pad(paddingAmount: Int = 1, padChars: String = " ", prefix: String = "", postFix: String = ""): StringBuilder
{
    this.append(prefix).append(padChars.repeat(paddingAmount)).append(postFix)
    return this
}

fun String.multiLinePad(
    paddingLength: Int,
    padChars: String = "─",
    firstLinePad: String = "┌",
    linePad: String = "├",
    lastLinePad: String = "└",
    newLineChar: String = "\n"): String
{
    val builder = StringBuilder()
    val listIterator = this.lines().listIterator()
    var first = true
    while (listIterator.hasNext())
    {
        val string = listIterator.next()
        when
        {
            !listIterator.hasNext() -> builder.append(lastLinePad)
            first ->
            {
                builder.append(firstLinePad)
                first = false
            }
            listIterator.hasNext() -> builder.append(linePad)
            else ->
            {
                builder.append(linePad)
            }
        }

        builder.append(padChars.repeat(paddingLength - 1))
        builder.append(string)
        builder.append(newLineChar)
    }

    return builder.toString()
}

fun StringBuilder.multiLinePad(
    paddingLength: Int,
    padChars: String = "─",
    firstLinePad: String = "┌",
    linePad: String = "├",
    lastLinePad: String = "└",
    newLineChar: String = "\n"): StringBuilder
{
    val builder = StringBuilder()
    val listIterator = this.lines().listIterator()
    var first = true
    while (listIterator.hasNext())
    {
        val string = listIterator.next()
        when
        {
            !listIterator.hasNext() -> builder.append(lastLinePad)
            first ->
            {
                builder.append(firstLinePad)
                first = false
            }
            listIterator.hasNext() -> builder.append(linePad)
            else ->
            {
                builder.append(linePad)
            }
        }

        builder.append(padChars.repeat(paddingLength - 1))
        builder.append(string)
        builder.append(newLineChar)
    }

    return builder
}

/**
 * The 'old' way to get the stacktrace
 * probably does not get everything
 * is only used if the 'new' way returns null or an empty string
 */
fun Throwable.errorMessageLegacy(): String
{
    val trace = StringBuilder()
    for (traceElement in this.stackTrace)
    {
        trace.append("\tat ").append(traceElement).append("\n")
    }
    var returnString = "Error: "
    val causeMessageBuilder = StringBuilder()
    var cause: Throwable? = this.cause
    while (cause != null)
    {
        causeMessageBuilder.insert(0, cause.toString()).append(" - ")
        cause = cause.cause
    }
    returnString += causeMessageBuilder.toString()
    returnString += this.message + "\n" + trace.toString()
    return returnString
}

/**
 * Converts the stacktrace of an exception to a string
 *
 * @return The stacktrace of the exception as string
 */
fun Throwable.errorMessage(): String
{
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    this.printStackTrace(printWriter)
    val returnString = stringWriter.toString()
    return if (!returnString.isBlank())
    {
        returnString
    } else
    {
        this.errorMessageLegacy()
    }
}

/**
 * Splits the input string, by an separator but only after n chars
 * The separator will be included in the output
 *
 * @param split  The separator
 * @param length The amount of chars to be splitted after
 * @return The String splitted into an array
 */
fun String.splitByAfterNChars(split: String, length: Int, secondSplit: String = ""): List<String>
{
    val splitRegex = "(?<=$split)"
    val splitted = this.split(splitRegex.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val stringList = ArrayList<String>()
    var stringBuilder = StringBuilder()
    for (string in splitted)
    {
        if (string.length >= length)
        {
            stringList.addAll(string.splitByAfterNChars(secondSplit, length))
        } else
        {
            stringBuilder.append(string)
        }
        if (stringBuilder.length >= length)
        {
            stringList.add(stringBuilder.toString())
            stringBuilder = StringBuilder()
        }
    }
    stringList.add(stringBuilder.toString())

    return stringList
}

/**
 * Splits the input string, by an separator but only when it would hit n chars
 * The separator will be included in the output
 *
 * @param split  The separator
 * @param length The amount of chars to be splitted
 * @return The String splitted into an array
 */
fun String.splitByBeforeNChars(split: String = "\n", length: Int = 1950, secondSplit: String = ""): List<String>
{
    val splitRegex = "(?<=$split)"
    val splitted = this.split(splitRegex.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val stringList = ArrayList<String>()
    var stringBuilder = StringBuilder()
    for (string in splitted)
    {
        if (stringBuilder.length + string.length >= length)
        {
            stringList.add(stringBuilder.toString())
            stringBuilder = StringBuilder()
        }
        if (string.length >= length)
        {
            stringList.addAll(string.splitByBeforeNChars(secondSplit, length))
        } else
        {
            stringBuilder.append(string)
        }
    }
    stringList.add(stringBuilder.toString())

    return stringList
}

fun InputStream.convertToString(): String
{
    Scanner(this).useDelimiter("\\A").use { s: Scanner -> return if (s.hasNext()) s.next() else "" }
}

fun String.center(width: Int, char: Char = ' '): String
{
    if (width <= this.length)
    {
        return this
    }
    var left = true
    var result = this
    for (i in 1..(width - this.length))
    {
        result = if (left)
        {
            "$char$result"
        } else
        {
            "$result$char"
        }

        left = !left
    }
    return result
}

fun String.center(width: Int, chars: CharSequence = " "): String
{
    if (width <= this.length)
    {
        return this
    }
    var left = true
    var result = this
    for (i in 1..(width - this.length) step chars.length)
    {
        result = if (left)
        {
            "$chars$result"
        } else
        {
            "$result$chars"
        }

        left = !left
    }
    return result
}

fun repeat(char: Char, amount: Int): String
{
    val builder = StringBuilder()
    for (i in 1..amount)
    {
        builder.append(char)
    }
    return builder.toString()
}

fun repeat(chars: CharSequence, amount: Int): String
{
    val builder = StringBuilder()
    for (i in 1..amount)
    {
        builder.append(chars)
    }
    return builder.toString()

}

fun String.toTableRenderer(center: Boolean = true, acceptBlankLines: Boolean = false): TableRenderer
{
    val renderer = TableRenderer(center)

    val lines = this.replace("\r\n", "\n").replace("\r", "\n").split("\n")

    for (line in lines)
    {
        if ((!line.isBlank() && !line.trim().isBlank()) || acceptBlankLines)
        {
            renderer.addRow(line)
        }
    }
    return renderer
}

fun String.singleLineBox(center: Boolean = true, acceptBlankLines: Boolean = false): String
{
    return this.toTableRenderer(center, acceptBlankLines).buildWithSingleFrame()
}

fun String.doubleLineBox(center: Boolean = true, acceptBlankLines: Boolean = false): String
{
    return this.toTableRenderer(center, acceptBlankLines).buildWithDoubleFrame()
}

fun String.containsAny(vararg others: CharSequence, ignoreCase: Boolean = false): Boolean
{
    for (other in others)
    {
        if (this.contains(other, ignoreCase))
        {
            return true
        }
    }
    return false
}

fun String.equalsAny(vararg others: String, ignoreCase: Boolean = false): Boolean
{
    for (other in others)
    {
        if (this.equals(other, ignoreCase))
        {
            return true
        }
    }
    return false
}

fun String.equalsAny(others: List<String>, ignoreCase: Boolean = false): Boolean
{
    for (other in others)
    {
        if (this.equals(other, ignoreCase))
        {
            return true
        }
    }
    return false
}
