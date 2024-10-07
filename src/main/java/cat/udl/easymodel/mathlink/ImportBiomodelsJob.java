package cat.udl.easymodel.mathlink;

import cat.udl.easymodel.utils.P;
import com.wolfram.jlink.MathLinkException;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ImportBiomodelsJob extends MathJob {
    public ImportBiomodelsJob() {
        super();
    }

    @Override
    public String call() throws Exception {
        this.startDate=Calendar.getInstance().getTime();
        this.setMathJobStatus(MathJobStatus.RUNNING);
        P.d("Start ImportBiomodelsJob "+this.getJobId());
        try {
            this.callable = new ImportBiomodelsJobCallable(this);
            errorMessage = executor.submit(this.callable).get(timeout, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            errorMessage= "Timeout";
        } catch (InterruptedException e) {
            e.printStackTrace();
            errorMessage= "Interrupt";
        } catch (ExecutionException e) {
            e.printStackTrace();
            errorMessage= "Execution";
        } finally {
            if (executor!=null)
                executor.shutdown();
            this.setFinishDate(Calendar.getInstance().getTime());
            this.setMathJobStatus(MathJobStatus.FINISHED);
            //System.out.println("Finished CheckSystemJob "+getJobId());
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
}

class ImportBiomodelsJobCallable implements Callable<String> {
    private ImportBiomodelsJob job;

    public ImportBiomodelsJobCallable(ImportBiomodelsJob job) {
        this.job = job;
    }

    @Override
    public String call() {
        String errorMsg = "";
        try {
            job.mathLinkOp = new MathLinkOp();
            job.mathLinkOp.evaluate("JLink`ConnectToFrontEnd[]");
            job.mathLinkOp.evaluate("JLink`UseFrontEnd[1]");
            job.mathLinkOp.evaluateToImage("Rasterize[Plot[x, {x, 0, 1}]]");
            job.mathLinkOp.evaluate("JLink`CloseFrontEnd[]");
        } catch (Exception e) {
            if (e instanceof MathLinkException) {
                P.d("SimMan:MathLinkException");
                P.d(e.getMessage());
                errorMsg = "MathLink";
            } else {
                P.e("SimMan:Other Exception");
                e.printStackTrace();
                errorMsg = "Other";
            }
        }
        finally {
            if (job.mathLinkOp!=null)
                job.mathLinkOp.closeMathLink();
        }
//        p.p("SimJobCallable finish");
        return errorMsg;
    }
}
