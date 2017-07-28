package com.yc.english.read.view.activitys;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yc.english.R;
import com.yc.english.base.view.FullScreenActivity;
import com.yc.english.read.model.domain.UnitInfo;
import com.yc.english.read.view.adapter.ReadBookUnitItemClickAdapter;
import com.yc.english.read.view.wdigets.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by admin on 2017/7/25.
 */

public class BookUnitActivity extends FullScreenActivity {

    @BindView(R.id.rv_book_unit_list)
    RecyclerView mBookUnitRecyclerView;

    ReadBookUnitItemClickAdapter mItemAdapter;

    private List<UnitInfo> mBookDatas;

    @Override
    public int getLayoutId() {
        return R.layout.read_activity_book_unit;
    }

    @Override
    public void init() {
        initData();

        mToolbar.setTitle(getString(R.string.read_book_unit_text));
        mToolbar.showNavigationIcon();

        mBookUnitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mItemAdapter = new ReadBookUnitItemClickAdapter(this, mBookDatas);
        mBookUnitRecyclerView.setAdapter(mItemAdapter);
        mBookUnitRecyclerView.addItemDecoration(new SpaceItemDecoration(SizeUtils.dp2px(10)));
        mItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                LogUtils.e("position --->" + position);
                Intent intent = new Intent(BookUnitActivity.this, CoursePlayActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 测试数据
     */
    public void initData() {
        mBookDatas = new ArrayList<UnitInfo>();
        for (int i = 0; i < 6; i++) {
            UnitInfo unitInfo = new UnitInfo(UnitInfo.CLICK_ITEM_VIEW);
            unitInfo.setUnitTitle("Unit 1 Hello,Good Morning");
            unitInfo.setUnitTotal((i + 1) * 2 + "句");
            mBookDatas.add(unitInfo);
        }
    }

}