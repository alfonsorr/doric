package doric
package syntax

import doric.DoricColumnPrivateAPI._

import org.apache.spark.sql.{Column, functions => f}
import org.apache.spark.sql.catalyst.expressions._

private[syntax] trait StringColumns {

  /**
    * Concatenate string columns to form a single one
    *
    * @group String Type
    * @param cols
    *   the String DoricColumns to concatenate
    * @return
    *   a reference of a single DoricColumn with all strings concatenated. If at
    *   least one is null will return null.
    * @see [[org.apache.spark.sql.functions.concat]]
    */
  def concat(cols: StringColumn*): StringColumn =
    cols.toList.mapDC(f.concat(_: _*))

  /**
    * Concatenates multiple input string columns together into a single string column,
    * using the given separator.
    *
    * @note even if `cols` contain null columns, it prints remaining string columns (or empty string).
    * @example {{{
    * df.withColumn("res", concatWs("-".lit, col("col1"), col("col2")))
    *   .show(false)
    *     +----+----+----+
    *     |col1|col2| res|
    *     +----+----+----+
    *     |   1|   1| 1-1|
    *     |null|   2|   2|
    *     |   3|null|   3|
    *     |null|null|    |
    *     +----+----+----+
    * }}}
    * @group String Type
    * @see [[org.apache.spark.sql.functions.concat_ws]]
    */
  def concatWs(sep: StringColumn, cols: StringColumn*): StringColumn =
    (sep +: cols).toList
      .mapDC(l => {
        new Column(ConcatWs(l.map(_.expr)))
      })

  /**
    * Formats the arguments in printf-style and returns the result as a string
    * column.
    *
    * @group String Type
    * @param format
    * Printf format
    * @param arguments
    * the String DoricColumns to format
    * @return
    * Formats the arguments in printf-style and returns the result as a string
    * column.
    * @see [[org.apache.spark.sql.functions.format_string]]
    */
  def formatString(
      format: StringColumn,
      arguments: DoricColumn[_]*
  ): StringColumn =
    (format :: arguments.toList)
      .mapDC(args =>
        new Column(FormatString((args.head +: args.tail).map(_.expr): _*))
      )

  /**
    * Creates a string column for the file name of the current Spark task.
    *
    * @group String Type
    * @see [[org.apache.spark.sql.functions.input_file_name]]
    */
  def inputFileName(): StringColumn = DoricColumn(f.input_file_name)

  /**
    * Creates a string column for the file name of the current Spark task.
    *
    * @group String Type
    * @see [[inputFileName]]
    */
  @inline def sparkTaskName(): StringColumn = inputFileName()

  /**
    * Unique column operations
    *
    * @param s
    *   Doric String column
    */
  implicit class StringOperationsSyntax(s: DoricColumn[String]) {

    /**
      * ********************************************************
      *             SPARK SQL EQUIVALENT FUNCTIONS
      * ********************************************************
      */

    /**
      * Computes the numeric value of the first character of the string column,
      * and returns the result as an int column.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.ascii]]
      */
    def ascii: IntegerColumn = s.mapDC(f.ascii)

    /**
      * Returns a new string column by converting the first letter of each word
      * to uppercase. Words are delimited by whitespace.
      *
      * @example For example, "hello world" will become "Hello World".
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.initcap]]
      */
    def initCap: StringColumn = s.mapDC(f.initcap)

    /**
      * Locate the position of the first occurrence of substr column in the
      * given string. Returns null if either of the arguments are null.
      *
      * @group String Type
      * @note
      * The position is not zero based, but 1 based index. Returns 0 if substr
      * could not be found in str.
      * @see [[org.apache.spark.sql.functions.instr]]
      */
    def inStr(substring: StringColumn): IntegerColumn =
      (s, substring)
        .mapNDC((str, substr) => {
          new Column(StringInstr(str.expr, substr.expr))
        })

    /**
      * Computes the character length of a given string or number of bytes of a
      * binary string. The length of character strings include the trailing
      * spaces. The length of binary strings includes binary zeros.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.length]]
      */
    def length: IntegerColumn = s.mapDC(f.length)

    /**
      * Computes the Levenshtein distance of the two given string columns.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.levenshtein]]
      */
    def levenshtein(dc: StringColumn): IntegerColumn =
      (s, dc).mapNDC(f.levenshtein)

    /**
      * Locate the position of the first occurrence of substr in a string
      * column, after position pos.
      *
      * @group String Type
      * @note
      * The position is not zero based, but 1 based index. returns 0 if substr
      * could not be found in str.
      * @see org.apache.spark.sql.functions.locate
      * @todo scaladoc link (issue #135)
      */
    def locate(
        substr: StringColumn,
        pos: IntegerColumn = 1.lit
    ): IntegerColumn =
      (substr, s, pos)
        .mapNDC((substring, str, position) => {
          new Column(StringLocate(substring.expr, str.expr, position.expr))
        })

    /**
      * Converts a string column to lower case.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.lower]]
      */
    def lower: StringColumn = s.mapDC(f.lower)

    /**
      * Left-pad the string column with pad to a length of len. If the string
      * column is longer than len, the return value is shortened to len
      * characters.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.lpad]]
      */
    def lpad(len: IntegerColumn, pad: StringColumn): StringColumn =
      (s, len, pad)
        .mapNDC((str, lenCol, lpad) => {
          new Column(StringLPad(str.expr, lenCol.expr, lpad.expr))
        })

    /**
      * Trim the spaces from left end for the specified string value.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.ltrim(e:org\.apache\.spark\.sql\.Column):* org.apache.spark.sql.functions.ltrim]]
      */
    def ltrim: StringColumn = s.mapDC(f.ltrim)

    /**
      * Trim the specified character string from left end for the specified
      * string column.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.ltrim(e:org\.apache\.spark\.sql\.Column,trimString:* org.apache.spark.sql.functions.ltrim]]
      */
    def ltrim(trimString: StringColumn): StringColumn =
      (s, trimString)
        .mapNDC((str, trimStr) => {
          new Column(StringTrimLeft(str.expr, trimStr.expr))
        })

    /**
      * Overlay the specified portion of `src` with `replace`, starting from
      * byte position `pos` of `src` and proceeding for `len` bytes.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.overlay(src:org\.apache\.spark\.sql\.Column,replace:org\.apache\.spark\.sql\.Column,pos:org\.apache\.spark\.sql\.Column,len:org\.apache\.spark\.sql\.Column):* org.apache.spark.sql.functions.overlay]]
      */
    def overlay(
        replace: StringColumn,
        pos: IntegerColumn,
        len: IntegerColumn = (-1).lit
    ): StringColumn =
      (s, replace, pos, len).mapNDC(f.overlay)

    /**
      * Extract a specific group matched by a Java regex, from the specified
      * string column. If the regex did not match, or the specified group did
      * not match, an empty string is returned. if the specified group index
      * exceeds the group count of regex, an IllegalArgumentException will be
      * thrown.
      *
      * @throws java.lang.IllegalArgumentException if the specified group index exceeds the group count of regex
      * @group String Type
      * @see [[org.apache.spark.sql.functions.regexp_extract]]
      */
    def regexpExtract(
        exp: StringColumn,
        groupIdx: IntegerColumn
    ): StringColumn =
      (s, exp, groupIdx)
        .mapNDC((str, regexp, gIdx) =>
          new Column(RegExpExtract(str.expr, regexp.expr, gIdx.expr))
        )

    /**
      * Replace all substrings of the specified string value that match regexp
      * with replacement.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.regexp_replace(e:org\.apache\.spark\.sql\.Column,pattern:org\.apache\.spark\.sql\.Column,* org.apache.spark.sql.functions.regexp_replace]]
      */
    def regexpReplace(
        pattern: StringColumn,
        replacement: StringColumn
    ): StringColumn =
      (s, pattern, replacement).mapNDC(f.regexp_replace)

    /**
      * Repeats a string column n times, and returns it as a new string column.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.repeat]]
      */
    def repeat(n: IntegerColumn): StringColumn = (s, n)
      .mapNDC((str, times) => new Column(StringRepeat(str.expr, times.expr)))

    /**
      * Right-pad the string column with pad to a length of len. If the string
      * column is longer than len, the return value is shortened to len
      * characters.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.rpad]]
      */
    def rpad(len: IntegerColumn, pad: StringColumn): StringColumn =
      (s, len, pad)
        .mapNDC((str, l, p) => new Column(StringRPad(str.expr, l.expr, p.expr)))

    /**
      * Trim the spaces from right end for the specified string value.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.rtrim(e:org\.apache\.spark\.sql\.Column):* org.apache.spark.sql.functions.rtrim]]
      */
    def rtrim: StringColumn = s.mapDC(f.rtrim)

    /**
      * Trim the specified character string from right end for the specified
      * string column.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.rtrim(e:org\.apache\.spark\.sql\.Column,trimString:* org.apache.spark.sql.functions.rtrim]]
      */
    def rtrim(trimString: StringColumn): StringColumn =
      (s, trimString)
        .mapNDC((str, t) => new Column(StringTrimRight(str.expr, t.expr)))

    /**
      * Returns the soundex code for the specified expression.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.soundex]]
      */
    def soundex: StringColumn = s.mapDC(f.soundex)

    /**
      * Splits str around matches of the given pattern.
      *
      * @group String Type
      * @param pattern
      *   a string representing a regular expression. The regex string should be
      *   a Java regular expression.
      * @param limit
      *   an integer expression which controls the number of times the regex is applied.
      *     - __limit greater than 0__: The resulting array's length
      *       will not be more than limit, and the resulting array's last entry will
      *       contain all input beyond the last matched regex.
      *     - __limit less than or equal to 0__: `regex` will be applied as many times as possible,
      *       and the resulting array can be of any size.
      * @see org.apache.spark.sql.functions.split
      * @todo scaladoc link (issue #135)
      */
    def split(
        pattern: StringColumn,
        limit: IntegerColumn = (-1).lit
    ): ArrayColumn[String] =
      (s, pattern, limit)
        .mapNDC((str, p, l) =>
          new Column(StringSplit(str.expr, p.expr, l.expr))
        )

    /**
      * Substring starts at `pos` and is of length `len` when str is String type
      * or returns the slice of byte array that starts at `pos` in byte and is
      * of length `len` when str is Binary type
      *
      * @group String Type
      * @note
      * The position is not zero based, but 1 based index.
      * @see [[org.apache.spark.sql.functions.substring]]
      */
    def substring(pos: IntegerColumn, len: IntegerColumn): StringColumn =
      (s, pos, len)
        .mapNDC((str, p, l) => new Column(Substring(str.expr, p.expr, l.expr)))

    /**
      * Returns the substring from string str before count occurrences of the
      * delimiter delim. If count is positive, everything the left of the final
      * delimiter (counting from left) is returned. If count is negative, every
      * to the right of the final delimiter (counting from the right) is
      * returned. substring_index performs a case-sensitive match when searching
      * for delim.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.substring_index]]
      */
    def substringIndex(
        delim: StringColumn,
        count: IntegerColumn
    ): StringColumn =
      (s, delim, count)
        .mapNDC((str, d, c) =>
          new Column(SubstringIndex(str.expr, d.expr, c.expr))
        )

    /**
      * Translate any character in the src by a character in replaceString. The
      * characters in replaceString correspond to the characters in
      * matchingString. The translate will happen when any character in the
      * string matches the character in the `matchingString`.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.translate]]
      */
    def translate(
        matchingString: StringColumn,
        replaceString: StringColumn
    ): StringColumn =
      (s, matchingString, replaceString)
        .mapNDC((str, m, r) =>
          new Column(StringTranslate(str.expr, m.expr, r.expr))
        )

    /**
      * Trim the spaces from both ends for the specified string column.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.trim(e:org\.apache\.spark\.sql\.Column):* org.apache.spark.sql.functions.trim]]
      */
    def trim: StringColumn = s.mapDC(f.trim)

    /**
      * Trim the specified character from both ends for the specified string
      * column (literal).
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.trim(e:org\.apache\.spark\.sql\.Column,trimString:* org.apache.spark.sql.functions.trim]]
      */
    def trim(trimString: StringColumn): StringColumn =
      (s, trimString)
        .mapNDC((str, trimStr) => {
          new Column(StringTrim(str.expr, trimStr.expr))
        })

    /**
      * Converts a string column to upper case.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.upper]]
      */
    def upper: StringColumn = s.mapDC(f.upper)

    /**
      * Returns a reversed string.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.reverse]]
      */
    def reverse: StringColumn = reverseAbstract(s)

    /**
      * ********************************************************
      *                     COLUMN FUNCTIONS
      * ********************************************************
      */

    /**
      * Contains the other element. Returns a boolean column based on a string
      * match.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.Column.contains]]
      */
    def contains(dc: StringColumn): BooleanColumn =
      (s, dc).mapNDC(_.contains(_))

    /**
      * String ends with. Returns a boolean column based on a string match.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.Column.endsWith(other:* org.apache.spark.sql.Column.endsWith]]
      */
    def endsWith(dc: StringColumn): BooleanColumn =
      (s, dc).mapNDC(_.endsWith(_))

    /**
      * SQL like expression. Returns a boolean column based on a SQL LIKE match.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.Column.like]]
      */
    def like(literal: StringColumn): BooleanColumn =
      (s, literal)
        .mapNDC((str, l) => new Column(new Like(str.expr, l.expr)))

    /**
      * SQL RLIKE expression (LIKE with Regex). Returns a boolean column based
      * on a regex match.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.Column.rlike]]
      */
    def rLike(literal: StringColumn): BooleanColumn =
      (s, literal)
        .mapNDC((str, regex) => new Column(RLike(str.expr, regex.expr)))

    /**
      * String starts with. Returns a boolean column based on a string match.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.Column.startsWith(other:* org.apache.spark.sql.Column.startsWith]]
      */
    def startsWith(dc: StringColumn): BooleanColumn =
      (s, dc).mapNDC(_.startsWith(_))

    /**
      * Same as rLike doric function.
      *
      * SQL RLIKE expression (LIKE with Regex). Returns a boolean column based
      * on a regex match.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.Column.rlike]]
      */
    def matchRegex(literal: StringColumn): BooleanColumn = rLike(literal)

    /**
      * Computes the first argument into a binary from a string using the provided character set
      * (one of 'US-ASCII', 'ISO-8859-1', 'UTF-8', 'UTF-16BE', 'UTF-16LE', 'UTF-16').
      * If either argument is null, the result will also be null.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.encode]]
      */
    def encode(charset: StringColumn): BinaryColumn =
      (s, charset)
        .mapNDC((col, char) => {
          new Column(Encode(col.expr, char.expr))
        })

    /**
      * Decodes a BASE64 encoded string column and returns it as a binary column.
      * This is the reverse of base64.
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.unbase64]]
      */
    def unbase64: BinaryColumn = s.mapDC(f.unbase64)

    /**
      * Converts date/timestamp to Unix timestamp (in seconds),
      * using the default timezone and the default locale.
      *
      * @return
      * A long
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.unix_timestamp(s:org\.apache\.spark\.sql\.Column):* org.apache.spark.sql.functions.unix_timestamp]]
      */
    def unixTimestamp: LongColumn = s.mapDC(f.unix_timestamp)

    /**
      * Converts date/timestamp with given pattern to Unix timestamp (in seconds).
      *
      * @return
      * A long, or null if the input was a string not of the correct format
      * @throws java.lang.IllegalArgumentException if invalid pattern
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.unix_timestamp(s:org\.apache\.spark\.sql\.Column,p:* org.apache.spark.sql.functions.unix_timestamp]]
      */
    def unixTimestamp(pattern: StringColumn): LongColumn =
      (s, pattern)
        .mapNDC((c, p) => {
          new Column(UnixTimestamp(c.expr, p.expr))
        })

    /**
      * ********************************************************
      *                     DORIC FUNCTIONS
      * ********************************************************
      */

    /**
      * Similar to concat doric function, but only with two columns
      *
      * @group String Type
      * @see [[org.apache.spark.sql.functions.concat]]
      */
    def +(s2: StringColumn): StringColumn = concat(s, s2)

    /**
      * Converts the column into a `DateType` with a specified format
      *
      * See <a
      * href="https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html">
      * Datetime Patterns</a> for valid date and time format patterns
      *
      * @group String Type
      * @param format
      * A date time pattern detailing the format of `e` when `e`is a string
      * @return
      * A date, or null if `e` was a string that could not be cast to a date
      * or `format` was an invalid format
      * @see [[org.apache.spark.sql.functions.to_date(e:org\.apache\.spark\.sql\.Column,fmt:* org.apache.spark.sql.functions.to_date]]
      */
    def toDate(format: StringColumn): LocalDateColumn =
      (s, format)
        .mapNDC((str, dateFormat) =>
          new Column(new ParseToDate(str.expr, dateFormat.expr))
        )

    /**
      * Converts time string with the given pattern to timestamp.
      *
      * See <a
      * href="https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html">
      * Datetime Patterns</a> for valid date and time format patterns
      *
      * @group String Type
      * @param format
      * A date time pattern detailing the format of `s` when `s` is a string
      * @return
      * A timestamp, or null if `s` was a string that could not be cast to a
      * timestamp or `format` was an invalid format
      * @see [[org.apache.spark.sql.functions.to_timestamp(s:org\.apache\.spark\.sql\.Column,fmt:* org.apache.spark.sql.functions.to_timestamp]]
      */
    def toTimestamp(format: StringColumn): InstantColumn =
      (s, format)
        .mapNDC((str, tsFormat) =>
          new Column(new ParseToTimestamp(str.expr, tsFormat.expr))
        )

  }
}
