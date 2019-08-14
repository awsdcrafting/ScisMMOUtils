package eu.scisneromam.mc.utils

import java.util.stream.Collectors
import java.util.stream.IntStream

class TableRenderer(private val center: Boolean = true, private val distance : Int = 1)
{

    private var width: Int = 0
    //private List<Object> header;
    private var header: List<String>? = null
    //private List<List<Object>> table = new ArrayList<>();
    private val table = ArrayList<List<String>>()

    private var empty = ""

    fun clear()
    {
        header = null
        table.clear()
        width = 0
    }

    fun setHeader(vararg header: Any)
    {
        val strings = ArrayList<String>()
        for (`object` in header)
        {
            strings.add(repeat(" ", distance) + `object`.toString().replace("\t", "    ") + repeat(" ", distance))
        }
        //this.header = Arrays.asList(header);
        this.header = strings
        if (header.size > this.width)
            this.width = header.size
    }

    fun addRow(vararg row: Any)
    {
        val strings = ArrayList<String>()
        for (`object` in row)
        {
            strings.add(repeat(" ", distance) + `object`.toString().replace("\t", "    ") + repeat(" ", distance))
        }
        table.add(strings)
        if (row.size > this.width)
            this.width = row.size
    }

    fun setEmptyString(str: String)
    {
        this.empty = str
    }

    private fun normalizeTable(): Array<Array<String>>
    {
        val height = if (header == null) table.size else table.size + 1
        val normalized = Array(height) { Array(width) { "" } }

        var vIndex = 0
        if (header != null)
        {
            for (hIndex in 0 until width)
            {
                if (header!!.size > hIndex)
                //normalized[vIndex][hIndex] = header.get(hIndex).toString();
                    normalized[vIndex][hIndex] = header!![hIndex]
                else
                    normalized[vIndex][hIndex] = this.empty
            }
            vIndex++
        }

        //for (List<Object> obj : table)
        for (obj in table)
        {
            for (hIndex in 0 until width)
            {
                if (obj.size > hIndex)
                //normalized[vIndex][hIndex] = obj.get(hIndex).toString();
                    normalized[vIndex][hIndex] = obj[hIndex]
                else
                    normalized[vIndex][hIndex] = this.empty
            }
            vIndex++
        }

        return normalized
    }

    private fun getColumnWidths(
        table: Array<Array<String>>,
        padding: Int): IntArray
    {
        val columns = IntArray(width)
        for (aTable in table)
            for (hIndex in 0 until width)
                if (aTable[hIndex].length + padding > columns[hIndex])
                    columns[hIndex] = aTable[hIndex].length + padding
        columns[columns.size - 1] -= padding
        return columns
    }

    private fun buildElement(element: String, width: Int, emptyChar: String): String
    {
        var result = element
        if (result.length < width)

            if (center)
            {
                result = result.center(width, emptyChar)
            } else
            {
                result += repeat(emptyChar, width - result.length)
            }
        return result
    }

    private fun buildLine(strings: Array<String>, widths: IntArray, header: Boolean): String
    {
        var line =
            IntStream.range(0, strings.size).mapToObj { i -> buildElement(strings[i], widths[i], " ") }.collect(Collectors.joining("│"))

        if (header)
        {
            val separator =
                IntStream.range(0, strings.size).mapToObj { i -> buildElement("", widths[i], "═") }.collect(Collectors.joining("╪"))
            line += "\n" + separator
        }
        return line
    }

    fun build(): String
    {
        val table = normalizeTable()
        val widths = getColumnWidths(table, 1)
        return IntStream.range(0, table.size).mapToObj { i -> buildLine(table[i], widths, header != null && i == 0) }
            .collect(Collectors.joining("\n"))
    }

    /**
     * Added by scisneromam
     */
    fun buildWithSingleFrame(): String
    {
        val table = normalizeTable()
        val widths = getColumnWidths(table, 1)
        var endString = StringBuilder(
            IntStream.range(0, table.size).mapToObj { i ->
                buildLine(
                    table[i],
                    widths,
                    header != null && i == 0)
            }.collect(Collectors.joining("\n")))

        val firstFrameLine = StringBuilder("┌")

        for (i in 0 until width)
        {
            if (i > 0)
            {
                firstFrameLine.append("┬")
            }
            for (x in 0 until widths[i])
            {
                firstFrameLine.append("─")
            }
        }
        firstFrameLine.append("┐\n")

        val lines = endString.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in lines.indices)
        {
            if (lines[i].startsWith("═"))
            {
                lines[i] = "╞" + lines[i]
            } else
            {
                lines[i] = "│" + lines[i]
            }
            if (lines[i].endsWith("═"))
            {
                lines[i] = lines[i] + "╡"
            } else
            {
                lines[i] = lines[i] + "│"
            }
            lines[i] = lines[i] + "\n"
        }

        val lastFrameLine = StringBuilder("└")

        for (i in 0 until width)
        {
            if (i > 0)
            {
                lastFrameLine.append("┴")
            }
            for (x in 0 until widths[i])
            {
                lastFrameLine.append("─")
            }
        }
        lastFrameLine.append("┘")

        endString = StringBuilder(firstFrameLine.toString())
        for (line in lines)
        {
            endString.append(line)
        }
        endString.append(lastFrameLine)

        return endString.toString()
    }

    /**
     * Added by scisneromam
     */
    fun buildWithDoubleFrame(): String
    {
        val table = normalizeTable()
        val widths = getColumnWidths(table, 1)
        var endString = StringBuilder(
            IntStream.range(0, table.size).mapToObj { i ->
                buildLine(
                    table[i],
                    widths,
                    header != null && i == 0)
            }.collect(Collectors.joining("\n")))

        val firstFrameLine = StringBuilder("╔")

        for (i in 0 until width)
        {
            if (i > 0)
            {
                firstFrameLine.append("╤")
            }
            for (x in 0 until widths[i])
            {
                firstFrameLine.append("═")
            }
        }
        firstFrameLine.append("╗\n")

        val lines = endString.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in lines.indices)
        {
            if (lines[i].startsWith("═"))
            {
                lines[i] = "╠" + lines[i]
            } else
            {
                lines[i] = "║" + lines[i]
            }
            if (lines[i].endsWith("═"))
            {
                lines[i] = lines[i] + "╣"
            } else
            {
                lines[i] = lines[i] + "║"
            }
            lines[i] = lines[i] + "\n"
        }

        val lastFrameLine = StringBuilder("╚")

        for (i in 0 until width)
        {
            if (i > 0)
            {
                lastFrameLine.append("╧")
            }
            for (x in 0 until widths[i])
            {
                lastFrameLine.append("═")
            }
        }
        lastFrameLine.append("╝")

        endString = StringBuilder(firstFrameLine.toString())
        for (line in lines)
        {
            endString.append(line)
        }
        endString.append(lastFrameLine)

        return endString.toString()
    }

}
