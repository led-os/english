package com.yc.english.group.contract;

import android.content.Context;

import com.yc.english.base.presenter.IPresenter;
import com.yc.english.base.view.IView;
import com.yc.english.group.model.bean.StudentTaskInfo;
import com.yc.english.group.model.bean.TaskInfo;

/**
 * Created by wanglin  on 2017/8/8 16:36.
 */

public interface GroupPublishTaskDetailContract {

    interface View extends IView {
        void showPublishTaskDetail(TaskInfo stringResultInfo);

        void showIsReadMemberList(StudentTaskInfo.ListBean data);
    }

    interface Presenter extends IPresenter {
        void getPublishTaskDetail(Context context, String id, String class_id, String user_id);

        void getIsReadTaskList(String class_id, String task_id);
    }
}
