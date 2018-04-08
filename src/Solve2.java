import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

public class Solve2 extends JPanel {
    FastScanner in;
    PrintWriter out;
    static PrintWriter importantLog;

    static void printImportant(String s) {
        if (importantLog == null) {
            try {
                importantLog = new PrintWriter("important" + Interaction.PORT + ".txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        importantLog.println(s);
        importantLog.flush();
    }

    final static int WIDTH = 2800;
    final static int HEIGHT = 1600;
    Snake[] mySnakes;
    Snake[] enemySnakes;
    Egg[] eggs;
    final Object lock = new Object();

    void drawText(String s, Graphics2D gr) {
        String[] ss = s.split("\n");
        for (int i = 0; i < ss.length; i++) {
            gr.drawString(ss[i], 20, 20 + i * 40);
        }
    }

    int top5snakes = 0;

    String getSnake(Snake[] snakes) {
        int[] res = new int[snakes.length];
        for (int i = 0; i < snakes.length; i++) {
            res[i] = snakes[i].pts.length;
        }
        Arrays.sort(res);
        String r = "";
        for (int i = 0; i < res.length; i++) {
            r += res[res.length - i - 1] + ", ";
        }
        if (res.length > 5) {
            top5snakes = res[res.length - 5];
        }
        return r + "\n";
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D gr = (Graphics2D) g;
        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, WIDTH, HEIGHT);
        gr.setColor(Color.BLACK);
        gr.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        String allInfo = "alpha = " + alpha + ", betta = " + betta + "\n";
        allInfo += "state = " + (world.feeding ? "FEEDING" : "PAINTING") + ", timeToChange = " + world.timeToChange
                + ", pr = " + world.probToChange + ", timeToEnd = " + world.timeToEnd + ", port = " + Interaction.PORT + "\n";
        allInfo += "money = " + world.money + ", paintLeft = " + world.paintLeft + "\n";
        allInfo += "money: " + Arrays.toString(world.moneyAll) + "\n";
        allInfo += "painted = " + world.paintedMyColor + "\n";
        allInfo += "paint: " + Arrays.toString(world.paintAll) + "\n";
        allInfo += getSnake(mySnakes);
        allInfo += getSnake(enemySnakes);
        drawText(allInfo, gr);
        synchronized (lock) {
            for (Snake s : mySnakes) {
                drawSnake(s, Color.BLUE, gr, 10);
            }
            Random rnd = new Random(333);
            for (Snake s : enemySnakes) {
                Color enemyColor = new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                if (s.pts.length >= top5snakes) {
                    drawSnake(s, enemyColor, gr, 3);
                }
            }
            Point ZERO = new Point(0, 0, 0);
            Point ZX = new Point(world.xmax + 1, 0, 0);
            Point ZY = new Point(0, world.ymax + 1, 0);
            Point ZZ = new Point(0, 0, world.zmax + 1);
            drawLine(gr, ZERO, ZX, Color.BLACK, false);
            drawLine(gr, ZERO, ZY, Color.BLACK, false);
            drawLine(gr, ZERO, ZZ, Color.BLACK, false);

            gr.setStroke(new BasicStroke(1));
//            for (int z = 0; z < world.zmax; z++) {
//                for (int x = 0; x <= world.xmax; x++) {
//                    drawLine(gr, new Point(x, 0, z), new Point(x, world.ymax, z), Color.GRAY, false);
//                }
//                for (int y = 0; y <= world.ymax; y++) {
//                    drawLine(gr, new Point(0, y, z), new Point(world.xmax, y, z), Color.GRAY, false);
//                }
//            }

            for (Egg e : eggs) {
                drawEgg(e, gr);
            }
        }
    }

    class Point2D {
        int x, y;

        public Point2D(double x, double y) {
            this.x = (int) x;
            this.y = (int) y;
        }
    }

    double alpha = 0;
    double betta = Math.PI;
    double zoom = 1000;

    class Point3D {
        double x, y, z;

        public Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    Point3D zoom(Point p, World w) {
        final double S = 0.5;
        return new Point3D(p.x / (w.xmax + 1.) - S, p.y / (w.ymax + 1.) - S, p.z / (w.zmax + 1.) - S);
    }

    double[][] mul(double[][] a, double[][] b) {
        double[][] r = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                for (int k = 0; k < b[0].length; k++) {
                    r[i][k] += a[i][j] * b[j][k];
                }
            }
        }
        return r;
    }

    Point3D rotate(Point3D p) {
        double[][] first = new double[][]{{p.x, p.y, p.z}};
        double[][] m1 = new double[][]{{1, 0, 0}, {0, Math.cos(alpha), -Math.sin(alpha)}, {0, Math.sin(alpha), Math.cos(alpha)}};
        double[][] m2 = new double[][]{{Math.cos(betta), 0, -Math.sin(betta)}, {0, 1, 0}, {Math.sin(betta), 0, Math.cos(betta)}};
        double[][] res = mul(mul(first, m1), m2);
        return new Point3D(res[0][0], res[0][1], res[0][2]);
    }

    Point2D convert(Point p) {
        Point3D next = rotate(zoom(p, world));
        return new Point2D(WIDTH / 2 + next.x * zoom, HEIGHT / 2 + next.y * zoom);
    }

    void drawLine(Graphics2D gr, Point a, Point b, Color c, boolean near) {
        if (Math.abs(a.x - b.x) + Math.abs(a.y - b.y) + Math.abs(a.z - b.z) > 1 && near) {
            return;
        }
        gr.setColor(c);
        Point2D left = convert(a);
        Point2D right = convert(b);
        gr.drawLine(left.x, left.y, right.x, right.y);
    }

    void drawEgg(Egg e, Graphics2D gr) {
        Point2D rp = convert(e.pos);
        gr.setColor(e.my ? Color.BLUE : Color.RED);
        final int SZ = 10;
        gr.drawOval(rp.x - SZ, rp.y - SZ, 2 * SZ, 2 * SZ);
    }

    void drawSnake(Snake s, Color c, Graphics2D gr, int SZ) {
        gr.setColor(c);
        for (Point p : s.pts) {
            Point2D rp = convert(p);
            gr.fillOval(rp.x - SZ, rp.y - SZ, 2 * SZ, 2 * SZ);
        }
        gr.setStroke(new BasicStroke(SZ / 3));
        for (int i = 0; i + 1 < s.pts.length; i++) {
            drawLine(gr, s.pts[i], s.pts[i + 1], c, true);
        }
    }

    World world;
    Random rnd = new Random(123);

    boolean makeCycle(Snake s) {
        Point head = s.pts[s.pts.length - 1];
        Path[][][] res = world.doBFS(head, s.pts[s.pts.length - 2]);
        int len = s.pts.length / 2;
        Point goingTo = s.pts[s.pts.length - len + 1];
        if (goingTo.equals(head)) {
            goingTo = s.pts[s.pts.length - len + 2];
        }
        Path p = res[goingTo.x][goingTo.y][goingTo.z];
        if (p != null) {
            Point cur = goingTo;
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
            System.err.println("go circle!");
            return true;
        }
        System.err.println("no circle(");
        return false;
    }

    boolean makeBestMove(Snake s, Egg[] eggs, boolean useOnlyRandom) {
        Point head = s.pts[s.pts.length - 1];
        Path[][][] res = world.doBFS(head, null);
        double bestScore = Double.MAX_VALUE;
        Point bestDest = null;
        if (!useOnlyRandom) {
            for (int useMinDist = 1; useMinDist >= 0; useMinDist--) {
                for (Egg e : eggs) {
                    if (e.my) {
                        continue;
                    }
                    Path p = res[e.pos.x][e.pos.y][e.pos.z];
                    if (p == null) {
                        continue;
                    }
                    if (useMinDist == 1 && p.dist > world.minDist[e.pos.x][e.pos.y][e.pos.z]) {
                        continue;
                    }
                    if (useMinDist == 1 && p.dist > 10) {
                        continue;
                    }
                    if (p.dist < bestScore && e.timeRemain > p.dist && p.dist != 0) {
                        bestScore = p.dist;
                        bestDest = e.pos;
                    }
                }
                if (bestDest != null && useMinDist == 1) {
                    System.err.println("going absolutely true with dist " + bestScore);
                    break;
                }
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
        } else {
            System.err.println("going to " + bestScore + ", from " + s.pts[s.pts.length - 1] + " to " + bestDest);
        }
//        System.err.println("dest in " + bestDest + ", dist = " + bestScore);
        if (bestDest == null) {
            System.err.println("FAILED to find best PATH");
            printImportant("failed to find path");
            return false;
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
            return true;
        }
    }

    void makeMove(Snake snake, World world, Snake[] enemySnakes, Egg[] eggs, Snake[] mySnakes) {
        boolean stay = !world.feeding && world.paintLeft < 10000 && snake.pts.length >= 8;
        if (world.paintLeft > 5000 && snake.pts.length < 15) {
            stay = false;
        }
        if (!stay) {
            world.addSnakes(enemySnakes, 1);
            if (snake.pts.length > 2) {
                world.addSnakesHead(enemySnakes, 1, world);
            }
            boolean useOnlyRandom = (world.probToChange == 1 && snake.pts.length >= 40 && world.paintLeft < 2500);
            boolean ok = makeBestMove(snake, eggs, useOnlyRandom);
            world.addSnakes(enemySnakes, -1);
            if (snake.pts.length > 2) {
                world.addSnakesHead(enemySnakes, -1, world);
            }
            if (!ok) {
                if (makeBestMove(snake, eggs, true)) {
                    printImportant("But I found exit! :)");
                    System.err.println("but it's ok!");
                }
            }
        } else {
            world.addSnakes(mySnakes, -1);
            if (!makeCycle(snake)) {
                int id = world.curTurn % 4;
                int[] dirs = new int[]{2, 4, 3, 5};
                Interaction.move(snake, dirs[id]);
            }
            world.addSnakes(mySnakes, 1);
        }
    }

    void solve() {
        Interaction.init();
        Interaction.login();
        while (true) {
            System.err.println("-------------------------------------");


            synchronized (lock) {
                world = new World();
                Interaction.getScores(world);
                mySnakes = Interaction.mySnakes(world);
                printImportant(getSnake(mySnakes));
                enemySnakes = Interaction.enemySnakes(world);
                eggs = Interaction.getEggs();
                Interaction.getTurnInfo(world);
            }
            printImportant("time to end " + world.timeToEnd);
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


            world.addSnakes(mySnakes, 1);
            world.calcScore(enemySnakes);
            for (Snake s : mySnakes) {
                makeMove(s, world, enemySnakes, eggs, mySnakes);
            }
            for (Snake s : mySnakes) {
                Interaction.layEgg(s, world);
            }

            repaint();
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
        boolean needVis = (args.length > 0 && args[0].equals("vis"));
        if (args.length != 0) {
            Interaction.PORT = Integer.parseInt(args[args.length - 1]);
        }
        Solve2 game = new Solve2();
        if (needVis) {
            JFrame frame = new JFrame("Let's rock! " + Interaction.PORT);
            frame.setSize(WIDTH, HEIGHT);
            frame.add(game);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            final double ANGLE_DIFF = Math.PI / 20;
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {

                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == 39) {
                        game.betta += ANGLE_DIFF;
                    } else if (keyEvent.getKeyCode() == 37) {
                        game.betta -= ANGLE_DIFF;
                    } else if (keyEvent.getKeyCode() == 38) {
                        game.alpha += ANGLE_DIFF;
                    } else if (keyEvent.getKeyCode() == 40) {
                        game.alpha -= ANGLE_DIFF;
                    } else if (keyEvent.getKeyCode() == 90) {
                        game.zoom *= 1.1;
                    } else if (keyEvent.getKeyCode() == 88) {
                        game.zoom *= 0.9;
                    } else {
                        System.err.println("!!! " + keyEvent.getKeyCode());

                    }
                    game.repaint();
                }


                @Override
                public void keyReleased(KeyEvent keyEvent) {

                }
            });
            frame.addMouseListener(new

                                           MouseListener() {
                                               @Override
                                               public void mouseClicked(MouseEvent mouseEvent) {

                                               }

                                               @Override
                                               public void mousePressed(MouseEvent mouseEvent) {

                                               }

                                               @Override
                                               public void mouseReleased(MouseEvent mouseEvent) {

                                               }

                                               @Override
                                               public void mouseEntered(MouseEvent mouseEvent) {

                                               }

                                               @Override
                                               public void mouseExited(MouseEvent mouseEvent) {

                                               }
                                           });
        }
//        System.err.println(Arrays.toString(args));
        game.runIO();
    }
}