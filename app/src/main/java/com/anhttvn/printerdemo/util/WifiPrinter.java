package com.anhttvn.printerdemo.util;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WifiPrinter {
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private OutputStreamWriter mWriter;
    private Socket mSocket;
    private String mEncode;
    private String mIp;
    private int mPort;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public WifiPrinter(String ip, int port) {
        mEncode = StandardCharsets.UTF_8.name();
        this.mIp = ip;
        this.mPort = port;
        try {
            if (mSocket != null) {
                closeIOAndSocket();
            } else {
                mSocket = new Socket();
            }
            mSocket.connect(new InetSocketAddress(mIp, mPort));
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
            mWriter = new OutputStreamWriter(mOutputStream, mEncode);
        } catch (Exception e) {
            Log.e("cmo", "wifi printer" + e.toString());
        }
    }

    public boolean isConnect() {
        if (mSocket != null) {
            if (!mSocket.isClosed() && mSocket.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public void closeIOAndSocket() {
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mWriter != null) {
                mWriter.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Log.e("cmo", "error");
        }
    }
    public void printLine(final int lineNum, final String tag) {
        if (mWriter != null) {
            try {
                for (int i = 0; i < lineNum; i++) {
                    mWriter.write(tag);
                }
                mWriter.flush();
            } catch (IOException e) {
                Log.e("cmo", "error：" + e.getMessage());
            }
        }
    }

    private void printTabSpace(int length) {
        printLine(length, "\t");
    }

    public void printText(final String text) {
        try {
            byte[] content = text.getBytes(mEncode);
            mOutputStream.write(content);
            mOutputStream.flush();
        } catch (IOException e) {
            Log.e("cmo", "error：" + e.getMessage());
        }
    }

    public void printPDF(final String pdfPath) {
        File pdf;
        if (TextUtils.isEmpty(pdfPath)) {
            return;
        }
        pdf = new File(pdfPath);
        if (!pdf.exists()) {
            return;
        }

        byte[] buf = new byte[1024];
        int bytesRead;
        FileInputStream input=null;
        try {
            input = new FileInputStream(pdfPath);
            while ((bytesRead = input.read(buf)) > 0) {
                mOutputStream.write(buf, 0, bytesRead);
            }
            mOutputStream.flush();
        } catch (IOException e) {
        }try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void qrCode(final String qrData) {
        int moduleSize = 8;
        try {
            int length = qrData.getBytes(mEncode).length;
            mWriter.write(0x1D);
            mWriter.write("(k");
            mWriter.write(length + 3);
            mWriter.write(0);
            mWriter.write(49);
            mWriter.write(80);
            mWriter.write(48);
            mWriter.write(qrData);
            mWriter.write(0x1D);
            mWriter.write("(k");
            mWriter.write(3);
            mWriter.write(0);
            mWriter.write(49);
            mWriter.write(69);
            mWriter.write(48);
            mWriter.write(0x1D);
            mWriter.write("(k");
            mWriter.write(3);
            mWriter.write(0);
            mWriter.write(49);
            mWriter.write(67);
            mWriter.write(moduleSize);
            mWriter.write(0x1D);
            mWriter.write("(k");
            mWriter.write(3);
            mWriter.write(0);
            mWriter.write(49);
            mWriter.write(81);
            mWriter.write(48);
            mWriter.flush();
        } catch (IOException e) {
        }
    }
    public static class CrazyThreadPool {
        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int CORE_POOL_SIZE = (CPU_COUNT + 1);
        private static final int KEEP_ALIVE = 1;
        private static final int MAXIMUM_POOL_SIZE = ((CPU_COUNT * 2) + 1);
        private static final BlockingQueue<Runnable> WORKQUEUE = new LinkedBlockingQueue<>(64);
        private static final ThreadFactory THREADFACTORY = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WifiPrint #" + this.mCount.getAndIncrement());
            }
        };
        public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                TimeUnit.SECONDS, WORKQUEUE, THREADFACTORY);
    }
}
