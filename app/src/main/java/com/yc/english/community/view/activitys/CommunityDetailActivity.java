package com.yc.english.community.view.activitys;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hwangjr.rxbus.RxBus;
import com.jakewharton.rxbinding.view.RxView;
import com.yc.english.R;
import com.yc.english.base.helper.GlideHelper;
import com.yc.english.base.helper.TipsHelper;
import com.yc.english.base.view.FullScreenActivity;
import com.yc.english.community.contract.CommunityInfoContract;
import com.yc.english.community.model.domain.CommentInfo;
import com.yc.english.community.model.domain.CommunityInfo;
import com.yc.english.community.presenter.CommunityInfoPresenter;
import com.yc.english.community.view.adapter.CommentItemAdapter;
import com.yc.english.community.view.adapter.ImageDetailSelectedAdapter;
import com.yc.english.main.hepler.UserInfoHelper;
import com.yc.english.main.model.domain.Constant;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import rx.functions.Action1;

/**
 * Created by admin on 2017/9/1.
 */

public class CommunityDetailActivity extends FullScreenActivity<CommunityInfoPresenter> implements CommunityInfoContract.View {

    @BindView(R.id.iv_note_user_img)
    ImageView noteUserImageView;

    @BindView(R.id.tv_note_user_name)
    TextView mUserNameTextView;

    @BindView(R.id.tv_note_date)
    TextView mNoteDateTextView;

    @BindView(R.id.tv_note_title)
    TextView mNoteTitleTextView;

    @BindView(R.id.tv_comment_count)
    TextView mCommentCountTextView;

    @BindView(R.id.tv_praise_count)
    TextView mPraiseCountTextView;

    @BindView(R.id.et_comment_content)
    EditText mCommentContentEditText;

    @BindView(R.id.tv_send_comment)
    TextView mSendCommentTextView;

    @BindView(R.id.note_detail_image_list)
    RecyclerView mNoteDetailImagesRecyclerView;

    @BindView(R.id.comment_list)
    RecyclerView mCommentRecyclerView;

    ImageDetailSelectedAdapter mImageDetailSelectedAdapter;

    CommentItemAdapter mCommentItemAdapter;

    private CommunityInfo communityInfo;

    @BindView(R.id.mysv_content)
    ScrollView mContextScrollView;

    List<String> imageList;

    private int currentPage = 1;
    private int pageSize = 10;

    @Override
    public int getLayoutId() {
        return R.layout.community_note_detail;
    }

    @Override
    public void init() {

        Intent intent = getIntent();
        communityInfo = (CommunityInfo) intent.getSerializableExtra("community_info");

        mToolbar.setTitle(communityInfo.getcType().equals("1") ? "学友圈" : "英粉圈");
        mToolbar.showNavigationIcon();

        mPresenter = new CommunityInfoPresenter(this, this);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentItemAdapter = new CommentItemAdapter(this, null);
        mCommentRecyclerView.setAdapter(mCommentItemAdapter);

        if (communityInfo != null) {
            setPraiseStatus(communityInfo.getAgreed());
            imageList = communityInfo.getImages();
            GlideHelper.circleImageView(this, noteUserImageView, communityInfo.getFace(), R.mipmap.main_tab_my);
            mNoteDetailImagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            mImageDetailSelectedAdapter = new ImageDetailSelectedAdapter(this, imageList);
            mNoteDetailImagesRecyclerView.setAdapter(mImageDetailSelectedAdapter);

            mUserNameTextView.setText(communityInfo.getUserName());

            if (!StringUtils.isEmpty(communityInfo.getAddTime())) {
                long addTime = Long.parseLong(communityInfo.getAddTime()) * 1000;
                mNoteDateTextView.setText(TimeUtils.millis2String(addTime));
            }

            mNoteTitleTextView.setText(communityInfo.getContent());
            mCommentCountTextView.setText(communityInfo.getFollowCount());
            mPraiseCountTextView.setText(communityInfo.getAgreeCount());

            mPresenter.commentInfoList(Integer.parseInt(communityInfo.getId()), currentPage, 10);
        }

        RxView.clicks(mSendCommentTextView).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {

                if (StringUtils.isEmpty(mCommentContentEditText.getText())) {
                    TipsHelper.tips(CommunityDetailActivity.this, "请输入回复内容");
                    return;
                }

                if (UserInfoHelper.getUserInfo() == null) {
                    UserInfoHelper.isGotoLogin(CommunityDetailActivity.this);
                    return;
                }

                if (communityInfo != null) {
                    CommentInfo commentInfo = new CommentInfo();
                    commentInfo.setNoteId(communityInfo.getId());
                    commentInfo.setUserId(UserInfoHelper.getUserInfo() != null ? UserInfoHelper.getUserInfo().getUid() : "");
                    commentInfo.setContent(mCommentContentEditText.getText().toString());

                    mPresenter.addCommentInfo(commentInfo);
                }
            }
        });
        RxView.clicks(mPraiseCountTextView).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {

                if (UserInfoHelper.getUserInfo() == null) {
                    UserInfoHelper.isGotoLogin(CommunityDetailActivity.this);
                    return;
                }

                if (communityInfo != null) {
                    mPresenter.addAgreeInfo(UserInfoHelper.getUserInfo() != null ? UserInfoHelper.getUserInfo().getUid() : "", communityInfo.getId());
                } else {
                    TipsHelper.tips(CommunityDetailActivity.this, "数据异常");
                }
            }
        });

        mImageDetailSelectedAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (communityInfo != null) {
                    Intent intent = new Intent(CommunityDetailActivity.this, CommunityImageShowActivity.class);
                    intent.putExtra("current_position", position);
                    intent.putExtra("images", (Serializable) communityInfo.getImages());
                    startActivity(intent);
                } else {
                    TipsHelper.tips(CommunityDetailActivity.this, "数据异常");
                }
            }
        });


    }


    @Override
    public void hideStateView() {

    }

    @Override
    public void showNoNet() {

    }

    @Override
    public void showNoData() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showCommunityInfoListData(List<CommunityInfo> list) {

    }

    @Override
    public void showAddCommunityInfo(CommunityInfo communityInfo) {

    }

    @Override
    public void showCommentList(List<CommentInfo> list) {
        if (currentPage > 1) {
            mCommentItemAdapter.addData(list);
        } else {
            mCommentItemAdapter.setNewData(list);
        }

        if (list.size() == pageSize) {
            currentPage++;
            mCommentItemAdapter.loadMoreComplete();

        } else {
            if (currentPage > 1) {
                mCommentItemAdapter.loadMoreEnd();
            }
        }

    }

    @Override
    public void showAddComment(CommentInfo commentInfo) {
        mCommentContentEditText.setText("");
        if (UserInfoHelper.getUserInfo() != null) {
            commentInfo.setUserName(UserInfoHelper.getUserInfo().getNickname());
            commentInfo.setFace(UserInfoHelper.getUserInfo().getAvatar());
        }

        if (communityInfo != null && !StringUtils.isEmpty(communityInfo.getFollowCount())) {
            try {
                int resCount = Integer.parseInt(communityInfo.getFollowCount()) + 1;
                mCommentCountTextView.setText(resCount + "");
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        mCommentItemAdapter.addData(0, commentInfo);
        mCommentItemAdapter.notifyDataSetChanged();

        RxBus.get().post(Constant.COMMUNITY_REFRESH, "from add communityInfo");
    }

    @Override
    public void showAgreeInfo(boolean flag) {
        setPraiseStatus("1");

        if (communityInfo != null && !StringUtils.isEmpty(communityInfo.getAgreeCount())) {
            try {
                int resCount = Integer.parseInt(communityInfo.getAgreeCount()) + 1;
                mPraiseCountTextView.setText(resCount + "");
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        RxBus.get().post(Constant.COMMUNITY_REFRESH, "from add communityInfo");
    }

    public void setPraiseStatus(String type) {
        if (type != null && type.equals("1")) {
            Drawable isZan = ContextCompat.getDrawable(CommunityDetailActivity.this, R.mipmap.is_zan_icon);
            isZan.setBounds(0, 0, isZan.getMinimumWidth(), isZan.getMinimumHeight());
            mPraiseCountTextView.setCompoundDrawables(isZan, null, null, null);
        } else {
            Drawable noZan = ContextCompat.getDrawable(CommunityDetailActivity.this, R.mipmap.no_zan_icon);
            noZan.setBounds(0, 0, noZan.getMinimumWidth(), noZan.getMinimumHeight());
            mPraiseCountTextView.setCompoundDrawables(noZan, null, null, null);
        }
    }
}
