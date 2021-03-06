package com.jimi.smt.eps_appclient.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.jimi.smt.eps_appclient.Activity.QCActivity;
import com.jimi.smt.eps_appclient.Adapter.MaterialAdapter;
import com.jimi.smt.eps_appclient.Beans.Material;
import com.jimi.smt.eps_appclient.Beans.Operation;
import com.jimi.smt.eps_appclient.Beans.ProgramItemVisit;
import com.jimi.smt.eps_appclient.Dao.GreenDaoUtil;
import com.jimi.smt.eps_appclient.Dao.QcCheckAll;
import com.jimi.smt.eps_appclient.Func.GlobalFunc;
import com.jimi.smt.eps_appclient.Func.HttpUtils;
import com.jimi.smt.eps_appclient.Func.Log;
import com.jimi.smt.eps_appclient.R;
import com.jimi.smt.eps_appclient.Unit.Constants;
import com.jimi.smt.eps_appclient.Unit.EvenBusTest;
import com.jimi.smt.eps_appclient.Unit.GlobalData;
import com.jimi.smt.eps_appclient.Views.InfoDialog;
import com.jimi.smt.eps_appclient.Views.InputDialog;
import com.jimi.smt.eps_appclient.Views.LoadingDialog;
import com.jimi.smt.eps_appclient.Views.MyEditTextDel;
import com.jimi.smt.eps_appclient.Interfaces.OkHttpInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QCcheckAllFragment extends Fragment implements TextView.OnEditorActionListener, OkHttpInterface {

    private final String TAG = this.getClass().getSimpleName();
    private View qcCheckAllView;
    private MyEditTextDel edt_ScanMaterial;
    private ListView lv_qcCheckAll;
    private List<Material.MaterialBean> mQcCheckALLMaterialBeans = new ArrayList<>();
    //IPQC全检纪录
    private List<QcCheckAll> qcCheckAllList = new ArrayList<>();
    //长按时选择的行
    private int selectRow = -1;
    private GlobalData globalData;
    private GlobalFunc globalFunc;
    private QCActivity mQcActivity;
    private boolean isRestoreCache = false;
    private int curCheckId;
    private MaterialAdapter materialAdapter;
    private InfoDialog resultInfoDialog;
    private InputDialog inputDialog;
    private boolean mHidden;
    private HttpUtils mHttpUtils;
    private int checkFeedCondition = -1;
    private int checkFirstCondition = -1;
    private int checkResetCondition = -1;
    private String dialogScanValue;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册订阅
        EventBus.getDefault().register(this);
        globalData = (GlobalData) getActivity().getApplication();
        globalFunc = new GlobalFunc(globalData);
        mQcActivity = (QCActivity) getActivity();
        if (Constants.isCache) {
            //查询本地数据库是否存在缓存
            List<QcCheckAll> qcCheckAlls = new GreenDaoUtil().queryQcCheckRecord(globalData.getOperator(),
                    globalData.getWork_order(), globalData.getLine(), globalData.getBoard_type());
            if (qcCheckAlls != null && qcCheckAlls.size() > 0) {
                qcCheckAllList.addAll(qcCheckAlls);
                isRestoreCache = true;
            } else {
                boolean result = new GreenDaoUtil().deleteAllQcCheck();
                Log.d(TAG, "deleteAllQcCheck - " + result);
                isRestoreCache = false;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        qcCheckAllView = inflater.inflate(R.layout.checkallmaterial_layout, container, false);
        Intent intent = getActivity().getIntent();
        savedInstanceState = intent.getExtras();
        if (mQcActivity.updateDialog != null && mQcActivity.updateDialog.isShowing()) {
            mQcActivity.updateDialog.cancel();
            mQcActivity.updateDialog.dismiss();
        }

        mHttpUtils = new HttpUtils(this);

        showLoading();
        checkFeedCondition = 0;
        mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FEEDMATERIAL);

        initView(savedInstanceState);
        initData();
        return qcCheckAllView;
    }

    //监听订阅的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EvenBusTest event) {
        if (Constants.isCache) {
            if (event.getUpdated() == 0) {
                Log.d(TAG, "onEventMainThread - update - " + event.getUpdated());
                //更新或重置
                if (inputDialog != null && inputDialog.isShowing()) {
                    inputDialog.cancel();
                    inputDialog.dismiss();
                    selectRow = -1;
                }
                if (resultInfoDialog != null && resultInfoDialog.isShowing()) {
                    resultInfoDialog.cancel();
                    resultInfoDialog.dismiss();
                }
                if (event.getQcCheckAllList() != null && event.getQcCheckAllList().size() > 0) {
                    qcCheckAllList.clear();
                    qcCheckAllList.addAll(event.getQcCheckAllList());
                    curCheckId = 0;
                    mQcCheckALLMaterialBeans.clear();
                    for (int i = 0; i < qcCheckAllList.size(); i++) {
                        QcCheckAll qcCheckAll = qcCheckAllList.get(i);
                        Material.MaterialBean bean = new Material.MaterialBean(qcCheckAll.getOrder(), qcCheckAll.getBoard_type(), qcCheckAll.getLine(),
                                qcCheckAll.getProgramId(), qcCheckAll.getSerialNo(), qcCheckAll.getAlternative(), qcCheckAll.getOrgLineSeat(), qcCheckAll.getOrgMaterial(),
                                qcCheckAll.getScanLineSeat(), qcCheckAll.getScanMaterial(), qcCheckAll.getResult(), qcCheckAll.getRemark());
                        mQcCheckALLMaterialBeans.add(bean);
                        if ((null != qcCheckAll.getResult()) && ((qcCheckAll.getResult().equalsIgnoreCase("PASS")) || (qcCheckAll.getResult().equalsIgnoreCase("FAIL")))) {
                            if (i == qcCheckAllList.size() - 1) {
                                curCheckId = i;
                            } else {
                                curCheckId = i + 1;
                            }
                        }
                    }
                }
                //更新显示
                materialAdapter.notifyDataSetChanged();
                edt_ScanMaterial.requestFocus();
                Log.d(TAG, "mHidden - " + mHidden);
                //提示首检或上料
                if (!mHidden) {
                    if (mQcActivity.updateDialog != null && mQcActivity.updateDialog.isShowing()) {
                        mQcActivity.updateDialog.cancel();
                        mQcActivity.updateDialog.dismiss();
                    }
                }
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "onHiddenChanged-" + hidden);
        this.mHidden = hidden;
        if (!hidden) {
            //判断是否上料
            if (mQcActivity.updateDialog != null && mQcActivity.updateDialog.isShowing()) {
                mQcActivity.updateDialog.cancel();
                mQcActivity.updateDialog.dismiss();
            }
            showLoading();
            checkFeedCondition = 0;
            mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FEEDMATERIAL);
        }
    }

    private void initView(Bundle bundle) {
        TextView tv_checkAll_Operation = qcCheckAllView.findViewById(R.id.tv_checkAll_Operation);
        tv_checkAll_Operation.setText(bundle.getString("operatorNum"));
        TextView tv_checkAll_order = qcCheckAllView.findViewById(R.id.tv_checkAll_order);
        tv_checkAll_order.setText(bundle.getString("orderNum"));
        edt_ScanMaterial = qcCheckAllView.findViewById(R.id.edt_material);
        lv_qcCheckAll = qcCheckAllView.findViewById(R.id.checkall_list_view);
        edt_ScanMaterial.requestFocus();
        edt_ScanMaterial.setOnEditorActionListener(this);
        //长按弹出框
        lv_qcCheckAll.setOnItemLongClickListener((adapterView, view, row, l) -> {
            //若未扫过不弹出框
            if (isFirstScaned()) {
                //弹出对话框
                selectRow = row;
                showLongClickDialog();
            }
            return true;
        });
    }

    //是否扫过了
    private boolean isFirstScaned() {
        boolean firstScaned = false;
        for (Material.MaterialBean materialItem : mQcCheckALLMaterialBeans) {
            if ((null != materialItem.getResult()) && (!(materialItem.getResult().equalsIgnoreCase("")))) {
                firstScaned = true;
                break;
            }
        }
        return firstScaned;
    }

    private void initData() {
        Log.i(TAG, "initData");
        //填充数据
        curCheckId = 0;
        mQcCheckALLMaterialBeans.clear();
        if (!isRestoreCache) {
            //不存在缓存
            mQcCheckALLMaterialBeans = globalData.getMaterialBeans();
            if (Constants.isCache) {
                for (Material.MaterialBean bean : mQcCheckALLMaterialBeans) {
                    QcCheckAll qcCheckAll = new QcCheckAll(null, bean.getProgramId(), bean.getWorkOrder(), globalData.getOperator(),
                            bean.getBoardType(), bean.getLine(), bean.getSerialNo(), bean.isAlternative(), bean.getLineseat(), bean.getMaterialNo(),
                            bean.getScanlineseat(), bean.getScanMaterial(), bean.getResult(), bean.getRemark());
                    qcCheckAllList.add(qcCheckAll);
                }
                boolean cacheResult = new GreenDaoUtil().insertMultiQcCheckMaterial(qcCheckAllList);
                Log.d(TAG, "insertMultiQcCheckMaterial - " + cacheResult);
            }
        } else {
            //存在缓存
            for (int i = 0; i < qcCheckAllList.size(); i++) {
                QcCheckAll qcCheckAll = qcCheckAllList.get(i);
                Material.MaterialBean bean = new Material.MaterialBean(qcCheckAll.getOrder(), qcCheckAll.getBoard_type(), qcCheckAll.getLine(), qcCheckAll.getProgramId(),
                        qcCheckAll.getSerialNo(), qcCheckAll.getAlternative(), qcCheckAll.getOrgLineSeat(), qcCheckAll.getOrgMaterial(), qcCheckAll.getScanLineSeat(),
                        qcCheckAll.getScanMaterial(), qcCheckAll.getResult(), qcCheckAll.getRemark());
                mQcCheckALLMaterialBeans.add(bean);
                if ((null != qcCheckAll.getResult()) && ((qcCheckAll.getResult().equalsIgnoreCase("PASS")) || (qcCheckAll.getResult().equalsIgnoreCase("FAIL")))) {
                    if (i == qcCheckAllList.size() - 1) {
                        curCheckId = i;
                    } else {
                        curCheckId = i + 1;
                    }
                }
            }

            //todo 需要更新全局变量为本地数据库的
            globalData.setMaterialBeans(mQcCheckALLMaterialBeans);
        }

        //设置Adapter
        materialAdapter = new MaterialAdapter(getActivity(), mQcCheckALLMaterialBeans);
        lv_qcCheckAll.setAdapter(materialAdapter);
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        //注销订阅
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    //弹出长按对话框
    private void showLongClickDialog() {
        inputDialog = new InputDialog(getActivity(),
                R.layout.input_dialog_layout, new int[]{R.id.input_dialog_title, R.id.et_input}, "请重新扫描新的料号");
        inputDialog.show();
        inputDialog.setOnDialogEditorActionListener((inputDialog, v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                switch (event.getAction()) {
                    //按下
                    case KeyEvent.ACTION_UP:
                        //先判断是否联网
                        if (globalFunc.isNetWorkConnected()) {

                            switch (v.getId()) {
                                case R.id.et_input:
                                    //扫描内容
                                    dialogScanValue = String.valueOf(((EditText) v).getText());
                                    dialogScanValue = globalFunc.getMaterial(dialogScanValue);
                                    v.setText(dialogScanValue);

                                    showLoading();
                                    checkFeedCondition = 3;
                                    mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FEEDMATERIAL);

                                    break;
                            }

                        } else {
                            globalFunc.showInfo("警告", "请检查网络连接是否正常!", "请连接网络!");
                            v.setText("");
                        }
                        return true;
                    default:
                        return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        //回车键
        if (i == EditorInfo.IME_ACTION_SEND || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            switch (keyEvent.getAction()) {
                //按下
                case KeyEvent.ACTION_UP:
                    //先判断是否联网
                    if (globalFunc.isNetWorkConnected()) {
                        switch (textView.getId()) {
                            case R.id.edt_material:
                                //扫描内容
                                String scanValue = String.valueOf(((EditText) textView).getText());
                                scanValue = scanValue.replaceAll("\r", "");
                                Log.i(TAG, "scan Value:" + scanValue);
                                //料号,若为二维码则提取@@前的料号
                                scanValue = globalFunc.getMaterial(scanValue);
                                textView.setText(scanValue);

                                showLoading();
                                checkFeedCondition = 2;
                                mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FEEDMATERIAL);

                                break;
                        }

                    } else {
                        globalFunc.showInfo("警告", "请检查网络连接是否正常!", "请连接网络!");
                        clearLineSeatMaterialScan();
                    }
                    return true;
                default:
                    return true;
            }
        }
        return false;
    }

    private void beginOperate(int checkIndex, String scanValue, int condition) {
        Log.d(TAG, "beginOperate - " + "checkIndex:" + checkIndex + " scanValue:" + scanValue + " condition:" + condition);

        //未重置
        Log.d(TAG, "curCheckId - " + curCheckId);
        Log.d(TAG, "checkIndex - " + checkIndex);
        //将扫描的内容更新至列表中
        Material.MaterialBean checkAllMaterialItem = mQcCheckALLMaterialBeans.get(checkIndex);
        //当前操作的站位
        String curCheckLineSeat = checkAllMaterialItem.getLineseat();
        //相同站位的索引数组
        ArrayList<Integer> sameLineSeatIndexs = new ArrayList<>();

        if (condition == 2) {//正常的全检操作
            //当前操作的位置
            curCheckId = curCheckId - 1;

            //遍历所有相同站位的位置
            for (int m = 0; m < mQcCheckALLMaterialBeans.size(); m++) {
                if (mQcCheckALLMaterialBeans.get(m).getLineseat().equalsIgnoreCase(curCheckLineSeat)) {
                    sameLineSeatIndexs.add(m);
                    if (m > curCheckId) {
                        //将检查的位置往后移
                        curCheckId++;
                    }
                }
            }
        } else if (condition == 3) {//长按弹窗的全检操作
            //相同站位的索引数组
            for (int i = 0; i < mQcCheckALLMaterialBeans.size(); i++) {
                if (mQcCheckALLMaterialBeans.get(i).getLineseat().equalsIgnoreCase(curCheckLineSeat)) {
                    sameLineSeatIndexs.add(i);
                }
            }
        }

        //根据站位索引数组检查料号与扫到的料号比对
        if (sameLineSeatIndexs.size() == 1) {
            //只有一个站位
            Material.MaterialBean singleMaterialItem = mQcCheckALLMaterialBeans.get(sameLineSeatIndexs.get(0));
            singleMaterialItem.setScanMaterial(scanValue);
            if (singleMaterialItem.getMaterialNo().equalsIgnoreCase(singleMaterialItem.getScanMaterial())) {
                singleMaterialItem.setScanlineseat(singleMaterialItem.getLineseat());
                singleMaterialItem.setResult("PASS");
                singleMaterialItem.setRemark("站位和料号正确");
            } else {
                singleMaterialItem.setScanlineseat(singleMaterialItem.getLineseat());
                singleMaterialItem.setResult("FAIL");
                singleMaterialItem.setRemark("料号与站位不对应");
            }

            updateVisitLog(sameLineSeatIndexs, singleMaterialItem, condition);

        } else if (sameLineSeatIndexs.size() > 1) {
            //多个相同的站位,即有替换料
            checkMultiItem(sameLineSeatIndexs, scanValue, condition);
        }

        materialAdapter.notifyDataSetChanged();
    }

    //更新全检缓存
    private void cacheCheckResult(ArrayList<Integer> integers, Material.MaterialBean materialItem) {
        if (Constants.isCache) {
            //保存缓存
            if (null != integers && integers.size() > 0) {
                for (int i = 0, len = integers.size(); i < len; i++) {
                    QcCheckAll qcCheckAll = qcCheckAllList.get(integers.get(i));
                    qcCheckAll.setScanLineSeat(materialItem.getScanlineseat());
                    qcCheckAll.setScanMaterial(materialItem.getScanMaterial());
                    qcCheckAll.setResult(materialItem.getResult());
                    qcCheckAll.setRemark(materialItem.getRemark());
                    new GreenDaoUtil().updateQcCheck(qcCheckAll);
                }
            }

        }
    }

    //清除刚刚的操作
    private void clearDisplay(ArrayList<Integer> integers) {
        if (null != integers && integers.size() > 0) {
            for (int i = 0, len = integers.size(); i < len; i++) {
                Material.MaterialBean bean = mQcCheckALLMaterialBeans.get(integers.get(i));
                bean.setScanlineseat("");
                bean.setScanMaterial("");
                bean.setResult("");
                bean.setRemark("");
            }
            materialAdapter.notifyDataSetChanged();
        }
    }

    //更新显示日志
    private void updateVisitLog(ArrayList<Integer> integers, Material.MaterialBean materialItem, int condition) {
        checkFirstCondition = 4;
        mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FIRST_CHECK_ALL, integers, materialItem, condition);
    }

    private void checkMultiItem(ArrayList<Integer> integers, String mScanValue, int condition) {
        //多个相同的站位,即有替换料
        Material.MaterialBean bean = new Material.MaterialBean();
        int index = -1;
        for (int i = 0, len = integers.size(); i < len; i++) {
            Material.MaterialBean multiMaterialItem = mQcCheckALLMaterialBeans.get(integers.get(i));
            multiMaterialItem.setScanMaterial(mScanValue);
            multiMaterialItem.setScanlineseat(multiMaterialItem.getLineseat());
            if (multiMaterialItem.getMaterialNo().equalsIgnoreCase(multiMaterialItem.getScanMaterial())) {
                index = integers.get(i);
            }
        }
        if (index > -1) {
            for (int j = 0; j < integers.size(); j++) {
                Material.MaterialBean innerMaterialItem = mQcCheckALLMaterialBeans.get(integers.get(j));
                innerMaterialItem.setResult("PASS");
                innerMaterialItem.setRemark("主替有一项成功");
            }
            bean = mQcCheckALLMaterialBeans.get(index);
        } else {
            for (int j = 0; j < integers.size(); j++) {
                Material.MaterialBean innerMaterialItem = mQcCheckALLMaterialBeans.get(integers.get(j));
                innerMaterialItem.setResult("FAIL");
                innerMaterialItem.setRemark("料号与站位不对应");

                bean = innerMaterialItem;
            }
        }
        //更新显示日志
        updateVisitLog(integers, bean, condition);
    }

    //IPQC未做首次全检
    private void showInfo(String message, String tip, final int userType) {
        //对话框所有控件id
        int itemResIds[] = new int[]{R.id.dialog_title_view,
                R.id.dialog_title, R.id.tv_alert_info, R.id.info_trust, R.id.tv_alert_msg};
        //标题和内容
        String titleMsg[];
        //内容的样式
        int msgStype[];

        if (userType == 3) {
            msgStype = new int[]{22, Color.argb(255, 102, 153, 0)};
            titleMsg = new String[]{"提示", message};
        } else {
            msgStype = new int[]{22, Color.RED, Color.argb(255, 219, 201, 36)};
            titleMsg = new String[]{"提示", message, tip};
        }

        InfoDialog infoDialog = new InfoDialog(getActivity(),
                R.layout.info_dialog_layout, itemResIds, titleMsg, msgStype);

        infoDialog.setOnDialogItemClickListener((dialog, view) -> {
            switch (view.getId()) {
                case R.id.info_trust:
                    dialog.dismiss();
                    clearLineSeatMaterialScan();
                    break;
            }
        });
        infoDialog.show();
    }

    private void checkNextMaterial() {
        lv_qcCheckAll.setSelection(curCheckId);
        if (curCheckId < mQcCheckALLMaterialBeans.size() - 1) {
            curCheckId++;
            clearLineSeatMaterialScan();
        } else {
            showCheckAllMaterialResult(0);
        }
        Log.i(TAG, "checkNextMaterial:" + curCheckId);
    }

    private void clearLineSeatMaterialScan() {
        edt_ScanMaterial.setText("");
        edt_ScanMaterial.requestFocus();
    }

    private void showCheckAllMaterialResult(int showType) {
        boolean checkResult = true;
        String[] titleMsg;
        int[] msgStyle;
        for (Material.MaterialBean checkMaterialItem : mQcCheckALLMaterialBeans) {
            if ((checkMaterialItem.getResult() == null) || (!(checkMaterialItem.getResult().equals("PASS")))) {
                checkResult = false;
                break;
            }
        }
        if ((showType == 0) || (checkResult && (showType == 1))) {

            if (checkResult) {
                titleMsg = new String[]{"全检完成", "PASS"};
                msgStyle = new int[]{66, Color.argb(255, 102, 153, 0)};
            } else {
                titleMsg = new String[]{"全检失败,请检查!", "FAIL"};
                msgStyle = new int[]{66, Color.RED};
            }
            showResultInfo(titleMsg, msgStyle, checkResult);
        }
    }

    //弹出消息窗口
    private void showResultInfo(String[] titleMsg, int[] msgStyle, final boolean result) {
        //对话框所有控件id
        int itemResIds[] = new int[]{R.id.dialog_title_view, R.id.dialog_title, R.id.tv_alert_info, R.id.info_trust};

        resultInfoDialog = new InfoDialog(getActivity(), R.layout.info_dialog_layout, itemResIds, titleMsg, msgStyle);

        resultInfoDialog.setOnDialogItemClickListener((dialog, view) -> {
            switch (view.getId()) {
                case R.id.info_trust:

                    //获取全检结果
                    checkFirstCondition = 0;
                    mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);


                    dialog.dismiss();
                    if (result) {
                        clearMaterialInfo();
                        mHttpUtils.resetCheckAllRR(globalData.getProgramId());
                    } else {
                        edt_ScanMaterial.setText("");
                        edt_ScanMaterial.requestFocus();
                    }
                    break;
            }
        });
        resultInfoDialog.show();

    }


    private void clearMaterialInfo() {
        Log.d(TAG, "clearMaterialInfo");
        clearLineSeatMaterialScan();
        curCheckId = 0;
        clearCheckAllDisplay();
        //清除本地数据库全检纪录
        boolean result = new GreenDaoUtil().updateAllQcCheck(qcCheckAllList);
        Log.d(TAG, "updateAllQcCheck - " + result);
    }

    //清除全检结果
    private void clearCheckAllDisplay() {
        for (int i = 0, len = mQcCheckALLMaterialBeans.size(); i < len; i++) {
            Material.MaterialBean materialItem = mQcCheckALLMaterialBeans.get(i);
            materialItem.setScanlineseat("");
            materialItem.setScanMaterial("");
            materialItem.setResult("");
            materialItem.setRemark("");
            if (Constants.isCache) {
                QcCheckAll qcCheckAll = qcCheckAllList.get(i);
                qcCheckAll.setScanLineSeat("");
                qcCheckAll.setScanMaterial("");
                qcCheckAll.setResult("");
                qcCheckAll.setRemark("");
            }
        }
        materialAdapter.notifyDataSetChanged();
    }


    /**
     * http返问回调
     *
     * @param code 请求码
     * @param s 返回信息
     */
    @Override
    public void showHttpResponse(int code, Object request, String s) {
        dismissLoading();
        Log.d(TAG, "showHttpResponse - " + "  code:" + code + "  s:" + s);
        switch (code) {
            case HttpUtils.CodeIsAllDone:
                Log.d(TAG, "CodeIsAllDone - " + HttpUtils.CodeIsAllDone);
                int checkFeedOrFirst = Integer.valueOf(s);
                int type = (Integer) ((Object[]) request)[1];
                Log.d(TAG, "checkFeedOrFirst - " + checkFeedOrFirst);
                Log.d(TAG, "type - " + type);
                if (type == 0) {//feed
                    Log.d(TAG, "checkFeedCondition - " + checkFeedCondition);
                    switch (checkFeedCondition) {
                        case 0:
                            if (checkFeedOrFirst == 1) {
                                checkFirstCondition = 0;
                                mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            } else {
                                edt_ScanMaterial.setText("");
                                showInfo("操作员未完成上料!", "", 2);
                                //检测是否重置,但不操作

                                checkFirstCondition = 1;
                                mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            }
                            break;

                        case 2://扫料号
                            if (checkFeedOrFirst == 1) {
                                //完成上料,检测是否重置,同时操作
                                checkFirstCondition = 2;
                                mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            } else {
                                //未完成上料
                                edt_ScanMaterial.setText("");
                                showInfo("操作员未完成上料!", "", 2);
                                //检测是否重置,但不操作

                                checkFirstCondition = 1;
                                mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            }

                            break;
                        case 3:
                            if (checkFeedOrFirst == 1) {
                                //完成上料,检测是否重置,同时操作
                                checkFirstCondition = 3;
                                mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            } else {
                                //未完成上料
                                edt_ScanMaterial.setText("");
                                showInfo("操作员未完成上料!", "", 2);
                                //检测是否重置,但不操作
                                checkFirstCondition = 1;
                                mHttpUtils.checkAllDone(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            }
                            break;
                    }
                } else if (type == 5) {//first
                    Log.d(TAG, "checkFirstCondition - " + checkFirstCondition);
                    switch (checkFirstCondition) {
                        case 0:
                            if (checkFeedOrFirst == 0) {
                                showInfo("将进行首次全检", "", 3);
                            }
                            break;

                        case 1:
                            checkResetCondition = 1;
                            if (checkFeedOrFirst == 1) {
                                mHttpUtils.isReset(globalData.getProgramId(), Constants.CHECKALLMATERIAL);
                            } else {
                                mHttpUtils.isReset(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            }

                            break;

                        case 2:
                            checkResetCondition = 2;
                            if (checkFeedOrFirst == 1) {
                                mHttpUtils.isReset(globalData.getProgramId(), Constants.CHECKALLMATERIAL);
                            } else {
                                mHttpUtils.isReset(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            }
                            break;

                        case 3:
                            checkResetCondition = 3;
                            if (checkFeedOrFirst == 1) {
                                mHttpUtils.isReset(globalData.getProgramId(), Constants.CHECKALLMATERIAL);
                            } else {
                                mHttpUtils.isReset(globalData.getProgramId(), Constants.FIRST_CHECK_ALL);
                            }
                            break;

                        case 4:
                            ArrayList<Integer> integers = (ArrayList<Integer>) ((Object[]) request)[2];
                            Material.MaterialBean bean = (Material.MaterialBean) ((Object[]) request)[3];
                            int condition = (int) ((Object[]) request)[4];
                            if (checkFeedOrFirst == 1) {
                                Operation operation = Operation.getOperation(globalData.getOperator(), Constants.CHECKALLMATERIAL, bean);
                                globalData.setUpdateType(Constants.CHECKALLMATERIAL);
                                mHttpUtils.operate(integers, bean, Constants.CHECKALLMATERIAL, condition);
                                mHttpUtils.addOperation(operation);
                            } else {
                                Operation operation = Operation.getOperation(globalData.getOperator(), Constants.FIRST_CHECK_ALL, bean);
                                globalData.setUpdateType(Constants.FIRST_CHECK_ALL);
                                ProgramItemVisit visit = ProgramItemVisit.getProgramItemVisit(Constants.FIRST_CHECK_ALL, bean);
                                mHttpUtils.updateVisit(bean, visit, condition, integers);
                                mHttpUtils.addOperation(operation);
                            }
                            break;
                    }

                }

                break;

            case HttpUtils.CodeCheckIsReset:
                Log.d(TAG, "CodeCheckIsReset - " + HttpUtils.CodeCheckIsReset);
                int reset = Integer.valueOf(s);
                boolean reseted = true;
                for (Material.MaterialBean materialItem : mQcCheckALLMaterialBeans) {
                    if ((null != materialItem.getResult()) && (!materialItem.getResult().equalsIgnoreCase(""))) {
                        reseted = false;
                    }
                }
                Log.d(TAG, "reset - " + reset);
                Log.d(TAG, "reseted - " + reseted);
                Log.d(TAG, "checkResetCondition - " + checkResetCondition);
                switch (checkResetCondition) {
                    case 1:
                        if (reset == 1 && !reseted) {
                            clearMaterialInfo();
                            Log.d(TAG, "isResteted - 重置了");
                        }
                        break;
                    case 2:
                        if (reset == 1 && !reseted) {
                            clearMaterialInfo();
                            Log.d(TAG, "isResteted - 重置了");
                        } else {
                            if (curCheckId >= 0) {
                                beginOperate(curCheckId, edt_ScanMaterial.getText().toString().trim(), 2);
                            } else {
                                curCheckId = 0;
                                clearLineSeatMaterialScan();
                            }
                        }
                        break;
                    case 3:
                        if (reset == 1 && !reseted) {
                            clearMaterialInfo();
                            Log.d(TAG, "isResteted - 重置了");
                        } else {
                            beginOperate(selectRow, dialogScanValue, 3);
                        }
                        break;
                }

                break;

            case HttpUtils.CodeResetCheckAll:
                //设置全检的时间为当前时间
                Log.d(TAG, "CodeResetCheckAll - " + HttpUtils.CodeResetCheckAll);
                break;

            case HttpUtils.CodeAddOperate:
                Log.d(TAG, "CodeAddOperate - " + HttpUtils.CodeAddOperate);
                break;

            case HttpUtils.CodeAddVisit:
                Log.d(TAG, "CodeAddVisit - " + HttpUtils.CodeAddVisit);
                //更新visit表
                int resCode = -1;
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    resCode = jsonObject.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "showHttpResponse - " + e.toString());
                }
                Log.d(TAG, "resCode - " + resCode);
                Material.MaterialBean materialBean = (Material.MaterialBean) ((Object[]) request)[0];
                int con = (int) ((Object[]) request)[2];
                ArrayList<Integer> arrayList = (ArrayList<Integer>) ((Object[]) request)[3];
                if (resCode == 1) {
                    //更新本地缓存
                    cacheCheckResult(arrayList, materialBean);
                    //清空站位
                    if (con == 2) {//正常的全检操作
                        //检查下一个料
                        checkNextMaterial();
                    } else if (con == 3) {//长按弹窗的全检操作
                        //判断全是否全部正确
                        showCheckAllMaterialResult(1);
                        if (inputDialog != null && inputDialog.isShowing()) {
                            inputDialog.dismiss();
                        }
                        edt_ScanMaterial.requestFocus();
                    }
                } else {
                    //清除刚刚的操作
                    clearDisplay(arrayList);
                    if (con == 2) {
                        //正常的全检操作,回退
                        if (null != arrayList && arrayList.size() > 0) {
                            curCheckId -= arrayList.size();
                        }
                    }
                    clearLineSeatMaterialScan();
                }
                break;

            case HttpUtils.CodeOperate:
                Log.d(TAG, "CodeOperate - " + HttpUtils.CodeOperate);
                //更新visit表
                String result = "";
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    result = jsonObject.getString("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "showHttpResponse - " + e.toString());
                }
                Log.d(TAG, "result - " + result);
                ArrayList<Integer> integers = (ArrayList<Integer>) ((Object[]) request)[0];
                Material.MaterialBean bean = (Material.MaterialBean) ((Object[]) request)[1];
                int condition = (int) ((Object[]) request)[2];
                if ((result.equalsIgnoreCase("succeed"))) {
                    //更新本地缓存
                    cacheCheckResult(integers, bean);
                    //清空站位
                    if (condition == 2) {//正常的全检操作
                        //检查下一个料
                        checkNextMaterial();
                    } else if (condition == 3) {//长按弹窗的全检操作
                        //判断全是否全部正确
                        showCheckAllMaterialResult(1);
                        if (inputDialog != null && inputDialog.isShowing()) {
                            inputDialog.dismiss();
                        }
                        edt_ScanMaterial.requestFocus();
                    }

                } else {
                    //清除刚刚的操作
                    clearDisplay(integers);
                    if (condition == 2) {
                        //正常的全检操作,回退
                        if ((null != integers && integers.size() > 0) && (curCheckId > 0)) {
                            curCheckId -= integers.size();
                        }
                    }
                    clearLineSeatMaterialScan();
                }
                break;
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
        Log.d(TAG, "showHttpResponse - " + s);
        globalFunc.showInfo("警告", "请检查网络连接是否正常!", "请连接网络!");
        switch (code) {
            case HttpUtils.CodeOperate:
                Log.d(TAG, "CodeOperate - " + HttpUtils.CodeOperate);
                //更新visit表
                ArrayList<Integer> integers = (ArrayList<Integer>) ((Object[]) request)[0];
                int condition = (int) ((Object[]) request)[2];
                //清除刚刚的操作
                clearDisplay(integers);
                if (condition == 2) {
                    //正常的全检操作,回退
                    if (null != integers && integers.size() > 0) {
                        curCheckId -= integers.size();
                    }
                }
                clearLineSeatMaterialScan();
                break;

            case HttpUtils.CodeAddVisit:
                Log.d(TAG, "CodeAddVisit - " + HttpUtils.CodeAddVisit);
                int con = (int) ((Object[]) request)[2];
                ArrayList<Integer> arrayList = (ArrayList<Integer>) ((Object[]) request)[3];
                //清除刚刚的操作
                clearDisplay(arrayList);
                if (con == 2) {
                    //正常的全检操作,回退
                    if (null != arrayList && arrayList.size() > 0) {
                        curCheckId -= arrayList.size();
                    }
                }
                clearLineSeatMaterialScan();
                break;
        }
    }


    private void showLoading() {
        dismissLoading();
        loadingDialog = new LoadingDialog(getActivity(), "正在加载...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    private void dismissLoading() {
        //取消弹出窗
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.cancel();
            loadingDialog.dismiss();
        }
    }

}
