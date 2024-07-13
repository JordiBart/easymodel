package cat.udl.easymodel.mathlink;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.results.ResultText;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.P;
import cat.udl.easymodel.logic.results.ResultList;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;
import com.wolfram.jlink.MathLinkException;

import java.util.Calendar;
import java.util.concurrent.*;

public class SimJob extends MathJob {
    private VaadinSession vaadinSession;
    private Model model;
    private ResultList resultList;

    public SimJob(Model model, VaadinSession vaadinSession) {
        super();
        this.vaadinSession=vaadinSession;
        this.model = model;
        this.resultList = new ResultList();
    }

    public VaadinSession getVaadinSession() {
        return vaadinSession;
    }

    @Override
    public String call() throws Exception {
        this.startDate=Calendar.getInstance().getTime();
        this.setMathJobStatus(MathJobStatus.RUNNING);
        P.d("Start job "+this.getJobId());
        if (isCancelJob){
            errorMessage="Cancel";
            return null;
        }
        try {
            this.callable = new SimJobCallable(this);
            errorMessage = executor.submit(this.callable).get(timeout, TimeUnit.MINUTES);
            if (errorMessage.equals("MathLink")){
                if (isCancelJob)
                    resultList.add(new ResultText("Simulation cancelled by user.",""));
                else
                    resultList.add(new ResultText("CRITICAL ERROR: Mathematica exception.",""));
            } else if (errorMessage.equals("CException")){

            }
        } catch (TimeoutException e) {
            errorMessage="Timeout";
            resultList.add(new ResultText("Simulation timeout reached: " + this.timeout + " minutes.",""));
        } catch (InterruptedException e) {
            errorMessage="Interrupted";
            e.printStackTrace();
        } catch (ExecutionException e) {
            errorMessage="Execution";
            e.printStackTrace();
        } finally {
            if (executor!=null)
                executor.shutdown();
            this.setFinishDate(Calendar.getInstance().getTime());
            this.setMathJobStatus(MathJobStatus.FINISHED);
            P.d("Finished job "+getJobId());
            return null;
        }
        //XXX do not put code beyond this point!
    }

    public void cancelSimulationByUser() {
        isCancelJob = true;
        if (mathLinkOp != null){
            mathLinkOp.closeMathLink();
        }
    }

    public boolean isCancelJob() {
        return isCancelJob;
    }

    public Model getModel() {
        return model;
    }

    public ResultList getResultList() {
        return resultList;
    }
}

class SimJobCallable implements Callable<String> {
    private SimulationCtrl simCtrl;
    private SimJob simulationJob;

    public SimJobCallable(SimJob simulationJob) {
        this.simulationJob = simulationJob;
    }

    @Override
    public String call() {
        String errorMsg = "";
        try {
            simulationJob.mathLinkOp = new MathLinkOp();
            simulationJob.mathLinkOp.addPacketListener(simulationJob.getResultList(), null);
            simCtrl = new SimulationCtrl(simulationJob);
            simCtrl.simulate();
        } catch (Exception e) {
            if (e instanceof MathLinkException) {
                P.d("SimMan:MathLinkException");
                P.d(e.getMessage());
                errorMsg = "MathLink";
            } else if (e instanceof CException) {
                P.e("SimMan:CException");
                P.e(e.getMessage());
                errorMsg = "CException";
            } else {
                P.e("SimMan:Other Exception");
                e.printStackTrace();
                errorMsg = "Other";
            }
        }
        finally {
            if (simulationJob.mathLinkOp!=null)
                simulationJob.mathLinkOp.closeMathLink();
        }
//        p.p("SimJobCallable finish");
        return errorMsg;
    }
}
