package cat.udl.easymodel.httpservlet;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.types.StochasticGradeType;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathJobStatus;
import cat.udl.easymodel.mathlink.MathQueue;
import cat.udl.easymodel.mathlink.SimJob;
import cat.udl.easymodel.utils.P;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/checkstochasticgrade", name = "checkstochasticgrade")
public class CheckStochasticGradeOfAllModels extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        try {
            out.write(check());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("CheckStochasticGradeOfAllModels");
    }

    private String check() throws Exception {
        int fails=0;
        SharedData sharedData = SharedData.getInstance();
        String message = "";
        Models allModels = new Models();
        allModels.semiLoadDB();
        for (Model parentModel : allModels){
            Model m = new Model(parentModel);
            m.loadDB();
            if (m.getStochasticGradeType() != StochasticGradeType.UNCHECKED)
                continue;
            P.d("Processing: "+m.getName());
            m.getSimConfig().getSimTypesToLaunch().clear();
            m.getSimConfig().checkAndPrepareToSimulate(m);
            SimJob job = new SimJob(m,null);
            MathQueue.getInstance().addNewMathJob(job);
            boolean isTimeout = true;
            int timeoutMinutes = 10;
            for (int i = 0; i < timeoutMinutes*60; i++) {
                if (job.getMathJobStatus() == MathJobStatus.FINISHED) {
                    if (!"".equals(job.getErrorMessage())) {
                        fails++;
                        message += job.getErrorMessage() + "\n";
                    }
                    isTimeout = false;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            if (isTimeout) {
                job.cancelSimulationByUser();
                fails++;
                message += "Timeout " + timeoutMinutes + " reached\n";
            }
//          break;
        }
        if (fails == 0)
            message = "DONE: stochastic grade updated!";
        return message;
    }
}