import java.io.*;
import java.util.*;

public class Solve {
    FastScanner in;
    PrintWriter out;

    World world;
    Random rnd = new Random(123);

    void makeBestMoveLowBattery(Snake s, Snake[] enemySnakes) {
        if (s.pts.length > 1) {
            for (int it = 0; it < Utils.DIRECTIONS.length(); it++) {
                Point next = s.pts[s.pts.length - 1].add(it, world);
                if (!next.equals(s.pts[s.pts.length - 2])) {
                    for (Point pp : s.pts) {
                        if (pp.equals(next)) {
                            System.err.println("I can kill myself by myself!");
                            Interaction.move(s, it);
                            return;
                        }
                    }
                }
            }
            Point head = s.pts[s.pts.length - 1];
            Path[][][] res = world.doBFS(head, null);
            double bestScore = Double.MAX_VALUE;
            Point bestDest = null;
            for (Snake sn : enemySnakes) {
                for (Point pp : sn.pts) {
                    Path p = res[pp.x][pp.y][pp.z];
                    if (p != null && p.dist < bestScore && p.dist != 0) {
                        bestScore = p.dist;
                        bestDest = pp;
                    }
                }
            }
            if (bestDest == null || s.pts.length > 3 && bestScore > 4) {
                for (int it = 0; it < 50; it++) {
                    int x = rnd.nextInt(world.xmax);
                    int y = rnd.nextInt(world.ymax);
                    int z = rnd.nextInt(world.zmax);
                    Path p = res[x][y][z];
                    if (p != null && p.dist != 0) {
                        bestDest = new Point(x, y, z);
                        break;
                    }
                }
            } else {
                System.err.println("going to kill myself");
            }
//        System.err.println("dest in " + bestDest + ", dist = " + bestScore);
            if (bestDest == null) {
                System.err.println("FAILED to find best PATH");
            } else {
                Point cur = bestDest;
                int dir = -1;
                while (true) {
                    Point prev = res[cur.x][cur.y][cur.z].prev;
                    if (prev.equals(head)) {
                        dir = res[cur.x][cur.y][cur.z].direction;
                        break;
                    }
                    cur = prev;
                }
//            System.err.println("goal at dist " + bestScore);
                Interaction.move(s, dir);
            }
        } else {
            Interaction.move(s, rnd.nextInt(Utils.DIRECTIONS.length()));
        }
    }

    void makeBestMove(Snake s, Egg[] eggs) {
        Point head = s.pts[s.pts.length - 1];
        Path[][][] res = world.doBFS(head, null);
        double bestScore = Double.MAX_VALUE;
        Point bestDest = null;
        for (Egg e : eggs) {
            if (e.my) {
                continue;
            }
            Path p = res[e.pos.x][e.pos.y][e.pos.z];
            if (p != null && p.dist < bestScore && e.timeRemain > p.dist && p.dist != 0) {
                bestScore = p.dist;
                bestDest = e.pos;
            }
        }
        if (bestDest == null) {
            for (int it = 0; it < 50; it++) {
                int x = rnd.nextInt(world.xmax);
                int y = rnd.nextInt(world.ymax);
                int z = rnd.nextInt(world.zmax);
                Path p = res[x][y][z];
                if (p != null && p.dist != 0) {
                    bestDest = new Point(x, y, z);
                    break;
                }
            }
        }
//        System.err.println("dest in " + bestDest + ", dist = " + bestScore);
        if (bestDest == null) {
            System.err.println("FAILED to find best PATH");
        } else {
            Point cur = bestDest;
            int dir = -1;
            while (true) {
                Point prev = res[cur.x][cur.y][cur.z].prev;
                if (prev.equals(head)) {
                    dir = res[cur.x][cur.y][cur.z].direction;
                    break;
                }
                cur = prev;
            }
//            System.err.println("goal at dist " + bestScore);
            Interaction.move(s, dir);
        }
    }

    void solve() {
        Interaction.init();
        Interaction.login();
        while (true) {
            System.err.println("-------------------------------------");
            world = new World();
            Interaction.getScores(world);
            Snake[] snakes = Interaction.mySnakes(world);
            Snake[] enemySnakes = Interaction.enemySnakes(world);
            Egg[] eggs = Interaction.getEggs();
            int myEggs = 0;
            for (Egg e : eggs) {
                if (e.my) {
                    myEggs++;
                    world.addPoint(e.pos, 1);
                }
            }
            System.err.println("eggs: " + eggs.length + ", mine = " + myEggs);
            for (Egg e : eggs) {
                System.err.print("(" + e.money + ", " + e.paint + "), ");
            }
            System.err.println();

            Interaction.getTurnInfo(world);

            world.addSnakes(snakes, 1);
            if (world.paintLeft < 1000 && (!world.feeding)) {
                for (Egg e : eggs) {
                    world.addPoint(e.pos, 1);
                }
                for (Snake s : snakes) {
                    makeBestMoveLowBattery(s, enemySnakes);
                }
            } else {
                world.addSnakes(enemySnakes, 1);
                for (Snake s : snakes) {
                    makeBestMove(s, eggs);
                }
            }
            for (Snake s : snakes) {
                Interaction.layEgg(s, world);
            }

            Interaction.sendWait();
        }
    }

    void run() {
        try {
            in = new FastScanner(new File("Snake.in"));
            out = new PrintWriter(new File("Snake.out"));

            solve();

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void runIO() {
        in = new FastScanner(System.in);
        out = new PrintWriter(System.out);

        while (true) {
            System.err.println("Let's go!");
            try {
                solve();
            } catch (Exception e) {
                System.err.println("got exception " + e.getMessage() + ", sleep 5 seconds");
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    class FastScanner {
        BufferedReader br;
        StringTokenizer st;

        public FastScanner(File f) {
            try {
                br = new BufferedReader(new FileReader(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public FastScanner(InputStream f) {
            br = new BufferedReader(new InputStreamReader(f));
        }

        String next() {
            while (st == null || !st.hasMoreTokens()) {
                String s = null;
                try {
                    s = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (s == null)
                    return null;
                st = new StringTokenizer(s);
            }
            return st.nextToken();
        }

        boolean hasMoreTokens() {
            while (st == null || !st.hasMoreTokens()) {
                String s = null;
                try {
                    s = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (s == null)
                    return false;
                st = new StringTokenizer(s);
            }
            return true;
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }
    }

    public static void main(String[] args) {
        if (args.length != 0) {
            Interaction.PORT = Integer.parseInt(args[0]);
        }
//        System.err.println(Arrays.toString(args));
        new Solve().runIO();
    }
}