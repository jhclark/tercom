/*
                                                                
Copyright� 2006 by BBN Technologies and University of Maryland (UMD)

License: LGPL v. 2.1.  See LICENSE.txt.

TERcost.java v1
Matthew Snover (snover@cs.umd.edu)                           

*/

package ter.core;


/* 

If you wish to experiment with other cost functions, (including
specific word to word cost matrices), a child class of TERcost should
be made, and an instance of it should be passed as the third arguement
to the TER function in TERcalc.

All costs must be in the range of 0.0 to 1.0.  Deviating outside of
this range may break the functionality of the TERcalc code.

This code does not do phrasal costs functions, such modification would require
changes to the TERcalc code.

In addition shifts only occur when the two subsequences are equal(),
and hash to the same location.  This can be modified from the standard
definition by using a new String data structure which redefines
those functions.

*/

public class CostFunction {
  /* For all of these functions, the score should be between 0 and 1
   * (inclusive).  If it isn't, then it will break TERcalc! */

  /* The cost of matching ref word for hyp word. (They are equal) */
  public double match_cost(String hyp, String ref) { 
      return _match_cost; 
  }

  /* The cost of substituting ref word for hyp word. (They are not equal) */
  public double substitute_cost(String hyp, String ref) {
	return _substitute_cost;
  }

  /* The cost of inserting the hyp word */
  public double insert_cost(String hyp) {
	return _insert_cost;
  }

  /* The cost of deleting the ref word */
  public double delete_cost(String ref) {
	return _delete_cost;
  }

  /* The cost of making a shift */
  public double shift_cost(Shift shift) {
      return _shift_cost;
  }

    public double _shift_cost = 1.0;
    public double _insert_cost = 1.0;
    public double _delete_cost = 1.0;
    public double _substitute_cost = 1.0;
    public double _match_cost = 0.0;

}
