/*
                                                                
Copyright� 2006 by BBN Technologies and University of Maryland (UMD)

License: LGPL v. 2.1.  See LICENSE.txt.

TERshift.java v1
Matthew Snover (snover@cs.umd.edu)                           

*/

package ter.core;

import java.util.List;

/* This is just a useful class for containing shift information */
public class Shift {
  Shift () {
	start = 0;
	end = 0;
	moveto = 0;
	newloc = 0;
  }
  Shift (int _start, int _end, int _moveto, int _newloc) {
	start = _start;
	end = _end;
	moveto = _moveto;
	newloc = _newloc;
  }
//  TERshift (int _start, int _end, int _moveto, int _newloc, List _shifted) {
//	start = _start;
//	end = _end;
//	moveto = _moveto;
//	newloc = _newloc;
//    shifted = _shifted;
//  }

  public String toString() {
	String s = "[" + start + ", " + end + ", " + moveto + "/" + newloc + "]";
	if (shifted != null)
      s += " (" + shifted + ")";
	return s;
  }

  /* The distance of the shift. */
  public int distance() {       
	if (moveto < start) {
      return start - moveto;
	} else if (moveto > end) {
      return moveto - end;
	} else {
      return moveto - start;
	}
  }

  public boolean leftShift() {
	return (moveto < start);
  }

  public int size() {
	return (end - start) + 1;
  }

  public int start = 0;
  public int end = 0;
  public int moveto = 0;
  public int newloc = 0;
  public List<String> shifted = null; // The words we shifted
  public char[] alignment = null; // for pra_more output
  public String[] aftershift = null; // for pra_more output
  // This is used to store the cost of a shift, so we don't have to
  // calculate it multiple times.
  public double cost = 1.0; 
}
