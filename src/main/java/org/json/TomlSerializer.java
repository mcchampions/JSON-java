package org.json;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonToToml
 * 使用的表都是内联表
 */
public class TomlSerializer {
    public static String toToml(JSONObject jsonObject) {
        return toToml(jsonObject.toString());
    }

    public static String toToml(Map<?,?> map) {
        return toToml(new JSONObject(map));
    }

    public static String toToml(String str) {
        JSONObject jsonObject = new JSONObject(str);
        str = jsonObject.toString();
        str = str.replaceAll("([^\\\\])\":","$1\" = ");
        str = str.substring(1,str.length()-1);
        Map<String,?> map = jsonObject.toMap();
        Iterator<String> iter = jsonObject.keys();
        String lastKey = null;
        while (iter.hasNext()) {
            String key = iter.next();
            StringBuilder sb = new StringBuilder();
            if (lastKey != null) {
                Object object = map.get(lastKey);
                if (object instanceof String || object instanceof Character ||
                        object instanceof Byte) {
                    String s = object.toString().replaceAll("\n","\\\\n");
                    sb.append("\"").append(lastKey).append("\" = \"").append(s).append("\", \n\"").append(key).append("\" = ");
                    Object o = map.get(key);
                    if (o instanceof String || o instanceof Character ||
                            o instanceof Byte) {
                        String st = o.toString().replaceAll("\n","\\\\n");
                        sb.append("\"").append(st).append("\"");
                    } else {
                        sb.append(o);
                    }
                } else {
                    sb.append("\"").append(lastKey).append("\" = ").append(object).append(", \n\"").append(key).append("\" = ");
                    Object o = map.get(key);
                    if (o instanceof String || o instanceof Character ||
                            o instanceof Byte) {
                        String s = o.toString().replaceAll("\n","\\\\n");
                        sb.append("\"").append(s).append("\"");
                    } else {
                        sb.append(o);
                    }
                }
            } else {
                sb.append("\"").append(key).append("\" = ");
                Object o = map.get(key);
                if (o instanceof String || o instanceof Character ||
                        o instanceof Byte) {
                    sb.append("\"").append(o).append("\"");
                } else {
                    sb.append(o);
                }
            }
            if (iter.hasNext()) {
                sb.append(",");
            }
            str = str.replaceFirst(Pattern.quote(sb.toString().replaceAll("([^\\\\])\":","$1\" = ")), iter.hasNext() ? Matcher.quoteReplacement(sb.toString().replaceAll("([^\\\\])\":","$1\" = ") + " \n") : Matcher.quoteReplacement(sb.toString().replaceAll("([^\\\\])\":","$1\" = ")));
            lastKey = key;
        }
        str = str.replaceAll(", (\n\"[^,])","$1");
        return str;
    }
}
