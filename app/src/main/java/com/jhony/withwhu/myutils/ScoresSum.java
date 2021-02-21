package com.jhony.withwhu.myutils;

import java.util.Locale;

public class ScoresSum {
    //单个学年学期汇总
    private float mTotalCredits;
    private float mTotalRequiredCredits;
    private float mTotalRequiredGPA;
    private float mTotalGPA;
    private float mTotalScore;
    //全部学年学期汇总
    private float mHeaderTotalCredits;
    private float mHeaderTotalRequiredCredits;
    private float mHeaderTotalRequiredGPA;
    private float mHeaderTotalGPA;
    private float mHeaderTotalScore;
    //未出分课程处理
    private float mNoneScoreCredit;
    private float mHeaderNoneScoreCredit;

    private ScoresSum() {
        //初始化各值
        resetTermScoreInfo();
        resetAllScoreInfo();
    }

    //重置各学期成绩信息
    private void resetTermScoreInfo(){
        mTotalCredits = 0;
        mTotalRequiredCredits = 0;
        mTotalRequiredGPA = 0;
        mTotalGPA = 0;
        mTotalScore = 0;
        mNoneScoreCredit = 0;
    }

    //重置全部学期成绩信息
    private void resetAllScoreInfo(){
        mHeaderTotalCredits = 0;
        mHeaderTotalRequiredCredits = 0;
        mHeaderTotalRequiredGPA = 0;
        mHeaderTotalGPA = 0;
        mHeaderTotalScore = 0;
        mHeaderNoneScoreCredit = 0;
    }

    public static ScoresSum getInstance() {
        return new ScoresSum();
    }

    //添加成绩信息
    public void addScoreInfo(String creditStr, String requiredGPAStr,
                             String GPAStr, String scoreStr) {
        float credit = Float.parseFloat(creditStr);
        float requiredGPA = Float.parseFloat(requiredGPAStr);
        float GPA = Float.parseFloat(GPAStr);
        float score = Float.parseFloat(scoreStr);
        if(score == -1.0){
            mNoneScoreCredit += credit;
            mHeaderNoneScoreCredit += credit;
        }else{
            mTotalCredits += credit;
            if(requiredGPA != 0){
                mTotalRequiredCredits += credit;
                mHeaderTotalRequiredCredits += credit;
            }
            mTotalRequiredGPA += requiredGPA*credit;
            mTotalGPA += GPA*credit;
            mTotalScore += score*credit;
            mHeaderTotalCredits += credit;
            mHeaderTotalRequiredGPA += requiredGPA*credit;
            mHeaderTotalGPA += GPA*credit;
            mHeaderTotalScore += score*credit;
        }
    }

    //输出各学期成绩信息, 总学分, 必修GPA, 总GPA, 总学分, 未出分学分, 必修学分
    public String[] getTermScoreInfo() {
        String[] termScoreInfo = new String[6];
        termScoreInfo[0] = String.format(Locale.getDefault(),
                "%.1f", mTotalCredits+mNoneScoreCredit);
        termScoreInfo[1] = String.format(Locale.getDefault(),
                "%.2f", mTotalRequiredGPA/mTotalRequiredCredits);
        termScoreInfo[2] = String.format(Locale.getDefault(),
                "%.2f", mTotalGPA/mTotalCredits);
        termScoreInfo[3] = String.format(Locale.getDefault(),
                "%.2f", mTotalScore/mTotalCredits);
        termScoreInfo[4] = String.format(Locale.getDefault(),
                "%.1f", mNoneScoreCredit);
        termScoreInfo[4] = String.format(Locale.getDefault(),
                "%.1f", mTotalRequiredCredits);
        resetTermScoreInfo();
        return termScoreInfo;
    }

    //输出全部学期成绩信息, 总学分, 必修GPA, 总GPA, 总学分, 未出分学分
    public String[] getAllTermScoreInfo(){
        String[] allTermScoreInfo = new String[6];
        allTermScoreInfo[0] = String.format(Locale.getDefault(),
                "%.1f", mHeaderTotalCredits+mHeaderNoneScoreCredit);
        allTermScoreInfo[1] = String.format(Locale.getDefault(),
                "%.2f", mHeaderTotalRequiredGPA/mHeaderTotalRequiredCredits);
        allTermScoreInfo[2] = String.format(Locale.getDefault(),
                "%.2f", mHeaderTotalGPA/mHeaderTotalCredits);
        allTermScoreInfo[3] = String.format(Locale.getDefault(),
                "%.2f", mHeaderTotalScore/mHeaderTotalCredits);
        allTermScoreInfo[4] = String.format(Locale.getDefault(),
                "%.1f", mHeaderNoneScoreCredit);
        allTermScoreInfo[4] = String.format(Locale.getDefault(),
                "%.1f", mHeaderTotalRequiredCredits);
        resetAllScoreInfo();
        return allTermScoreInfo;
    }
}
