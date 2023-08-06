package com.example.toolkit.slice;

import com.example.toolkit.util.PageProvider;
import com.example.toolkit.ResourceTable;
import com.example.toolkit.util.PreferenceUtils;
import com.example.toolkit.util.UploadUtils;
import com.example.toolkit.util.Utils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.utils.zson.ZSONArray;
import ohos.utils.zson.ZSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice {

    private PageSlider pageSlider;
    private TabList tablist;
    private long lastClickTime = 0L;
    private final int FAST_CLICK_DELAY_TIME = 1000;
    Text text;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        initPageSlider();
        initTablist();
        initToolSlice();
        initHistorySlice();
    }

    @Override
    protected void onResult(int requestCode, Intent resultIntent) {
        super.onResult(requestCode, resultIntent);
        if(requestCode==123){
            String username = resultIntent.getStringParam("username");
            if (!username.equalsIgnoreCase("null")){
                Utils.showToast(MainAbilitySlice.this, "welcome "+username);
                text.setText(username);
                String password = PreferenceUtils.getInstance().getString(getApplicationContext(), "password", "unknown");
                Utils.log(password);
                getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
                    String res = UploadUtils.upload(username, password, "", "history", "", "", "", "");
                    ZSONArray zsonArray = ZSONArray.stringToZSONArray(res);
                    // 列表数据（1-1000整型值）
                    List<String> date = new ArrayList<>();
                    List<String> output_file = new ArrayList<>();
                    List<String> output_url = new ArrayList<>();
                    Utils.log("it is HistoryAbilitySlice");

                    // 初始化列表数据对象
                    for (int i = 0; i < zsonArray.size(); i++) {
                        ZSONObject zsonObject = zsonArray.getZSONObject(i);
                        date.add(zsonObject.getString("date"));
                        output_file.add(zsonObject.getString("output"));
                        output_url.add("http://43.143.220.47:8082/file/"+zsonObject.getString("output"));
                    }

                    getUITaskDispatcher().asyncDispatch(new Runnable() {
                        @Override
                        public void run() {
                            // ListContainer对象
                            ListContainer mListContainer;
                            // 获取ListContainer对象
                            mListContainer = (ListContainer) findComponentById(ResourceTable.Id_list_container);
                            // 为ListContainer对象设置RecycleItemProvider
                            mListContainer.setItemProvider(new BaseItemProvider() {
                                @Override
                                public int getCount() {
                                    // 列表项数
                                    return date.size();
                                }
                                @Override
                                public Object getItem(int i) {
                                    // 当前列表项的数据
                                    return date.get(i);
                                }
                                @Override
                                public long getItemId(int i) {
                                    // 当前列表项ID
                                    return i;
                                }
                                @Override
                                public Component getComponent(int i, Component component, ComponentContainer componentContainer) {
                                    // 列表项用户界面 （如果可以复用之前的界面，则直接复用）
                                    DirectionalLayout layout = (DirectionalLayout) component;
                                    if (layout == null) {
                                        // 如果之前的界面为空，则创建新的列表项用户界面
                                        layout = (DirectionalLayout) LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_recycle_item, null, false);
                                    }
                                    Text text1 = (Text) layout.findComponentById(ResourceTable.Id_item_text1);
                                    text1.setText(date.get(i));
                                    Text text2 = (Text) layout.findComponentById(ResourceTable.Id_item_text2);
                                    text2.setText(output_file.get(i));
                                    Button btn = (Button) layout.findComponentById(ResourceTable.Id_item_btn1);
                                    btn.setClickedListener(c -> {
                                        if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                                            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
                                                UploadUtils.download(getExternalCacheDir().getAbsolutePath()+"/"+output_file.get(i), output_url.get(i), getContext());
                                                Utils.log(getExternalCacheDir().getAbsolutePath()+"/"+output_file.get(i));
                                            });
                                            Utils.showToast(getContext(), "存储路径:"+getExternalCacheDir().getAbsolutePath()+"/"+output_file.get(i));
                                            lastClickTime = System.currentTimeMillis();
                                        }
                                    });
                                    // 返回该列表项用户界面
                                    return layout;
                                }
                            });
                        }
                    });
                });
            }
        }
    }

    private void initPageSlider(){
        LayoutScatter layoutScatter = LayoutScatter.getInstance(getContext());
        DirectionalLayout tool_layout = (DirectionalLayout) layoutScatter.parse(ResourceTable.Layout_ability_tool, null, false);
        DirectionalLayout history_layout = (DirectionalLayout) layoutScatter.parse(ResourceTable.Layout_ability_history, null, false);
        List<Component> layouts = new ArrayList<>();
        layouts.add(tool_layout);
        layouts.add(history_layout);

        pageSlider = (PageSlider) findComponentById(ResourceTable.Id_pager_slider);
        pageSlider.setProvider(new PageProvider(layouts));
        pageSlider.addPageChangedListener(new PageSlider.PageChangedListener() {
            @Override
            public void onPageSliding(int i, float v, int i1) {}
            @Override
            public void onPageSlideStateChanged(int i) {}
            @Override
            public void onPageChosen(int i) {
                //i 表示slider的索引
                if(tablist.getSelectedTabIndex() != i){//标签页与slider显示不一致
                    tablist.selectTabAt(i);
                }
            }
        });
    }

    private void initTablist(){
        // 获取TabList对象
        tablist = (TabList) findComponentById(ResourceTable.Id_tab_list);
        // 设置TabList的Tab总宽度
        tablist.setTabLength(getResourceManager().getDeviceCapability().width);
        TabList.Tab toolPageTab = tablist.new Tab(getContext());
        toolPageTab.setText("小工具列表");
        toolPageTab.setMarginsLeftAndRight(10, 10);
        toolPageTab.setTag(0);
        tablist.addTab(toolPageTab, true);
        TabList.Tab historyPageTab = tablist.new Tab(getContext());
        historyPageTab.setText("历史记录");
        historyPageTab.setMarginsLeftAndRight(10, 10);
        historyPageTab.setTag(1);
        tablist.addTab(historyPageTab);

        tablist.addTabSelectedListener(new TabList.TabSelectedListener() {
            @Override
            public void onSelected(TabList.Tab tab) {
                Utils.log("onSelected: " + tab.getText());
                pageSlider.setCurrentPage((int)tab.getTag());
            }
            @Override
            public void onUnselected(TabList.Tab tab) {
                Utils.log("onUnselected: " + tab.getText());
            }

            @Override
            public void onReselected(TabList.Tab tab) {
                Utils.log("onReselected: " + tab.getText());
            }
        });
    }

    private void initToolSlice(){
        Text text_web = (Text) findComponentById(ResourceTable.Id_text_tool1);
        Text text_pdf = (Text) findComponentById(ResourceTable.Id_text_tool2);
        Text text_img = (Text) findComponentById(ResourceTable.Id_text_tool3);
        Image image_web = (Image) findComponentById(ResourceTable.Id_image_website);
        Image image_pdf = (Image) findComponentById(ResourceTable.Id_image_pdf);
        Image image_img = (Image) findComponentById(ResourceTable.Id_image_img);
        text_web.setClickedListener((Component component) -> present(new WebAbilitySlice(), new Intent()));
        image_web.setClickedListener((Component component) -> present(new WebAbilitySlice(), new Intent()));
        text_pdf.setClickedListener((Component component) -> present(new PdfAbilitySlice(), new Intent()));
        image_pdf.setClickedListener((Component component) -> present(new PdfAbilitySlice(), new Intent()));
        text_img.setClickedListener((Component component) -> present(new ImgAbilitySlice(), new Intent()));
        image_img.setClickedListener((Component component) -> present(new ImgAbilitySlice(), new Intent()));
    }

    private void initHistorySlice(){
        text = (Text) findComponentById(ResourceTable.Id_history_text1);
        String username = PreferenceUtils.getInstance().getString(getApplicationContext(), "username", "unknown");
        String password = PreferenceUtils.getInstance().getString(getApplicationContext(), "password", "unknown");
        if (username.equalsIgnoreCase("unknown")){
            Utils.showToast(MainAbilitySlice.this, "welcome unknown");
        }else {
            Utils.showToast(MainAbilitySlice.this, "welcome " + username);
            text.setText(username);
        }
        text.setClickedListener((Component c) -> {
            Intent intent1 = new Intent();
            intent1.setParam("username", "unknown");
            presentForResult(new LoginAbilitySlice(), intent1,123);
        });

        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
            String res = UploadUtils.upload(username, password, "", "history", "", "", "", "");
            ZSONArray zsonArray = ZSONArray.stringToZSONArray(res);
            // 列表数据（1-1000整型值）
            List<String> date = new ArrayList<>();
            List<String> output_file = new ArrayList<>();
            List<String> output_url = new ArrayList<>();
            Utils.log("it is HistoryAbilitySlice");

            // 初始化列表数据对象
            for (int i = 0; i < zsonArray.size(); i++) {
                ZSONObject zsonObject = zsonArray.getZSONObject(i);
                date.add(zsonObject.getString("date"));
                output_file.add(zsonObject.getString("output"));
                output_url.add("http://43.143.220.47:8082/file/"+zsonObject.getString("output"));
            }

            getUITaskDispatcher().asyncDispatch(new Runnable() {
                @Override
                public void run() {
                    // ListContainer对象
                    ListContainer mListContainer;
                    // 获取ListContainer对象
                    mListContainer = (ListContainer) findComponentById(ResourceTable.Id_list_container);
                    // 为ListContainer对象设置RecycleItemProvider
                    mListContainer.setItemProvider(new BaseItemProvider() {
                        @Override
                        public int getCount() {
                            // 列表项数
                            return date.size();
                        }
                        @Override
                        public Object getItem(int i) {
                            // 当前列表项的数据
                            return date.get(i);
                        }
                        @Override
                        public long getItemId(int i) {
                            // 当前列表项ID
                            return i;
                        }
                        @Override
                        public Component getComponent(int i, Component component, ComponentContainer componentContainer) {
                            // 列表项用户界面 （如果可以复用之前的界面，则直接复用）
                            DirectionalLayout layout = (DirectionalLayout) component;
                            if (layout == null) {
                                // 如果之前的界面为空，则创建新的列表项用户界面
                                layout = (DirectionalLayout) LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_recycle_item, null, false);
                            }
                            Text text1 = (Text) layout.findComponentById(ResourceTable.Id_item_text1);
                            text1.setText(date.get(i));
                            Text text2 = (Text) layout.findComponentById(ResourceTable.Id_item_text2);
                            text2.setText(output_file.get(i));
                            Button btn = (Button) layout.findComponentById(ResourceTable.Id_item_btn1);
                            btn.setClickedListener(c -> {
                                if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                                    getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
                                        UploadUtils.download(getExternalCacheDir().getAbsolutePath()+"/"+output_file.get(i), output_url.get(i), getContext());
                                        Utils.log(getExternalCacheDir().getAbsolutePath()+"/"+output_file.get(i));
                                    });
                                    Utils.showToast(getContext(), "存储路径:"+getExternalCacheDir().getAbsolutePath()+"/"+output_file.get(i));
                                    lastClickTime = System.currentTimeMillis();
                                }
                            });
                            // 返回该列表项用户界面
                            return layout;
                        }
                    });
                }
            });
        });

    }

}
