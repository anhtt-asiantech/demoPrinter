package com.anhttvn.printerdemo.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ActivityHelper {
    public static final String APPID_PRINTHAND = "com.dynamixsoftware.printhand";

    public static final String MAIN_PRINTHAND = APPID_PRINTHAND+".ui.ActivityMain";
    public static final String APPID_HP="com.hp.printercontrol";

    public static final String MAIN_HP = APPID_HP+".base.PrinterControlActivity";
    public static void startActivityByComponentName(Context context, String pkg, String cls) {
        ComponentName comp = new ComponentName(pkg,cls);
        Intent intent = new Intent();
        intent.setComponent(comp);
        intent.setAction("android.intent.action.VIEW");
        intent.setAction("android.intent.action.SEND");
        intent.addCategory("android.intent.category.DEFAULT");
        context.startActivity(intent);
    }
}
