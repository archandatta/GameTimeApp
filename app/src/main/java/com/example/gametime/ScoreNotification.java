package com.example.gametime;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.example.gametime.App.GAMESCORES_CHANNEL_ID;

class ScoreNotification extends Game implements GameMonitor {

    private NotificationCompat.Builder mNotification;
    private Notification mSummaryNotification;
    private NotificationManagerCompat mManager;

    private String mHomeTeam;
    private String mAwayTeam;
    private int mHomeScore;
    private int mAwayScore;
    private String mLatestPlay;

    private int userPointdiff;
    private int userTimeRemaining;
    private int userQuarter;
    private String userTeam;


    ScoreNotification(MainActivity mainActivity, NotificationManagerCompat manager, Game gameData, int pointdiff, int timeRemaining, int quarter, String team) {
        super(
                gameData.getHomeTeam(),
                gameData.getAwayTeam(),
                gameData.getHomeScore(),
                gameData.getAwayScore(),
                gameData.getQuarter(),
                gameData.getLastPlay(),
                gameData.getMatchTime(),
                gameData.getQuarterTime());

        mHomeTeam = gameData.getHomeTeam();
        mAwayTeam = gameData.getAwayTeam();
        mManager = manager;

        userPointdiff = pointdiff;
        userTimeRemaining = timeRemaining;
        userQuarter = quarter;
        userTeam = team;

        Intent activityIntent = new Intent(mainActivity, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mainActivity,
                0, activityIntent, 0);

        mNotification = new NotificationCompat.Builder(mainActivity, GAMESCORES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup("scores_group")
                .setColor(Color.BLUE)
                .setContentIntent(pendingIntent);

        mSummaryNotification = new NotificationCompat.Builder(mainActivity, GAMESCORES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setStyle(new NotificationCompat.InboxStyle()
                        .setSummaryText("Summary"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup("scores_group")
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setGroupSummary(true)
                .build();
    }

    private void NotifyUser(int id) {
        // update the notification content
        mNotification.setContentTitle(mHomeTeam + " " + mHomeScore + " - " + mAwayScore + " " + mAwayTeam);
        mNotification.setContentText(mLatestPlay);

        mManager.notify(id + 20, mNotification.build());
        mManager.notify(10, mSummaryNotification);
    }

    @Override
    public void UpdateData(Game updatedData, int id) {

        boolean dataChanged = false;
        boolean notify = false;
        boolean teamSelected = true;

        if (mHomeScore != updatedData.getHomeScore()) {
            mHomeScore = updatedData.getHomeScore();
            dataChanged = true;
        }
        if (mAwayScore != updatedData.getAwayScore()) {
            mAwayScore = updatedData.getAwayScore();
            dataChanged = true;
        }
        if (!(updatedData.getLastPlay().equals(mLatestPlay))) {
            mLatestPlay = updatedData.getLastPlay();
            dataChanged = true;
        }

        int pointDiff = Math.abs(updatedData.getHomeScore() - updatedData.getAwayScore());

        if (userTeam.equals("N/A"))
            teamSelected = false;

        // using default values
        if (teamSelected) {
            if (userTeam.equals(updatedData.getHomeTeam()) || userTeam.equals(updatedData.getAwayTeam())) {
                // specific points in a given quarter
                if (userQuarter == updatedData.getQuarter()) {
                    if (userPointdiff < pointDiff || userPointdiff == pointDiff) {
                        notify = true;
                    }
                }
            }
        }

        if (userQuarter == updatedData.getQuarter()) {
            if (userPointdiff > pointDiff || userPointdiff == pointDiff) {
                notify = true;
            }
        }

        //TODO: Add in time criteria

        if (dataChanged && notify)
            NotifyUser(id);
    }

    @Override
    public boolean IsUpdated(Game updatedGame) {
        return !(updatedGame.getHomeTeam().equals(mHomeTeam));
    }

    @Override
    public void Register(EventStream e) {
    }
}