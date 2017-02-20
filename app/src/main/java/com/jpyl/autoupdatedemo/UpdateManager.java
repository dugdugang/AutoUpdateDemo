package com.jpyl.autoupdatedemo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by dg on 2017/2/20.
 */

public class UpdateManager {
    private static UpdateManager instance;
    private ThreadPoolExecutor threadPoolExecutor;
    private UpdateDownloadRequest downloadRequest;

    private UpdateManager() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    static public UpdateManager getInsatnce() {
        if (instance == null) {
            synchronized (UpdateManager.class) {
                if (instance == null) {
                    instance = new UpdateManager();
                }
            }
        }
        return instance;
    }

    public void startDownLoads(String downloadUrl, String filePath, UpdateDownLoadListenter downLoadListenter) {
        if (downloadRequest != null) {
            return;
        }
        checkLocalFilePath(filePath);
        downloadRequest = new UpdateDownloadRequest(downloadUrl, filePath, downLoadListenter);
        Future<?> future = threadPoolExecutor.submit(downloadRequest);
    }

    private void checkLocalFilePath(String filePath) {
        File dirs = new File(filePath.substring(0, filePath.lastIndexOf("/") + 1));
        if (!dirs.exists()) {
            dirs.mkdirs();
        }
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
