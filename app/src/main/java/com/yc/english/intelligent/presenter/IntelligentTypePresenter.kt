package com.yc.english.intelligent.presenter

import android.content.Context
import com.yc.english.base.model.BaseEngin
import com.yc.english.base.presenter.BasePresenter
import com.yc.english.base.view.IView

/**
 * Created by zhangkai on 2017/11/24.
 */
open class IntelligentTypePresenter(context: Context, v: IView?) : BasePresenter<BaseEngin, IView>(context, v) {
    override fun loadData(forceUpdate: Boolean, showLoadingUI: Boolean) {

    }
}