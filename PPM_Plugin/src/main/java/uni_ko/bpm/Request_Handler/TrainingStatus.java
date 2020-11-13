package uni_ko.bpm.Request_Handler;

import java.io.Serializable;

public class TrainingStatus implements Serializable {

    private boolean done;
    private boolean success;
    private double accuracy;

    public TrainingStatus(boolean done, boolean success, double accuracy){
        super();
        this.done = done;
        this.success = success;
        this.accuracy = accuracy;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accurracy) {
        this.accuracy = accurracy;
    }

}
