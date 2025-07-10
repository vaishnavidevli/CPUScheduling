import java.awt.Color;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class CPUScheduler extends Thread {
    private Job[] jobBatch;
    private Scheduler policy;
    private JTextArea textArea;
    private JTextField textField;
    private JProgressBar[] pbars;
    private JLabel[] burstTimes, waitingTimes, priorities;
    private ComputationThread[] myThreads = new ComputationThread[10];
    private CalcSimulation gui;

    public CPUScheduler(Job[] jobBatch, Scheduler policy, JTextArea textArea, JTextField textField,
                        JProgressBar[] pbars, JLabel[] burstTimes, JLabel[] waitingTimes,
                        JLabel[] priorities, CalcSimulation gui) {
        this.jobBatch = jobBatch;
        this.policy = policy;
        this.textArea = textArea;
        this.textField = textField;
        this.pbars = pbars;
        this.burstTimes = burstTimes;
        this.waitingTimes = waitingTimes;
        this.priorities = priorities;
        this.gui = gui;
    }

    @Override
    public void run() {
        switch (gui.getalgo()){
            case "FCFS":
                runFCFS();
                break;
            case "Round Robin":
                runRoundRobin();
                break;
            case "Priority Scheduling":
                runPriorityScheduling();
                break;
            default:
                runSJF();
                break;
        }

        finalizeExecution();
    }

    private void runFCFS() {
        for (Job job : jobBatch) {
            policy.enqueue(job);
        }

        for (int i = 0; !policy.isEmpty(); i++) {
            try {
                Thread.sleep(policy.peek().job.arrivalTime);
            } catch (Exception ignored) {}

            Job newJob = policy.dequeue();
            waitingTimes[i].setText(toSeconds(newJob.waitTime) + "s");

            myThreads[i] = new ComputationThread(newJob, policy, textArea, textField, pbars[i], burstTimes[i]);
            myThreads[i].t.start();
        }
    }

    private void runRoundRobin() {
        for (int i = 0; i < jobBatch.length; i++) {
            jobBatch[i].progressBar = pbars[i];
            jobBatch[i].burstTimeLabel = burstTimes[i];
            jobBatch[i].waitTimeLabel = waitingTimes[i];
            policy.enqueue(jobBatch[i]);
        }

        while (!policy.isEmpty()) {
            try {
                Thread.sleep(policy.peek().job.arrivalTime);
            } catch (Exception ignored) {}

            Job newJob = policy.dequeue();
            newJob.waitTimeLabel.setText(toSeconds(newJob.waitTime) + "s");

            ComputationThread cpu = new ComputationThread(newJob, policy, textArea, textField,
                                                          newJob.progressBar, newJob.burstTimeLabel);
            cpu.t.start();
            try {
                cpu.t.join();
            } catch (Exception ignored) {}
        }
    }

    private void runPriorityScheduling() {
        MaxPriorityQueue mp = new MaxPriorityQueue();
        MaxPriorityQueue temp = new MaxPriorityQueue();

        for (int i = 0; i < jobBatch.length; i++) {
            int priority = (int)(Math.random() * 10) + 1;
            jobBatch[i].priority = priority;
            jobBatch[i].progressBar = pbars[i];
            jobBatch[i].burstTimeLabel = burstTimes[i];
            jobBatch[i].waitTimeLabel = waitingTimes[i];
            priorities[i].setText(String.valueOf(priority));
            mp.insert(jobBatch[i]);
            temp.insert(jobBatch[i]);
        }

        while (!temp.isEmpty()) {
            policy.enqueue(temp.extractMax());
        }

        while (!mp.isEmpty()) {
            try {
                Thread.sleep(mp.getMax().arrivalTime);
            } catch (Exception ignored) {}

            Job newJob = mp.extractMax();
            policy.dequeue();
            newJob.waitTime = System.nanoTime() - newJob.startTime;
            newJob.waitTimeLabel.setText(toSeconds(newJob.waitTime) + "s");

            ComputationThread cpu = new ComputationThread(newJob, policy, textArea, textField,
                                                          newJob.progressBar, newJob.burstTimeLabel);
            cpu.t.start();
            try {
                cpu.t.join();
            } catch (InterruptedException ignored) {}
        }
    }

    private void runSJF() {
        MaxPriorityQueue temp = new MaxPriorityQueue();

        for (int i = 0; i < jobBatch.length; i++) {
            jobBatch[i].priority = (int) jobBatch[i].burstTime;
            jobBatch[i].progressBar = pbars[i];
            jobBatch[i].burstTimeLabel = burstTimes[i];
            jobBatch[i].waitTimeLabel = waitingTimes[i];
            priorities[i].setText(String.valueOf(jobBatch[i].priority));
            temp.insert(jobBatch[i]);
        }

        while (!temp.isEmpty()) {
            policy.enqueue(temp.extractMax());
        }

        while (!policy.isEmpty()) {
            try {
                Thread.sleep(policy.peek().job.arrivalTime);
            } catch (Exception ignored) {}

            Job newJob = policy.dequeue();
            newJob.waitTime = System.nanoTime() - newJob.startTime;
            newJob.waitTimeLabel.setText(toSeconds(newJob.waitTime) + "s");

            ComputationThread cpu = new ComputationThread(newJob, policy, textArea, textField,
                                                          newJob.progressBar, newJob.burstTimeLabel);
            cpu.t.start();
            try {
                cpu.t.join();
            } catch (InterruptedException ignored) {}
        }
    }

    private void finalizeExecution() {
        textField.setText("Idle");
        textField.setForeground(Color.RED);

        long totalWait = 0, totalTurnaround = 0;
        for (Job job : jobBatch) {
            totalWait += job.waitTime;
            totalTurnaround += job.endTime;
        }

        long avgWait = TimeUnit.NANOSECONDS.toSeconds(totalWait / jobBatch.length);
        long avgTurn = TimeUnit.NANOSECONDS.toSeconds(totalTurnaround / jobBatch.length);
        long totalExec = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - gui.getStartTime());

        gui.getAvgWaitField().setText(avgWait + "s");
        gui.getAvgTurnField().setText(avgTurn + "s");
        gui.getTotalExecField().setText(totalExec + "s");
    }

    private String toSeconds(long nano) {
        return String.valueOf(TimeUnit.NANOSECONDS.toSeconds(nano));
    }
}
