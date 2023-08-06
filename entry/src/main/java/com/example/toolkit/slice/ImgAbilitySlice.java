package com.example.toolkit.slice;

import com.example.toolkit.ResourceTable;
import com.example.toolkit.util.PreferenceUtils;
import com.example.toolkit.util.UploadUtils;
import com.example.toolkit.util.Utils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Image;
import ohos.agp.components.Picker;
import ohos.agp.components.Text;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.media.image.ImagePacker;
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

public class ImgAbilitySlice extends AbilitySlice {

    SystemPasteboard pasteboard;
    PasteData pasteData;
    private final int imgRequestCode=0;
    private long lastClickTime = 0L;
    private final int FAST_CLICK_DELAY_TIME = 1000;
    private String file_path;
    private String download_url;
    private String download_content;
    Image showChooseImg=null;
    Picker picker;
    Text file_text;;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_img);

        String username = PreferenceUtils.getInstance().getString(getApplicationContext(), "username", "unknown");
        String password = PreferenceUtils.getInstance().getString(getApplicationContext(), "password", "unknown");

        String[] languages = new String[]{"chi_tra", "chi_sim", "eng", "fra", "frk", "rus", "jpn", "kor"};
        picker=(Picker) findComponentById(ResourceTable.Id_img_picker);
        picker.setDisplayedData(new String[]{"繁体中文", "简体中文", "英语", "德语", "法语", "俄语", "日语", "韩语"});
        Button btnChooseImg=(Button)findComponentById(ResourceTable.Id_img_btn1);
        btnChooseImg.setClickedListener(c->{
            //选择图片
            selectPic();
        });
        showChooseImg=(Image)findComponentById(ResourceTable.Id_img_img);
        file_text=(Text) findComponentById(ResourceTable.Id_img_text3);

        Button btn2 = (Button) findComponentById(ResourceTable.Id_img_btn2);
        Button btn3 = (Button) findComponentById(ResourceTable.Id_img_btn3);
        btn2.setClickedListener(c -> {
            if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
//                    Utils.log(picker.getValue() + "");
                    String res = UploadUtils.upload(username, password, file_text.getText(), "pic", "", "", "", languages[picker.getValue()]);
                    ZSONObject zsonObject = ZSONObject.stringToZSON(res);
                    Utils.log(res);
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
                        Utils.showToast(getContext(), "保存位置:"+getExternalCacheDir().getAbsolutePath()+"/"+decode_url[decode_url.length-1]);
                    }
                });

            });
        });
    }

    private void selectPic() {
//        Intent intent = new Intent();
//        Operation opt=new Intent.OperationBuilder().withAction("android.intent.action.GET_CONTENT").build();
//        intent.setOperation(opt);
//        intent.addFlags(Intent.FLAG_NOT_OHOS_COMPONENT);
//        intent.setType("image/*");
//        startAbilityForResult(intent, imgRequestCode);

        Operation operation = new Intent.OperationBuilder()
                .withAction(IntentConstants.ACTION_HIDISK_CHOOSE)
                .build();
        Intent intent_ = new Intent();
        intent_.setOperation(operation);
        startAbilityForResult(intent_, imgRequestCode);
    }

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        if(requestCode==imgRequestCode)
        {
            if (resultData == null) {
                Utils.log("no choose");
            } else {
                ArrayList<?> uris = (ArrayList<?>) resultData.getParams().getParam("select-item-list");
                if (uris.size() == 0) {
                    Utils.log("empty");
                } else {
                    DataAbilityHelper dataAbilityHelper = DataAbilityHelper.creator(getContext());
                    //定义图片来源对象
                    ImageSource imageSource = null;
                    //获取选择的Img对应的Id
                    String chooseImgId=null;
                    try {
                        ohos.utils.net.Uri uri = ohos.utils.net.Uri.parse(uris.get(0).toString().replace("content://", "dataability:///"));
                        Utils.log(dataAbilityHelper.getType(uri));
                        Utils.log(uri.toString());
                        FileDescriptor fd = dataAbilityHelper.openFile(uri, "r");
                        imageSource = ImageSource.create(fd, null);
                        //创建位图
                        PixelMap pixelMap = imageSource.createPixelmap(null);
                        //设置图片控件对应的位图
                        showChooseImg.setPixelMap(pixelMap);

                        file_path = getExternalCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis()+".jpg";

                        ImagePacker imagePacker = ImagePacker.create();
                        File file = new File(file_path);
                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                        } catch (FileNotFoundException e) {
                            Utils.log(e.getMessage());
                        }
                        ImagePacker.PackingOptions packingOptions = new ImagePacker.PackingOptions();
                        packingOptions.format = "image/jpeg";
                        packingOptions.quality = 100;// 设置图片质量
                        boolean result = imagePacker.initializePacking(outputStream, packingOptions);
                        result = imagePacker.addImage(pixelMap);
                        long dataSize = imagePacker.finalizePacking();

//                        FileInputStream fis = new FileInputStream(fd);
//                        byte[] b=new byte[fis.available()];
//                        fis.read(b);
//                        fis.close();
//                        File file=new File(file_path);
//                        //创建文件字节输出流对象，准备向d.txt文件中写出数据,true表示在原有的基础上增加内容
//                        FileOutputStream fout = new FileOutputStream(file,true);
//                        fout.write(b);//使用字节数组输出到文件
//                        fout.flush();//强制刷新输出流
//                        fout.close();//关闭输出流
                        file_text.setText(file_path);
                        Utils.log(file_path);
                    } catch (Exception e) {
                        Utils.log(e.toString());
                    } finally {
                        if (imageSource != null) {
                            imageSource.release();
                        }
                    }
                }
            }


//            Utils.log("选择图片getUriString:"+resultData.getUriString());
//            //选择的Img对应的Uri
//            String chooseImgUri=resultData.getUriString();
//            Utils.log("选择图片getScheme:"+chooseImgUri.substring(chooseImgUri.lastIndexOf('/')));
//
//            //定义数据能力帮助对象
//            DataAbilityHelper helper=DataAbilityHelper.creator(getContext());
//            //定义图片来源对象
//            ImageSource imageSource = null;
//            //获取选择的Img对应的Id
//            String chooseImgId=null;
//            //如果是选择文件则getUriString结果为content://com.android.providers.media.documents/document/image%3A30，其中%3A是":"的URL编码结果，后面的数字就是image对应的Id
//            //如果选择的是图库则getUriString结果为content://media/external/images/media/30，最后就是image对应的Id
//            //这里需要判断是选择了文件还是图库
//            if(chooseImgUri.lastIndexOf("%3A")!=-1){
//                chooseImgId = chooseImgUri.substring(chooseImgUri.lastIndexOf("%3A")+3);
//            }
//            else {
//                chooseImgId = chooseImgUri.substring(chooseImgUri.lastIndexOf('/')+1);
//            }
//            //获取图片对应的uri，由于获取到的前缀是content，我们替换成对应的dataability前缀
//            Uri uri=Uri.appendEncodedPathToUri(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI,chooseImgId);
//            Utils.log("选择图片dataability路径:"+uri.toString());
//            try {
//                //读取图片
//                FileDescriptor fd = helper.openFile(uri, "r");
//                imageSource = ImageSource.create(fd, null);
//                //创建位图
//                PixelMap pixelMap = imageSource.createPixelmap(null);
//                //设置图片控件对应的位图
//                showChooseImg.setPixelMap(pixelMap);
//                FileInputStream fis = new FileInputStream(fd);
//                byte[] b=new byte[fis.available()];
//                fis.read(b);
//                fis.close();
//                file_path = getExternalCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis()+".png";
//                File file=new File(file_path);
//                //创建文件字节输出流对象，准备向d.txt文件中写出数据,true表示在原有的基础上增加内容
//                FileOutputStream fout = new FileOutputStream(file,true);
//                fout.write(b);//使用字节数组输出到文件
//                fout.flush();//强制刷新输出流
//                fout.close();//关闭输出流
//                Utils.log(file_path);
//                file_text.setText(file_path);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (imageSource != null) {
//                    imageSource.release();
//                }
//            }
        }
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
