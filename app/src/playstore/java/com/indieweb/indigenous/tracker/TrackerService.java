package com.indieweb.indigenous.tracker;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.TrackerPoint;
import com.indieweb.indigenous.model.Track;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

public class TrackerService extends Service {

    public static final long TRACKER_UPDATE_INTERVAL_IN_MILLISECONDS = 20000;
    public static final long TRACKER_FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = TRACKER_UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final String PACKAGE_NAME = "com.indieweb.indigenous.tracker.trackerservice";

    /**
     * Tracker tag.
     */
    public static final String TRACKER_TAG = "indigenous_tracker";

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    /**
     * Notification manager.
     */
    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * Location service channel id.
     */
    public static String LOCATION_SERVICE_CHANNEL_ID = "Indigenous Tracker";

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 123456789;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    /**
     * Service handler.
     */
    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;

    public TrackerService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TRACKER_TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TRACKER_TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TRACKER_TAG, "Last client unbound from service");
        if (!mChangingConfiguration && TrackerUtils.requestingLocationUpdates(this)) {
            Log.i(TRACKER_TAG, "Starting foreground service");
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest(getApplicationContext());
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TRACKER_TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(LOCATION_SERVICE_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TRACKER_TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopService(getApplicationContext());
        }

        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TRACKER_TAG, "Requesting location updates");
        TrackerUtils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), TrackerService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e(TRACKER_TAG, "Lost location permission. Could not request updates. " + unlikely);
            TrackerUtils.setRequestingLocationUpdates(this, false);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TRACKER_TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            TrackerUtils.setRequestingLocationUpdates(this, false);
            stopService(getApplicationContext());
        } catch (SecurityException unlikely) {
            TrackerUtils.setRequestingLocationUpdates(this, true);
            Log.e(TRACKER_TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, TrackerService.class);

        CharSequence text = TrackerUtils.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(android.R.drawable.ic_media_pause, getString(R.string.stop_track), servicePendingIntent)
                .setContentText(text)
                .setContentTitle(TrackerUtils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(LOCATION_SERVICE_CHANNEL_ID);
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TRACKER_TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TRACKER_TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TRACKER_TAG, "New location: " + location);

        mLocation = location;
        storeTrackerPoint(getApplicationContext());

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest(Context context) {
        mLocationRequest = TrackerUtils.getLocationRequest(context);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        TrackerService getService() {
            return TrackerService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Store tracker point.
     */
    public void storeTrackerPoint(Context context) {
        if (mLocation != null) {
            User user = new Accounts(context).getCurrentUser();
            DatabaseHelper db = new DatabaseHelper(getApplicationContext());
            int trackerId = db.getLatestTrackId(user.getMeWithoutProtocol());
            if (trackerId > 0) {
                String coordinates = String.format("%s,%s,%s", mLocation.getLatitude(), mLocation.getLongitude(), mLocation.getAltitude());
                TrackerPoint p = new TrackerPoint();
                String geo = "geo:" + coordinates;
                p.setTrackId(trackerId);
                p.setPoint(geo);
                db.savePoint(p);
            }
            else {
                Log.i(TRACKER_TAG, "No tracker id found, stopping.");
                stopService(context);
            }
        }
    }

    /**
     * Stop service.
     *
     * @param context
     *   The current context.
     */
    public void stopService(Context context) {

        User user = new Accounts(context).getCurrentUser();
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        int trackerId = db.getLatestTrackId(user.getMeWithoutProtocol());
        if (trackerId > 0) {
            Track track = db.getTrack(trackerId);
            if (track != null && track.getId() > 0) {
                db.saveTrack(track, true);
            }
        }

        stopSelf();
    }

}
