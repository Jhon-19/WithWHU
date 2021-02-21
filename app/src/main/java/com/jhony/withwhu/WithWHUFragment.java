package com.jhony.withwhu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jhony.withwhu.myutils.MyOnClickListener;
import com.jhony.withwhu.myutils.MyPreferences;
import com.jhony.withwhu.myutils.ScoresSum;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WithWHUFragment extends Fragment {
    private static final String TAG = "WithWHUFragment";
    private Activity mContext;
    private Resources mResources;

    //各组件
    private Button mQueryBtn;
    private LinearLayout mScores;
    private MyOnClickListener mOnClickListener;
    private EditText mUserText;
    private EditText mPasswordText;

    private String mUser;
    private String mPassword;

    //教务系统cookie
    private String mBkjwCookie;
    private CookieManager mCookieManager;
    private String mCsrftoken;

    private WebView mWebView;
    private OkHttpClient mClient;
    private Headers mHeaders;

    //允许查询标志位
    private boolean canQuery;
    //拥有用户信息标志位
    private boolean hasUserInfo;

    //成绩信息的列表
    private List<ScoreData> mScoreDataList;
    private final int[] mDataIndex = {0, 1, 4, 5, 6, 7, 8, 9, 10};
    private LayoutInflater mInflater;

    //成绩项列表
    private List<View> mViewList;

    //请求头
    private final String mAgent = "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/88.0.4324.104 Mobile Safari/537.36";
    //统一认证登录url
    private final String mLoginUrl = "https://cas.whu.edu.cn/authserver/login?" +
            "service=http%3A%2F%2Fehall.whu.edu.cn" +
            "%2Flogin%3Fservice%3Dhttp%3A%2F%2Fehall.whu.edu.cn%2Fnew%2Findex.html";
    //信息门户首页url
    private final String mEhallUrl = "http://ehall.whu.edu.cn/new/mobile/index.html";
    //教务系统首页url
    private final String mBkjwUrl = "http://bkjw.whu.edu.cn/stu/stu_index.jsp";
    //查询成绩的url
    private final String mScoresUrl = "http://bkjw.whu.edu.cn/servlet/Svlt_QueryStuScore";

    //动态导入jQuery库
    private final String mLoadJQuery = "var jquery = document.createElement('script');" +
            "jquery.src = 'https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js';" +
            "document.getElementsByTagName('head')[0].appendChild(jquery);";
    //统一认证登录的js脚本
    private String mLoginScript = "$('#mobileUsername').prop('value', '$mUser$');" +
            "$('#mobilePassword').prop('value', '$mPassword$');" +
            "$('#load').click();";
    //注入jQuery并登录教务系统的js脚本
    private final String mGotoBkjwScript = "$('.yyzx-item').each(function(){" +
            "if($(this).children('div').text().trim() === '本科教务管理系统'){" +
//            "alert('ehall');" +
            "$(this).click();" +
            "return false;" +
            "}" +
            "});";
    //获取csrftoken的js脚本
    private final String mGetCsrftoken = "$('#top').html().trim()";

    public static Fragment newInstance() {
        return new WithWHUFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_with_whu, container, false);

        //获取上下文
        mContext = getActivity();
        if (mContext != null) {
            mInflater = LayoutInflater.from(mContext);//初始化布局inflater
            LitePal.initialize(mContext);//初始化数据库
            mResources = mContext.getResources();//初始化资源管理器
        }
        mCookieManager = CookieManager.getInstance();
        mClient = new OkHttpClient();
        initHeaders();

        //清除上次保留的cookie
        mCookieManager.removeAllCookies(null);

        //初始化各组件
        mQueryBtn = view.findViewById(R.id.query_btn);
        initQueryBtn();
        mWebView = view.findViewById(R.id.web_view);
        mScores = view.findViewById(R.id.scores);
        mUserText = view.findViewById(R.id.user_text);
        mPasswordText = view.findViewById(R.id.password_text);
        initInputText();
        tryLogin();

        return view;
    }

    //设置EditText并判断账号密码是否为空
    private void initInputText() {
        mUser = MyPreferences.getUser(mContext);
        mPassword = MyPreferences.getPassword(mContext);
        mUserText.setText(mUser);
        mPasswordText.setText(mPassword);
        hasUserInfo = mUserText != null && mPassword != null;
    }

    //程序启动后尝试登录教务系统
    private void tryLogin() {
        canQuery = false;
        //账号密码不为空则尝试登录
        if (hasUserInfo) {
            mQueryBtn.setClickable(false);
            mQueryBtn.setBackgroundColor(mResources.getColor(R.color.gray_blue, null));
            initWebView();
        }
        if (MyPreferences.getHasDatas(mContext)) {//有成绩信息则先显示
            initScores();
        }
    }

    //初始化成绩列表
    private void initScores() {
        getScoreData();
        //清除原有信息
        mScores.removeAllViews();

        //初始化监听器
        mViewList = new ArrayList<>();
        mOnClickListener = new MyOnClickListener();
        mOnClickListener.setContext(mContext);
        mOnClickListener.setScoreDataList(mScoreDataList);

        //记录当前学年学期
        YearAndTerm current = new YearAndTerm();
        current.setYear(mScoreDataList.get(0).getAcademicYear());
        current.setTerm(mScoreDataList.get(0).getAcademicTerm());

        int year;
        int term;
        ScoreData data;
        ScoresSum scoresSum = ScoresSum.getInstance();
        for (int dataIndex = 0; dataIndex < mScoreDataList.size(); dataIndex++) {
            data = mScoreDataList.get(dataIndex);//mScoreList升序排列, 采用头插法
            year = data.getAcademicYear();
            term = data.getAcademicTerm();
            if (year == current.getYear()) {//每换一次学期添加一个header
                if (term != current.getTerm()) {
                    String[] info = scoresSum.getTermScoreInfo();
                    setScoresHeaders(current.getYear() + "学年",
                            "第" + current.getTerm() + "学期",
                            info[0], info[1], info[2], info[3]);
                    current.setTerm(term);
                }
            } else {
                String[] info = scoresSum.getTermScoreInfo();
                setScoresHeaders(current.getYear() + "学年",
                        "第" + current.getTerm() + "学期",
                        info[0], info[1], info[2], info[3]);
                current.setYear(year);
                current.setTerm(term);
            }
            setScoresItem(data.getCourseName(), data.getCourseType(),
                    data.getCredit(), data.getScore());
            if (data.getCourseType().contains("必修")) {
                scoresSum.addScoreInfo(data.getCredit(), data.getGpa(), data.getGpa(), data.getScore());
            } else {
                scoresSum.addScoreInfo(data.getCredit(), String.valueOf(0), data.getGpa(), data.getScore());
            }
        }
        mOnClickListener.setViewList(mViewList);
        //最后一个学期的header需要手动添加
        String[] info = scoresSum.getTermScoreInfo();
        setScoresHeaders(current.getYear() + "学年",
                "第" + current.getTerm() + "学期",
                info[0], info[1], info[2], info[3]);
        //添加全部学期的header
        info = scoresSum.getAllTermScoreInfo();
        setScoresHeaders("全部学年", "全部学期",
                info[0], info[1], info[2], info[3]);
    }

    //设置成绩列表表头, index == -1 表示加在末尾, index == 0 表示加在最前面
    private void setScoresHeaders(String year, String term, String termCredits,
                                  String requiredGPA, String totalGPA,
                                  String meanScore) {
        //表头的6个TextView
        TextView yearText;
        TextView termText;
        TextView termCreditsText;
        TextView requiredGPAText;
        TextView totalGPAText;
        TextView meanScoreText;
        //初始化表头
        View header = mInflater.inflate(R.layout.score_header, mScores, false);
        yearText = header.findViewById(R.id.academic_year_text);
        termText = header.findViewById(R.id.academic_term_text);
        termCreditsText = header.findViewById(R.id.term_credits_text);
        requiredGPAText = header.findViewById(R.id.term_required_gpa);
        totalGPAText = header.findViewById(R.id.term_total_gpa);
        meanScoreText = header.findViewById(R.id.term_mean_score_text);
        yearText.setText(year);
        termText.setText(term);
        termCreditsText.setText(mResources.getString(R.string.total_credit_prefix, termCredits));
        requiredGPAText.setText(mResources.getString(R.string.required_gpa_prefix, requiredGPA));
        totalGPAText.setText(mResources.getString(R.string.totla_gpa_prefix, totalGPA));
        meanScoreText.setText(mResources.getString(R.string.mean_score_prefix, meanScore));
        mScores.addView(header, 0);
    }

    //设置成绩列表表项
    private void setScoresItem(String courseName, String courseType,
                               String credit, String score) {
        //记录成绩信息的4个TextView
        TextView courseNameText;
        TextView courseTypeText;
        TextView creditText;
        TextView scoreText;
        //初始化成绩项
        View item = mInflater.inflate(R.layout.score_item, mScores, false);
        item.setOnClickListener(mOnClickListener);
        mViewList.add(item);
        courseNameText = item.findViewById(R.id.course_name_text);
        courseTypeText = item.findViewById(R.id.course_type_text);
        creditText = item.findViewById(R.id.credit_text);
        scoreText = item.findViewById(R.id.score_text);
        courseNameText.setText(courseName);
        courseTypeText.setText(courseType);
        creditText.setText(mResources.getString(R.string.credit_suffix, credit));
        if (score.equals("-1.0")) {
            score = "未出";
        }
        scoreText.setText(mResources.getString(R.string.score_suffix, score));
        mScores.addView(item, 0);
    }

    //初始化请求头
    private void initHeaders() {
        mHeaders = new Headers.Builder()
                .add("Host: bkjw.whu.edu.cn")
                .add("User-Agent", mAgent)
                .add("Referer: http://bkjw.whu.edu.cn/stu/stu_score_parent.jsp?index=0")
                .build();
    }

    //添加cookie
    private void addCookie() {
        if (mHeaders.get("Cookie") != null) {
            mHeaders = mHeaders.newBuilder()
                    .set("Cookie", mBkjwCookie)//存在则直接修改
                    .build();
        } else {
            mHeaders = mHeaders.newBuilder()
                    .add("Cookie", mBkjwCookie)
                    .build();
        }
    }

    //js设置延时
    private String setTimeout(String script, int time) {
        return "setTimeout(function(){" + script + "}, " + time + ");";
    }

    //初始化查询按钮
    private void initQueryBtn() {
        mQueryBtn.setOnClickListener(v -> {
            canQuery = true;
            if (hasUserInfo) {
                mWebView.evaluateJavascript(
                        mGetCsrftoken, value -> {
                            getCsrftoken(value);
                            mBkjwCookie = mCookieManager.getCookie(mBkjwUrl);
                            getScores();
                        });
            } else {
                //保存用户信息并登录
                mUser = mUserText.getText().toString();
                mPassword = mPasswordText.getText().toString();
                MyPreferences.setUser(mContext, mUser);
                MyPreferences.setPassword(mContext, mPassword);
                initWebView();
            }
        });
    }

    //处理返回值获取csrftoken
    private void getCsrftoken(String value) {
        String regex = "(csrftoken=)(.*)(','calendarRight.jsp')";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            mCsrftoken = matcher.group(2);
            if (mCsrftoken != null) {
                Log.i(TAG, mCsrftoken);
            }
        } else {
            Log.i(TAG, "not found");
        }
    }

    //初始化WebView
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains(mEhallUrl)) {
//                    Log.i(TAG, "ehall");
                    mWebView.evaluateJavascript(mLoadJQuery, null);
                    mWebView.evaluateJavascript(setTimeout(mGotoBkjwScript, 2000), null);
                } else if (url.equals(mBkjwUrl)) {
//                    Log.i(TAG, "bkjw");
                    if (canQuery) {
                        mWebView.evaluateJavascript(
                                mGetCsrftoken, value -> {
//                                Log.i(TAG, value);
                                    getCsrftoken(value);
                                    //得到csrftoken再查询
                                    mBkjwCookie = mCookieManager.getCookie(url);
//                    Log.i(TAG, mBkjwCookie);
                                    getScores();
                                });
                    } else {
                        mQueryBtn.setClickable(true);
                        mQueryBtn.setBackgroundColor(
                                mResources.getColor(R.color.btn_color, null));
                    }
                } else if (url.equals(mLoginUrl)) {
                    mWebView.evaluateJavascript(mLoadJQuery, null);
                    mLoginScript = mLoginScript.replace("$mUser$", mUser)
                            .replace("$mPassword$", mPassword);
                    mWebView.evaluateJavascript(setTimeout(mLoginScript, 1000), null);
                }
                super.onPageFinished(view, url);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                result.confirm();
                return true;
            }
        });

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        mWebView.loadUrl(mLoginUrl);
    }

    //展示信息
    private void showToast(String message) {
        mContext.runOnUiThread(
                () -> Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show());
    }

    private void getScores() {
        addCookie();
        //产生带参数url
        Uri.Builder builder = Uri.parse(mScoresUrl).buildUpon();
        builder.appendQueryParameter("csrftoken", mCsrftoken)
                .appendQueryParameter("year", "0")
                .appendQueryParameter("term", "")//指定null参数会被填为null
                .appendQueryParameter("learnType", "")
                .appendQueryParameter("scoreFlag", "0");
        String scoreUrl = builder.build().toString();
//        Log.i(TAG, scoreUrl);
        Request request = new Request.Builder()
                .headers(mHeaders)
                .url(scoreUrl)
                .get()
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                showToast("网络故障...");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();
                if (TextUtils.isEmpty(result)) {
                    showToast("查询失败, 请稍后重试...");
                } else {
                    showToast("查询成功!");
                }
//                Log.i(TAG, result);
//                System.out.println(result);
                Document document = Jsoup.parse(result);
                Elements scoresList = document.select(".table.listTable");
//                Log.i(TAG, scoresList.html());
                Elements scoresRows = scoresList.select("tr");
//                Log.i(TAG, scoresRows.html());
                saveScoreData(scoresRows);
                canQuery = false;//查询完后禁止查询
                mContext.runOnUiThread(() -> initScores());
            }
        });
    }

    //保存查询到的信息
    private void saveScoreData(Elements scoresRows) {
        for (int i = 1; i < scoresRows.size(); i++) {
            Elements datas = scoresRows.get(i).select("td");
            ScoreData scoreData = new ScoreData();
//                    Element data = datas.get(mDataIndex[0]);
//                        Log.i(TAG, data.text());
            //保存数据
            scoreData.setCourseName(datas.get(mDataIndex[0]).text().trim());
            scoreData.setCourseType(datas.get(mDataIndex[1]).text().trim());
            scoreData.setCredit(datas.get(mDataIndex[2]).text().trim());
            scoreData.setTeacher(datas.get(mDataIndex[3]).text().trim());
            scoreData.setSchool(datas.get(mDataIndex[4]).text().trim());
            scoreData.setStudyType(datas.get(mDataIndex[5]).text().trim());
            scoreData.setAcademicYear(Integer.parseInt(datas.get(mDataIndex[6]).text().trim()));
            scoreData.setAcademicTerm(Integer.parseInt(datas.get(mDataIndex[7]).text().trim()));
            String scoreStr = datas.get(mDataIndex[8]).text().trim();
            if (TextUtils.isEmpty(scoreStr)) {
                scoreStr = "-1.0";
            }
            scoreData.setScore(scoreStr);
            //计算GPA
            scoreData.setGpa(calculateGPA(scoreStr));
            //根据学年，学期，课程名，学习类型来判断是否为同一门课
            scoreData.saveOrUpdate(
                    "mAcademicYear = ? and " +
                            "mAcademicTerm = ? and " +
                            "mCourseName = ? and " +
                            "mStudyType = ?",
                    String.valueOf(scoreData.getAcademicYear()),
                    String.valueOf(scoreData.getAcademicTerm()),
                    scoreData.getCourseName(),
                    scoreData.getStudyType());
        }
        //设置标志位为有数据
        MyPreferences.setHasDatas(mContext, true);
    }

    //读取成绩信息并排序
    private void getScoreData() {
        mScoreDataList = LitePal.findAll(ScoreData.class);
        sortByTerm();
//        Log.i(TAG, mScoreDataList.size()+"");
//        for (ScoreData data : mScoreDataList){
//            Log.i(TAG, data.getAcademicYear()+" "+data.getAcademicTerm());
//        }
    }

    //根据学年学期排序, 升序排列
    private void sortByTerm() {
        Collections.sort(mScoreDataList, (o1, o2) -> {
            int flag = 0;//0不交换, 1交换, -1不交换
            if (o1.getAcademicYear() < o2.getAcademicYear()) {
                flag = -1;
            } else if (o1.getAcademicYear() > o2.getAcademicYear()) {
                flag = 1;
            } else if (o1.getAcademicYear() == o2.getAcademicYear()) {
                flag = -Integer.compare(o2.getAcademicTerm(), o1.getAcademicTerm());//默认降序
            }
            return flag;
        });
    }

    //计算每门课GPA
    private String calculateGPA(String scoreStr) {
        String GPA = null;
        float score = Float.parseFloat(scoreStr);
        if (score == -1.0) {//表示未出成绩
            GPA = "-1.0";
        } else {
            if (score >= 90) {
                GPA = "4.0";
            } else if (score >= 85 && score <= 89) {
                GPA = "3.7";
            } else if (score >= 82 && score <= 84) {
                GPA = "3.3";
            } else if (score >= 78 && score <= 81) {
                GPA = "3.0";
            } else if (score >= 75 && score <= 77) {
                GPA = "2.7";
            } else if (score >= 72 && score <= 74) {
                GPA = "2.3";
            } else if (score >= 68 && score <= 71) {
                GPA = "2.0";
            } else if (score >= 64 && score <= 67) {
                GPA = "1.5";
            } else if (score >= 60 && score <= 63) {
                GPA = "1.0";
            } else if (score < 60) {
                GPA = "0.0";
            }
        }
        return GPA;
    }
}
