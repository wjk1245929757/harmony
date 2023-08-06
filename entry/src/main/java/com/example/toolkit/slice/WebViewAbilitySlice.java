package com.example.toolkit.slice;

import com.example.toolkit.util.Utils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.webengine.WebView;

public class WebViewAbilitySlice extends AbilitySlice {

    // WebView
    private WebView mWebView;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);

        String url = intent.getStringParam("url");
        Utils.log(url);

        ComponentContainer.LayoutConfig config = new ComponentContainer.LayoutConfig(
                ComponentContainer.LayoutConfig.MATCH_PARENT,
                ComponentContainer.LayoutConfig.MATCH_PARENT);
        DirectionalLayout layout = new DirectionalLayout(this);
        layout.setLayoutConfig(config);
        layout.setOrientation(DirectionalLayout.VERTICAL);

        // 实例化WebView对象
        mWebView = new WebView(this);
        mWebView.setLayoutConfig(config);
        // 允许JavaScript
        mWebView.getWebConfig().setJavaScriptPermit(true);
        // 加载网页
        mWebView.load(url);
        layout.addComponent(mWebView);

        super.setUIContent(layout);
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
