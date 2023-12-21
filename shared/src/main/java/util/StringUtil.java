package util;

public class StringUtil {
    public static boolean isEqual(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return (a.equals(b));
    }
}
