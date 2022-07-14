package dev.phystech.mipt.base.interfaces;

import android.location.Location;

public interface LocationTracker {

    interface LocationUpdateListener {
        void onUpdateLocation(Location oldLoc, long oldTime, Location newLoc, long newTime);
    }

    void start();

    void start(LocationUpdateListener updateListener);

    void stop();

    boolean hasLocation();

    boolean hasPossiblyStaleLocation();

    Location getLocation();

    Location getPossiblyStaleLocation();
}
