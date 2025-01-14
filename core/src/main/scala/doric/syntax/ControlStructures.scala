package doric
package syntax

private[syntax] trait ControlStructures {

  /**
    * Initialize a when builder
    * @group Control structure
    * @tparam T
    *   the type of the returnign DoricColumn
    * @return
    *   WhenBuilder instance to add the required logic.
    */
  def when[T]: WhenBuilder[T] = WhenBuilder()
}
