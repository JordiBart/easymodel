package cat.udl.easymodel.mathlink;

import cat.udl.easymodel.main.SharedData;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class MathJob implements Callable<String> {
    // this is a father class of others like SimJob class
    private String id;
    protected MathLinkOp mathLinkOp;
    protected SharedData sharedData = SharedData.getInstance();
    protected Integer timeout=Integer.valueOf(SharedData.getInstance().getProperties().getProperty("simulationTimeoutMinutes"));
    protected ExecutorService executor= Executors.newSingleThreadExecutor();
    protected Callable<String> callable;
    protected Date creationDate = Calendar.getInstance().getTime();
    protected Date startDate=null;
    protected Date finishDate=null;
    protected MathJobStatus mathJobStatus=MathJobStatus.PENDING;
    protected boolean isCancelJob = false;
    protected String errorMessage="";

    public MathJob() {
        this.id=MathQueue.getInstance().getNextId();
    }

    public MathLinkOp getMathLinkOp() {
        return mathLinkOp;
    }

    public void finish() {
        finishDate = Calendar.getInstance().getTime();
        mathJobStatus = MathJobStatus.FINISHED;
    }

    public void clean() {
        id=null;
        mathLinkOp=null;
        sharedData=null;
        timeout=null;
        executor=null;
        callable=null;
        creationDate=null;
        startDate=null;
        finishDate=null;
        mathJobStatus=null;
        errorMessage=null;
    }

    public String getJobId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public MathJobStatus getMathJobStatus() {
        return mathJobStatus;
    }

    protected void setMathJobStatus(MathJobStatus mathJobStatus) {
        this.mathJobStatus=mathJobStatus;
    }

    public Integer getQueuePosition() {
        return MathQueue.getInstance().getPendingQueue().lastIndexOf(this)+1;
    }

    public String getQueuePositionString() {
        Integer pos=MathQueue.getInstance().getPendingQueue().lastIndexOf(this)+1;
        if (pos==0) {
            if (MathQueue.getInstance().getExecutingJob() == this && this.getMathJobStatus()==MathJobStatus.RUNNING)
                return "R";
            else
                return "-";
        }
        return String.valueOf(pos);
    }

    @Override
    public String call() throws Exception {
        // real callable is implemented in extended classes
        return null;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(this.id);
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        MathJob other = (MathJob) o;
        return this.id.equals(other.id);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
