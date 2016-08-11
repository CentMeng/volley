package com.luoteng.folk;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.core.utils.OnClickUtil;
import com.android.core.utils.SharePreferenceStorageService;
import com.android.core.utils.Text.ObjectUtils;
import com.android.core.utils.Toast.ToastUtil;
import com.android.core.utils.Toast.ToastUtilHaveRight;
import com.android.core.view.swipemore.SwipeRefreshAndLoadLayout;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.core.api.common.HttpService;
import com.luoteng.folk.activity.MainActivity;
import com.luoteng.folk.listener.BaseFragmentListener;
import com.luoteng.folk.utils.ActivityCollector;
import com.luoteng.folk.utils.VolleyHttpClient;
import com.luoteng.folk.utils.wheelview.ArrayWheelAdapter;
import com.luoteng.folk.utils.wheelview.OnWheelChangedListener;
import com.luoteng.folk.utils.wheelview.WheelView;
import com.luoteng.folk.view.PrompfDialog;

import java.io.File;
import java.util.List;

import cache.DataCache;

/**
 * Created by CentMeng csdn@vip.163.com on 15/9/6.
 */
public class BaseActivity extends FragmentActivity implements BaseFragmentListener {

    protected final static String TAG = "BaseActivity";

    // 网络请求队列
    protected RequestQueue queue;

    // 网络连接失败
    public static final int WLAN_FALSE = 100001;

    protected Context context;

    public static ToastUtil toastUtil;

    public static ToastUtilHaveRight toastUtilRight;

//    public GlobalPhone phone;

    public static HttpService httpService;
    // 判断是不是第一次
    public static SharePreferenceStorageService preferenceStorageService;
    public VolleyHttpClient volleyHttpClient;

    public Dialog progressDialog;

    public GlobalPhone app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = BaseActivity.this;
        app = (GlobalPhone) this.getApplication();
        ActivityCollector.addActivity(this);

        queue = Volley.newRequestQueue(this);

        if (toastUtil == null) {
            toastUtil = new ToastUtil(this);
        }


        httpService = HttpService.newInstance(getApplicationContext());
        preferenceStorageService = SharePreferenceStorageService
                .newInstance(getApplicationContext());
        volleyHttpClient = VolleyHttpClient.newInstance(httpService, this);
        DataCache.newInstance(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void showLoadingDialog(String strMsg) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        progressDialog = new Dialog(this, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        TextView msg = (TextView) progressDialog
                .findViewById(R.id.id_tv_loadingmsg);
        if (strMsg.isEmpty())
            msg.setText("正在加载");
        else
            msg.setText(strMsg);

        try {
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void cancelLoadingDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void showSystemToast(String msg) {
        if (!OnClickUtil.isFastDoubleClick(3000)) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    public void showSystemShortToast(String msg) {
        if (!OnClickUtil.isFastDoubleClick(2000)) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void showToast(String msg) {
        try {
            if (toastUtil == null) {
                toastUtil = new ToastUtil(this);
            }
            if (!OnClickUtil.isFastDoubleClick(3000)) {
                toastUtil.show(msg);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void showToastRight(String msg) {
        try {
            if (toastUtilRight == null) {
                toastUtilRight = new ToastUtilHaveRight(this);
            }
            toastUtilRight.show(msg);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void showToast(int res) {
        try {
            if (toastUtil == null) {
                toastUtil = new ToastUtil(this);
            }
            toastUtil.show(res);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void showToastShort(int res) {
        try {
            if (toastUtil == null) {
                toastUtil = new ToastUtil(this);
            }
            if (!OnClickUtil.isFastDoubleClick(3000)) {
                toastUtil.showShort(res);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void showToastShort(String res) {
        try {
            if (toastUtil == null) {
                toastUtil = new ToastUtil(this);
            }
            if (!OnClickUtil.isFastDoubleClick(2000)) {
                toastUtil.showShort(res);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    ;

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    /**
     * 创建和获取缓存路径 
     *
     * @param uniqueName
     * @return
     */
    public File getDiskCacheDir(String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
            // || !Environment.isExternalStorageRemovable()
                ) {
            try {
                cachePath = getExternalCacheDir().getPath();
            } catch (Exception e) {
                cachePath = getCacheDir().getPath();
            }
        } else {
            cachePath = getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public int getAppVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }


    private Dialog chooseSingeDialog = null;

    protected void showChooseDateDialog(final String[] date, final String title) {
        // TODO Auto-generated method stub
        View view = getLayoutInflater().inflate(R.layout.wheelview_dialog, null);
        chooseSingeDialog = new Dialog(this,
                R.style.transparentFrameWindowStyle);
        chooseSingeDialog.setContentView(view, new WindowManager.LayoutParams(
                WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT));
        Window window = chooseSingeDialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 设置显示位置
        chooseSingeDialog.onWindowAttributesChanged(wl);
        TextView tv = (TextView) view.findViewById(R.id.wheelview_dialog_tv);
        tv.setText(title);
        final WheelView byWhat = (WheelView) view.findViewById(R.id.empty);
        byWhat.setAdapter(new ArrayWheelAdapter<String>(date));
        byWhat.setVisibleItems(5);
        Button btn = (Button) view.findViewById(R.id.wheelview_dialog_btn);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getChooseDialog(byWhat.getCurrentItem(),
                        date[byWhat.getCurrentItem()], title);
                chooseSingeDialog.dismiss();

            }
        });
        // 设置点击外围解散
        chooseSingeDialog.setCanceledOnTouchOutside(true);
        chooseSingeDialog.show();
    }

    protected void getChooseDialog(int index, String message, String title) {

    }

    private Dialog chooseDoubleDialog = null;

    protected void showChooseDateDialog(final String[] date,
                                        final String[][] secondDate, final String title) {
        // TODO Auto-generated method stub
        View view = getLayoutInflater().inflate(
                R.layout.doublewheelview_dialog, null);
        chooseDoubleDialog = new Dialog(this,
                R.style.transparentFrameWindowStyle);
        chooseDoubleDialog.setContentView(view, new WindowManager.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        Window window = chooseDoubleDialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 设置显示位置
        chooseDoubleDialog.onWindowAttributesChanged(wl);
        TextView tv = (TextView) view.findViewById(R.id.wheelview_dialog_tv);
        tv.setText(title);
        final WheelView wv_first = (WheelView) view.findViewById(R.id.wv_first);
        final WheelView wv_second = (WheelView) view
                .findViewById(R.id.wv_second);
        wv_first.setAdapter(new ArrayWheelAdapter<String>(date));
        wv_first.setVisibleItems(5);
        wv_first.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                wv_second.setAdapter(new ArrayWheelAdapter<String>(
                        secondDate[newValue]));
                // wv_second.setCurrentItem(secondDate[newValue].length / 2);
                wv_second.setCurrentItem(0);
            }
        });
        wv_second.setVisibleItems(5);
        wv_first.setCurrentItem(1);
        wv_first.setCurrentItem(0);
        Button btn = (Button) view.findViewById(R.id.wheelview_dialog_btn);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getChooseDialog(
                        0,
                        date[wv_first.getCurrentItem()]
                                + ","
                                + secondDate[wv_first.getCurrentItem()][wv_second
                                .getCurrentItem()], title);
                chooseDoubleDialog.dismiss();

            }
        });
        // 设置点击外围解散
        chooseDoubleDialog.setCanceledOnTouchOutside(true);
        chooseDoubleDialog.show();
    }


    public PrompfDialog loginDialog;

    public void showLoginDialog(final Context context) {
        if (loginDialog == null) {
            loginDialog = new PrompfDialog(context,
                    R.style.transparentFrameWindowStyle, "登  录", "关  闭",
                    "您还没有登录，请登录", this.getString(R.string.app_name));
            loginDialog.setCanceledOnTouchOutside(false);
            loginDialog.setCancelable(false);
            loginDialog
                    .setUpdateOnClickListener(new PrompfDialog.UpdateOnclickListener() {
                        // 这里的逻辑和后面的逻辑正好相反 所以使用！
                        @Override
                        public void dismiss() {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void BtnYesOnClickListener(View v) {
//                            Intent intent = new Intent(context,
//                                    LoginActivity.class);
//                            startActivity(intent);
                            loginDialog.dismiss();
                        }

                        @Override
                        public void BtnCancleOnClickListener(View v) {

                            // if(context instanceof MainActivity){
                            // MainActivity.radioGroup.check(R.id.rb_tab_main);
                            // }
                            loginDialog.dismiss();
                        }

                    });
            Window window = loginDialog.getWindow();
            window.setGravity(Gravity.CENTER);
            loginDialog.show();
            // WindowManager m = this.getWindowManager();
            //
            // // 获取屏幕宽、高用
            // Display d = m.getDefaultDisplay();
            // delete_dialog.getWindow().setLayout((int) (d.getWidth() * 0.7),
            // (int) (d.getHeight() * (0.3)));
        } else {
            loginDialog.show();
        }
    }


    PrompfDialog logOutDialog;

    public void showLogOutDialog(final Context context) {
        if (logOutDialog == null) {
            logOutDialog = new PrompfDialog(context,
                    R.style.transparentFrameWindowStyle, "退  出", "关  闭",
                    "您确定要退出登录账户吗？",this.getString(R.string.app_name));
            logOutDialog.setCanceledOnTouchOutside(false);
            logOutDialog
                    .setUpdateOnClickListener(new PrompfDialog.UpdateOnclickListener() {
                        // 这里的逻辑和后面的逻辑正好相反 所以使用！
                        @Override
                        public void dismiss() {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void BtnYesOnClickListener(View v) {
                            logout(context);
                            logOutDialog.dismiss();
                        }

                        @Override
                        public void BtnCancleOnClickListener(View v) {
                            logOutDialog.dismiss();
                        }

                    });
            Window window = logOutDialog.getWindow();
            window.setGravity(Gravity.CENTER);
            logOutDialog.show();
            // WindowManager m = this.getWindowManager();
            //
            // // 获取屏幕宽、高用
            // Display d = m.getDefaultDisplay();
            // delete_dialog.getWindow().setLayout((int) (d.getWidth() * 0.7),
            // (int) (d.getHeight() * (0.3)));
        } else {
            logOutDialog.show();
        }
    }

    //

    PrompfDialog updateDialog;

    public void showUpdateDialog(final MainActivity context,
                                 final String versionPath, final String versionName,
                                 final String versionCode) {
        if (updateDialog == null) {
            updateDialog = new PrompfDialog(context,
                    R.style.transparentFrameWindowStyle, "更  新", "关  闭",
                    "检测到最新版本，需要更新吗？",this.getString(R.string.app_name));
            updateDialog.setCanceledOnTouchOutside(false);
            updateDialog
                    .setUpdateOnClickListener(new PrompfDialog.UpdateOnclickListener() {
                        // 这里的逻辑和后面的逻辑正好相反 所以使用！
                        @Override
                        public void dismiss() {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void BtnYesOnClickListener(View v) {
//                            Intent it = new Intent(context,
//                                    NotificationUpdateActivity.class);
//                            it.putExtra("versionPath", versionPath);
//                            it.putExtra("versionName", versionName);
//                            it.putExtra("versionCode", versionCode);
//                            startActivity(it);
//                            phone.setDownload(true);
                            updateDialog.dismiss();
                        }

                        @Override
                        public void BtnCancleOnClickListener(View v) {
                            updateDialog.dismiss();
                        }

                    });
            Window window = updateDialog.getWindow();
            window.setGravity(Gravity.CENTER);
            updateDialog.show();
            // WindowManager m = this.getWindowManager();
            //
            // // 获取屏幕宽、高用
            // Display d = m.getDefaultDisplay();
            // delete_dialog.getWindow().setLayout((int) (d.getWidth() * 0.7),
            // (int) (d.getHeight() * (0.3)));
        } else {
            updateDialog.show();
        }
    }


    private Dialog call_dialog = null;
    Button btn_call;

    public void showCallDialog(final String phone_num) {

        if (call_dialog == null) {
            View view = getLayoutInflater().inflate(R.layout.dialog_call, null);
            call_dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
            call_dialog.setContentView(view, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            Window window = call_dialog.getWindow();
            // 设置显示动画
            window.setWindowAnimations(R.style.main_menu_animstyle);
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.x = 0;
            wl.y = getWindowManager().getDefaultDisplay().getHeight()-50;
            // 以下这两句是为了保证按钮可以水平满屏
            wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
            wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            // 设置显示位置
            call_dialog.onWindowAttributesChanged(wl);
            // 设置点击外围解散
            call_dialog.setCanceledOnTouchOutside(true);
            call_dialog.show();
            btn_call = (Button) view.findViewById(R.id.btn_call);
            // btn_call.setText(phone_num);
            Button btn_cancle = (Button) view.findViewById(R.id.btn_cancle);
            btn_call.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (phone_num != null && !phone_num.equals("")) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri
                                .parse("tel:" + phone_num));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        call_dialog.dismiss();
                    }
                }
            });
            btn_cancle.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    call_dialog.dismiss();
                }
            });
        } else {
            btn_call.setText(phone_num);
            call_dialog.show();
        }
    }

    public void logout(Context context) {
        preferenceStorageService.setLogin(false,"","","","");
//        Intent intent = new Intent(context, LoginActivity.class);
//        startActivity(intent);
//        ActivityCollector.finishAllExceptLogin();
    }




    /**
     * 程序是否在前台运行
     */

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    public VolleyHttpClient getHttpClient() {
        return volleyHttpClient;
    }




}
