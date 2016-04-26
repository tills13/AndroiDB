package ca.sbstn.androidb.task;

import android.content.Context;
import android.os.AsyncTask;
import android.telecom.Call;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;

import java.util.List;

import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.sql.Database;

/**
 * Created by tyler on 24/04/16.
 */
public abstract class BaseTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected Callback<Result> callback;
    protected Context context;
    protected Exception exception;
    protected ProgressBar progressBar;

    public BaseTask() {
        this(null);
    }

    public BaseTask(Context context) {
        this(context, null);
    }

    public BaseTask(Context context, Callback<Result> callback) {
        super();

        this.context = context;
        this.callback = callback;

        if (this.callback != null) this.callback.setTask(this);
    }

    public void setCallback(Callback<Result> callback) {
        this.callback = callback;

        if (this.callback != null) this.callback.setTask(this);
    }

    public boolean hasCallback() {
        return this.callback != null;
    }

    public Callback<Result> getCallback() {
        return this.callback;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean hasException() {
        return this.exception != null;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    protected void onProgressUpdate(Progress ... progress) {
        if (this.progressBar != null) {
            this.progressBar.setProgress((Integer) progress[0]);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (this.progressBar != null) {
            this.progressBar.setIndeterminate(true);
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);

        if (this.hasCallback()) this.callback.onResult(result);

        if (this.getProgressBar() != null) {
            ViewGroup parent = (ViewGroup) this.progressBar.getParent();
            parent.removeView(this.progressBar);
        }
    }
}
