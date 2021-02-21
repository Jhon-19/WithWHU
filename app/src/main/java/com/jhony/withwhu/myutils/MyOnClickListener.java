package com.jhony.withwhu.myutils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jhony.withwhu.R;
import com.jhony.withwhu.ScoreData;

import java.util.List;

public class MyOnClickListener implements View.OnClickListener {
    private static final String TAG = "MyOnClickListener";

    private Activity mContext;
    private List<ScoreData> mScoreDataList;
    private List<View> mViewList;
    private LayoutInflater mInflater;

    public void setScoreDataList(List<ScoreData> scoreDataList) {
        mScoreDataList = scoreDataList;
    }

    public void setViewList(List<View> viewList) {
        mViewList = viewList;
    }

    public void setContext(Activity context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public void onClick(View v) {
        int index = mViewList.indexOf(v);
//        Log.i(TAG, index+"");
        showDialog(index);
    }

    //显示对话框信息
    private void showDialog(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View dialog = View.inflate(mContext, R.layout.dialog_score_info, null);
        LinearLayout scoreInfo = dialog.findViewById(R.id.score_info);

        for (int i = 0; i < 10; i++) {
            setItemInfo(scoreInfo, index, i);
        }
        builder.setTitle("详细信息")
                .setView(dialog)
                .create()
                .show();
    }

    //设置信息item
    private void setItemInfo(LinearLayout scoreInfo, int index, int i) {
        View item = mInflater.inflate(R.layout.dialog_item, scoreInfo, false);
        TextView itemTypeText = item.findViewById(R.id.item_type_text);
        TextView itemInfoText = item.findViewById(R.id.item_info_text);
        String itemType = null;
        String itemInfo = null;
        ScoreData data = mScoreDataList.get(index);
        switch (i) {
            case 0:
                itemType = "课程名";
                itemInfo = data.getCourseName();
                break;
            case 1:
                itemType = "课程类型";
                itemInfo = data.getCourseType();
                break;
            case 2:
                itemType = "学分";
                itemInfo = data.getCredit();
                break;
            case 3:
                itemType = "教师名";
                itemInfo = data.getTeacher();
                break;
            case 4:
                itemType = "授课学院";
                itemInfo = data.getSchool();
                break;
            case 5:
                itemType = "学习类型";
                itemInfo = data.getStudyType();
                break;
            case 6:
                itemType = "学年";
                itemInfo = String.valueOf(data.getAcademicYear());
                break;
            case 7:
                itemType = "学期";
                itemInfo = String.valueOf(data.getAcademicTerm());
                break;
            case 8:
                itemType = "成绩";
                itemInfo = data.getScore();
                if(itemInfo.equals("-1.0")){
                    itemInfo = "未出";
                }
                break;
            case 9:
                itemType = "绩点";
                itemInfo = data.getGpa();
                if(itemInfo.equals("-1.0")){
                    itemInfo = "未出";
                }
                break;
        }
        itemTypeText.setText(itemType);
        itemInfoText.setText(itemInfo);
        scoreInfo.addView(item);
    }
}
