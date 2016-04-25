package ca.sbstn.androidb.callback;

import ca.sbstn.androidb.task.BaseTask;

/**
 * Created by tyler on 24/04/16.
 */
public abstract class Callback<T> {
    protected BaseTask task;

    public Callback() {}

    public void setTask(BaseTask task) {
        this.task = task;
    }

    public BaseTask getTask() {
        return task;
    }

    public abstract void onResult(T result);
}
