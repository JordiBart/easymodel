package cat.udl.easymodel.httpservlet;

import java.io.IOException;
import java.io.PrintWriter;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.CheckSystemJob;
import cat.udl.easymodel.mathlink.MathJobStatus;
import cat.udl.easymodel.mathlink.MathQueue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/checksystem", name = "checksystem")
public class ScriptCheckSystem extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        String msg = check();
        out.write(msg);
        System.out.println("ScriptCheckSystem: "+msg);
    }

    private String check() {
        SharedData sharedData = SharedData.getInstance();
        String message = "";
        int fails = 0;
        try {
            // check mysql
            sharedData.getDbManager().open();
            sharedData.getDbManager().close();
        } catch (Throwable t) {
            fails++;
            message += t.getMessage() + "\n";
            t.printStackTrace();
        }
        // check math queue + virtual display/front end available for generating mathematica graphics
        CheckSystemJob job = new CheckSystemJob();
        try {
            MathQueue.getInstance().addNewMathJob(job);
        } catch (Exception e) {
                throw new RuntimeException(e);
        }
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
            fails++;
            message += "Timeout " + timeoutMinutes + " reached\n";
        }
        if (fails == 0)
            message = "OK";
        return message;
    }
}