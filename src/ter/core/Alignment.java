/*

Copyright� 2006 by BBN Technologies and University of Maryland (UMD)

License: LGPL v. 2.1.  See LICENSE.txt.

TERalignment.java v1
Matthew Snover (snover@cs.umd.edu)                           

*/

package ter.core;


import ter.util.StringUtils;


/* Storage Class for TER alignments */
public class Alignment {

  @Override
  public String toString() {
	String s = "Original Ref: " + StringUtils.join(" ", ref) + 
      "\nOriginal Hyp: " + StringUtils.join(" ", hyp) + 
      "\nHyp After Shift: " + StringUtils.join(" ", aftershift);

	if (alignment != null) {
      s += "\nAlignment: (";
      for (int i = 0; i < alignment.length; i++) {s+=alignment[i];}
      s += ")";
	}
	if (allshifts == null) {
      s += "\nNumShifts: 0";
	} else {
      s += "\nNumShifts: " + allshifts.length;
      for (int i = 0; i < allshifts.length; i++) {
		s += "\n  " + allshifts[i];
      }
	}

    s += "\nScore: " + this.score() +
      " (" + this.numEdits + "/" + this.numWords + ")";
	return s;
  }

  public String toMoreString() {
	String s = "Best Ref: " + StringUtils.join(" ", ref) + 
      "\nOrig Hyp: " + StringUtils.join(" ", hyp) + "\n";
    s += AlignmentStringUtils.getPraStr(hyp, ref, aftershift, alignment, allshifts, false);
    s += String.format("TER Score: %1$6.2f (%2$5.1f/%3$5.1f)\n", 100*this.score(),
                       this.numEdits, this.numWords);
    s += AlignmentStringUtils.formatShiftsString(hyp, ref, allshifts);
	return s;
  }

  public double score() { 
      if ((numWords <= 0.0) && (this.numEdits > 0.0)) { return 1.0; }
      if (numWords <= 0.0) { return 0.0; } 
      return (double) numEdits / numWords;
  }    

  public void populateScoreDetails() {
	numIns = numDel = numSub = numWsf = numSft = 0;
	if(allshifts != null) {
      for(int i = 0; i < allshifts.length; ++i)
		numWsf += allshifts[i].size();
      numSft = allshifts.length;
	}
		
	if(alignment != null) {
      for(int i = 0; i < alignment.length; ++i) {
		switch (alignment[i]) {
          case 'S':
          case 'T':
		    numSub++;
		    break;
          case 'D':
		    numDel++;
		    break;
          case 'I':
		    numIns++;
		    break;
		}		
      }
	}
	//	if(numEdits != numSft + numDel + numIns + numSub)
	//      System.out.println("** Error, unmatch edit erros " + numEdits + 
	//                         " vs " + (numSft + numDel + numIns + numSub));
  }

  public String[] ref;
  public String[] hyp;
  public String[] aftershift;

  public Shift[] allshifts = null;

  public double numEdits = 0;
  public double numWords = 0.0;
  public char[] alignment = null;
  public String bestRef = "";

  public int numIns = 0;
  public int numDel = 0;
  public int numSub = 0;
  public int numSft = 0;
  public int numWsf = 0;
}
