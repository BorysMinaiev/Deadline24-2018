import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Snake {
    int turnsToEggDrop;
    int id;
    Point[] pts;
    int headDir;
    int turnsToEggReady;
    PaintSet[] colors;

    Snake(World world, boolean my) {
        if (my) {
            id = Interaction.getInt();
        }
        //        System.err.println("snake id = " + id);
        int xHead = Interaction.getInt() - 1;
        int yHead = Interaction.getInt() - 1;
        int zHead = Interaction.getInt() - 1;
        if (my) {
            headDir = Interaction.getDir();
//        System.err.println("dir = " + headDir);
            turnsToEggReady = Interaction.getInt();
            turnsToEggDrop = Interaction.getInt();
        }
//        System.err.println("turns = " + turnsToEggReady + " " + turnsToEggDrop);
        int len = Interaction.getInt();
        pts = new Point[len];
        pts[pts.length - 1] = new Point(xHead, yHead, zHead);
        String dirs = Interaction.getNextToken();
        if (dirs.equals("TAILLESS")) {

        } else {
            int it = 0;
            for (int i = pts.length - 1; i > 0; i--) {
                char c = dirs.charAt(it++);
                int dir = Utils.convertDir(c);
                pts[i - 1] = pts[i].add(dir, world);
            }
        }
//        System.err.println("pts = " + Arrays.toString(pts));
        if (my) {
            int sumLen = 0;
            colors = new PaintSet[Interaction.getInt()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = new PaintSet(Interaction.getInt(), Interaction.getInt());
                sumLen += colors[i].cnt;
            }
            System.err.println("sumLen =" + sumLen);
        }
    }

    class PaintSet {
        int colorId, cnt;

        public PaintSet(int colorId, int cnt) {
            this.colorId = colorId;
            this.cnt = cnt;
        }
    }
}