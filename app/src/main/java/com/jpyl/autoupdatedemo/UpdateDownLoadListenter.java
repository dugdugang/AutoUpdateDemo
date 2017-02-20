package com.jpyl.autoupdatedemo;

/**
 * Created by dg on 2017/2/20.
 */

public interface UpdateDownLoadListenter {
    /**
     * 开始下载回调
     */
    public void onStart();

    /**
     * 进度更新回调
     */
    public void onProgressChange(int progress, String downloadUrl);

    /**
     * 下载完成回调
     */
    public void onFinish(int completeSize, String downUrl);

    /**
     * 下载失败回调
     */
    public void onFailure();

}
