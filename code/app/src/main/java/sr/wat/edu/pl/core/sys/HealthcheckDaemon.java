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
        
        try {
            while(!Thread.interrupted()) {
                RaSystem.getInstance().sendHealthcheck();
                Thread.sleep(period);        
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void start() {
        thread = new Thread(this);
        thread.start();

        Logger.log_info(this.getClass().getSimpleName(), "Healthcheck daemon started.");
    }

    public void stop() {
        thread.interrupt();

        Logger.log_info(this.getClass().getSimpleName(), "Healthcheck daemon stoped.");
    }


    public void setPeriod(int period) {
        this.period = period * 1000;
    }
}
