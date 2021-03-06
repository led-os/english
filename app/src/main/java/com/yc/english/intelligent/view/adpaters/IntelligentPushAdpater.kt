package com.yc.english.intelligent.view.adpaters

import android.support.v4.content.ContextCompat
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.yc.english.R
import com.yc.english.intelligent.model.domain.UnitInfoWrapper
import com.yc.english.intelligent.utils.fromHtml
import com.yc.english.intelligent.view.wdigets.IntelligentPushQuestionView
import yc.com.blankj.utilcode.util.SizeUtils


/**
 * Created by zhangkai on 2017/11/29.
 */

class IntelligentPushAdpater : BaseQuickAdapter<UnitInfoWrapper.ComleteItemInfo, BaseViewHolder>(R.layout.intelligent_item_push) {

    override fun convert(helper: BaseViewHolder?, item: UnitInfoWrapper.ComleteItemInfo?) {
        val questionView = helper?.getView<IntelligentPushQuestionView>(R.id.mIntelligentPushType1)

        questionView?.mIconImageView?.setImageDrawable(ContextCompat.getDrawable(mContext, getResourceId
        ("intelligent_push_type${item?.value}", "mipmap")))
        questionView?.mTitleTextView?.text = mContext.getString(getResourceId
        ("intelligents_type${item?.value}", "string"))
        if (item?.isComplete == 1) {
            questionView?.mTitleTextView?.text = fromHtml("${questionView?.mTitleTextView?.text}(<font " +
                    "color='#FB4C30'>已完成</font>)")
        }

        helper?.let {
            if (helper.adapterPosition % 2 == 0) {
                (questionView?.layoutParams as LinearLayout.LayoutParams).leftMargin = SizeUtils.dp2px(10f)
                (questionView.layoutParams as LinearLayout.LayoutParams).rightMargin = SizeUtils.dp2px(5f)

            } else {
                (questionView?.layoutParams as LinearLayout.LayoutParams).leftMargin = SizeUtils.dp2px(5f)
                (questionView.layoutParams as LinearLayout.LayoutParams).rightMargin = SizeUtils.dp2px(10f)
            }
        }

    }

    private fun getResourceId(resourceName: String, type: String): Int {
        val packageName = mContext.packageName
        return mContext.resources.getIdentifier(resourceName, type, packageName)
    }
}