package com.github.vcoppe.mcpmip;

import gurobi.*;

public class Model {

    private GRBEnv env;
    private GRBModel model;
    private GRBVar[] x;
    private GRBVar[][] c;
    private GRBVar[][] z;

    public Model(int n, int[][] w) throws GRBException {
        env = new GRBEnv("mcp.log");
        model = new GRBModel(env);

        model.set(GRB.IntAttr.ModelSense, -1); // maximization

        x = new GRBVar[n];
        z = new GRBVar[n][n];
        c = new GRBVar[n][n];

        for (int u = 0; u < n; u++) {
            x[u] = model.addVar(0, 1, 0, GRB.BINARY, "x_" + u);
            for (int v = u + 1; v < n; v++) {
                z[u][v] = model.addVar(0, 1, 0, GRB.BINARY, "z_" + u + "_" + v);
                c[u][v] = model.addVar(0, 1, w[u][v], GRB.BINARY, "c_" + u + "_" + v);
            }
        }

        GRBLinExpr rhs;
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                rhs = new GRBLinExpr();
                rhs.addTerm(1, x[u]);
                rhs.addTerm(1, x[v]);
                rhs.addTerm(-2, z[u][v]);
                model.addConstr(c[u][v], GRB.EQUAL, rhs, "constraint_c_" + u + "_" + v);
                model.addConstr(z[u][v], GRB.LESS_EQUAL, x[u], "constraint_z_a_" + u + "_" + v);
                model.addConstr(z[u][v], GRB.LESS_EQUAL, x[v], "constraint_z_b_" + u + "_" + v);
                rhs = new GRBLinExpr();
                rhs.addTerm(1, x[u]);
                rhs.addTerm(1, x[v]);
                rhs.addConstant(-1);
                model.addConstr(z[u][v], GRB.GREATER_EQUAL, rhs, "constraint_z_c_" + u + "_" + v);
            }
        }
    }

    public void solve(double timeLimit, int threads) throws GRBException {
        model.set(GRB.DoubleParam.TimeLimit, timeLimit);
        if (threads > 0) model.set(GRB.IntParam.Threads, threads);
        model.optimize();

        System.out.print("assignment       :");
        for (int i = 0; i < x.length; i++)
            System.out.printf("%2.0f ", x[i].get(GRB.DoubleAttr.X));

        System.out.println();
    }

    public double gap() throws GRBException {
        return model.get(GRB.DoubleAttr.MIPGap);
    }

    public double runTime() throws GRBException {
        return model.get(GRB.DoubleAttr.Runtime);
    }

    public double objVal() throws GRBException {
        return model.get(GRB.DoubleAttr.ObjVal);
    }

    public void dispose() throws GRBException {
        model.dispose();
        env.dispose();
    }
}
