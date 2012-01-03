package ter.io;

import java.util.HashMap;
import java.util.Map;

public class Tag {
  public String name;
  public Map<String,String> content;
  public String rest;

  public Tag() {
	name = "";
	content = new HashMap<String,String>();
	rest = "";
  }
}
