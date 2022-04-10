package doric
package syntax

import doric.types.{BinaryType, SparkType}
import doric.DoricColumnPrivateAPI._

import org.apache.spark.sql.{functions => f}

private[syntax] trait BinaryColumns {

  /**
    * Concatenates multiple binary columns together into a single column.
    *
    * @group Binary Type
    * @param col
    *   the first binary column
    * @param cols
    *   the binary columns
    * @return
    *   Doric Column with the concatenation.
    * @see [[org.apache.spark.sql.functions.concat]]
    */
  def concatBinary(
      col: BinaryColumn,
      cols: BinaryColumn*
  ): BinaryColumn =
    (col +: cols).toList.mapDC(f.concat(_: _*))

  implicit class BinaryOperationsSyntax[T: BinaryType: SparkType](
      column: DoricColumn[T]
  ) {

    /**
      * Calculates the MD5 digest of a binary column and returns the value
      * as a 32 character hex string.
      *
      * @group Binary Type
      * @see [[org.apache.spark.sql.functions.md5]]
      */
    def md5: StringColumn = column.mapDC(f.md5)

    /**
      * Calculates the SHA-1 digest of a binary column and returns the value
      * as a 40 character hex string.
      *
      * @group Binary Type
      * @see [[org.apache.spark.sql.functions.sha1]]
      */
    def sha1: StringColumn = column.mapDC(f.sha1)

    /**
      * Calculates the SHA-2 family of hash functions of a binary column and
      * returns the value as a hex string.
      *
      * @throws java.lang.IllegalArgumentException if numBits is not in the permitted values
      * @group Binary Type
      * @see [[org.apache.spark.sql.functions.sha2]]
      */
    def sha2(numBits: Int): StringColumn =
      column.mapDC(x => f.sha2(x, numBits))

    /**
      * Calculates the cyclic redundancy check value (CRC32) of a binary column and
      * returns the value as a long column.
      *
      * @group Binary Type
      * @see [[org.apache.spark.sql.functions.crc32]]
      */
    def crc32: LongColumn = column.mapDC(f.crc32)

    /**
      * Computes the BASE64 encoding of a binary column and returns it as a string column.
      * This is the reverse of unbase64.
      *
      * @group Binary Type
      * @see [[org.apache.spark.sql.functions.base64]]
      */
    def base64: StringColumn = column.mapDC(f.base64)
  }

}
