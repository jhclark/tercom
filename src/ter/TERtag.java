package ter;

import java.util.HashMap;
import java.util.Map;

public class TERtag {
  public String name;
  public Map<String,String> content;
  public String rest;

  public TERtag() {
	name = "";
	content = new HashMap<String,String>();
	rest = "";
  }
}
