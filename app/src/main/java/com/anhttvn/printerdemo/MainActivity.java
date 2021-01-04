package com.anhttvn.printerdemo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.print.PrintHelper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;

import com.anhttvn.printerdemo.adapter.PrintPdfAdapter;
import com.anhttvn.printerdemo.util.ActivityHelper;
import com.anhttvn.printerdemo.util.WifiPrinter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void openPrint(View view) {
        if (view.getId() == R.id.btn_open_hp) {
            ActivityHelper.startActivityByComponentName(this, ActivityHelper.APPID_HP, ActivityHelper.MAIN_HP);
        } else if (view.getId() == R.id.btn_open_print_hand) {
            ActivityHelper.startActivityByComponentName(this, ActivityHelper.APPID_PRINTHAND, ActivityHelper.MAIN_PRINTHAND);
        } else if (view.getId() == R.id.btn_print_photo) {
            printPhoto(Environment.getExternalStorageDirectory() + "/test.PNG");
        } else if (view.getId() == R.id.btn_wifi_print) {
            ///wifiPrint("");
            WifiPrinter.CrazyThreadPool.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    WifiPrinter wifiPrintHelper= new WifiPrinter("192.168.0.105",9100);
                    wifiPrintHelper.printText("676810029020988879217932789789989879797978798178668");
                    wifiPrintHelper.printLine(1,"\n");
                    wifiPrintHelper.printText("android wifi print!");
                    wifiPrintHelper.closeIOAndSocket();
                }
            });
            ///WifiPrintHelper.getInstance().qrCode("hello world !!");
        }else if(view.getId() ==R.id.btn_img_print){
            WifiPrinter.CrazyThreadPool.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    WifiPrinter wifiPrintHelper = new WifiPrinter("192.168.0.101", 9100);
                    wifiPrintHelper.printPDF(Environment.getExternalStorageDirectory() + File.separator + "table3.pdf");
                    wifiPrintHelper.closeIOAndSocket();
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createPdfFromView(@NonNull View view, @NonNull final String pdfName ){
        final PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                .Builder(view.getMeasuredWidth()   , view.getMeasuredHeight(), 1)
                .setContentRect(new Rect(10,10,view.getMeasuredWidth()-10,view.getMeasuredHeight()-10))
                .create();
        PdfDocument.Page page = document.startPage(pageInfo);
        view.draw(page.getCanvas());
        document.finishPage(page);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = Environment.getExternalStorageDirectory() + File.separator + pdfName;
                    File e = new File(path);
                    if (e.exists()) {
                        e.delete();
                    }
                    document.writeTo(new FileOutputStream(e));
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void doPdfPrint(String filePath) {
        String jobName = "jobName";
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintPdfAdapter myPrintAdapter = new PrintPdfAdapter(filePath);
        PrintAttributes attributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("id", Context.PRINT_SERVICE, 480, 320))
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();
        printManager.print(jobName, myPrintAdapter, attributes);
    }
    private void printPhoto(String path) {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        photoPrinter.printBitmap("jpgTestPrint", bitmap);
    }

    public void save(Bitmap bitmap, File file, int dpi) {
        try {
            ByteArrayOutputStream imageByteArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageByteArray);
            byte[] imageData = imageByteArray.toByteArray();
            imageByteArray.close();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(setDpi(imageData, dpi));
            fileOutputStream.close();
            imageData = null;
            Log.e("aaa", "saved");
        } catch (Exception e) {
            Log.e("aaa", "Wrong in Class 'BitmapToPng'");
        }
    }

    private byte[] setDpi(byte[] imageData, int dpi) {
        byte[] imageDataCopy = new byte[imageData.length + 21];
        System.arraycopy(imageData, 0, imageDataCopy, 0, 33);
        System.arraycopy(imageData, 33, imageDataCopy, 33 + 21, imageData.length - 33);

        int[] pHYs = new int[]{0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 23, 18, 0, 0, 23, 18, 1, 103, 159, 210, 82};

        for (int i = 0; i < 21; i++) {
            imageDataCopy[i + 33] = (byte) (pHYs[i] & 0xff);
        }

        dpi = (int) (dpi / 0.0254);
        imageDataCopy[41] = (byte) (dpi >> 24);
        imageDataCopy[42] = (byte) (dpi >> 16);
        imageDataCopy[43] = (byte) (dpi >> 8);
        imageDataCopy[44] = (byte) (dpi & 0xff);

        imageDataCopy[45] = (byte) (dpi >> 24);
        imageDataCopy[46] = (byte) (dpi >> 16);
        imageDataCopy[47] = (byte) (dpi >> 8);
        imageDataCopy[48] = (byte) (dpi & 0xff);
        return imageDataCopy;
    }
}