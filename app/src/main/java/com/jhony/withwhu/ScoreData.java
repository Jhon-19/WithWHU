package com.jhony.withwhu;

import org.litepal.crud.LitePalSupport;

public class ScoreData extends LitePalSupport {
    private int mId;
    private String mCourseName;//课程名
    private String mCourseType;//课程类型
    private String mCredit;//学分
    private String mTeacher;//教师名
    private String mSchool;//授课学院
    private String mStudyType;//学习类型
    private int mAcademicYear;//学年
    private int mAcademicTerm;//学期
    private String mScore;//成绩
    private String mGpa;//绩点

    public int getId() {
        return mId;
    }

    public String getCourseName() {
        return mCourseName;
    }

    public void setCourseName(String courseName) {
        mCourseName = courseName;
    }

    public String getCourseType() {
        return mCourseType;
    }

    public void setCourseType(String courseType) {
        mCourseType = courseType;
    }

    public String getCredit() {
        return mCredit;
    }

    public void setCredit(String credit) {
        mCredit = credit;
    }

    public String getTeacher() {
        return mTeacher;
    }

    public void setTeacher(String teacher) {
        mTeacher = teacher;
    }

    public String getSchool() {
        return mSchool;
    }

    public void setSchool(String school) {
        mSchool = school;
    }

    public String getStudyType() {
        return mStudyType;
    }

    public void setStudyType(String studyType) {
        mStudyType = studyType;
    }

    public int getAcademicYear() {
        return mAcademicYear;
    }

    public void setAcademicYear(int academicYear) {
        mAcademicYear = academicYear;
    }

    public int getAcademicTerm() {
        return mAcademicTerm;
    }

    public void setAcademicTerm(int academicTerm) {
        mAcademicTerm = academicTerm;
    }

    public String getScore() {
        return mScore;
    }

    public void setScore(String score) {
        mScore = score;
    }

    public String getGpa() {
        return mGpa;
    }

    public void setGpa(String gpa) {
        mGpa = gpa;
    }
}
