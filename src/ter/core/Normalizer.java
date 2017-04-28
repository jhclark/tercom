package ter.core;

public class Normalizer {

    private static String asianPunct = "([\\x{3001}\\x{3002}\\x{3008}-\\x{3011}\\x{3014}-\\x{301f}\\x{ff61}-\\x{ff65}\\x{30fb}])";
    private static String fullwidthPunct = "([\\x{ff0e}\\x{ff0c}\\x{ff1f}\\x{ff1a}\\x{ff1b}\\x{ff01}\\x{ff02}\\x{ff08}\\x{ff09}])";

    public static String[] tokenize(String s, boolean normalized, boolean nopunct, boolean asianSupport, boolean noTagBrackets) {

	/* tokenizes according to the mtevalv11 specs, plus added material
	   for handling languages written with CJK ideographs */

	if(normalized) {
	    // language-independent part:
	    s = s.replaceAll("<skipped>", ""); // strip "skipped" tags
	    s = s.replaceAll("-\n", ""); // strip end-of-line hyphenation and join lines
	    s = s.replaceAll("\n", " "); // join lines
	    s = s.replaceAll("&quot;", "\""); // convert SGML tag for quote to " 
	    s = s.replaceAll("&amp;", "&"); // convert SGML tag for ampersand to &
	    s = s.replaceAll("&lt;", "<"); // convert SGML tag for less-than to >
	    s = s.replaceAll("&gt;", ">"); // convert SGML tag for greater-than to <

	    // remove HTML-style tag brackets: "<br/>" -> "br/"
	    if(noTagBrackets) {
		s = s.replaceAll("<([^<>]+)>", " $1 ");
	    }

	    // language-dependent part (assuming Western languages):
	    s = " " + s + " ";
	    s = s.replaceAll("([\\{-\\~\\[-\\` -\\&\\(-\\+\\:-\\@\\/])", " $1 ");   // tokenize punctuation
	    s = s.replaceAll("'s ", " 's "); // handle possesives
	    s = s.replaceAll("'s$", " 's"); // handle possesives     
	    s = s.replaceAll("([^0-9])([\\.,])", "$1 $2 "); // tokenize period and comma unless preceded by a digit
	    s = s.replaceAll("([\\.,])([^0-9])", " $1 $2"); // tokenize period and comma unless followed by a digit
	    s = s.replaceAll("([0-9])(-)", "$1 $2 "); // tokenize dash when preceded by a digit

	    // further language-dependent part (if Asian support turned on):
	    if(asianSupport) {
		// Split Chinese chars and Japanese kanji down to character level:
		s = s.replaceAll("([\\p{InCJKUnifiedIdeographs}\\p{InCJKUnifiedIdeographsExtensionA}])", " $1 ");
		s = s.replaceAll("([\\p{InCJKStrokes}\\p{InCJKRadicalsSupplement}])", " $1 ");
		s = s.replaceAll("([\\p{InCJKCompatibility}\\p{InCJKCompatibilityIdeographs}\\p{InCJKCompatibilityForms}])", " $1 ");
		s = s.replaceAll("([\\p{InEnclosedCJKLettersAndMonths}])", " $1 ");

		// Split Hiragana, Katakana, and KatakanaPhoneticExtensions
		// only when adjacent to something else:
		s = s.replaceAll("(^|\\P{InHiragana})(\\p{InHiragana}+)(?=$|\\P{InHiragana})", "$1 $2 ");
		s = s.replaceAll("(^|\\P{InKatakana})(\\p{InKatakana}+)(?=$|\\P{InKatakana})", "$1 $2 ");
		s = s.replaceAll("(^|\\P{InKatakanaPhoneticExtensions})(\\p{InKatakanaPhoneticExtensions}+)(?=$|\\P{InKatakanaPhoneticExtensions})", "$1 $2 ");

		// Split punctuation:
		s = s.replaceAll(asianPunct, " $1 ");
		s = s.replaceAll(fullwidthPunct, " $1 ");
	    }

	    s = s.replaceAll("\\s+"," "); // one space only between words
	    s = s.replaceAll("^\\s+", "");  // no leading space
	    s = s.replaceAll("\\s+$", "");  // no trailing space
	}
	if(nopunct) s = Normalizer.removePunctuation(s, asianSupport);
	return s.split("\\s+");
    }

    static String removePunctuation(String str, boolean asianSupport) {
	String s = str.replaceAll("[\\.,\\?:;!\"\\(\\)]", "");
	if(asianSupport) {
	    s = s.replaceAll(asianPunct, "");
	    s = s.replaceAll(fullwidthPunct, "");
	}
	s = s.replaceAll("\\s+", " ");
	return s;
    }

}
