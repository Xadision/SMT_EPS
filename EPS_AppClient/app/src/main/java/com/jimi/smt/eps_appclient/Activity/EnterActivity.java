package com.jimi.smt.eps_appclient.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jimi.smt.eps_appclient.Adapter.EnterOrdersAdapter;
import com.jimi.smt.eps_appclient.Beans.BaseMsg;
import com.jimi.smt.eps_appclient.Beans.Material;
import com.jimi.smt.eps_appclient.Func.CheckPermissionUtils;
import com.jimi.smt.eps_appclient.Func.GlobalFunc;
import com.jimi.smt.eps_appclient.Func.HttpUtils;
import com.jimi.smt.eps_appclient.Func.Log;
import com.jimi.smt.eps_appclient.Interfaces.OkHttpInterface;
import com.jimi.smt.eps_appclient.Unit.GlobalData;
import com.jimi.smt.eps_appclient.R;
import com.jimi.smt.eps_appclient.Unit.Constants;
import com.jimi.smt.eps_appclient.Views.InfoDialog;
import com.jimi.smt.eps_appclient.Views.MyEditTextDel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 类名:EnterActivity
 * 创建人:Liang GuoChang
 * 创建时间:2017/10/19 9:29
 * 描述: app入口
 */
public class EnterActivity extends Activity implements TextView.OnEditorActionListener, AdapterView.OnItemClickListener, View.OnClickListener, OkHttpInterface {

    private final String TAG = "EnterActivity";
    private MyEditTextDel et_enter_operator;
    private ProgressDialog progressDialog;
    private GlobalData globalData;
    private String curOrderNum;//工单号
    private String curOperatorNum;//操作员
    private com.jimi.smt.eps_appclient.Beans.Operator.OperatorBean mOperatorBean;//扫描到的操作员

    private final int SET_ORDER = 8;//设置工单号
    private final int NET_MATERIAL_FALL = 400;//获取料号表失败
    private static final int UPDATE_APK = 5;//更新apk

    private List<com.jimi.smt.eps_appclient.Beans.Program.ProgramBean> mProgramBeans = new ArrayList<>();//工单号列表


    private int oldCheckIndex = -1;//之前选中的
    private int curCheckIndex = -1;//现在选中的

    private ProgressDialog updateProgress;
    private UpdateApkReceiver apkReceiver;
    private GlobalFunc globalFunc;
    private MyEditTextDel et_enter_line;
    private ListView lv_orders;
    private EnterOrdersAdapter ordersAdapter;
    private PackageInfo packageInfo;
    private HttpUtils mHttpUtils;


    //    0:仓库操作员;1:厂线操作员;2:IPQC;3:超级管理员;4:生产管理员；5：品质管理员；6：工程管理员
    @SuppressLint("HandlerLeak")
    private Handler enterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //取消弹出窗
            dismissDialog();
            switch (msg.what) {
                case SET_ORDER:
//                    initData(mProgramList);
                    break;
                case NET_MATERIAL_FALL:
                    //获取料号表失败
                    Toast.makeText(getApplicationContext(), "获取料号表失败", Toast.LENGTH_SHORT).show();
                    showInfo();
                    break;

                case Constants.WARE_HOUSE:
                    //仓库
                    globalData.setUserType(Constants.WARE_HOUSE);
                    globalData.setOperator(curOperatorNum);
                    Toast.makeText(getApplicationContext(), "仓库", Toast.LENGTH_SHORT).show();
                    Intent warehouse = new Intent(getApplicationContext(), WareHouseActivity.class);
                    Bundle warehouseBundle = new Bundle();
                    warehouseBundle.putString("orderNum", curOrderNum);
                    warehouseBundle.putString("operatorNum", curOperatorNum);
                    warehouse.putExtras(warehouseBundle);
                    startActivityForResult(warehouse, Constants.ACTIVITY_RESULT);
                    break;
                case Constants.FACTORY:
                    //厂线
                    globalData.setUserType(Constants.FACTORY);
                    globalData.setOperator(curOperatorNum);
                    Toast.makeText(getApplicationContext(), "厂线", Toast.LENGTH_SHORT).show();
                    Intent factory = new Intent(getApplicationContext(), FactoryLineActivity.class);
                    Bundle factoryBundle = new Bundle();
                    factoryBundle.putString("orderNum", curOrderNum);
                    factoryBundle.putString("operatorNum", curOperatorNum);
                    factory.putExtras(factoryBundle);
                    startActivityForResult(factory, Constants.ACTIVITY_RESULT);
                    break;
                case Constants.QC:
                    //QC
                    globalData.setUserType(Constants.QC);
                    globalData.setOperator(curOperatorNum);
                    Toast.makeText(getApplicationContext(), "QC", Toast.LENGTH_SHORT).show();
                    Intent qc = new Intent(getApplicationContext(), QCActivity.class);
                    Bundle qcBundle = new Bundle();
                    qcBundle.putString("orderNum", curOrderNum);
                    qcBundle.putString("operatorNum", curOperatorNum);
                    qc.putExtras(qcBundle);
                    startActivityForResult(qc, Constants.ACTIVITY_RESULT);
                    break;
                case Constants.ADMIN:
                    //管理员
                    globalData.setUserType(Constants.ADMIN);
                    globalData.setOperator(curOperatorNum);
                    Intent admin = new Intent(getApplicationContext(), AdminActivity.class);
                    Bundle adminBundle = new Bundle();
                    adminBundle.putString("orderNum", curOrderNum);
                    adminBundle.putString("operatorNum", curOperatorNum);
                    admin.putExtras(adminBundle);
                    startActivityForResult(admin, Constants.ACTIVITY_RESULT);
                    break;
                case UPDATE_APK:
                    et_enter_operator.setText("");
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_enter);
        //使屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //申请权限
        CheckPermissionUtils.initPermission(this);
        try {
            packageInfo = getPackageManager().getPackageInfo("com.jimi.smt.eps_appclient", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //获取全局变量
        globalData = (GlobalData) getApplication();
        globalFunc = new GlobalFunc(this);
        mHttpUtils = new HttpUtils(this);

        //创建apk下载路径
        createApkDownloadDir();
        initViews();
        //先判断是否有网络连接
        boolean netConnect = globalFunc.isNetWorkConnected();
        if (netConnect) {
            //更新app todo
//            boolean result = updateApp();
//            Log.d(TAG, "updateApp-result::" + result);
        } else {
            showInfo();
        }

        //注册广播
        registeReceiver();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("lifecycle-", TAG + "--onRestart");

        if (et_enter_operator != null) {
            et_enter_operator.setText("");
        }

        if (et_enter_line != null) {
            et_enter_line.setText("");
            et_enter_line.requestFocus();
        }

        if ((mProgramBeans != null) && (mProgramBeans.size() > 0)) {
            mProgramBeans.clear();
            ordersAdapter.notifyDataSetChanged();
        }

        curCheckIndex = -1;
    }

    //初始化布局
    private void initViews() {
        TextView tv_version_no = findViewById(R.id.tv_version_no);
        tv_version_no.setText(packageInfo.versionName);
        et_enter_line = findViewById(R.id.et_enter_line);
        et_enter_operator = findViewById(R.id.et_enter_operator);
        lv_orders = findViewById(R.id.lv_orders);
        RelativeLayout rl_enter = findViewById(R.id.rl_enter);
        rl_enter.setOnClickListener(this);
        et_enter_line.setOnEditorActionListener(this);
        et_enter_operator.setOnEditorActionListener(this);
        et_enter_operator.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (TextUtils.isEmpty(et_enter_line.getText().toString())) {
                    et_enter_line.setText("");
                    et_enter_line.requestFocus();
                }
            }
        });
        lv_orders.setOnItemClickListener(this);

    }

    //操作员输入框监听事件
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        //回车键
        if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            Log.d(TAG, "event.getAction()::" + event.getAction());
            Log.d(TAG, "event.getKeyCode()::" + event.getKeyCode());

            switch (event.getAction()) {
                case KeyEvent.ACTION_UP:
                    //判断网络是否连接
                    if (globalFunc.isNetWorkConnected()) {
                        if (!TextUtils.isEmpty(v.getText().toString().trim())) {
                            //扫描内容
                            String scanValue = String.valueOf(((EditText) v).getText());
                            scanValue = scanValue.replaceAll("\r", "");
                            Log.i(TAG, "scan Value:" + scanValue);

                            switch (v.getId()) {
                                case R.id.et_enter_line:
                                    //扫描线号
                                    String scanLine = globalFunc.getLineNum(scanValue);
                                    v.setText(scanLine);
                                    //判断线号是否存在
                                    showDialog("请稍候!", "正在加载工单...");
                                    mHttpUtils.getOrdersByLine(scanLine);
                                    break;

                                case R.id.et_enter_operator:
                                    showDialog("请稍候!", "正在加载工单...");
                                    mHttpUtils.getUserInfo(scanValue);
                                    break;
                            }
                        } else {
                            Toast.makeText(this, "请重新扫描!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        showInfo();
                    }

                    return true;
                default:
                    return true;
            }

        }
        return false;
    }

    //弹出提示消息窗口
    private void showInfo() {

        //对话框所有控件id
        int itemResIds[] = new int[]{R.id.dialog_title_view,
                R.id.dialog_title, R.id.tv_alert_info, R.id.info_trust};
        //标题和内容
        String titleMsg[] = new String[]{"警告", "请检查网络连接是否正常!"};
        //内容的样式
        int msgStype[] = new int[]{22, Color.RED};

        InfoDialog infoDialog = new InfoDialog(this,
                R.layout.info_dialog_layout, itemResIds, titleMsg, msgStype);

        infoDialog.setOnDialogItemClickListener((dialog, view) -> {
            switch (view.getId()) {
                case R.id.info_trust:
                    dialog.dismiss();
                    et_enter_line.setText("");
                    et_enter_operator.setText("");
                    et_enter_line.requestFocus();
                    break;
            }
        });
        infoDialog.show();
    }


    //创建apk下载路径
    private void createApkDownloadDir() {
        File downloadDir;
        boolean sd = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sd) {
            File sdPath = Environment.getExternalStorageDirectory();
            String path = sdPath.getPath();
            downloadDir = new File(path);
            if (!downloadDir.exists()) {
                downloadDir.mkdir();
            }
            globalData.setApkDownloadDir(downloadDir.getPath());
            //删除之前下载的apk
            deleteDownLoadApk();
        }
    }

    //更新安装apk之后，重新启动app，将下载的apk删除
    private void deleteDownLoadApk() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo("com.jimi.smt.eps_appclient", 0);
            int mVersionCode = packageInfo.versionCode;
            String mVersionName = packageInfo.versionName;
            File file = new File(globalData.getApkDownloadDir() + "/" + mVersionName + ".apk");
            Log.d(TAG, "deleteDownLoadApk-" + file.getAbsolutePath());
            if (file.exists()) {
                file.delete();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.ACTIVITY_RESULT:
                //设置之前的选中项为初始化状态
                oldCheckIndex = -1;
                //先判断网络是否连接
                boolean net = globalFunc.isNetWorkConnected();
                if (net) {
                    //更新app
//                    boolean result = updateApp();
//                    Log.d(TAG, "updateApp-result::" + result);

                } else {
                    showInfo();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 工单选择点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        curCheckIndex = position;
        com.jimi.smt.eps_appclient.Beans.Program.ProgramBean curProgram = mProgramBeans.get(curCheckIndex);
        curProgram.setChecked(true);
        if (oldCheckIndex >= 0) {
            if (curCheckIndex != oldCheckIndex) {
                //设置之前的选中为不选中状态，当前的选中为选中状态
                com.jimi.smt.eps_appclient.Beans.Program.ProgramBean oldProgram = mProgramBeans.get(oldCheckIndex);
                oldProgram.setChecked(false);
                //将当前赋值给之前
                oldCheckIndex = curCheckIndex;
            }
        } else {
            //将当前赋值给之前
            oldCheckIndex = curCheckIndex;
        }
        ordersAdapter.notifyDataSetChanged();
        lv_orders.setSelection(curCheckIndex);
        Log.d(TAG, "curProgram-order::" + curProgram.getWorkOrder());
        Log.d(TAG, "curProgram-ProgramID::" + curProgram.getId());
    }

    //进入按钮点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_enter:

                if (globalFunc.isNetWorkConnected()) {
                    if ((!TextUtils.isEmpty(et_enter_line.getText().toString().trim())) && (!TextUtils.isEmpty(et_enter_operator.getText().toString().trim()))) {
                        if (curCheckIndex >= 0) {
                            lv_orders.setSelection(curCheckIndex);
                            //显示加载框
                            showDialog("请稍等!", "正在加载数据...");
                            //获取工单号对应的料号表
                            Log.d(TAG, "onClick - programId " + mProgramBeans.get(curCheckIndex).getId());

                            mHttpUtils.getMaterials(mProgramBeans.get(curCheckIndex).getId());

                        } else {
                            Toast.makeText(getApplicationContext(), "请选择工单号", Toast.LENGTH_LONG).show();
                        }
                    } else if (TextUtils.isEmpty(et_enter_line.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), "请扫描线号", Toast.LENGTH_LONG).show();
                        et_enter_line.setText("");
                        et_enter_line.requestFocus();
                    } else if (TextUtils.isEmpty(et_enter_operator.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), "请扫描工号", Toast.LENGTH_LONG).show();
                        et_enter_operator.setText("");
                        et_enter_operator.requestFocus();
                    }
                } else {
                    globalFunc.showInfo("警告", "请检查网络连接是否正常!", "请连接网络!");
                }

                break;
        }
    }

    /**
     * http返问回调
     *
     * @param code 请求码
     * @param s 返回信息
     */
    @Override
    public void showHttpResponse(int code, Object request, String s) {
        // TODO: 2018/8/27  showHttpResponse
        Log.d(TAG, "showHttpResponse - " + s);
        int resCode = -1;
        try {
            JSONObject jsonObject = new JSONObject(s);
            resCode = jsonObject.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "showHttpResponse - " + s);
        }
        switch (code) {
            case HttpUtils.CodeWokingOrders://线号与其工单
                //重扫线号
                oldCheckIndex = -1;
                Gson line = new Gson();
                if (resCode == 0) {
                    BaseMsg baseMsg = line.fromJson(s, BaseMsg.class);
                    //"该线号不存在，重新扫描线号!"
                    Toast.makeText(getApplicationContext(), baseMsg.getMsg(), Toast.LENGTH_LONG).show();
                    et_enter_line.setText("");
                    et_enter_line.requestFocus();
                } else if (resCode == 1) {
                    this.mProgramBeans = line.fromJson(s, com.jimi.smt.eps_appclient.Beans.Program.class).getData();
                    if ((mProgramBeans != null) && (mProgramBeans.size() > 0)) {
                        //显示工单
                        ordersAdapter = new EnterOrdersAdapter(getApplicationContext(), mProgramBeans);
                        lv_orders.setAdapter(ordersAdapter);
                        et_enter_operator.requestFocus();
                    } else {
                        Toast.makeText(this, "请重新扫描线号", Toast.LENGTH_LONG).show();
                        et_enter_line.setText("");
                        et_enter_line.requestFocus();
                    }
                }
                break;


            case HttpUtils.CodeUserInfo://操作员信息
                Gson operator = new Gson();
                if (resCode == 0) {
                    BaseMsg baseMsg = operator.fromJson(s, BaseMsg.class);
                    //不存在该操作员
                    Toast.makeText(getApplicationContext(), baseMsg.getMsg(), Toast.LENGTH_LONG).show();
                    et_enter_operator.setText("");
                    et_enter_operator.requestFocus();
                } else if (resCode == 1) {
                    com.jimi.smt.eps_appclient.Beans.Operator.OperatorBean operatorBean = operator.fromJson(s, com.jimi.smt.eps_appclient.Beans.Operator.class).getData();
                    if (!operatorBean.isEnabled()) {
                        //操作员离职
                        Toast.makeText(getApplicationContext(), "操作员离职", Toast.LENGTH_SHORT).show();
                        et_enter_operator.setText("");
                        et_enter_operator.requestFocus();
                    } else {
                        //操作员在职
                        mOperatorBean = operatorBean;
                    }
                }
                break;

            case HttpUtils.CodeMaterials://料号表
                Gson material = new Gson();
                if (resCode == 0) {
                    BaseMsg baseMsg = material.fromJson(s, BaseMsg.class);
                    //不存在该操作员
                    Toast.makeText(getApplicationContext(), baseMsg.getMsg(), Toast.LENGTH_LONG).show();
                    showInfo();
                } else if (resCode == 1) {
                    List<Material.MaterialBean> materialBeans = material.fromJson(s, Material.class).getData();
                    com.jimi.smt.eps_appclient.Beans.Program.ProgramBean programBean = mProgramBeans.get(curCheckIndex);
                    for (Material.MaterialBean bean : materialBeans) {
                        bean.setLine(programBean.getLine());
                        bean.setWorkOrder(programBean.getWorkOrder());
                        bean.setBoardType(programBean.getBoardType());
                    }
                    globalData.setProgramId(programBean.getId());
                    globalData.setMaterialBeans(materialBeans);
                    globalData.setLine(programBean.getLine());
                    globalData.setMinusLine(programBean.getLine());
                    globalData.setWork_order(programBean.getWorkOrder());
                    globalData.setBoard_type(programBean.getBoardType());
                    //成功获取料号表,操作员在职
                    Log.d(TAG, "mOperatorBean - " + mOperatorBean.getType());
                    Message message = Message.obtain();
                    message.what = mOperatorBean.getType();
                    curOrderNum = programBean.getWorkOrder();
                    curOperatorNum = mOperatorBean.getId();
                    //发送消息
                    enterHandler.sendMessage(message);

                }
                break;

            default:
                break;
        }
        //取消弹出窗
        dismissDialog();
    }

    //显示提示窗
    private void showDialog(String title, String msg) {
        dismissDialog();
        progressDialog = ProgressDialog.show(this, title, msg, true);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    //取消提示窗
    private void dismissDialog() {
        //取消弹出窗
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    /**
     * http返问出错回调
     *
     * @param code 请求码
     * @param s 返回信息
     */
    @Override
    public void showHttpError(int code, Object request, String s) {
        //取消弹出窗
        dismissDialog();
        Log.d(TAG, "showHttpError-" + s);
        showInfo();
    }

    /**
     * 升级apk进度广播
     */
    private class UpdateApkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到进度更新ui
            int progress = intent.getIntExtra("progress", 0);
            showUpdateProgress(progress);
            if (progress >= 100) {
                Log.d(TAG, "progress-" + progress);
                updateProgress.dismiss();
                updateProgress.cancel();
            }

        }
    }

    //注册广播
    private void registeReceiver() {
        updateProgress = new ProgressDialog(this);
        apkReceiver = new UpdateApkReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jimi.smt.eps_appclient.UPDATE");
        registerReceiver(apkReceiver, intentFilter);
        Log.d(TAG, "===registeReceiver===");
    }

    //显示下载进度
    private void showUpdateProgress(int progress) {
//        updateProgress.show();
        updateProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        updateProgress.setCancelable(false);
        updateProgress.setCanceledOnTouchOutside(false);
        updateProgress.setIcon(R.mipmap.icon_download);
        updateProgress.setMax(100);
        updateProgress.setProgress(progress);
        updateProgress.setTitle("下载更新App");
        if (progress > 0 && progress < 100) {
            updateProgress.setMessage("正在下载...");
        } else if (progress >= 100) {
            updateProgress.setTitle("下载完成");
//            updateProgress.dismiss();
        }
        updateProgress.show();
    }

    @Override
    public void onBackPressed() {
        backToHome();
    }

    //退出
    private void backToHome() {
        AlertDialog.Builder exit = new AlertDialog.Builder(this);
        exit.setTitle("系统提示");
        exit.setMessage("确定要退出吗?");
        exit.setPositiveButton("确定", (dialog, which) -> {
            dialog.dismiss();
            finish();
            System.exit(0);
        });
        exit.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        exit.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("lifecycle-", TAG + "--onDestroy");
        if (apkReceiver != null) {
            unregisterReceiver(apkReceiver);
        }
    }

}
