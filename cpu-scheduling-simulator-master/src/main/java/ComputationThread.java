import javax.swing.*;

public class ComputationThread implements Runnable {
    private Scheduler policy;
    public Thread t;
    private Job job;
    private JTextField textField;
    private JTextArea readyQueue;
    private JProgressBar pbar;
    private JLabel burstTime;

    public ComputationThread(Job job, Scheduler policy, JTextArea readyQueue, JTextField textField,
                              JProgressBar pbar, JLabel burstTime) {
        this.policy = policy;
        this.job = job;
        this.readyQueue = readyQueue;
        this.textField = textField;
        this.pbar = pbar;
        this.burstTime = burstTime;
        this.t = new Thread(this);
    }

    @Override
    public void run() {
        policy.allocateCPU(job);

        String algo = policy.getClass().getSimpleName(); // Get algo name from policy class name

        if (algo.contains("FCFS") || algo.contains("Priority") || algo.contains("Stack")) {
            runNonPreemptive();
        } else if (algo.contains("RoundRobin")) {
            runRoundRobin();
        }

        job.endTime = System.nanoTime() - job.startTime;
    }

    private void runNonPreemptive() {
        pbar.setMinimum(0);
        pbar.setMaximum((int) job.burstTime);
        String remaining = policy.getRemainingProcesses();
        readyQueue.setText(remaining);

        for (int i = 0; i < job.burstTime; i++) {
            pbar.setValue(i);
            burstTime.setText((job.burstTime - i) + "ms");
            updateRunningJob();
            sleep(50);
        }
    }

    private void runRoundRobin() {
        int min = job.pBarValue;
        int max = (int) job.burstTime;
        int remainingBurst = (int) job.lastRemainingBurst;
        int exceedTime = 0;

        pbar.setMinimum(0);
        pbar.setMaximum(max);
        readyQueue.setText(policy.getRemainingProcesses());

        for (int i = min; i < max; i++) {
            pbar.setValue(i);
            burstTime.setText((--remainingBurst) + "ms");
            updateRunningJob();
            sleep(50);

            if (++exceedTime == RoundRobinPolicy.TIME_QUANTUM && remainingBurst > 0) {
                job.lastRemainingBurst = remainingBurst;
                job.pBarValue = i;
                policy.enqueue(job);  // Put job back in queue
                break;
            }
        }

        readyQueue.setText(policy.getRemainingProcesses());
    }

    private void updateRunningJob() {
        SwingUtilities.invokeLater(() -> textField.setText(job.processID));
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
