package doric
package syntax
/*
trait MapColumns3x {

  /**
    * Extension methods for Map Columns
    *
    * @group Map Type
    */
  implicit class MapColumnOps3x[K: SparkType, V: SparkType](
      private val map: MapColumn[K, V]
  ) {

    /**
      * Returns a map whose key-value pairs satisfy a predicate.
      *
      * @example {{{
      *   df.select(colMap("m").filter((k, v) => k * 10 === v))
      * }}}
      *
      * @group Map Type
      * @see [[org.apache.spark.sql.functions.map_filter]]
      */
    /*def filter(
        function: (DoricColumn[K], DoricColumn[V]) => BooleanColumn
    ): MapColumn[K, V] = {
      val xv: DoricColumn[K] = x[MapColumn[K, V]]
      (
        map.elem,
        function(map.keys.getIndex(0), map.values.getIndex(0)).elem,
        function(xv).elem,
        xv.elem
      ).mapN { (a, func, x) =>
        df.fn("filter", a, createLambda(func, x))
      }.toDC
    }*/

    /**
      * Merge two given maps, key-wise into a single map using a function.
      *
      * @example {{{
      *   df.select(colMap("m1").zipWith(col("m2"), (k, v1, v2) => k === v1 + v2))
      * }}}
      *
      * @group Map Type
      * @see [[org.apache.spark.sql.functions.map_zip_with]]
      */
    def zipWith[V2: SparkType, R](
        map2: MapColumn[K, V2],
        function: (
            DoricColumn[K],
            DoricColumn[V],
            DoricColumn[V2]
        ) => DoricColumn[R]
    ): MapColumn[K, R] = {
      val xv: DoricColumn[K]  = x[K]
      val yv: DoricColumn[V]  = y[V]
      val zv: DoricColumn[V2] = z[V2]
      (
        map.elem,
        map2.elem,
        function(
          map.keys.getIndex(0),
          map.values.getIndex(0),
          map2.values.getIndex(0)
        ).elem,
        xv.elem,
        yv.elem,
        zv.elem
      ).mapN ( (a, b, func, x, y, z) => df.fn("zip_with", a, b, createLambda(func, x, y, z)))
      .toDC
    }

    /**
      * Applies a function to every key-value pair in a map and returns
      * a map with the results of those applications as the new keys for the pairs.
      *
      * @example {{{
      *   df.select(colMap("m").transformKeys((k, v) => k + v))
      * }}}
      *
      * @group Map Type
      * @see [[org.apache.spark.sql.functions.transform_keys]]
      */
    def transformKeys[K2](
        function: (DoricColumn[K], DoricColumn[V]) => DoricColumn[K2]
    ): MapColumn[K2, V] = {
      val xv: DoricColumn[K] = x[K]
      val yv: DoricColumn[V] = y[V]
      (
        map.elem,
        function(map.keys.getIndex(0), map.values.getIndex(0)).elem,
        function(xv, yv).elem,
        xv.elem,
        yv.elem
      ).mapN ( (a, _, f, x, y) => df.fn("transform_keys", a, createLambda(f, x, y))).toDC
    }

    /**
      * Applies a function to every key-value pair in a map and returns
      * a map with the results of those applications as the new values for the pairs.
      *
      * @example {{{
      *   df.select(colMap("m").transformValues((k, v) => k + v))
      * }}}
      *
      * @group Map Type
      * @see [[org.apache.spark.sql.functions.transform_values]]
      */
    def transformValues[V2](
        function: (DoricColumn[K], DoricColumn[V]) => DoricColumn[V2]
    ): MapColumn[K, V2] = {
      val xv: DoricColumn[K] = x[K]
      val yv: DoricColumn[V] = y[V]
      (
        map.elem,
        function(map.keys.getIndex(0), map.values.getIndex(0)).elem,
        function(xv, yv).elem,
        xv.elem,
        yv.elem
      ).mapN( (a, _, f, x, y) => df.fn("transform_values", a, createLambda(f, x, y)))
        .toDC
    }

    /**
      * Returns an unordered array of all entries in the given map.
      *
      * @group Map Type
      * @see [[org.apache.spark.sql.functions.map_entries]]
      */
    def mapEntries: ArrayColumn[Row] = map.elem.map(f.map_entries).toDC

  }
}
*/
