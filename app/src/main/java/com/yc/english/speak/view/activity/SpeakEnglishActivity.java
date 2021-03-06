package com.yc.english.speak.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.kk.utils.LogUtil;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.yc.english.R;
import com.yc.english.base.view.FullScreenActivity;
import com.yc.english.base.view.StateView;
import com.yc.english.read.common.AudioPlayManager;
import com.yc.english.read.common.MediaPlayerPlayer;
import com.yc.english.read.common.OnUiUpdateManager;
import com.yc.english.read.view.wdigets.SpaceItemDecoration;
import com.yc.english.speak.contract.SpeakEnglishContract;
import com.yc.english.speak.model.bean.SpeakAndReadInfo;
import com.yc.english.speak.model.bean.SpeakAndReadItemInfo;
import com.yc.english.speak.model.bean.SpeakEnglishBean;
import com.yc.english.speak.presenter.SpeakEnglishListPresenter;
import com.yc.english.speak.utils.IatSettings;
import com.yc.english.speak.utils.VoiceJsonParser;
import com.yc.english.speak.view.adapter.SpeakItemAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.BindView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import yc.com.blankj.utilcode.util.LogUtils;
import yc.com.blankj.utilcode.util.SizeUtils;
import yc.com.blankj.utilcode.util.StringUtils;
import yc.com.blankj.utilcode.util.ToastUtils;

/**
 * Created by admin on 2017/10/16.
 *
 * @author admin
 */

public class SpeakEnglishActivity extends FullScreenActivity<SpeakEnglishListPresenter> implements SpeakEnglishContract.View, OnUiUpdateManager {

    @BindView(R.id.sv_loading)
    StateView mStateView;

    @BindView(R.id.speak_list_layout)
    LinearLayout mSpeakListLayout;

    @BindView(R.id.listen_english_list)
    RecyclerView mListenEnglishRecyclerView;

    @BindView(R.id.speak_seek_bar)
    SeekBar mSpeakSeekBar;

    @BindView(R.id.tv_current_speak_pos)
    TextView mCurrentTextView;

    @BindView(R.id.tv_total_speak_pos)
    TextView mTotalTextView;

    private SpeakItemAdapter mSpeakItemAdapter;

    private LinearLayoutManager mLinearLayoutManager;

    private int lastPosition = 0;

    private boolean isPlay;//播放点读

    private boolean isTape;//录音

    private boolean isPlayTape;//播放录音

    private boolean listenSuccess = false;//听写录音是否录入正确

    private CircularProgressBar playReadProgressBar;

    private CircularProgressBar progressBar;

    private CircularProgressBar playProgressBar;

    private float progress = 0;

    private MediaPlayer mPlayer;

    private File audioFile;

    private String audioFilePath;

    int playNum;

    int playCount;

    // 语音听写对象
    private SpeechRecognizer mIat;


    /**
     * 用HashMap存储听写结果
     */
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();


    private SharedPreferences mSharedPreferences;
    /**
     * 引擎类型
     */
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private boolean mTranslateEnable = false;

    private String voiceText;


    /**
     * 函数调用返回值
     */
    private int ret = 0;


    private SpeakAndReadItemInfo currentItemInfo;

    private int max;
    private AudioPlayManager manager;

    @Override
    public int getLayoutId() {
        return R.layout.speak_english_activity;
    }

    @Override
    public void init() {
        mToolbar.setTitle("说英语");
        mToolbar.showNavigationIcon();
        mToolbar.setTitleColor(ContextCompat.getColor(this, R.color.white));

        mSpeakSeekBar.setEnabled(false);
        mSpeakSeekBar.setProgress(1);

        Intent intent = getIntent();
        currentItemInfo = intent.getParcelableExtra("itemInfo");

        mPresenter = new SpeakEnglishListPresenter(this, this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mListenEnglishRecyclerView.addItemDecoration(new SpaceItemDecoration(SizeUtils.dp2px(0.3f)));
        mSpeakItemAdapter = new SpeakItemAdapter(this, null, true);

        mListenEnglishRecyclerView.setLayoutManager(mLinearLayoutManager);
        mListenEnglishRecyclerView.setAdapter(mSpeakItemAdapter);

        mPresenter.getListenEnglishDetail(currentItemInfo.getId());

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(SpeakEnglishActivity.this, mInitListener);


        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        initMediaPlayer();


        initListener();
    }

    private void initListener() {
        mSpeakItemAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (position == lastPosition) {
                return;
            }

            progress = 0;
            playNum = 0;
            playCount = 0;
            stopTask();
            tapeStop();
            stopPlayTape();
            enableState(position);

            mCurrentTextView.setText((position + 1) + "");
            mSpeakSeekBar.setProgress(position + 1);
        });

        mSpeakItemAdapter.setOnItemChildClickListener((adapter, view, position) -> {


            if (view.getId() == R.id.iv_speak_tape && !isTape && !isPlayTape && !isPlay) {
                View currentView = mLinearLayoutManager.findViewByPosition(position);
                if (currentView != null) {
                    currentView.findViewById(R.id.speak_tape_layout).setVisibility(View.VISIBLE);
                    progressBar = currentView.findViewById(R.id.progress_bar);
                }
                view.setVisibility(View.GONE);
                initTask();
                tapeStart();
                isTape = true;
            }

            if (view.getId() == R.id.speak_tape_layout && isTape && !isPlayTape && !isPlay) {
                View currentView = mLinearLayoutManager.findViewByPosition(position);
                if (currentView != null) {
                    currentView.findViewById(R.id.iv_speak_tape).setVisibility(View.VISIBLE);
                }
                view.setVisibility(View.GONE);
                stopTask();
                tapeStop();
                isTape = false;
            }

            if (view.getId() == R.id.iv_play_self_speak && !isPlayTape && !isTape && !isPlay && listenSuccess) {
                if (audioFile != null && audioFile.exists()) {
                    View currentView = mLinearLayoutManager.findViewByPosition(position);
                    if (currentView != null) {
                        playProgressBar = currentView.findViewById(R.id.play_progress_bar);
                        currentView.findViewById(R.id.play_speak_tape_layout).setVisibility(View.VISIBLE);
                    }
                    view.setVisibility(View.GONE);
                    playTape(position);
                    isPlayTape = true;
                }
            }

            if (view.getId() == R.id.play_speak_tape_layout && isPlayTape && !isTape && !isPlay) {
                View currentView = mLinearLayoutManager.findViewByPosition(position);
                if (currentView != null) {
                    currentView.findViewById(R.id.iv_play_self_speak).setVisibility(View.VISIBLE);
                }
                view.setVisibility(View.GONE);
                stopPlayTape();
            }

            //播放点读
            if (view.getId() == R.id.iv_play_read && !isPlay && !isPlayTape && !isTape) {
                View currentView = mLinearLayoutManager.findViewByPosition(position);
                if (currentView != null) {
                    playReadProgressBar = currentView.findViewById(R.id.play_read_progress_bar);
                    currentView.findViewById(R.id.play_layout).setVisibility(View.VISIBLE);
                }
                view.setVisibility(View.GONE);
                //TODO
                //播放点读
                startPlay(position);
                isPlay = true;
            }

            //停止播放点读
            if (view.getId() == R.id.play_layout && isPlay && !isPlayTape && !isTape) {
                View currentView = mLinearLayoutManager.findViewByPosition(position);
                if (currentView != null) {
                    currentView.findViewById(R.id.iv_play_read).setVisibility(View.VISIBLE);
                }
                view.setVisibility(View.GONE);
                //TODO
                //停止播放点读

                if (manager != null) manager.stop();
            }

        });
    }


    public void enableState(int position) {
        mSpeakItemAdapter.getData().get(position).setShowSpeak(true);
        if (lastPosition > -1) {
            if (position != lastPosition) {
                mSpeakItemAdapter.getData().get(lastPosition).setShowSpeak(false);
                isTape = false;
            }
        }
        lastPosition = position;
        mSpeakItemAdapter.setFirst(false);
        mSpeakItemAdapter.notifyDataSetChanged();
    }


    private void updateProgress() {
        if (progressBar != null && isTape) {
            int max = 5;
            int min = 1;
            Random random = new Random();
            int num = random.nextInt(max) % (max - min + 1) + min;

            if (progress >= 45) {
                progress = progress - num;
            } else {
                progress = progress + num;
            }
            progressBar.setProgress(progress);
        }

        if (playProgressBar != null && isPlayTape) {

            if (progress > 100) {
                progress = 100;
            } else {
                progress = progress + playCount;
            }

            playProgressBar.setProgress(progress);
        }

    }

    private Subscription subscriber;

    public void initTask() {
        if (subscriber != null) {
            return;
        }
        subscriber = Observable.interval(200, TimeUnit.MILLISECONDS).delay(150, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                updateProgress();
            }
        });
    }

    public void stopTask() {
        if (subscriber != null && subscriber.isUnsubscribed()) {
            subscriber.unsubscribe();
            subscriber = null;
        }
    }

    /**
     * 开始录音
     */
    public void tapeStart() {
        mIatResults.clear();
        // 设置参数
        setParam();
        boolean isShowDialog = mSharedPreferences.getBoolean(
                getString(R.string.pref_key_iat_show), false);
        if (isShowDialog) {
            // 显示听写对话框
            /*mIatDialog.setOnScrollChangeListener(mRecognizerDialogListener);
            mIatDialog.show();*/
            //ToastUtils.showLong("开始");
        } else {
            // 不显示听写对话框
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                ToastUtils.showLong("听写失败,错误码：" + ret);
            } else {
                //ToastUtils.showLong("开始");
            }
        }
    }

    /**
     * 停止录音
     */
    public void tapeStop() {
        isTape = false;
    }

    /**
     * 播放录音
     */
    public void playTape(final int position) {
        try {
            if (audioFile != null && audioFile.exists()) {
                initTask();

                mPlayer = new MediaPlayer();
                //设置要播放的文件
                mPlayer.setDataSource(audioFile.getAbsolutePath());
                mPlayer.prepare();
                //播放
                mPlayer.start();

                playNum = mPlayer.getDuration() % 150 == 0 ? mPlayer.getDuration() / 150 : mPlayer.getDuration() / 150 + 1;
                playCount = 100 % playNum == 0 ? 100 / playNum : 100 / playNum + 1;
                progress = 0;

                mPlayer.setOnCompletionListener(mp -> {
                    playProgressBar.setProgress(100);

                    View currentView = mLinearLayoutManager.findViewByPosition(position);
                    if (currentView != null) {
                        currentView.findViewById(R.id.play_speak_tape_layout).setVisibility(View.GONE);
                        currentView.findViewById(R.id.iv_play_self_speak).setVisibility(View.VISIBLE);
                    }
                    stopPlayTape();
                    stopTask();
                });
            }
        } catch (Exception e) {
            LogUtils.e("prepare() failed");
        }
    }

    /**
     * 停止播放录音
     */
    public void stopPlayTape() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        isPlayTape = false;
        if (playProgressBar != null) {
            playProgressBar.setProgress(0);
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = code -> {
        LogUtils.e("SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            ToastUtils.showLong("初始化失败，错误码：" + code);
        }
    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            //ToastUtils.showLong("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            if (mTranslateEnable && error.getErrorCode() == 14002) {
                ToastUtils.showLong(error.getPlainDescription(true) + "\n请确认是否已开通翻译功能");
            } else {
                //ToastUtils.showLong(error.getPlainDescription(true));

                ToastUtils.showLong("听写识别错误，请重试");
                View currentView = mLinearLayoutManager.findViewByPosition(lastPosition);
                if (currentView != null) {
                    currentView.findViewById(R.id.iv_speak_tape).setVisibility(View.VISIBLE);
                    currentView.findViewById(R.id.speak_tape_layout).setVisibility(View.GONE);
                }
                stopTask();
                tapeStop();

                if (mIat != null) {
                    mIat.stopListening();
                }
            }

            listenSuccess = false;//听写错误，设置不能播放自己的录音
            LogUtils.e("listenSuccess111 ---> " + listenSuccess);
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            //ToastUtils.showLong("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            LogUtils.e(results.getResultString());

            listenSuccess = true;
            LogUtils.e("listenSuccess222 ---> " + listenSuccess);
            if (mTranslateEnable) {
                printTransResult(results);
            } else {
                printResult(results);
            }

            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            //ToastUtils.showLong("当前正在说话，音量大小：" + volume);
            LogUtils.e("返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void printResult(RecognizerResult results) {
        String text = VoiceJsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        voiceText = resultBuffer.toString();
        if (!StringUtils.isEmpty(voiceText)) {
            LogUtils.e("语音录入结果--->" + voiceText);
        }

        View currentView = mLinearLayoutManager.findViewByPosition(lastPosition);
        if (currentView != null) {
            currentView.findViewById(R.id.iv_speak_tape).setVisibility(View.VISIBLE);
            currentView.findViewById(R.id.speak_tape_layout).setVisibility(View.GONE);
        }
        mSpeakItemAdapter.getData().get(lastPosition).setShowResult(true);

        if (compareResult(mSpeakItemAdapter.getData().get(lastPosition).getEnSentence(), voiceText)) {
            mSpeakItemAdapter.getData().get(lastPosition).setSpeakResult(true);
        } else {
            mSpeakItemAdapter.getData().get(lastPosition).setSpeakResult(false);
        }
        mSpeakItemAdapter.setFirst(false);
        mSpeakItemAdapter.notifyDataSetChanged();

        stopTask();
        tapeStop();

        if (mIat != null) {
            mIat.stopListening();
        }
    }

    /**
     * 将录入的语音与源语音进行对比
     *
     * @param sourceSen
     * @param speakSen
     * @return
     */
    public boolean compareResult(String sourceSen, String speakSen) {

        try {
            if (StringUtils.isEmpty(sourceSen) || StringUtils.isEmpty(speakSen)) {
                return false;
            }

            String regEx = " |、|，|。|；|？|！|,|\\.|;|\\?|!|]|:|：|\"|-";
            Pattern p = Pattern.compile(regEx);

            //按照句子结束符分割句子
            String[] words = p.split(sourceSen);
            List<String> sourceList = new ArrayList<>();
            for (int i = 0; i < words.length; i++) {
                if (!StringUtils.isTrimEmpty(words[i])) {
                    sourceList.add(words[i]);
                }
            }

            List<String> speakList = new ArrayList<>();
            String[] speakWords = p.split(speakSen);
            for (int m = 0; m < speakWords.length; m++) {
                if (!StringUtils.isTrimEmpty(speakWords[m])) {
                    speakList.add(speakWords[m]);
                }
            }

            int matchCount = 0;
            float percent = 0;
            for (String str : sourceList) {
                if (speakList.contains(str)) {
                    matchCount++;
                }
            }

            if (matchCount > 0) {
                percent = (float) matchCount / (float) sourceList.size() * 100;
            } else {
                return false;
            }

            if (percent >= 60) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        this.mTranslateEnable = mSharedPreferences.getBoolean(this.getString(R.string.pref_key_translate), false);
        if (mTranslateEnable) {
            LogUtils.e("translate enable");
            mIat.setParameter(SpeechConstant.ASR_SCH, "1");
            mIat.setParameter(SpeechConstant.ADD_CAP, "translate");
            mIat.setParameter(SpeechConstant.TRS_SRC, "its");
        }

        String lag = mSharedPreferences.getString("iat_language_preference",
                "en_us");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIat.setParameter(SpeechConstant.ACCENT, null);

            if (mTranslateEnable) {
                mIat.setParameter(SpeechConstant.ORI_LANG, "en");
                mIat.setParameter(SpeechConstant.TRANS_LANG, "cn");
            }
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);

            if (mTranslateEnable) {
                mIat.setParameter(SpeechConstant.ORI_LANG, "cn");
                mIat.setParameter(SpeechConstant.TRANS_LANG, "en");
            }
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        audioFilePath = Environment.getExternalStorageDirectory() + "/msc/iat.wav";
        audioFile = new File(audioFilePath);
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, audioFilePath);
    }

    private void printTransResult(RecognizerResult results) {
        String trans = VoiceJsonParser.parseTransResult(results.getResultString(), "dst");
        String oris = VoiceJsonParser.parseTransResult(results.getResultString(), "src");

        if (TextUtils.isEmpty(trans) || TextUtils.isEmpty(oris)) {
            ToastUtils.showLong("解析结果失败，请确认是否已开通翻译功能。");
        } else {
            voiceText = "原始语言:\n" + oris + "\n目标语言:\n" + trans;
        }
    }

    @Override
    public void shoReadAndSpeakMorList(List<SpeakAndReadInfo> list, int page, boolean isFitst) {

    }

    @Override
    public void showSpeakEnglishDetail(List<SpeakEnglishBean> list) {
        mSpeakItemAdapter.setNewData(list);
        mTotalTextView.setText(list.size() + "");
        mSpeakSeekBar.setMax(list.size());
    }

    @Override
    public void hide() {
        mStateView.hide();
    }

    @Override
    public void showNoNet() {
        mStateView.showNoNet(mSpeakListLayout, "网络不给力", v -> mPresenter.getListenEnglishDetail("69"));
    }

    @Override
    public void showNoData() {
        mStateView.showNoData(mSpeakListLayout);
    }

    @Override
    public void showLoading() {
        mStateView.showLoading(mSpeakListLayout);
    }


    private void initMediaPlayer() {
        manager = new MediaPlayerPlayer(this);
    }


    public void startPlay(int position) {
        if (position < 0 || position >= mSpeakItemAdapter.getData().size()) {
            return;
        }
//        resetMediaPlay();
        if (manager == null) manager = new MediaPlayerPlayer(this);
        manager.stop();
        String url = mSpeakItemAdapter.getData().get(position).getMp3url();

        manager.start(url);


    }


    @Override
    public boolean isStatusBarMateria() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (manager != null)
            manager.onDestroy();

        if (mHandler != null)
            mHandler.removeCallbacks(myRunable);
    }


    private Runnable myRunable = new Runnable() {
        @Override
        public void run() {
            if (manager != null && manager.isPlaying()) {
                int duration = manager.getPlayPosition();
                duration = (int) ((duration / (max * 1f)) * 100);
                LogUtil.msg("duration: " + duration);
                playReadProgressBar.setProgress(duration);

                mHandler.postDelayed(this, 100);
            }
        }
    };


    private void setPlayDoneState() {
        runOnUiThread(() -> {
            View currentView = mLinearLayoutManager.findViewByPosition(lastPosition);
            if (currentView != null) {
                currentView.findViewById(R.id.iv_play_read).setVisibility(View.VISIBLE);
                currentView.findViewById(R.id.play_layout).setVisibility(View.GONE);
            }
            isPlay = false;
            playReadProgressBar.setProgress(0);
        });
    }

    @Override
    public void onCompleteUI() {
        setPlayDoneState();
    }

    @Override
    public void onErrorUI(int what, int extra, String msg) {
        mHandler.postDelayed(this::setPlayDoneState, 1000);

    }

    @Override
    public void onStopUI() {
        isPlay = false;
    }

    @Override
    public void onStartUI(int duration) {
        max = duration;
        mHandler.postDelayed(myRunable, 100);
    }

}
