package com.wongxd.video.exception;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * http://blog.csdn.net/zly921112/article/details/51867079
 * <p>
 * http://www.jianshu.com/p/7742ea195bac
 * <p>
 * CrashHandler.getInstance().init(this);//初始化全局异常管理
 * <p>
 * 全局异常捕获
 * Created by zly on 2016/7/3.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    /**
     * 应用崩溃后要 重启的 activity
     */
    private Class mRestartActivity;

    /**
     * 是否需要重启应用
     */
    private boolean mIsRestartApp = false;

    /**
     * 在崩溃时让主线程休眠一段时间 以供 保存日志
     */
    private long mSleepTime = 2000;

    /**
     * 系统默认UncaughtExceptionHandler
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    /**
     * context
     */
    private Context mContext;

    /**
     * 存储异常和参数信息
     */
    private Map<String, String> paramsMap = new HashMap<>();

    /**
     * 格式化时间
     */
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private String TAG = "WCrash---" + this.getClass().getSimpleName();

    private static CrashHandler mInstance;


    /**
     * 提示信息
     */
    private String tip = " \"程序开小差了呢...即将退出\"";

    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例
     */
    public static synchronized CrashHandler getInstance() {
        if (null == mInstance) {
            mInstance = new CrashHandler();
        }
        return mInstance;
    }

    /**
     * 调用次方法即可 最好在Application 中
     *
     * @param context * @param sleepTime  在崩溃时让主线程休眠一段时间 以供 保存日志
     */
    public void init(Context context, long sleepTime) {
        mSleepTime = sleepTime;
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为系统默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 调用次方法即可 最好在Application 中
     *
     * @param context
     * @param sleepTime       在崩溃时让主线程休眠一段时间 以供 保存日志
     * @param isRestartApp    是否重启应用
     * @param restartActivity 要重启的activity 最好是 应用最初入口
     */
    public void init(Context context, long sleepTime, boolean isRestartApp, Class restartActivity) {
       init(context,sleepTime);

        mIsRestartApp = isRestartApp;
        mRestartActivity = restartActivity;
    }

    /**
     * uncaughtException 回调函数
     */
    @SuppressWarnings("WrongConstant")
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {//如果自己没处理交给系统处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {//自己处理
            try {//延迟 mSleepTime 杀进程
                Thread.sleep(mSleepTime);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }

            if (mIsRestartApp) { // 如果需要重启
                Intent intent = new Intent(mContext.getApplicationContext(), mRestartActivity);
                AlarmManager mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                //重启应用，得使用PendingIntent
                PendingIntent restartIntent = PendingIntent.getActivity(
                        mContext.getApplicationContext(), 0, intent,
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + mSleepTime + 200, restartIntent); // 重启应用
            } else {
                //退出程序
                AppManager.getAppManager().AppExit(mContext);
            }
        }

    }

    /**
     * 收集错误信息.发送到服务器
     *
     * @return 处理了该异常返回true, 否则false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //收集设备参数信息
        collectDeviceInfo(mContext);
        //添加自定义信息
        addCustomInfo();
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext,tip, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        //保存日志文件
        String logName = saveCrashInfo2File(ex);
        Toast.makeText(mContext, "错误日志保存在手机 “crash” 目录 " + logName + " 中，请联系开发人员。", Toast.LENGTH_SHORT).show();
        return true;
    }


    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        //获取versionName,versionCode
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                paramsMap.put("versionName", versionName);
                paramsMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        //获取所有系统信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                paramsMap.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 添加自定义参数
     */
    private void addCustomInfo() {

    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = format.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crash/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }
}