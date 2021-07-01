package com.pa.paperless.fragment;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;


import com.pa.paperless.adapter.UrlAdapter;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.utils.LogUtil;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.pa.boling.paperless.R;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.utils.MyUtils;
import com.pa.paperless.ui.X5WebView;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import static com.pa.paperless.service.App.isDebug;


/**
 * Created by Administrator on 2017/10/31.
 * 打开网页
 */

public class WebBrowseFragment extends BaseFragment implements View.OnClickListener {

    private final String TAG = "WebBrowseFragment-->";
    private X5WebView mWebView;
    private ImageButton back_pre, goto_nex, home, debug;
    private EditText edt_url;
    private Button goto_url;
    public static boolean webView_isshowing = false;
    private final String HOME_URL = "http://www.baidu.com/";
    private final String TBS_URL = "http://debugtbs.qq.com";
    private AVLoadingIndicatorView pro_bar;
    private RecyclerView web_url_rv;
    private List<InterfaceBase.pbui_Item_UrlDetailInfo> urlLists=new ArrayList<>();
    private UrlAdapter urlAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //网页中的视频，上屏幕的时候，可能出现闪烁的情况，需要如下设置：Activity在onCreate时需要设置:
        getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);
        try {
            if (Build.VERSION.SDK_INT >= 11) {
                Window window = getActivity().getWindow();
                window.setFlags(android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
                //避免输入法界面弹出后遮挡输入光标的问题
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        } catch (Exception e) {
        }
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        View inflate = inflater.inflate(R.layout.right_webbrowse, container, false);
        initView(inflate);
        initEvent();
        webView_isshowing = true;
        fun_webQuery();
        EventBus.getDefault().register(this);
        return inflate;
    }

    private void fun_webQuery() {
        try {
            InterfaceBase.pbui_meetUrl object = jni.webQuery();
            urlLists.clear();
            if (object != null) {
                urlLists.addAll(object.getItemList());
            }
            if (urlAdapter == null) {
                urlAdapter = new UrlAdapter(urlLists);
                web_url_rv.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                web_url_rv.setAdapter(urlAdapter);
                urlAdapter.setOnItemClickListener((adapter, view, position) -> {
                    InterfaceBase.pbui_Item_UrlDetailInfo item = urlLists.get(position);
                    String addr = item.getAddr().toStringUtf8();
                    mWebView.loadUrl(uriHttpFirst(addr));
                    web_url_rv.setVisibility(View.GONE);
                    mWebView.setVisibility(View.VISIBLE);
                });
            } else {
                urlAdapter.notifyDataSetChanged();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void initEvent() {
        //以下接口禁止(直接或反射)调用，避免视频画面无法显示：
//        mWebView.setLayerType(LAYER_TYPE_NONE,null);
//        mWebView.setDrawingCacheEnabled(true);
        mWebView.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {
            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                pro_bar.setVisibility(View.VISIBLE);
                super.onPageStarted(webView, s, bitmap);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                return super.shouldOverrideUrlLoading(webView, webResourceRequest);
            }

            @Override
            public void onPageFinished(com.tencent.smtt.sdk.WebView webView, String s) {
                LogUtil.e(TAG, "WebBrowseFragment.onPageFinished : 加载结束 url --> " + s);
                edt_url.setText(s != null ? s : "");
                pro_bar.setVisibility(View.GONE);
                super.onPageFinished(webView, s);
            }

//            @Override
//            public void onReceivedSslError(com.tencent.smtt.sdk.WebView webView, com.tencent.smtt.export.external.interfaces.SslErrorHandler sslErrorHandler, com.tencent.smtt.export.external.interfaces.SslError sslError) {
//                LogUtil.e(TAG, "WebBrowseFragment.onReceivedSslError :   --> ");
//                sslErrorHandler.proceed();//接受所有网站的证书
//                super.onReceivedSslError(webView, sslErrorHandler, sslError);
//            }
        });
//        mWebView.loadUrl("http://debugtbs.qq.com");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) throws InvalidProtocolBufferException {
        switch (message.getAction()) {
            case EventType.NETWEB_INFORM:
                LogUtil.e(TAG, "WebBrowseFragment.getEventMessage :  网页变更通知 --> ");
                fun_webQuery();
                break;
            case EventType.go_back_html:
                LogUtil.e(TAG, "WebBrowseFragment.getEventMessage :  返回上一个网页 --> ");
                mWebView.goBack();
                break;
        }
    }

    //地址HTTP协议判断，无HTTP打头的，增加http://，并返回。
    private String uriHttpFirst(String strUri) {
        if (strUri.indexOf("http://", 0) != 0 && strUri.indexOf("https://", 0) != 0) {
            strUri = "http://" + strUri;
        }
        return strUri;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
            mWebView.resumeTimers();
            mWebView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        if (mWebView != null) {
            String videoJs = "javascript: var v = document.getElementsByTagName('video'); for(var i=0;i<v.length;i++){v[i].pause();} ";
            mWebView.loadUrl(videoJs);//遍历所有的Vedio标签，主动调用暂停方法
            mWebView.onPause();
            mWebView.pauseTimers();
            mWebView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            //webview停止加载
            mWebView.stopLoading();
            //webview清理内存
            mWebView.clearCache(true);
            //webview清理历史记录
            mWebView.clearHistory();
            //webview销毁
            mWebView.destroy();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initView(View inflate) {
        mWebView = inflate.findViewById(R.id.web_view);
        mWebView.setOnClickListener(this);
        back_pre = inflate.findViewById(R.id.back_pre);
        back_pre.setOnClickListener(this);
        goto_nex = inflate.findViewById(R.id.goto_nex);
        goto_nex.setOnClickListener(this);
        home = inflate.findViewById(R.id.home);
        home.setOnClickListener(this);
        debug = inflate.findViewById(R.id.debug);
        debug.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        debug.setOnClickListener(this);
        edt_url = inflate.findViewById(R.id.edt_url);
        edt_url.setOnClickListener(this);
        goto_url = inflate.findViewById(R.id.goto_url);
        goto_url.setOnClickListener(this);
        pro_bar = inflate.findViewById(R.id.pro_bar);
        web_url_rv = inflate.findViewById(R.id.web_url_rv);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.d(TAG, "onHiddenChanged: .........." + hidden);
        webView_isshowing = !hidden;
        if (hidden) {
            LogUtil.d(TAG, "onHiddenChanged: 隐藏");
            mWebView.setAlpha(0);
            onPause();
        } else {
            debug.setVisibility(isDebug ? View.VISIBLE : View.GONE);
            LogUtil.d(TAG, "onHiddenChanged: 显示");
            mWebView.setAlpha(1);
            onResume();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_pre://上一个
                mWebView.goBack();
                break;
            case R.id.goto_nex://下一个
                mWebView.goForward();
                break;
            case R.id.home://回到主页
//                mWebView.loadUrl(HOME_URL);
                web_url_rv.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.GONE);
                break;
            case R.id.goto_url://前往
                String url = edt_url.getText().toString();
                mWebView.loadUrl(uriHttpFirst(url));
                web_url_rv.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                break;
            case R.id.debug:
                mWebView.loadUrl(TBS_URL);
                break;
        }
    }
}
