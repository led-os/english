package com.yc.english.weixin.views.fragments;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.umeng.analytics.MobclickAgent;
import com.yc.english.R;
import com.yc.english.base.view.StateView;
import com.yc.english.news.view.activity.NewsDetailActivity;
import com.yc.english.weixin.contract.CourseContract;
import com.yc.english.weixin.model.domain.CourseInfo;
import com.yc.english.weixin.presenter.CoursePresenter;
import com.yc.english.weixin.views.adapters.CourseAdapter;

import java.util.List;

import butterknife.BindView;
import yc.com.base.BaseFragment;

/**
 * Created by admin on 2018/1/17.
 * 答案分类页
 */

public class AnswerFragment extends BaseFragment<CoursePresenter> implements CourseContract.View {
    @BindView(R.id.sv_loading)
    StateView mLoadingStateView;

    @BindView(R.id.rv_course)
    RecyclerView mCourseRecyclerView;

    private CourseAdapter mCourseAdapter;
    private String type = "";
    private int page = 1;
    private int pageSize = 20;

    @Override
    public void init() {
        mPresenter = new CoursePresenter(getActivity(), AnswerFragment.this);
        /*Intent intent = getIntent();
        if (intent != null) {
            mToolbar.setTitle(intent.getStringExtra("title") + "");
            type = intent.getStringExtra("type") + "";
        }*/

        mCourseRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCourseAdapter = new CourseAdapter(null);
        mCourseRecyclerView.setAdapter(mCourseAdapter);

        mCourseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                MobclickAgent.onEvent(getActivity(), "toady_hot_click", "今日热点");
                Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
                intent.putExtra("info", mCourseAdapter.getData().get(position));
                startActivity(intent);
            }
        });

        mCourseAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mPresenter.getWeiXinList(type, page , pageSize );
            }
        }, mCourseRecyclerView);

        mPresenter.getWeiXinList(type, page, pageSize );
    }

    @Override
    public int getLayoutId() {
        return R.layout.weixin_activity_course;
    }

    @Override
    public void hide() {
        mLoadingStateView.hide();
    }

    @Override
    public void showNoNet() {
        mLoadingStateView.showNoNet(mCourseRecyclerView, "网络不给力", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.getWeiXinList(type, page, pageSize );
            }
        });
    }

    @Override
    public void showNoData() {
        mLoadingStateView.showNoData(mCourseRecyclerView);
    }

    @Override
    public void showLoading() {
        mLoadingStateView.showLoading(mCourseRecyclerView);
    }

    @Override
    public void showWeixinList(List<CourseInfo> list) {
        if (list == null) {
            mCourseAdapter.loadMoreEnd();
            return;
        }

        if (page == 1) {
            mCourseAdapter.setNewData(list);
        } else {
            mCourseAdapter.addData(list);
        }

        if (list.size() == pageSize) {
            page++;
            mCourseAdapter.loadMoreComplete();
        } else {
            mCourseAdapter.loadMoreEnd();
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void fail() {
        mCourseAdapter.loadMoreFail();
    }

    @Override
    public void end() {
        mCourseAdapter.loadMoreEnd();
    }
}
