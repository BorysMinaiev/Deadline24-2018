public class Utils {
    static final String DIRECTIONS = "UDNSWE";
    static final int[] dz = new int[]{1, -1, 0, 0, 0, 0};
    static final int[] dx = new int[]{0, 0, 0, 0, -1, 1};
    static final int[] dy = new int[]{0, 0, 1, -1, 0, 0};

    static int convertDir(String s) {
        if (s.equals("NONE")) {
            return -1;
        }
        return DIRECTIONS.indexOf(s.charAt(0));
    }

    static int convertDir(char c) {
        return DIRECTIONS.indexOf(c);
    }
}
