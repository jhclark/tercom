package ter.util;

public class StringUtils {

	public static String join(String delim, String[] arr) {
		if (arr == null) return "";
		if (delim == null) delim = new String("");
		String s = new String("");
		for (int i = 0; i < arr.length; i++) {
	      if (i == 0) { s += arr[i]; }
	      else { s += delim + arr[i]; }
		}
		return s;
	  }

	public static String join(String delim, char[] arr) {
		if (arr == null) return "";
		if (delim == null) delim = new String("");
		String s = new String("");
		for (int i = 0; i < arr.length; i++) {
	      if (i == 0) { s += arr[i]; }
	      else { s += delim + arr[i]; }
		}
		return s;
	  }

}
