package ter.core;

public class Normalizer {
	
  public static String[] tokenize(String s, boolean normalized, boolean nopunct) {
		/* tokenizes according to the mtevalv11 specs */

		if(normalized) {
	      // language-independent part:
	      s = s.replaceAll("<skipped>", ""); // strip "skipped" tags
	      s = s.replaceAll("-\n", ""); // strip end-of-line hyphenation and join lines
	      s = s.replaceAll("\n", " "); // join lines
	      s = s.replaceAll("&quot;", "\""); // convert SGML tag for quote to " 
	      s = s.replaceAll("&amp;", "&"); // convert SGML tag for ampersand to &
	      s = s.replaceAll("&lt;", "<"); // convert SGML tag for less-than to >
	      s = s.replaceAll("&gt;", ">"); // convert SGML tag for greater-than to <
		    
	      // language-dependent part (assuming Western languages):
	      s = " " + s + " ";
	      s = s.replaceAll("([\\{-\\~\\[-\\` -\\&\\(-\\+\\:-\\@\\/])", " $1 ");   // tokenize punctuation
	      s = s.replaceAll("'s ", " 's "); // handle possesives
	      s = s.replaceAll("'s$", " 's"); // handle possesives     
	      s = s.replaceAll("([^0-9])([\\.,])", "$1 $2 "); // tokenize period and comma unless preceded by a digit
	      s = s.replaceAll("([\\.,])([^0-9])", " $1 $2"); // tokenize period and comma unless followed by a digit
	      s = s.replaceAll("([0-9])(-)", "$1 $2 "); // tokenize dash when preceded by a digit
	      s = s.replaceAll("\\s+"," "); // one space only between words
	      s = s.replaceAll("^\\s+", "");  // no leading space
	      s = s.replaceAll("\\s+$", "");  // no trailing space
		}
		if(nopunct) s = Normalizer.removePunctuation(s);
		return s.split("\\s+");
	  }

	static String removePunctuation(String str) {
		String s = str.replaceAll("[\\.,\\?:;!\"\\(\\)]", "");
		s = s.replaceAll("\\s+", " ");
		return s;
	  }

}
