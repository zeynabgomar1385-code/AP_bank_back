package storage;

import java.util.*;

public class JsonUtil {

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String quote(String s) {
        return "\"" + escape(s) + "\"";
    }

    public static List<String> splitTopLevelObjects(String jsonArray) {
        List<String> objs = new ArrayList<>();
        if (jsonArray == null) return objs;

        String s = jsonArray.trim();
        if (s.isEmpty() || s.equals("[]")) return objs;

        int i = 0;
        if (s.charAt(0) == '[') i++;

        int depth = 0;
        int start = -1;
        boolean inString = false;

        for (; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) continue;

            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    objs.add(s.substring(start, i + 1).trim());
                    start = -1;
                }
            }
        }
        return objs;
    }

    public static Map<String, String> parseObjectToRawMap(String obj) {
        Map<String, String> map = new LinkedHashMap<>();
        if (obj == null) return map;

        String s = obj.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);

        int i = 0;
        boolean inString = false;
        int depthArr = 0;
        StringBuilder key = new StringBuilder();
        StringBuilder val = new StringBuilder();
        boolean readingKey = true;
        boolean afterColon = false;

        while (i < s.length()) {
            char c = s.charAt(i);

            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inString = !inString;
            }

            if (!inString) {
                if (c == '[') depthArr++;
                if (c == ']') depthArr--;
            }

            if (!inString && depthArr == 0 && c == ':' && readingKey) {
                readingKey = false;
                afterColon = true;
                i++;
                continue;
            }

            if (!inString && depthArr == 0 && c == ',' && !readingKey) {
                String k = cleanupKey(key.toString());
                String v = val.toString().trim();
                if (!k.isEmpty()) map.put(k, v);
                key.setLength(0);
                val.setLength(0);
                readingKey = true;
                afterColon = false;
                i++;
                continue;
            }

            if (readingKey) key.append(c);
            else {
                if (afterColon && Character.isWhitespace(c)) {
                    i++;
                    continue;
                }
                val.append(c);
            }

            i++;
        }

        String k = cleanupKey(key.toString());
        String v = val.toString().trim();
        if (!k.isEmpty()) map.put(k, v);

        return map;
    }

    private static String cleanupKey(String k) {
        String t = k.trim();
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1);
        }
        return t.trim();
    }

    public static String rawToString(String raw) {
        if (raw == null) return "";
        String t = raw.trim();
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1);
        }
        t = t.replace("\\\"", "\"").replace("\\\\", "\\");
        return t;
    }

    public static long rawToLong(String raw, long def) {
        try {
            String t = raw.trim();
            if (t.isEmpty() || t.equals("null")) return def;
            return Long.parseLong(t);
        } catch (Exception e) {
            return def;
        }
    }

    public static List<String> rawToStringList(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null) return out;

        String t = raw.trim();
        if (!t.startsWith("[") || !t.endsWith("]")) return out;
        t = t.substring(1, t.length() - 1).trim();
        if (t.isEmpty()) return out;

        boolean inString = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '"' && (i == 0 || t.charAt(i - 1) != '\\')) inString = !inString;

            if (!inString && c == ',') {
                out.add(rawToString(cur.toString().trim()));
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        if (cur.length() > 0) out.add(rawToString(cur.toString().trim()));
        return out;
    }

    public static String stringListToRaw(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(quote(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
}
