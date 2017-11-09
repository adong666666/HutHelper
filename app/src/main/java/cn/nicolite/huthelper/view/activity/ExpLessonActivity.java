package cn.nicolite.huthelper.view.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nicolite.huthelper.R;
import cn.nicolite.huthelper.base.activity.BaseActivity;
import cn.nicolite.huthelper.model.bean.Configure;
import cn.nicolite.huthelper.model.bean.ExpLesson;
import cn.nicolite.huthelper.presenter.ExplessonPresenter;
import cn.nicolite.huthelper.utils.DateUtiils;
import cn.nicolite.huthelper.utils.ListUtils;
import cn.nicolite.huthelper.utils.SnackbarUtils;
import cn.nicolite.huthelper.view.adapter.TabAdapter;
import cn.nicolite.huthelper.view.fragment.ExpLessonFragment;
import cn.nicolite.huthelper.view.iview.IExplessonView;
import cn.nicolite.huthelper.view.widget.LoadingDialog;

/**
 * 实验课表部分
 * Created by nicolite on 17-11-4.
 */

public class ExpLessonActivity extends BaseActivity implements IExplessonView{
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.rootView)
    LinearLayout rootView;
    private ExplessonPresenter explessonPresenter;
    private LoadingDialog loadingDialog;
    private TabAdapter adapter;
    private ExpLessonFragment expLessonFragmentUnfinish;
    private ExpLessonFragment expLessonFragmentFinish;

    @Override
    protected void initConfig(Bundle savedInstanceState) {
        setImmersiveStatusBar(true);
        setDeepColorStatusBar(true);
        setSlideExit(true);
    }

    @Override
    protected void initBundleData(Bundle bundle) {

    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_explesson;
    }

    @Override
    protected void doBusiness() {
        toolbarTitle.setText("实验课表");
        adapter = new TabAdapter(getSupportFragmentManager(), getTitleList(), getFragmentList());
        viewpager.setAdapter(adapter);
        tab.setupWithViewPager(viewpager);
        explessonPresenter = new ExplessonPresenter(this, this);
        explessonPresenter.showExplesson(false);
    }

    @OnClick({R.id.toolbar_back, R.id.toolbar_menu})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.toolbar_back:
                finish();
                break;
            case R.id.toolbar_menu:
                explessonPresenter.showExplesson(true);
                break;
        }
    }

    private List<Fragment> getFragmentList(){
        List<Fragment> fragmentList = new ArrayList<>();

        if (expLessonFragmentUnfinish == null){
            expLessonFragmentUnfinish = ExpLessonFragment.newInstance(ExpLessonFragment.FINISHED);
        }

        if (expLessonFragmentFinish == null){
            expLessonFragmentFinish = ExpLessonFragment.newInstance(ExpLessonFragment.FINISHED);
        }

        fragmentList.add(expLessonFragmentUnfinish);
        fragmentList.add(expLessonFragmentFinish);

        return fragmentList;
    }

    private List<String> getTitleList(){
        List<String> titleList = new ArrayList<>();
        titleList.add("未完成");
        titleList.add("已完成");
        return titleList;
    }

    @Override
    public void showLoading() {
        loadingDialog = new LoadingDialog(context)
                .setLoadingText("查询中...");
        loadingDialog.show();
    }

    @Override
    public void closeLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void showMessage(String msg) {
        SnackbarUtils.showShortSnackbar(rootView, msg);
    }

    @Override
    public void showExpLesson(List<ExpLesson> expLessonList) {

        Collections.sort(expLessonList, new Comparator<ExpLesson>() {
            @Override
            public int compare(ExpLesson expLesson, ExpLesson t1) {
                return Integer.parseInt(expLesson.getWeeks_no()) - Integer.parseInt(t1.getWeeks_no());
            }
        });

        String userId = getLoginUser();

        if (TextUtils.isEmpty(userId)){
            showMessage("获取用户信息失败！");
            return;
        }

        List<Configure> configureList = getConfigureList();

        if (ListUtils.isEmpty(configureList)){
            showMessage("获取用户信息失败！");
            return;
        }

        Configure configure = configureList.get(0);

        int nowWeek = DateUtiils.getNowWeek(configure.getNewTermDate());

        List<ExpLesson> unfinish = new ArrayList<>();
        List<ExpLesson> finish = new ArrayList<>();

        for (ExpLesson explesson : expLessonList) {
            if (Integer.parseInt(explesson.getWeeks_no()) > nowWeek){
                unfinish.add(explesson);
            }else {
                finish.add(explesson);
            }
        }

        expLessonFragmentUnfinish.updateDate(unfinish);
        expLessonFragmentFinish.updateDate(finish);
    }
}