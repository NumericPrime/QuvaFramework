package quva.core;

import java.util.*;

public class QuvaDebug {
	public static Set<String> tags=new HashSet<String>();
	public static boolean debug=false;
	public static void addTag(String tag) {
		tags.add(tag);
		debug=true;
	}
	public static void remTag(String tag) {
		tags.remove(tag);
		debug=(tags.isEmpty());
	}
	public static boolean checkTag(String tag) {
		if(!debug) return false;
		boolean allowed=false;
		loop: for(String entry:tags) if((allowed=(tag.matches(entry)||entry.matches(tag.replaceAll("\\.", "\\.")+"\\.?.*"))))break loop;
		return allowed;
	}
	public static void log(String tag,Object... message) {
		boolean allowed=false;
		loop: for(String entry:tags) if((allowed=(tag.matches(entry)||entry.matches(tag.replaceAll("\\.", "\\.")+"\\.?.*"))))break loop;
		if(allowed) for(Object ob:message) System.out.println(ob);
	}
	public static void logprnt(String tag,Object... message) {
		boolean allowed=false;
		loop: for(String entry:tags) if((allowed=(tag.matches(entry)||entry.matches(tag.replaceAll("\\.", "\\.")+"\\.?.*")))) break loop;
		if(allowed) for(Object ob:message) System.out.print(ob);
	}
	public static String booleanOutput(boolean... bs) {
		String c="";
		for(boolean b:bs) c+=b+" ";
		return c;
	}
	public static String BooleanOutput(Boolean... array) {
		String c="";
		for(boolean b:array) c+=b+" ";
		return c;
	}
}
