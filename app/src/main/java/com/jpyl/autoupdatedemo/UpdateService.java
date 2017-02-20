package com.jpyl.autoupdatedemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import java.io.File;

/**
 * Created by dg on 2017/2/20.
 */

public class UpdateService extends Service {
    private String apkUrl;
    private String filepath;
    private NotificationManager nm;
    private Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        filepath = Environment.getExternalStorageDirectory() + "/test/AutoUpdateDemo.apk";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            notifyUser(getString(R.string.update_download_failed), getString(R.string.update_download_failed_msg), 0);
            stopSelf();
        }
        apkUrl = intent.getStringExtra("apkUrl");
        notifyUser("下载开始", "下载开始", 0);
        startDownLaoad();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownLaoad() {
        UpdateManager.getInsatnce().startDownLoads(apkUrl, filepath, new UpdateDownLoadListenter() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgressChange(int progress, String downloadUrl) {
                notifyUser("正在下载", "正在下载", progress);
            }

            @Override
            public void onFinish(int completeSize, String downUrl) {
                notifyUser("正在完成", "正在完成", 100);
                stopSelf();

            }

            @Override
            public void onFailure() {
                notifyUser("下载失败", "失败原因", 0);
                stopSelf();
            }
        });
    }

    private void notifyUser(String string, String string1, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("autoupdate");
        if (progress >= 0 && progress <= 100) {
            builder.setProgress(100, progress, false);
        } else {
            builder.setProgress(0, 0, false);
        }
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setTicker(string);
        builder.setContentIntent(progress > 100 ? getIntent() : PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT));
        notification = builder.build();
        nm.notify(0, notification);
    }

    private PendingIntent getIntent() {
        File apkFile = new File(filepath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkFile.getAbsolutePath()), "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

}
