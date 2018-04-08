import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class World {
    int xmax, ymax, zmax;
    double paintCost, moneyErnedPerSegment, crashPenalty;
    int[][][] empty;
    public int paintLeft;
    public boolean feeding;
    public int timeToChange;
    public int curTurn;
    public double money;
    public double probToChange;
    public double[] moneyAll;
    public double[] paintAll;
    public double paintedMyColor;
    int[][][] minDist;
    public int timeToEnd;


    World() {
        Interaction.send("DESCRIBE_WORLD");
        Interaction.expect(Interaction.OK);
        double K = Interaction.getDouble();
        System.err.println("K = " + K);
        xmax = Interaction.getInt();
        ymax = Interaction.getInt();
        zmax = Interaction.getInt();
        empty = new int[xmax][ymax][zmax];
        System.err.println("size = " + xmax + " " + ymax + " " + zmax);
        paintCost = Interaction.getDouble();
        System.err.println("paint cost = " + paintCost);
        moneyErnedPerSegment = Interaction.getDouble();
        System.err.println("money earned per segment = " + moneyErnedPerSegment);
        crashPenalty = Interaction.getDouble();
        System.err.println("crash penalty = " + crashPenalty);
    }

    public void addSnakes(Snake[] snakes, int mul) {
        for (Snake s : snakes) {
            for (Point p : s.pts) {
                addPoint(p, mul);
            }
        }
    }

    public void addSnakesHead(Snake[] snakes, int mul, World world) {
        for (Snake s : snakes) {
            Point p = s.pts[s.pts.length - 1];
            for (int it = 0; it < Utils.DIRECTIONS.length(); it++) {
                addPoint(p.add(it, world), mul);
            }
        }
    }

    public void addPoint(Point p, int mul) {
        empty[p.x][p.y][p.z] += mul;
    }

    Path[][][] doBFS(Point startPoint, Point notUseThisAtFirst) {
        Path[][][] res = new Path[xmax][ymax][zmax];
        res[startPoint.x][startPoint.y][startPoint.z] = new Path(null, -1, 0);
        List<Point> pts = new ArrayList<>();
        pts.add(startPoint);
        int qIt = 0;
        while (qIt < pts.size()) {
            Point p = pts.get(qIt++);
            int dist = res[p.x][p.y][p.z].dist;
            for (int it = 0; it < Utils.DIRECTIONS.length(); it++) {
                Point next = p.add(it, this);
                if (notUseThisAtFirst != null && next.equals(notUseThisAtFirst) && p.equals(startPoint)) {
                    continue;
                }
                int nx = next.x, ny = next.y, nz = next.z;
                if (empty[nx][ny][nz] != 0 || res[nx][ny][nz] != null) {
                    continue;
                }
                res[nx][ny][nz] = new Path(p, it, dist + 1);
                pts.add(next);
            }
        }
        return res;
    }

    public void calcScore(Snake[] enemySnakes) {
        addSnakes(enemySnakes, 1);
        minDist = new int[xmax][ymax][zmax];
        for (int i = 0; i < xmax; i++) {
            for (int j = 0; j < ymax; j++) {
                Arrays.fill(minDist[i][j], Integer.MAX_VALUE);
            }
        }
        for (Snake s : enemySnakes) {
            if (s.pts.length <= 3) {
                continue;
            }
            Path[][][] r = doBFS(s.pts[s.pts.length - 1], null);
            for (int i = 0; i < xmax; i++) {
                for (int j = 0; j < ymax; j++) {
                    for (int k = 0; k < zmax; k++) {
                        if (r[i][j][k] == null) {
                            continue;
                        }
                        minDist[i][j][k] = Math.min(minDist[i][j][k], r[i][j][k].dist);
                    }
                }
            }
        }
        addSnakes(enemySnakes, -1);
    }
}
