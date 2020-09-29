package com.pa.paperless.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;

import com.pa.paperless.utils.LogUtil;

import android.view.View;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class X5WebView extends WebView {
    private final String TAG = "X5WebView-->";
    private WebChromeClient chromeClient = new WebChromeClient() {

    };
    private WebViewClient client = new WebViewClient() {
        /**
         * 防止加载网页时调起系统浏览器
         */
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //使得打开网页时不调用系统浏览器， 而是在本WebView中显示
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            //super.onReceivedSslError(webView, sslErrorHandler, sslError);//注意一定要去除这行代码，否则设置无效。
            // handler.cancel();// Android默认的处理方式
            sslErrorHandler.proceed();// 接受所有网站的证书
            // handleMessage(Message msg);// 进行其他处理
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    public X5WebView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        this.setWebViewClient(client);
        this.setWebChromeClient(chromeClient);
        // WebStorage webStorage = WebStorage.getInstance();
        initWebViewSettings();
        this.getView().setClickable(true);
    }

    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSetting.setJavaScriptEnabled(true);

        //缩放操作
        webSetting.setSupportZoom(true);//支持缩放，默认为true。是下面那个的前提。
        webSetting.setBuiltInZoomControls(true);//只有setSupportZoom为true时才有用
        webSetting.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //支持插件
        //webSetting.setPluginsEnabled(true);

        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        webSetting.setAllowFileAccess(true);//设置可以访问文件
        webSetting.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSetting.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportMultipleWindows(true);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //缓存模式如下：
        //LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        //LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
        //LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
        //LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。

        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension settings 的设计

        //设置自适应屏幕，两者合用（下面这两个方法合用）:无法根据浏览器居中显示内容这个问题只要设置下面两个熟悉即可
        webSetting.setLoadWithOverviewMode(true);//缩放至屏幕的大小
        webSetting.setUseWideViewPort(true);//将图片调整到适合webview的大小

        webSetting.setSaveFormData(true);//保存表单数据
        webSetting.setMinimumFontSize(5);//设置WebView逻辑上最小字体值，默认值8，取值1到72
        webSetting.setDefaultFontSize(8);//设置WebView默认值字体值，默认值16，取值1到72
        webSetting.setDefaultFixedFontSize(8);//设置WebView默认固定的字体值，默认值16，取值1到72
        //5.0 以后的WebView加载的链接为Https开头，但是链接里面的内容，比如图片为Http链接，这时候，图片就会加载不出来
        //原因是Android 5.0上Webview默认不允许加载Http与Https混合内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //两者都可以
            webSetting.setMixedContentMode(webSetting.getMixedContentMode());
            //如果大于5.0设置混合模式
            //mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    @Override
    public void onResume() {
        LogUtil.e(TAG, "X5WebView.onResume :   --> ");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtil.e(TAG, "X5WebView.onPause :   --> ");
        super.onPause();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean ret = super.drawChild(canvas, child, drawingTime);
        canvas.save();
        //		Paint paint = new Paint();
//		paint.setColor(0x7fff0000);
//		paint.setTextSize(24.f);
//		paint.setAntiAlias(true);
//		if (getX5WebViewExtension() != null) {
//			canvas.drawText(this.getContext().getPackageName() + "-pid:"
//					+ android.os.Process.myPid(), 10, 50, paint);
//			canvas.drawText(
//					"X5  Core:" + QbSdk.getTbsVersion(this.getContext()), 10,
//					100, paint);
//		} else {
//			canvas.drawText(this.getContext().getPackageName() + "-pid:"
//					+ android.os.Process.myPid(), 10, 50, paint);
//			canvas.drawText("Sys Core", 10, 100, paint);
//		}
//		canvas.drawText(Build.MANUFACTURER, 10, 150, paint);
//		canvas.drawText(Build.MODEL, 10, 200, paint);
//		canvas.restore();
        return ret;
    }

    public X5WebView(Context arg0) {
        this(arg0, (AttributeSet) null);
//        super(arg0);
        setBackgroundColor(85621);
    }

}
