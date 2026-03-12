package doric.syntax

private[doric] trait All
//    extends ArrayColumns
    extends TypeMatcher
    with CommonColumns
    with DStructs
    with LiteralConversions
    with MapColumns
    with NumericColumns
    with DateColumns
    with TimestampColumns
    with BooleanColumns
    with StringColumns
    with ControlStructures
    with AggregationColumns
    with CNameOps
    with BinaryColumns
    with Interpolators
