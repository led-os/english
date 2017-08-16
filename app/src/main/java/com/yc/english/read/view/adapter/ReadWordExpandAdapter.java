package com.yc.english.read.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yc.english.R;
import com.yc.english.read.model.domain.WordDetailInfo;
import com.yc.english.read.model.domain.WordInfo;

import java.util.List;

/**
 * Created by admin on 2017/8/16.
 */

public class ReadWordExpandAdapter extends BaseExpandableListAdapter {

    private Context mContext;

    private List<WordInfo> wordInfos;

    private List<WordDetailInfo> wordDetailInfos;

    public interface ItemViewClickListener {
        public void groupWordClick(int gPosition);
    }

    ItemViewClickListener mItemClick;

    public void setItemDetailClick(ItemViewClickListener itemClick) {
        this.mItemClick = itemClick;
    }

    public ReadWordExpandAdapter(Context context, List<WordInfo> wordInfos, List<WordDetailInfo> wordDetailInfos) {
        this.mContext = context;
        this.wordInfos = wordInfos;
        this.wordDetailInfos = wordDetailInfos;
    }

    public void setNewDatas(List<WordInfo> wordInfos, List<WordDetailInfo> wordDetailInfos) {
        this.wordInfos = wordInfos;
        this.wordDetailInfos = wordDetailInfos;
    }

    @Override
    public int getGroupCount() {
        return wordInfos != null ? wordInfos.size() : 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return wordInfos.get(groupPosition) != null ? wordInfos.get(groupPosition).getName() : "";
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return wordDetailInfos.get(groupPosition) != null ? wordDetailInfos.get(groupPosition).getWordExample() : "";
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = (LinearLayout) LinearLayout.inflate(mContext,
                R.layout.read_word_play_item, null);

        TextView wordNumberTv = (TextView) convertView.findViewById(R.id.tv_word_number);
        TextView wordTv = (TextView) convertView.findViewById(R.id.tv_en_word);
        TextView wordCnTv = (TextView) convertView.findViewById(R.id.tv_cn_word);
        LinearLayout readWordLayout = (LinearLayout) convertView.findViewById(R.id.layout_read_word_audio);

        wordNumberTv.setText((groupPosition + 1) + "");
        wordTv.setText(wordInfos.get(groupPosition).getName());
        wordCnTv.setText(wordInfos.get(groupPosition).getMeans());

        if (isExpanded) {
            convertView.setBackgroundResource(R.mipmap.read_word_item_selected);
        } else {
            convertView.setBackgroundResource(R.mipmap.read_word_item_normal);
        }

        readWordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClick.groupWordClick(groupPosition);
            }
        });

        return convertView;
    }

    public void setViewPlayState(int position, ListView listView, boolean isPlay) {
        int hCount = listView.getHeaderViewsCount();
        int fCount = listView.getFooterViewsCount();
        int fVisiblePos = listView.getFirstVisiblePosition() - hCount;
        int lVisiblePos = listView.getLastVisiblePosition() - fCount;

        View view = null;
        if (position >= fVisiblePos && position <= lVisiblePos) {
            view = (listView.getChildAt(position - fVisiblePos));
        }

        if (view != null) {
            if (isPlay) {
                if((ImageView) view.findViewById(R.id.iv_read_word) != null)
                Glide.with(mContext).load(R.mipmap.read_audio_gif_play).into((ImageView) view.findViewById(R.id.iv_read_word));
            } else {
                if((ImageView) view.findViewById(R.id.iv_read_word) != null)
                Glide.with(mContext).load(R.mipmap.read_word_default).into((ImageView) view.findViewById(R.id.iv_read_word));
            }
        }
    }

    public void setChildViewPlayState(View view, boolean isPlay) {

        if (view != null) {
            if (isPlay) {
                if((ImageView) view.findViewById(R.id.iv_word_detail_audio) != null)
                Glide.with(mContext).load(R.mipmap.read_audio_gif_play).into((ImageView) view.findViewById(R.id.iv_word_detail_audio));
            } else {
                if((ImageView) view.findViewById(R.id.iv_word_detail_audio) != null)
                Glide.with(mContext).load(R.mipmap.read_word_default).into((ImageView) view.findViewById(R.id.iv_word_detail_audio));
            }
        }
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View childConvertView, ViewGroup parent) {
        childConvertView = (LinearLayout) LinearLayout.inflate(mContext,
                R.layout.read_word_play_item_detail, null);
        TextView wordTv = (TextView) childConvertView.findViewById(R.id.tv_en_word_detail);
        wordTv.setText(wordDetailInfos.get(groupPosition).getWordExample());
        return childConvertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
