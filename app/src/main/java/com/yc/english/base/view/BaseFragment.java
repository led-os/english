package com.yc.english.base.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.EmptyUtils;
import com.blankj.utilcode.util.LogUtils;
import com.hwangjr.rxbus.RxBus;
import com.umeng.analytics.MobclickAgent;
import com.yc.english.base.presenter.BasePresenter;

import butterknife.ButterKnife;

/**
 * Created by zhangkai on 2017/7/21.
 */

public abstract class BaseFragment<P extends BasePresenter> extends Fragment implements IView {
    protected View mRootView;
    protected P mPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RxBus.get().register(this);
        if (mRootView == null) {
            mRootView = View.inflate(getActivity(), getLayoutID(), null);
            try {
                ButterKnife.bind(this,mRootView);
                init();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.i(this.getClass().getSimpleName() + " init->初始化失败 原因:" + e);
            }
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
        if(EmptyUtils.isNotEmpty(mPresenter))
             mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
        if(EmptyUtils.isNotEmpty(mPresenter))
             mPresenter.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }
}
