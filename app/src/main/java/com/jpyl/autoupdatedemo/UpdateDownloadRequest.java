package com.jpyl.autoupdatedemo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Created by dg on 2017/2/20.
 */

public class UpdateDownloadRequest implements Runnable {
    private String downUrl;
    private String filePath;
    private UpdateDownLoadListenter loadListenter;
    private boolean isDownloading = false;
    private long currentLength;
    private DownLoadHander loadHander;

    public UpdateDownloadRequest(String downUrl, String filePath, UpdateDownLoadListenter loadListenter) {
        this.downUrl = downUrl;
        this.filePath = filePath;
        this.loadListenter = loadListenter;
        this.isDownloading = true;
        this.loadHander = new DownLoadHander();
    }

    private void makeRequest() throws IOException, InterruptedException {
        if (!Thread.currentThread().interrupted()) {
            try {
                URL url = new URL(downUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.connect();//阻塞我们当前线程
                currentLength = connection.getContentLength();
                if (!Thread.currentThread().isInterrupted()) {
                    //真正的完成文件的下载
                    loadHander.sendResponseMessage(connection.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
        }
    }

    /**
     * 得到小数点后两位的字符串
     *
     * @param value
     * @return
     */
    private String getTwoPointFloatStr(float value) {
        DecimalFormat fnum = new DecimalFormat("0.00");
        return fnum.format(value);
    }

    /**
     * 包含了下载过程中所有可能出现的异常情况
     */
    public enum FailureCode {
        UnknownHost, Socket, SocketTimeOut, ConnectTimeOut,
        IO, HttpResponse, Json, Interrupted
    }

    public class DownLoadHander {
        protected static final int SUCCESS_MESSAGE = 0;
        protected static final int FAILURE_MESSAGE = 1;
        protected static final int START_MESSAGE = 2;
        protected static final int FINSH_MESSAGE = 3;
        protected static final int NETWORK_OFF = 4;
        protected static final int PROGRESS_CHANGE = 5;
        private int mCompleteSize = 0;
        private int progress = 0;
        private Handler handler;//真正的完成线程通信

        public DownLoadHander() {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            };
        }

        /**
         * 发送不同的消息对象
         */
        protected void sendFinishMessage() {
            sendMessage(obtainMessage(FINSH_MESSAGE, null));
        }

        protected void sendProgressChangedMessage(int progress) {
            sendMessage(obtainMessage(PROGRESS_CHANGE, new Object[]{progress}));
        }

        protected void sendFailureMessage(FailureCode code) {
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{code}));
        }

        protected void sendMessage(Message msg) {
            if (handler != null) {
                handler.sendMessage(msg);
            } else {
                handleSelfMessage(msg);
            }
        }

        protected Message obtainMessage(int responseMessage, Object response) {
            Message msg=null;
            if (handler!=null){
                msg=handler.obtainMessage(responseMessage,response);
            }else {
                msg=Message.obtain();
                msg.what=responseMessage;
                msg.obj=response;
            }
            return  msg;

        }

        protected void handleSelfMessage(Message mgs) {
            Object[] reponse;
            switch (mgs.what) {
                case FAILURE_MESSAGE:
                    reponse = (Object[]) mgs.obj;
                    handleFailureMessage((FailureCode) reponse[0]);
                    break;
                case FINSH_MESSAGE:
                    onFinish();
                    break;
                case PROGRESS_CHANGE:
                    reponse = (Object[]) mgs.obj;
                    handleProgressChangeMessage(((Integer) reponse[0]).intValue());
                    break;
            }
        }

        protected void handleProgressChangeMessage(int progress) {

        }

        protected void handleFailureMessage(FailureCode code) {
            onFailure(code);
        }

        public void onFinish() {
            loadListenter.onFinish(mCompleteSize, "");
        }

        public void onFailure(FailureCode code) {
            loadListenter.onFailure();
        }

        public void sendResponseMessage(InputStream inputStream) {
            RandomAccessFile randomAccessFile = null;
            mCompleteSize = 0;
            try {
                byte[] buffer = new byte[1024];
                int length = -1;
                int limit = 0;
                randomAccessFile = new RandomAccessFile(filePath, "rwd");
                while ((length = inputStream.read(buffer)) != -1) {
                    if (isDownloading) {
                        randomAccessFile.write(buffer, 0, length);
                        mCompleteSize += length;
                        if (mCompleteSize < currentLength) {
                            progress = (int) Float.parseFloat(getTwoPointFloatStr(mCompleteSize / currentLength));
                            if (limit / 30 == 0 || progress <= 100) {
                                sendProgressChangedMessage(progress);
                            }
                            limit++;
                        }
                    }
                }
                sendFinishMessage();
            } catch (IOException e) {
                sendFailureMessage(FailureCode.IO);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        randomAccessFile.close();
                    } catch (IOException e) {
                        sendFailureMessage(FailureCode.IO);
                    }
                }
            }
        }
    }
}
