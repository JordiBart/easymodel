package cat.udl.easymodel.mathlink;

import cat.udl.easymodel.logic.results.ResultText;
import cat.udl.easymodel.main.SharedData;
import com.vaadin.flow.server.VaadinSession;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MathQueue implements Callable<String> {
    private static MathQueue thisSingleton = new MathQueue();
    private ExecutorService jobExecutorService=Executors.newCachedThreadPool();
    private static long pollingMillis = 500;
    private LinkedList<MathJob> pendingQueue = new LinkedList<>();
    private TreeMap<String, MathJob> allJobs = new TreeMap<>();
    private TreeMap<String, SimJob> allSimJobs = new TreeMap<>();
    private MathJob executingJob = null;
    private Integer idCounter = 1;
    private boolean isStoppingThread = false;
    private boolean isStoppedThread = false;

    private MathQueue() {
    }

    @Override
    public String call() throws Exception {
        System.out.println("MATH QUEUE START!");
        while (true) {
            if (executingJob != null && executingJob.getMathJobStatus() == MathJobStatus.FINISHED) {
                executingJob = null;
            }
            if (executingJob == null) {
                if (isStoppingThread) {
                    break;
                } else if (!pendingQueue.isEmpty()) {
                    executingJob = pendingQueue.getFirst();
                    pendingQueue.removeFirst();
                    jobExecutorService.submit(executingJob);
                }
            }
            try {
                Thread.sleep(pollingMillis);
            } catch (InterruptedException e) {
                System.err.println("MathQueue sleep error!");
            }
        }
        isStoppedThread =true;
        return null;
    }

    public static MathQueue getInstance() {
        return thisSingleton;
    }

    public LinkedList<MathJob> getPendingQueue() {
        return pendingQueue;
    }

    public MathJob getExecutingJob() {
        return executingJob;
    }

    public boolean isStoppingThread() {
        return isStoppingThread;
    }

    public TreeMap<String, MathJob> getAllJobs() {
        return allJobs;
    }

    public TreeMap<String, SimJob> getAllSimJobs() {
        return allSimJobs;
    }

    public LinkedList<SimJob> getSimJobsByVaadinSession(VaadinSession vaadinSession) {
        LinkedList<SimJob> ret = new LinkedList<>();
        for (SimJob sj : allSimJobs.values())
            if (sj.getVaadinSession() == vaadinSession) {
                //p.p(sj.id + "-" + sj.mathJobStatus.toString());
                ret.add(sj);
            }
        return ret;
    }

    public boolean areAllSimJobsFinishedByVaadinSession(VaadinSession vaadinSession){
        for (SimJob sj : getSimJobsByVaadinSession(vaadinSession)) {
            if (sj.getMathJobStatus() != MathJobStatus.FINISHED) {
                return false;
            }
        }
        return true;
    }

    public String addNewMathJob(MathJob job) throws Exception {
        if (job instanceof SimJob) {
            if (getNumberOfActiveSimJobsByVaadinSession(((SimJob) job).getVaadinSession()) >= SharedData.maxSimJobsPerUser)
                throw new Exception("User reached launched simulation limit: "+SharedData.maxSimJobsPerUser);
            allSimJobs.put(job.getJobId(), (SimJob) job);
        }
        allJobs.put(job.getJobId(), job);
        pendingQueue.addLast(job);
        return job.getJobId();
    }

    private int getNumberOfActiveSimJobsByVaadinSession(VaadinSession vaadinSession) {
        int counter=0;
        for (SimJob job2 : allSimJobs.values()){
            if (job2.getVaadinSession() == vaadinSession)
                if (job2.getMathJobStatus()!=MathJobStatus.FINISHED)
                    counter++;
        }
        return counter;
    }

    protected String getNextId() {
        String ret = String.format("%08d", idCounter);
        if (idCounter < 99999999)
            idCounter += 1; //new Random().nextInt(5);
        else
            idCounter = 1;
        return ret;
    }

    public void stopThread() {
        isStoppingThread = true;
        if (executingJob != null && executingJob.getMathJobStatus()==MathJobStatus.RUNNING) {
            executingJob.mathLinkOp.closeMathLink();
        }
    }

    public boolean isStoppedThread() {
        return isStoppedThread;
    }

    public void cancelSimulationByUser(SimJob simJob) {
        if (allSimJobs.containsKey(simJob.getJobId())){
            simJob.cancelSimulationByUser();
            if (simJob.getMathJobStatus() == MathJobStatus.PENDING) {
                pendingQueue.remove(simJob);
                simJob.setMathJobStatus(MathJobStatus.FINISHED);
                simJob.getResultList().add(new ResultText("Simulation cancelled by user",""));
            }
        }
    }
}
