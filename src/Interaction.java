import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Interaction {
    static int PORT = 20003;
    final static String SERVER = "universum.dl24";
    static Scanner sc;
    static PrintWriter pw;
    final static String TEAM_NAME = "team2";
    final static String PASSWD = "aqejjxzpfj";
    final static String OK = "OK";

    static void login() {
        expect("LOGIN");
        send(TEAM_NAME);
        expect("PASS");
        send(PASSWD);
        expect(OK);
    }

    static void sendWait() {
        send("WAIT");
        expect(OK);
        expect("WAITING");
        double moreTime = getDouble();
//        System.err.println("wait for " + moreTime);
        expect(OK);
//        System.err.println("next turn");
    }

    static String getLine() {
        String s = sc.nextLine();
        if (s.equals("")) {
            return getLine();
        }
        return s;
    }

    static String getNextToken() {
        return sc.next();
    }

    static int getInt() {
        String s = sc.next();
        if (s.equals("NONE")) {
            return -1;
        }
        return Integer.parseInt(s);
    }

    static void move(Snake snake, int dir) {
        send("MOVE " + snake.id + " " + Utils.DIRECTIONS.substring(dir, dir + 1));
        try {
            expect(OK);
        } catch (AssertionError e) {
            System.err.println("failed to move : " + e.getMessage());
        }
    }

    static void getScores(World world) {
        send("SCORE");
        expect(OK);
        world.money = getDouble();
        int paintedMyColor = getInt();
        world.paintLeft = getInt();
        int cashRank = getInt();
        int paintRank = getInt();
        System.err.println("[money = " + world.money + ", painted = " + paintedMyColor + ", havePaint = " + world.paintLeft + "]");
        System.err.println("[cash rank = " + cashRank + ", paint rank = " + paintRank + "]");
        System.err.println("money ranking:");
        int cnt = getInt();
        world.moneyAll = new double[cnt];
        for (int i = 0; i < cnt; i++) {
            world.moneyAll[i] = getDouble();
            if (i == cashRank - 1) {
                System.err.print("[ " + world.moneyAll[i] + " ] ");
            } else {
                System.err.print(world.moneyAll[i] + " ");
            }
        }
        System.err.println();
        System.err.println("paint ranking:");
        cnt = getInt();
        world.paintAll = new double[cnt];
        for (int i = 0; i < cnt; i++) {
            world.paintAll[i] = getDouble();
            if (i == paintRank - 1) {
                System.err.print("[ " + world.paintAll[i] + " ] ");
                world.paintedMyColor = world.paintAll[i];
            } else {
                System.err.print(world.paintAll[i] + " ");
            }
        }
        System.err.println();
    }

    static double getDouble() {
        return sc.nextDouble();
    }

    static void send(String s) {
        pw.println(s);
        pw.flush();
        System.err.println(">>> " + s);
    }

    static int getDir() {
        String s = getNextToken();
//        System.err.println("line = " + s);
        return Utils.convertDir(s);
    }

    static PrintWriter logSnakesLen;

    static Snake[] mySnakes(World world) {
        Interaction.send("MY_SNAKES");
        Interaction.expect(Interaction.OK);
        int n = Interaction.getInt();
//        System.err.println("snakes num = " + n);
        Snake[] snakes = new Snake[n];
        for (int i = 0; i < n; i++) {
            snakes[i] = new Snake(world, true);
        }
        for (Snake s : snakes) {
            System.err.print(s.pts.length + " ");
        }
        System.err.println();
        if (logSnakesLen == null) {
            try {
                logSnakesLen = new PrintWriter(new File("logSnakesLen" + PORT + ".txt"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (Snake s : snakes) {
            logSnakesLen.print(s.pts.length + " ");
        }
        logSnakesLen.println();
        logSnakesLen.flush();
        return snakes;
    }

    static Snake[] enemySnakes(World world) {
        Interaction.send("ENEMY_SNAKES");
        Interaction.expect(Interaction.OK);
        int n = Interaction.getInt();
        Snake[] snakes = new Snake[n];
        for (int i = 0; i < n; i++) {
            snakes[i] = new Snake(world, false);
        }
        for (Snake s : snakes) {
            System.err.print(s.pts.length + " ");
        }
        System.err.println();
        return snakes;
    }

    static void expect(String s) {
        String real = getNextToken();
        if (!real.equals(s)) {
            if (real.equals("FAILED")) {
                int code = getInt();
                String msg = getLine();
                System.err.println("GOT error: " + code + ", msg =" + msg);
            }
            throw new AssertionError("Expected " + s + ", found " + real);
        }
    }

    static Egg getEgg() {
        Egg r = new Egg();
        r.pos = new Point(getInt() - 1, getInt() - 1, getInt() - 1);
        String s = getNextToken();
//        System.err.println("CUR EGG : " + s);
        if (s.equals("OWN")) {
            r.my = true;
        } else if (!s.equals("THEIR")) {
            System.err.println("strange, expected OWN/THEIR, found " + s);
        }
        r.paint = getDouble();
        r.money = getDouble();
        r.timeRemain = getDouble();
        return r;
    }

    static void getTurnInfo(World world) {
        send("TURN_INFO");
        expect(OK);
        world.curTurn = getInt();
        int turnsLeft = getInt();
        world.timeToEnd = turnsLeft;
        System.err.println("turn: " + world.curTurn + " of  " + (world.curTurn + turnsLeft));
        String phase = getNextToken();
        System.err.println("phase = " + phase);
        if (phase.equals("FEEDING")) {
            world.feeding = true;
        } else {
            world.feeding = false;
        }
        world.timeToChange = getInt();
        System.err.println("time to mode change = " + world.timeToChange);
        world.probToChange = getDouble();
        System.err.println("switch prob = " + world.probToChange);
    }

    static int paintLeftLast = -1;
    static int moreSpendOnThisGame = 0;

    static void layEgg(Snake snake, World world) {
        if (snake.turnsToEggReady != 0) {
            return;
        }
        if (world.paintLeft > paintLeftLast) {
            moreSpendOnThisGame = (int) (world.paintLeft * 0.5);
        }
        paintLeftLast = world.paintLeft;
        boolean lowBattery = world.paintLeft < 500 + world.timeToEnd * 2 + (world.probToChange > 0.7 ? 300 : 0);
        if (moreSpendOnThisGame > 100 && world.paintLeft > 500) {
            lowBattery = false;
        }
        if (world.timeToEnd < 400) {
            lowBattery = true;
        }
        if (lowBattery && snake.turnsToEggDrop > 2) {
            return;
        }
        int paint = lowBattery ? 1 : (int) (world.paintLeft * 0.05);
        paint = Math.max(Math.min(paint, 500), 1);
        int money = 0;
        moreSpendOnThisGame -= paint;
        send("LAY_EGG " + snake.id + " " + paint + " " + money);
        Solve2.printImportant("LAY EGG " + paint + " " + money);
        try {
            expect(OK);
            int x = getInt() - 1, y = getInt() - 1, z = getInt() - 1;
            System.err.println("layed egg: " + x + " " + y + " " + z);
        } catch (AssertionError e) {
            System.err.println("failed to lay egg :(");
        }
    }

    static Egg[] getEggs() {
        send("EGGS");
        expect(OK);
        Egg[] res = new Egg[getInt()];
        for (int i = 0; i < res.length; i++) {
            res[i] = getEgg();
        }
        return res;
    }

    static void init() {
        try {
            Socket socket = new Socket(SERVER, PORT);
            socket.setTcpNoDelay(true);
            sc = new Scanner(socket.getInputStream());
            pw = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
