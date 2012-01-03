/*

Copyright� 2006 by BBN Technologies and University of Maryland (UMD)

BBN and UMD grant a nonexclusive, source code, royalty-free right to
use this Software known as Translation Error Rate COMpute (the
"Software") solely for research purposes. Provided, you must agree
to abide by the license and terms stated herein. Title to the
Software and its documentation and all applicable copyrights, trade
secrets, patents and other intellectual rights in it are and remain
with BBN and UMD and shall not be used, revealed, disclosed in
marketing or advertisement or any other activity not explicitly
permitted in writing.

BBN and UMD make no representation about suitability of this
Software for any purposes.  It is provided "AS IS" without express
or implied warranties including (but not limited to) all implied
warranties of merchantability or fitness for a particular purpose.
In no event shall BBN or UMD be liable for any special, indirect or
consequential damages whatsoever resulting from loss of use, data or
profits, whether in an action of contract, negligence or other
tortuous action, arising out of or in connection with the use or
performance of this Software.

Without limitation of the foregoing, user agrees to commit no act
which, directly or indirectly, would violate any U.S. law,
regulation, or treaty, or any other international treaty or
agreement to which the United States adheres or with which the
United States complies, relating to the export or re-export of any
commodities, software, or technical data.  This Software is licensed
to you on the condition that upon completion you will cease to use
the Software and, on request of BBN and UMD, will destroy copies of
the Software in your possession.                                                

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
