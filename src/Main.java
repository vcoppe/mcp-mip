import gurobi.GRBException;

import java.io.File;
import java.util.Scanner;

public class Main {

    private static int n;
    private static int[][] w;

    private static void readMCP(String path) {
        try {
            Scanner scan = new Scanner(new File(path));

            int i = 0, m = -1;
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("c")) continue;

                String[] tokens = line.split("\\s+");

                if (m == -1) {
                    n = Integer.parseInt(tokens[0]);
                    m = Integer.parseInt(tokens[1]);
                    w = new int[n][n];
                } else if (i < m) {
                    int u = Integer.parseInt(tokens[0]) - 1;
                    int v = Integer.parseInt(tokens[1]) - 1;
                    int weight = Integer.parseInt(tokens[2]);
                    w[u][v] = weight;
                    w[v][u] = weight;
                    i++;
                }
            }

            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to read input file");
            System.exit(0);
        }
    }

    public static void main(String[] args) throws GRBException {
        if (args.length < 1) {
            System.out.println("Arguments needed :\n\tfilename\n\t[timeLimit]");
            return;
        }

        readMCP(args[0]);

        int timeLimit = Integer.MAX_VALUE;
        if (args.length == 2)
            timeLimit = Integer.parseInt(args[1]);

        Model mip = new Model(n, w);

        System.out.println("MIP model created");
        System.out.println("Solving...");

        mip.solve(timeLimit);

        System.out.println("runTime          : " + mip.runTime());
        System.out.printf("objValue         : %.0f\n", mip.objVal());
        System.out.println("gap              : " + mip.gap());

        mip.dispose();
    }

}
