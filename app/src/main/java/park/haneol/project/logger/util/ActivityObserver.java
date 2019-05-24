package park.haneol.project.logger.util;

import java.lang.ref.WeakReference;

import park.haneol.project.logger.component.MainActivity;

public class ActivityObserver {

    private WeakReference<MainActivity> activity;

    private ActivityObserver() {}

    private static class Holder {
        static final ActivityObserver INSTANCE = new ActivityObserver();
    }

    public static ActivityObserver getInstance() {
        return Holder.INSTANCE;
    }

    public void setActivity(MainActivity activity) {
        if (activity != null) {
            this.activity = new WeakReference<>(activity);
        }
    }

    public MainActivity getActivity() {
        if (activity != null) {
            return activity.get();
        }
        return null;
    }

}
