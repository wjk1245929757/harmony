package com.example.toolkit.slice;

import com.example.toolkit.ResourceTable;
import com.example.toolkit.util.PreferenceUtils;
import com.example.toolkit.util.UploadUtils;
import com.example.toolkit.util.Utils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.TextField;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.miscservices.pasteboard.PasteData;
import ohos.miscservices.pasteboard.SystemPasteboard;
import ohos.utils.zson.ZSONObject;

public class WebAbilitySlice extends AbilitySlice {

    //全局定义
    private long lastClickTime = 0L;
    // 两次点击间隔不能少于1000ms
    private final int FAST_CLICK_DELAY_TIME = 1000;

    private String download_url;
    private String download_content;

    SystemPasteboard pasteboard;
    PasteData pasteData;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_web);
        String username = PreferenceUtils.getInstance().getString(getApplicationContext(),"username", "unknown");
        String password = PreferenceUtils.getInstance().getString(getApplicationContext(),"password", "unknown");

        pasteboard = SystemPasteboard.getSystemPasteboard(this.getContext());
        pasteData = pasteboard.getPasteData();

        TextField textField = (TextField) findComponentById(ResourceTable.Id_web_textfield1);
        textField.setClickedListener((Component component) -> {
            if (pasteData != null) {
                Utils.log("click");
                PasteData.DataProperty dataProperty = pasteData.getProperty();
                boolean hasHtml = dataProperty.hasMimeType(PasteData.MIMETYPE_TEXT_HTML);
                boolean hasText = dataProperty.hasMimeType(PasteData.MIMETYPE_TEXT_PLAIN);
                if (hasHtml || hasText) {
                    for (int i = 0; i < pasteData.getRecordCount(); i++) {
                        PasteData.Record record = pasteData.getRecordAt(i);
                        String mimeType = record.getMimeType();
                        if (mimeType.equals(PasteData.MIMETYPE_TEXT_HTML)) {
                            textField.setText(record.getHtmlText());
                            break;
                        } else if (mimeType.equals(PasteData.MIMETYPE_TEXT_PLAIN)) {
                            textField.setText(record.getPlainText().toString());
                            break;
                        } else {
                            // skip records of other Mime type
                        }
                    }
                }
            }
        });
        textField.setDoubleClickedListener((Component component) -> {
            Utils.log("double click");
            String s = textField.getText();
            if (!s.equalsIgnoreCase("")){
                Utils.log(s);
                pasteboard.setPasteData(PasteData.creatPlainTextData(s));
                Utils.showToast(WebAbilitySlice.this, "copied!");
            }
        });

        TextField textField1 = (TextField) findComponentById(ResourceTable.Id_web_text4);
        textField1.setDoubleClickedListener((Component component) -> {
            Utils.showToast(WebAbilitySlice.this, "copied!");
            Utils.log("click text field 2");
            String s = textField1.getText();
            Utils.log(s);
            pasteboard.setPasteData(PasteData.creatPlainTextData(s));
        });

        Button btn1=(Button)findComponentById(ResourceTable.Id_web_button1);
        Button btn2=(Button)findComponentById(ResourceTable.Id_web_button2);
        btn1.setClickedListener(c->{
            if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()-> {
                    Utils.log(username);
                    String res = UploadUtils.upload(username, password, null, "html", textField.getText(), "", "", "");
                    ZSONObject zsonObject = ZSONObject.stringToZSON(res);
                    Utils.log(zsonObject.get("url").toString());
                    Utils.log(zsonObject.get("content").toString());
                    download_url = zsonObject.get("url").toString();
                    download_content = zsonObject.get("content").toString();
                    btn2.setEnabled(true);
                    getUITaskDispatcher().asyncDispatch(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showToast(getContext(), "OK");
                            textField1.setText(download_content);
                        }
                    });
                });
                lastClickTime = System.currentTimeMillis();
            }
        });

        btn2.setEnabled(false);
        btn2.setClickedListener(c->{
//            WebViewAbilitySlice webViewAbilitySlice = new WebViewAbilitySlice();
//            Intent intent1 = new Intent();
//            intent1.setParam("url", download_url);
//            present(webViewAbilitySlice, intent1);
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
                String[] decode_url = download_url.split("/");
                UploadUtils.download(getExternalCacheDir().getAbsolutePath()+"/"+decode_url[decode_url.length-1], download_url, getContext());
                getUITaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(getContext(), "存储位置："+getExternalCacheDir().getAbsolutePath()+"/"+decode_url[decode_url.length-1]);
                    }
                });
            });
        });
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
