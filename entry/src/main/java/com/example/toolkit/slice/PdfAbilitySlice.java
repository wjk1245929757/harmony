package com.example.toolkit.slice;

import com.example.toolkit.ResourceTable;
import com.example.toolkit.util.PreferenceUtils;
import com.example.toolkit.util.UploadUtils;
import com.example.toolkit.util.Utils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.photokit.metadata.AVStorage;
import ohos.miscservices.pasteboard.PasteData;
import ohos.miscservices.pasteboard.SystemPasteboard;
import ohos.utils.IntentConstants;
import ohos.utils.net.Uri;
import ohos.utils.zson.ZSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PdfAbilitySlice extends AbilitySlice {

    //全局定义
    private long lastClickTime = 0L;
    // 两次点击间隔不能少于1000ms
    private final int FAST_CLICK_DELAY_TIME = 1000;

    private String file_path;

    private String download_url;
    private String download_content;

    SystemPasteboard pasteboard;
    PasteData pasteData;
    Text file_text;
    Picker picker;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_pdf);

        String username = PreferenceUtils.getInstance().getString(getApplicationContext(), "username", "unknown");
        String password = PreferenceUtils.getInstance().getString(getApplicationContext(), "password", "unknown");

        String[] format = new String[]{"DOC", "PNG", "SVG", "HTML"};
        file_text = (Text) findComponentById(ResourceTable.Id_pdf_text3);
        picker = (Picker) findComponentById(ResourceTable.Id_pdf_picker);
        picker.setDisplayedData(new String[]{"DOC", "PNG", "SVG", "HTML"});

        Button btnChooseImg = (Button) findComponentById(ResourceTable.Id_pdf_btn1);
        Button btn2 = (Button) findComponentById(ResourceTable.Id_pdf_btn2);
        Button btn3 = (Button) findComponentById(ResourceTable.Id_pdf_btn3);
        btnChooseImg.setClickedListener(c -> {
            selectPdf();
        });
        btn2.setClickedListener(c -> {
            if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
//                    Utils.log(picker.getValue() + "");
                    String res = UploadUtils.upload(username, password, file_text.getText(), "pdf", "", format[picker.getValue()], "not_fast", "");
                    ZSONObject zsonObject = ZSONObject.stringToZSON(res);
                    Utils.log(zsonObject.toString());
                    btn3.setEnabled(true);
                    getUITaskDispatcher().asyncDispatch(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showToast(getContext(), "转换成功");
                        }
                    });
                    Utils.log(zsonObject.get("url").toString());
//                    Utils.log(zsonObject.get("content").toString());
                    download_url = zsonObject.get("url").toString();
//                    download_content = zsonObject.get("content").toString();
                });
                lastClickTime = System.currentTimeMillis();
            }
        });
        btn3.setEnabled(false);
        btn3.setClickedListener(c -> {
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

    private void selectPdf() {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        String authority = context.getPackageName() + ".fileprovider";
//        Uri uri = FileProvider.getUriForFile(context, authority, file);
//        intent.setDataAndType(uri, "application/pdf");
//        return intent;
        Operation operation = new Intent.OperationBuilder()
                .withAction(IntentConstants.ACTION_HIDISK_CHOOSE)
                .build();
        Intent intent_ = new Intent();
        intent_.setOperation(operation);
        startAbilityForResult(intent_, 1);
    }

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 1) {
            if (resultData == null) {
                Utils.log("no choose");
            } else {
                ArrayList<?> uris = (ArrayList<?>) resultData.getParams().getParam("select-item-list");
                if (uris.size() == 0) {
                    Utils.log("empty");
                } else {
                    DataAbilityHelper dataAbilityHelper = DataAbilityHelper.creator(getContext());
                    try {
                        ohos.utils.net.Uri uri = ohos.utils.net.Uri.parse(uris.get(0).toString().replace("content://", "dataability:///"));
                        Utils.log(dataAbilityHelper.getType(uri));
                        Utils.log(uri.toString());
                        FileDescriptor fd = dataAbilityHelper.openFile(uri, "r");
                        FileInputStream fis = new FileInputStream(fd);
                        byte[] b=new byte[fis.available()];
                        fis.read(b);
                        fis.close();
                        file_path = getExternalCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis()+".pdf";
                        File file=new File(file_path);
                        //创建文件字节输出流对象，准备向d.txt文件中写出数据,true表示在原有的基础上增加内容
                        FileOutputStream fout = new FileOutputStream(file,true);
                        fout.write(b);//使用字节数组输出到文件
                        fout.flush();//强制刷新输出流
                        fout.close();//关闭输出流
                        file_text.setText(file_path);
                        Utils.log(file_path);
                    } catch (Exception e) {
                        Utils.log(e.toString());
                    }
                }
            }
        }
//        if(requestCode==0 && resultData!=null)
//        {
//            Utils.log("选择文件getUriString:"+resultData.getUriString());
//            //选择的Img对应的Uri
//            String chooseFileUri=resultData.getUriString();
//            Utils.log("选择文件getScheme:"+chooseFileUri.substring(chooseFileUri.lastIndexOf('/')));
//            Utils.log("选择文件dataability路径:"+resultData.getUriString());
//            file_text.setText(resultData.getUriString());
//
//        }
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
