package be.peopleware.value_I;


/**
 * This class contains static convenience methods for working
 * with values. This methods do not use the {@link Value}
 * interface, because there are many classes in the outside
 * world that also use these patterns, that do not implement
 * these proprietary interfaces.
 *
 * @author Jan Dockx
 * @author PeopleWare n.v.
 */
public abstract class Values {

  /*<section name="Meta Information">*/
  //------------------------------------------------------------------

  /** {@value} */
  public static final String CVS_REVISION = "$Revision$"; //$NON-NLS-1$
  /** {@value} */
  public static final String CVS_DATE = "$Date$"; //$NON-NLS-1$
  /** {@value} */
  public static final String CVS_STATE = "$State$"; //$NON-NLS-1$
  /** {@value} */
  public static final String CVS_TAG = "$Name$"; //$NON-NLS-1$

  /*</section>*/



  /*<construction>*/
  //------------------------------------------------------------------

  private Values() {
    // NOP
  }

  /*</construction>*/


  /**
   * Is one value {@link Object#equals(java.lang.Object) equal}
   * to another, if <code>null</code> is also allowed as value
   * for a property.
   *
   * @return    (one == null)
   *                ? (other == null)
   *                : one.equals(other);
   */
  public static boolean equalsWithNull(final Object one, final Object other) {
    return (one == null)
              ? (other == null)
              : one.equals(other);
  }


  /**
   * Assert that <code>p</code> is needed at least to make
   * <code>result</code> <code>true</code>.
   * 
   * @param p
   *        A boolean expression that has to be <code>true</code> at least
   *        to make <code>result</code> <code>true</code>.
   * @param result
   *        The result to make <code>true</code>.
   * @return ! p ==> ! result;
   */
  public static boolean assertAtLeast(boolean p, boolean result) {
    return p || (! result);
  }

}