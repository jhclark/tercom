package ter.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ter.io.SgmlProcessor;

public class AlignmentStringUtils {

	static String formatShiftsString(String[] hyp,
			  					String[] ref,
	                          Shift[] allshifts) {
		String to_return = "";
		int ostart, oend, odest;
		int nstart;
		@SuppressWarnings("unused") int nend;
		int dist;
		String direction = "";
	
		if(allshifts != null) {
	      for(int i = 0; i < allshifts.length; ++i) {
	        Shift[] oneshift = new Shift[1];
			ostart = allshifts[i].start;
			oend = allshifts[i].end;
			odest = allshifts[i].newloc;
	
			if(odest >= oend) {
	          // right
	          nstart = odest + 1 - allshifts[i].size();
	          nend = nstart + allshifts[i].size() - 1;
	          dist = odest - oend;
	          direction = "right";
			} else {
	          // left
	          nstart = odest + 1;
	          nend = nstart + allshifts[i].size() - 1;
	          dist = ostart - odest -1;
	          direction = "left";
			}
	
			to_return += "\nShift " + allshifts[i].shifted + " " + dist + " words " + direction;
			oneshift[0] = new Shift(ostart, oend, allshifts[i].moveto, odest);
			to_return += getPraStr(hyp, ref, allshifts[i].aftershift, allshifts[i].alignment, oneshift, true); 
	      }
	      to_return += "\n";
		}
		return to_return;
	  }

	static String getPraStr(String[] hyp,
			  				   String[] ref,
	                           String[] aftershift,
	                           char[] alignment,
	                           Shift[] allshifts,
	                           boolean shiftonly) {
		String to_return = "";
		String rstr = "";
		String hstr = "";
		String estr = "";
		String sstr = "";
		HashMap<String, List<Integer>> align_info = new HashMap<String, List<Integer>>();
		ArrayList<Integer> shift_dists = new ArrayList<Integer>();
		int anum = 1;
		int ind_start = 0;
		int ind_end = 1;
		int ind_from = 2;
		int ind_in = 3;
		int ostart, oend, odest;
		int slen = 0;
		int nstart, nend, nfrom, dist;
		@SuppressWarnings("unused") int non_inserr = 0;
	
		if(allshifts != null) {
	      for(int i = 0; i < allshifts.length; ++i) {
			ostart = allshifts[i].start;
			oend = allshifts[i].end;
			odest = allshifts[i].newloc;
			slen = allshifts[i].size();
	
			if(odest >= oend) {
	          // right
	          nstart = odest + 1 - slen;
	          nend = nstart + slen - 1;
	          nfrom = ostart;
	          dist = odest - oend;
			} else {
	          // left
	          nstart = odest + 1;
	          nend = nstart + slen - 1;
	          nfrom = ostart + slen;
	          dist = (ostart - odest -1) * -1;
			}
		
			//dist = (allshifts[i].leftShift())?-1*allshifts[i].distance():allshifts[i].distance();
			shift_dists.add(dist);
			//		System.out.println("[" + hyp[ostart] + ".." + hyp[oend] + " are shifted " + dist);
	
			if(anum > 1) AlignmentStringUtils.performShiftArray(align_info, ostart, oend, odest, alignment.length);
	
			List<Integer> val = align_info.get(nstart + "-" + ind_start);
			if(val == null) {
	          List<Integer> al = new ArrayList<Integer>();
	          al.add(anum);
	          align_info.put(nstart + "-" + ind_start, al);
			} else {
	          List<Integer> al = val;
	          al.add(anum);
			}
			
			val = align_info.get(nend + "-" + ind_end);
			if(val == null) {
	          ArrayList<Integer> al = new ArrayList<Integer>();
	          al.add(anum);
	          align_info.put(nend + "-" + ind_end, al);
			} else {
	          ArrayList<Integer> al = (ArrayList<Integer>) val;
	          al.add(anum);
			}
			
			val = align_info.get(nfrom + "-" + ind_from);
			if(val == null) {
	          ArrayList<Integer> al = new ArrayList<Integer>();
	          al.add(anum);
	          align_info.put(nfrom + "-" + ind_from, al);
			} else {
	          ArrayList<Integer> al = (ArrayList<Integer>) val;
	          al.add(anum);
			}
	
			/*
	          val = align_info.get("60-"+ind_start);
	          if(val != null)
	          System.out.println(((ArrayList) val).get(0));
	          else
	          System.out.println("empty");
	
	          System.out.println("nstart: " + nstart + ", nend:" + nend + "," + ostart +"," + oend +","+ odest + "," + align_info.size());
			*/
			if(slen > 0) {
	          for(int j = nstart; j <= nend; ++j) {
				val = align_info.get(j + "-" + ind_in);
				if(val == null) {
	              ArrayList<Integer> al = new ArrayList<Integer>();
	              al.add(anum);
	              align_info.put(j + "-" + ind_in, al);
				} else {
	              ArrayList<Integer> al = (ArrayList<Integer>) val;
	              al.add(anum);
				}
	          }
			}
			anum++;
	      }
		}
	
		int hyp_idx = 0;
		int ref_idx = 0;
		if(alignment != null) {
	      for(int i = 0; i < alignment.length; ++i) {
			String shift_in_str = "";
			String ref_wd = (ref_idx < ref.length)?String.valueOf(ref[ref_idx]):"";
			String hyp_wd = (hyp_idx < hyp.length)?String.valueOf(aftershift[hyp_idx]):"";
			int l = 0;
	
			if(alignment[i] != 'D') {
	          List<Integer> val = align_info.get(hyp_idx + "-" + ind_from);
	          if(val != null) {
				//						System.out.println("hyp_idx: " + hyp_idx + "," + hyp_wd);
				List<Integer> list = val;
				for(int j = 0; j < list.size(); ++j) {
	              String s = "" + list.get(j);
	              hstr += " @";
	              rstr += "  ";
	              estr += "  ";
	              sstr += " " + s;
	              for(int k = 1; k < s.length(); ++k) {
					hstr += " ";
					rstr += " ";
					estr += " ";
	              }
				}
	          }
	
	          val = align_info.get(hyp_idx + "-" + ind_start);
	          if(val != null) {
				//			System.out.println("hyp_idx: " + hyp_idx + "," + hyp_wd + "," + alignment.length);
				List<Integer> list = val;
				for(int j = 0; j < list.size(); ++j) {
	              String s = "" + list.get(j);
	              hstr += " [";
	              rstr += "  ";
	              estr += "  ";
	              sstr += " " + s;
	              for(int k = 1; k < s.length(); ++k) {
					hstr += " ";
					rstr += " ";
					estr += " ";
	              }
				}
	          }
	          if(slen > 0) {
				val = align_info.get(hyp_idx + "-" + ind_in);
				if(val != null)
	              shift_in_str = SgmlProcessor.join(",", (ArrayList<Integer>) val);
				//	if(val != null) System.out.println("shiftstr: " + ref_idx + "," + hyp_idx + "-" + ind_in + ":" + shift_in_str);
	          } 
			}
			switch (alignment[i]) {
	          case ' ':
			    l = Math.max(ref_wd.length(), hyp_wd.length());
			    hstr += " " + hyp_wd;
			    rstr += " " + ref_wd;
			    estr += " ";
			    sstr += " ";
			    for(int j = 0; j < l; ++j) {
	              if(hyp_wd.length() <= j) hstr += " ";
	              if(ref_wd.length() <= j) rstr += " ";
	              estr += " ";
	              sstr += " ";
			    }
			    hyp_idx++;
			    ref_idx++;
			    non_inserr++;
			    break;
	          case 'S':
	          case 'T':
			    l = Math.max(ref_wd.length(), Math.max(hyp_wd.length(), Math.max(1, shift_in_str.length())));
			    hstr += " " + hyp_wd;
			    rstr += " " + ref_wd;
			    if(hyp_wd.equals("") || ref_wd.equals("")) System.out.println("unexpected empty");
			    estr += " " + alignment[i];
			    sstr += " " + shift_in_str;
			    for(int j = 0; j < l; ++j) {
	              if(hyp_wd.length() <= j) hstr += " ";
	              if(ref_wd.length() <= j) rstr += " ";
	              if(j > 0) estr += " ";
	              if(j >= shift_in_str.length()) sstr += " ";
			    }
			    ref_idx++;
			    hyp_idx++;
			    non_inserr++;
			    break;
	          case 'D':
			    l = ref_wd.length();
			    hstr += " ";
			    rstr += " " + ref_wd;
			    estr += " D";
			    sstr += " ";
			    for(int j = 0; j < l; ++j) {
	              hstr += "*"; 
	              if(j > 0) estr += " ";
	              sstr += " ";
			    }
			    ref_idx++;
			    non_inserr++;
			    break;
	          case 'I':
			    l = Math.max(hyp_wd.length(), shift_in_str.length());
			    hstr += " " + hyp_wd;
			    rstr += " ";
			    estr += " I";
			    sstr += " " + shift_in_str;
			    for(int j = 0; j < l; ++j) {
	              rstr += "*"; 
	              if(j >= hyp_wd.length()) hstr += " ";
	              if(j > 0) estr += " ";
	              if(j >= shift_in_str.length()) sstr += " ";
			    }
			    hyp_idx++;
			    break;
			}
			
			if(alignment[i] != 'D') {
	          List<Integer> val = align_info.get((hyp_idx-1) + "-" + ind_end);
	          if(val != null) {
				List<Integer> list = val;
				for(int j = 0; j < list.size(); ++j) {
	              String s = "" + list.get(j);
	              hstr += " ]";
	              rstr += "  ";
	              estr += "  ";
	              sstr += " " + s;
	              for(int k = 1; k < s.length(); ++k) {
					hstr += " ";
					rstr += " ";
					estr += " ";
	              }
				}
	          }		    
			}
	      }
		}
		//	if(non_inserr != ref.length && ref.length > 1)
		    //      System.out.println("** Error, unmatch non-insertion erros " + non_inserr + 
		    //                         " and reference length " + ref.length );	
	    String indent = "";
	    if (shiftonly) indent = " ";
		to_return += "\n" + indent + "REF: " + rstr;
		to_return += "\n" + indent + "HYP: " + hstr;
	    if(!shiftonly) {
	      to_return += "\n" + indent + "EVAL:" + estr;
	      to_return += "\n" + indent + "SHFT:" + sstr;
	    }
		to_return += "\n";
		return to_return;
	  }

	public static void performShiftArray(HashMap<String, List<Integer>> hwords,
	                                       int start,
	                                       int end,
	                                       int moveto,
	                                       int capacity) {
		  
		HashMap<String, List<Integer>> nhwords = new HashMap<String, List<Integer>>();
		
		if(moveto == -1) {
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, start, end, 0);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, 0, start - 1, end - start + 1);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, end + 1, capacity, end + 1);	    
		} else if (moveto < start) {
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, 0, moveto, 0);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, start, end, moveto + 1);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, moveto + 1, start - 1, end - start + moveto + 2);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, end + 1, capacity, end + 1);
		} else if (moveto > end) {
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, 0, start - 1, 0);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, end + 1, moveto, start);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, start, end, start + moveto - end);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, moveto + 1, capacity, moveto + 1);
		} else {
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, 0, start - 1, 0);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, end + 1, end + moveto - start, start);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, start, end, moveto);
	      AlignmentStringUtils.copyHashWords(hwords, nhwords, end + moveto - start + 1, capacity, end + moveto - start + 1);
		}
		hwords.clear();
		hwords.putAll(nhwords);
	  }

	static void copyHashWords(HashMap<String, List<Integer>> ohwords,
	                                    HashMap<String, List<Integer>> nhwords,
	                                    int start,
	                                    int end,
	                                    int nstart) {
		int ind_start = 0;
		@SuppressWarnings("unused") int ind_end = 1;
		@SuppressWarnings("unused") int ind_from = 2;
		int ind_in = 3;
		int k = nstart;
	
		for(int i = start; i <= end; ++k, ++i) {
	      for(int j = ind_start; j <= ind_in; ++j) {
			List<Integer> val = ohwords.get(i + "-" + j);
			if(val != null) {	    
	          nhwords.put(k + "-" + j, val);
			}
	      }
		}
	  }
}
