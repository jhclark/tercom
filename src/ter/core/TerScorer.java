/*
                                                                
Copyright� 2006 by BBN Technologies and University of Maryland (UMD)

License: LGPL v. 2.1.  See LICENSE.txt.

TERcalc.java v1
Matthew Snover (snover@cs.umd.edu)                           

*/

package ter.core;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import ter.util.IntPair;
import ter.util.Pair;
import ter.util.StringUtils;

public class TerScorer {
  /* Turn on if you want a lot of debugging info. */
  static final private boolean DEBUG = false;
  private boolean normalized = false;
  private boolean caseon = false;
  private boolean nopunct = false;
    private boolean asiansupport = false;
    private boolean notagbrackets = false;
  private IntPair[] refSpans = null;
  private IntPair[] hypSpans = null;
  public double ref_len = -1.;

  public void setNormalize(boolean b) {
	normalized = b;
  }

  public void setCase(boolean b) {
	caseon = b;
  }

  public void setPunct(boolean b) {
	nopunct = b;
  }

    public void setAsian(boolean b) {
	asiansupport = b;
    }

    public void setTagBrackets(boolean b) {
	notagbrackets = b;
    }

  public void setBeamWidth(int i) {
	BEAM_WIDTH = i;
  }

  public void setShiftDist(int i) {
    MAX_SHIFT_DIST = i;
  }

  public void setRefSpan(String span) {
    if(span != null && span.trim() != "") {
      String[] spans = span.split("\\s+");
      refSpans = new IntPair[spans.length];
      for(int i = 0; i < spans.length; ++i) {
        String s[] = spans[i].split(":");
        refSpans[i] = new IntPair(Integer.valueOf(s[0]),
                                     Integer.valueOf(s[1]));
      }
    }
  }

  public void setHypSpan(String span) {
    if(span != null && span.trim() != "") {
      //      hypSpans = span.split("\\s+");
      String[] spans = span.split("\\s+");
      hypSpans = new IntPair[spans.length];
      for(int i = 0; i < spans.length; ++i) {
        String s[] = spans[i].split(":");
        hypSpans[i] = new IntPair(Integer.valueOf(s[0]),
                                     Integer.valueOf(s[1]));
      }
    }
  }

  public void setRefLen(List<String> reflens) {
    String reflen = "";

    if (reflens == null || reflens.size() == 0) {
      ref_len = -1.0;
      return;
    }

    ref_len = 0.0;
    for (int i = 0; i < reflens.size(); ++i) {
      reflen = reflens.get(i);
      if(reflen.length() == 0)
        ref_len += 0.;
      else
        ref_len += tokenize(reflen).length;
    }
    ref_len /= reflens.size();
  }

  public void setRefLen(double d) {
    ref_len = (d >= 0) ? d : -1;
  }

  public Alignment TER(String[] hyp, String[] ref) {
	return TER(hyp, ref, new CostFunction());
  }

  public Alignment TER(String hyp, String ref) {
	return TER(hyp, ref, new CostFunction());
  }

  public Alignment TER(String hyp, String ref, CostFunction costfunc) {
	/* Tokenize the strings and pass them off to TER */
	Alignment to_return;

	if(!caseon) {
      hyp = hyp.toLowerCase();
      ref = ref.toLowerCase();
	}

	if(ref.length() == 0 || hyp.length() == 0) {
      to_return = TERnullstr(hyp, ref, costfunc);
      if (ref_len >= 0) to_return.numWords = ref_len;
	} else {
      String[] hyparr = tokenize(hyp);
      String[] refarr = tokenize(ref);

      to_return = TER(hyparr, refarr, costfunc);
      if(ref_len >= 0) to_return.numWords = ref_len;
	}
	return to_return;
  }    

  public Alignment TERnullstr(String hyp, String ref, CostFunction costfunc) {
	Alignment to_return = new Alignment();
	String [] hyparr = tokenize(hyp);
	String [] refarr = tokenize(ref);

	if(hyp.length() == 0 && ref.length() == 0) {
      to_return.numWords = 0;
      to_return.numEdits = 0;
	} else if (hyp.length() == 0) {
      to_return.alignment = new char[refarr.length];
      for(int i = 0; i < refarr.length; ++i)
		to_return.alignment[i] = 'D';
      to_return.numWords = refarr.length;
      to_return.numEdits = refarr.length;
	} else {
      to_return.alignment = new char[hyparr.length];
      for(int i = 0; i < hyparr.length; ++i)
		to_return.alignment[i] = 'I';
      to_return.numWords = 0;
      to_return.numEdits = hyparr.length;
	}
	to_return.hyp = hyparr;
	to_return.ref = refarr;
	to_return.aftershift = hyparr;

	return to_return;
  }
				   
  public Alignment TER(String[] hyp, String[] ref, 
                                 CostFunction costfunc) {
	/* Calculates the TER score for the hyp/ref pair */
	Map<List<String>, Set<Integer>> rloc = BuildWordMatches(hyp, ref);
	Alignment cur_align = MinEditDist(hyp,ref,costfunc,hypSpans);
	String[] cur = hyp;

	cur_align.hyp = hyp;
	cur_align.ref = ref;
	cur_align.aftershift = hyp;

	double edits = 0;
	@SuppressWarnings("unused") int numshifts = 0;
	ArrayList<Shift> allshifts = new ArrayList<Shift>(hyp.length+ref.length);
	
	if (DEBUG)
	    System.out.println("Initial Alignment:\n" + cur_align + "\n");
	
	while (true) {
      Object[] returns = CalcBestShift(cur, hyp, ref, rloc, cur_align, 
                                       costfunc);
      if (returns == null) {
		break;
      }
      Shift bestShift = (Shift) returns[0];
      edits += bestShift.cost;
      cur_align = (Alignment) returns[1];


      bestShift.alignment = cur_align.alignment;
      bestShift.aftershift = cur_align.aftershift;


      allshifts.add(bestShift);
      cur = cur_align.aftershift;  
	}

	Alignment to_return = cur_align;
	to_return.allshifts = allshifts.toArray(new Shift[0]);
	
	to_return.numEdits += edits;

	NUM_SEGMENTS_SCORED++;
	return to_return;
  }

  public String[] tokenize(String s) {
      return Normalizer.tokenize(s, normalized, nopunct, asiansupport, notagbrackets);
  }
    
  private Map<List<String>, Set<Integer>> BuildWordMatches(String[] hyp, 
                                      String[] ref) {
	Set<String> hwhash = new HashSet<String>();
	for (int i = 0; i < hyp.length; i++) {
      hwhash.add(hyp[i]);
	}
	boolean[] cor = new boolean[ref.length];
	for (int i = 0; i < ref.length; i++) {
      if (hwhash.contains(ref[i])) {
		cor[i] = true;
      } else {
		cor[i] = false;
      }
	}

	List<String> reflist = Arrays.asList(ref);
	HashMap<List<String>, Set<Integer>> to_return = new HashMap<List<String>, Set<Integer>>();
	for (int start = 0; start < ref.length; start++) {
      if (cor[start]) {
		for (int end = start; ((end < ref.length) &&
                               (end - start <= MAX_SHIFT_SIZE) &&
                               (cor[end]));
		     end++) {
          List<String> topush = reflist.subList(start, end+1);
          if (to_return.containsKey(topush)) {
			Set<Integer> vals = to_return.get(topush);
			vals.add(new Integer(start));
          } else {
			Set<Integer> vals = new TreeSet<Integer>();
			vals.add(new Integer(start));
			to_return.put(topush, vals);			
          }
		}
      }
	}
	return to_return;
  }    

  private static void FindAlignErr(Alignment align, boolean[] herr,
                                   boolean[] rerr,
                                   int[] ralign) {
	int hpos = -1;
	int rpos = -1;
	for (int i = 0; i < align.alignment.length; i++) {
      char sym = align.alignment[i];
      if (sym == ' ') {
		hpos++; rpos++;
		herr[hpos] = false; 
		rerr[rpos] = false;
		ralign[rpos] = hpos;
      } else if (sym == 'S') {
		hpos++; rpos++;
		herr[hpos] = true; 
		rerr[rpos] = true;
		ralign[rpos] = hpos;
      } else if (sym == 'I') {
		hpos++;
		herr[hpos] = true; 
      } else if (sym == 'D') {
		rpos++; 		
		rerr[rpos] = true;
		ralign[rpos] = hpos;
      } else {
        throw new IllegalStateException("Error! Invalid mini align sequence " + sym + " at pos " + i + "\n");
      }
	}
  }

  // TODO: Don't use Object
  private Object[] CalcBestShift(String[] cur,
                                        String[] hyp, String[] ref, 
                                        Map<List<String>, Set<Integer>> rloc, Alignment med_align,
                                        CostFunction costfunc) {
	/* 
	   return null if no good shift is found
	   or return Object[ TERshift bestShift, 
       TERalignment cur_align ]
	*/
	Object[] to_return = new Object[2];

	boolean anygain = false;

	/* Arrays that records which hyp and ref words are currently wrong */
	boolean[] herr = new boolean[hyp.length];
	boolean[] rerr = new boolean[ref.length];
	/* Array that records the alignment between ref and hyp */
	int[] ralign = new int[ref.length];
	FindAlignErr(med_align, herr, rerr, ralign);

	Shift[][] poss_shifts = GatherAllPossShifts(cur, ref, rloc, med_align, herr, rerr, ralign, costfunc);		
	double curerr = med_align.numEdits;
	
	if (DEBUG) {
      // CUT HERE        
      System.out.println("Possible Shifts:");
      for (int i = poss_shifts.length - 1; i >= 0; i--) {
		for (int j = 0; j < poss_shifts[i].length; j++) {
          System.out.println(" [" + i + "] " + poss_shifts[i][j]);
		}
      }
      System.out.println("");
      // CUT HERE
	}

	double cur_best_shift_cost = 0.0;
	Alignment cur_best_align = med_align;
	Shift cur_best_shift = new Shift();	

	for (int i = poss_shifts.length - 1; i >= 0; i--) {
      if (DEBUG) System.out.println("Considering shift of length " + i + " (" + poss_shifts[i].length  +")");

      /* Consider shifts of length i+1 */
      double curfix = curerr - 
		(cur_best_shift_cost + cur_best_align.numEdits);
      double maxfix = (2 * (1 + i));
	    
      if ((curfix > maxfix) || 
          ((cur_best_shift_cost != 0) && (curfix == maxfix))) {
		break;
      }
	    
      for (int s = 0; s < poss_shifts[i].length; s++) {		
		curfix = curerr - 
          (cur_best_shift_cost + cur_best_align.numEdits);
        if ((curfix > maxfix) || 
		    ((cur_best_shift_cost != 0) && (curfix == maxfix))) {
          break;
		}
		
		Shift curshift = poss_shifts[i][s];

		Pair<String[], IntPair[]> shiftReturns = PerformShift(cur, curshift);
        String[] shiftarr = shiftReturns.first;
        IntPair[] curHypSpans = shiftReturns.second;

		Alignment curalign = MinEditDist(shiftarr, ref, costfunc, curHypSpans);

		curalign.hyp = hyp;
		curalign.ref = ref;
		curalign.aftershift = shiftarr;

		double gain = (cur_best_align.numEdits + cur_best_shift_cost) 
          - (curalign.numEdits + curshift.cost);
		
		if (DEBUG) {
          System.out.println("Gain for " + curshift + " is " + gain + ". (result: [" + StringUtils.join(" ", shiftarr) + "]");
          System.out.println("" + curalign + "\n");		
		}

		if ((gain > 0) || ((cur_best_shift_cost == 0) && (gain == 0))) {
          anygain = true;
          cur_best_shift = curshift;
          cur_best_shift_cost = curshift.cost;
          cur_best_align = curalign;
          if (DEBUG) System.out.println("Tmp Choosing shift: " + cur_best_shift + " gives:\n" + cur_best_align + "\n");
		}

      }
	}

	if (anygain) {
      to_return[0] = cur_best_shift;
      to_return[1] = cur_best_align;	    
      return to_return;
	} else {
      if (DEBUG) System.out.println("No good shift found.\n");
      return null;
	}
  }

  private Shift[][] GatherAllPossShifts(String[] hyp, String[] ref, Map<List<String>, Set<Integer>> rloc,
                                                  Alignment align,
                                                  boolean[] herr, boolean[] rerr, int[] ralign, CostFunction costfunc) {
      
      // Don't even bother to look if shifts can't be done
      if ((MAX_SHIFT_SIZE <= 0) || (MAX_SHIFT_DIST <= 0)) {
	  Shift[][] to_return = new Shift[0][];
	  return to_return;
      }
      

	ArrayList<Shift>[] allshifts = new ArrayList[MAX_SHIFT_SIZE+1];
	for (int i = 0; i < allshifts.length; i++)
      allshifts[i] = new ArrayList<Shift>();

	List<String> hyplist = Arrays.asList(hyp);	
	for (int start = 0; start < hyp.length; start++) {
      if (! rloc.containsKey(hyplist.subList(start, start+1))) continue;

      boolean ok = false;
      Iterator<Integer> mti = rloc.get(hyplist.subList(start, start+1)).iterator();
      while (mti.hasNext() && (! ok)) {
		int moveto = mti.next();
		if ((start != ralign[moveto]) &&
		    (ralign[moveto] - start <= MAX_SHIFT_DIST) &&
		    ((start - ralign[moveto] - 1) <= MAX_SHIFT_DIST)) 
          ok = true;		
      }
      if (! ok) continue;

      ok = true;
      for (int end = start; (ok && (end < hyp.length) && (end < start + MAX_SHIFT_SIZE));
           end++) {

		/* check if cand is good if so, add it */
		List<String> cand = hyplist.subList(start, end+1);
		ok = false;		
		if (! (rloc.containsKey(cand))) continue;

		boolean any_herr = false;
		for (int i = 0; (i <= end - start) && (! any_herr); i++) {
          if (herr[start+i]) any_herr = true;
		}

		if (any_herr == false) {
          ok = true;
          continue;
		}
				
		Iterator<Integer> movetoit = rloc.get(cand).iterator();
		while (movetoit.hasNext()) {
          int moveto = movetoit.next();
          if (! ((ralign[moveto] != start) &&
                 ((ralign[moveto] < start) || (ralign[moveto] > end)) &&
                 ((ralign[moveto] - start) <= MAX_SHIFT_DIST) &&
                 ((start - ralign[moveto]) <= MAX_SHIFT_DIST)))
			continue;
          ok = true;

          /* check to see if there are any errors in either string
             (only move if this is the case!)
          */
		    
          boolean any_rerr = false;
          for (int i = 0; (i <= end - start) && (! any_rerr); i++) {
			if (rerr[moveto+i]) any_rerr = true;
          }
          if (! any_rerr) continue;

          for (int roff = -1; roff <= (end - start); roff++) {
	      Shift topush = null;
	      if ((roff == -1) && (moveto == 0)) {
		  if (DEBUG) System.out.println("Consider making " + start + "..." + end + " moveto: " + moveto + " roff: " 
				     + roff + " ralign[mt+roff]: " + -1); 

		  topush = new Shift(start, end, -1, -1);
	      } else if ((start != ralign[moveto+roff]) &&
			    ((roff == 0) || 
			     (ralign[moveto+roff] != ralign[moveto]))) {
		  int newloc = ralign[moveto+roff];
		  if (DEBUG) System.out.println("Consider making " + start + "..." + end + " moveto: " + moveto + " roff: " 
				     + roff + " ralign[mt+roff]: " + newloc); 
		  
		  //		  if (newloc != start + 1) {
		      topush = new Shift(start, end, moveto+roff, newloc);
		      // }
	      }
	      if (topush != null) {
		  topush.shifted = cand;
		  topush.cost  = costfunc.shift_cost(topush);
		  allshifts[end - start].add(topush);
	      }	      
          }		
      }
      }
	}

	Shift[][] to_return = new Shift[MAX_SHIFT_SIZE+1][];
	for (int i = 0; i < to_return.length; i++) {
	    to_return[i] = (Shift[]) allshifts[i].toArray(new Shift[allshifts[i].size()]);
	}
	return to_return;
  }

  public Pair<String[], IntPair[]> PerformShift(String[] words, Shift s) {
	return PerformShift(words, s.start, s.end, s.newloc);
  }

  private Pair<String[], IntPair[]> PerformShift(String[] words, int start, int end, int newloc) {	
      int c = 0;
      String[] nwords = words.clone();
      IntPair[] spans = null;

    if(hypSpans != null) spans = new IntPair[hypSpans.length];
    if(DEBUG) { 
	if (hypSpans != null) {
	    System.out.println("word length: " + words.length + " span length: " + hypSpans.length);
	} else {
	    System.out.println("word length: " + words.length + " span length: null");
	}
    }

	if (newloc == -1) {
      for (int i = start; i<=end;i++) { nwords[c++] = words[i];  if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = 0; i<=start-1;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = end+1; i<words.length;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
	} else if (newloc < start) {
      for (int i = 0; i<=newloc; i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = start; i<=end;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = newloc+1; i<=start-1;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = end+1; i<words.length;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
	} else if (newloc > end) {
      for (int i = 0; i<=start-1; i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = end+1; i<=newloc;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }	    
      for (int i = start; i<=end;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = newloc+1; i<words.length;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }	    
	} else {
      // we are moving inside of ourselves
      for (int i = 0; i<=start-1; i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = end+1; (i< words.length) && (i<=(end + (newloc - start))); i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = start; i<=end;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
      for (int i = (end + (newloc - start)+1); i<words.length;i++) { nwords[c++] = words[i]; if(hypSpans != null) spans[c-1] = hypSpans[i]; }
	}
	NUM_SHIFTS_CONSIDERED++;

	return new Pair<String[], IntPair[]>(nwords, spans);
  }

  private Alignment MinEditDist(String[] hyp, String[] ref, 
                                          CostFunction costfunc, IntPair[] curHypSpans) {
	double current_best = INF;
	double last_best = INF;
	int first_good = 0;
	int current_first_good = 0;
	int last_good = -1;
	int cur_last_good = 0;
	@SuppressWarnings("unused") int last_peak = 0;
	int cur_last_peak = 0;
	int i, j;
	double cost, icost, dcost;
	double score;

	@SuppressWarnings("unused") int hwsize = hyp.length-1;
	@SuppressWarnings("unused") int rwsize = ref.length-1;
	
	final int size = Math.max(ref.length, hyp.length) + 1;
	final double[][] S = new double[size][size];
	final char[][] P = new char[size][size];

	NUM_BEAM_SEARCH_CALLS++;

	for (i=0; i <= ref.length; i++){
      for (j=0; j <= hyp.length; j++){
		S[i][j]=-1.0;
		P[i][j]='0';
      }
	}
 	S[0][0] = 0.0;
	
	for (j=0; j <= hyp.length; j++) {
      last_best = current_best;
      current_best = INF;
	    
      first_good = current_first_good;
      current_first_good = -1;
	    
      last_good = cur_last_good;
      cur_last_good = -1;
	    
      last_peak = cur_last_peak;
      cur_last_peak = 0;
	    
      for (i=first_good; i <= ref.length; i++) {
		if (i > last_good)
          break;
		if (S[i][j] < 0) 
          continue;
		score = S[i][j];

		if ((j < hyp.length) && (score > last_best+BEAM_WIDTH))
          continue;

		if (current_first_good == -1)
          current_first_good = i ;
		    
		if ((i < ref.length) && (j < hyp.length)) {
          if(refSpans == null || hypSpans == null || 
             spanIntersection(refSpans[i], curHypSpans[j])) {
            if (ref[i].equals(hyp[j])) {
              cost = costfunc.match_cost(hyp[j], ref[i]) + score;
              if ((S[i+1][j+1] == -1) || (cost < S[i+1][j+1])) {
                S[i+1][j+1] = cost;
                P[i+1][j+1] = ' ';
              }
              if (cost < current_best)
                current_best = cost;
              
              if (current_best == cost)
                cur_last_peak = i+1;
            } else {
              cost = costfunc.substitute_cost(hyp[j], ref[i]) + score;
              if ((S[i+1][j+1] <0) || (cost < S[i+1][j+1])) {
                S[i+1][j+1] = cost;
                P[i+1][j+1] = 'S';
                if (cost < current_best)
                  current_best = cost;
                if (current_best == cost)
                  cur_last_peak = i+1 ;
              }
            }
          }
		}
			
		cur_last_good = i+1;
			
		if  (j < hyp.length) {
          icost = score+costfunc.insert_cost(hyp[j]);
          if ((S[i][j+1] < 0) || (S[i][j+1] > icost)) {
			S[i][j+1] = icost;
			P[i][j+1] = 'I';
			if (( cur_last_peak <  i) && ( current_best ==  icost))
              cur_last_peak = i;
          }
		}		

		if  (i < ref.length) {
          dcost =  score + costfunc.delete_cost(ref[i]);
          if ((S[ i+1][ j]<0.0) || ( S[i+1][j] > dcost)) {
			S[i+1][j] = dcost;
			P[i+1][j] = 'D';
			if (i >= last_good)
              last_good = i + 1 ;
          }		
		}
      }
	}
	
	int tracelength = 0;
	i = ref.length;
	j = hyp.length;
	while ((i > 0) || (j > 0)) {
      tracelength++;
      if (P[i][j] == ' ') {
		i--; j--;
      } else if (P[i][j] == 'S') {
		i--; j--;
      } else if (P[i][j] == 'D') {
		i--;
      } else if (P[i][j] == 'I') {
		j--;
      } else {
        throw new IllegalStateException("Invalid path: " + P[i][j]);
      }
	}
	char[] path = new char[tracelength];
	i = ref.length;
	j = hyp.length;
	while ((i > 0) || (j > 0)) {
      path[--tracelength] = P[i][j];
      if (P[i][j] == ' ') {
		i--; j--;
      } else if (P[i][j] == 'S') {
		i--; j--;
      } else if (P[i][j] == 'D') {
		i--;
      } else if (P[i][j] == 'I') {
		j--;
      }
	}

	Alignment to_return = new Alignment();
	to_return.numWords = ref.length;
	to_return.alignment = path;
	to_return.numEdits = S[ref.length][hyp.length];

	return to_return;
  }

//  private static boolean spanIntersection (String refSpan,
//                                           String hypSpan) {
//    String[] hSpans = hypSpan.split(":");
//    String[] rSpans = refSpan.split(":");
//
//    return (Integer.valueOf(rSpans[1]) >= Integer.valueOf(hSpans[0]) && 
//            Integer.valueOf(rSpans[0]) <= Integer.valueOf(hSpans[1]));
//  } 

  private static boolean spanIntersection (IntPair refSpan,
                                           IntPair hypSpan) {
    return (refSpan.cdr >= hypSpan.car &&
            refSpan.car <= hypSpan.cdr);
  }

  /* Accessor functions to some internal counters */
  public int numBeamCalls () { return NUM_BEAM_SEARCH_CALLS; }
  public int numSegsScored () { return NUM_SEGMENTS_SCORED; }
  public int numShiftsTried () { return NUM_SHIFTS_CONSIDERED; }

  /* We may want to add some function to change the beam width */
  public int BEAM_WIDTH = 20;
    
  private static final double INF = 999999.0;

  private final int MAX_SHIFT_SIZE = 10;
  private int MAX_SHIFT_DIST = 50;

  /* Variables for some internal counting.  */
  private int NUM_SEGMENTS_SCORED = 0;
  private int NUM_SHIFTS_CONSIDERED = 0;
  private int NUM_BEAM_SEARCH_CALLS = 0;

  
}
