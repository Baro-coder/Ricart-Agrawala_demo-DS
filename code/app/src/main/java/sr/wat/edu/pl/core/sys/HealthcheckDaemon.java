package sr.wat.edu.pl.core.sys;

import javafx.concurrent.Task;
import sr.wat.edu.pl.core.Logger;

public class HealthcheckDaemon extends Task<Void> {
    private Thread thread;

    private int period;

    public HealthcheckDaemon(int period) {
        this.period = period * 1000;
    }

    @Override
    protected Void call() throws Exception {
        Thread.sleep(1000);

        try {
            while(!Thread.interrupted()) {
                RaSystem.getInstance().sendHealthcheck();
                Thread.sleep(period);        
            }
        } catch (Exception e) {
            Logger.log_debug(this.getClass().getSimpleName(), "Healthcheck daemon interrupted.");
        }
        
        return null;
    }
    
    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
            Logger.log_info(this.getClass().getSimpleName(), "Healthcheck daemon started.");
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            Logger.log_info(this.getClass().getSimpleName(), "Healthcheck daemon stoped.");
            thread = null;
        }
    }


    public void setPeriod(int period) {
        this.period = period * 1000;
    }
}
