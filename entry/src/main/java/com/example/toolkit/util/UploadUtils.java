package com.example.toolkit.util;

import com.example.toolkit.ResourceTable;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.app.Context;
import ohos.utils.zson.ZSONObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.concurrent.Executors;

public class UploadUtils {
    public static String ip = "http://43.143.220.47:8082/";

    public static String upload(String username, String password, String filePath, String url_interface,
                                String html_url,
                                String pdf_format, String pdf_speed,
                                String pic_language){
        // 创建OkHttpClient对象
        OkHttpClient client = new OkHttpClient();
        String url= ip + url_interface ;//上传URL
        Request request;
        if (url_interface.equalsIgnoreCase("html")){
            // 创建FormBody，添加POST请求信息
            FormBody body = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .add("url", html_url)
                    .build();
            // 创建请求对象
            request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
        }
        else if (url_interface.equalsIgnoreCase("history")){
            FormBody body = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build();
            // 创建请求对象
            request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
        }
        else {
            File file=new File(filePath);//文件路径(本地)

            RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            String filename = file.getName();
            MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("password", password);

            if (url_interface.equalsIgnoreCase("pdf")){
                Utils.log(pdf_format);
                Utils.log(pdf_speed);
                requestBody.addFormDataPart("format",pdf_format);
                requestBody.addFormDataPart("speed",pdf_speed);
                requestBody.addFormDataPart("file", filename, body);
            }
            if (url_interface.equalsIgnoreCase("pic")){
                requestBody.addFormDataPart("language",pic_language);
                requestBody.addFormDataPart("pic", filename, body);
            }
            request = new Request.Builder()
                    .url(url)
                    .post(requestBody.build())
                    .build();
        }

        // 获取结果数据
        Response response = null;
        String output = null;
        try {
            response = client.newCall(request).execute();
            // 打印结果数据
            output = response.body().string();
            Utils.log(output);
        } catch (IOException e) {
            Utils.log(e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭response对象
            if (response!=null) {
                response.close();
            }
        }
        return output;
    }

    public static void download(String filePath, String url, Context context){

        try
        {
            File f = new File(filePath);
            File parent = f.getParentFile();

            if (!parent.exists()) parent.mkdirs();
            if (!f.exists()) f.createNewFile();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        Executors.newCachedThreadPool().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    URL webUrl = new URL(url);
                    URLConnection con = webUrl.openConnection();	// 打开连接
                    InputStream in = con.getInputStream();			// 获取InputStream

                    File f = new File(filePath);					// 创建文件输出流
                    FileOutputStream fo = new FileOutputStream(f);

                    byte[] buffer = new byte[1024 * 1024];
                    int len = 0;
                    while( (len = in.read(buffer)) > 0)		// 读取文件
                    {
                        fo.write(buffer, 0, len); 			// 写入文件
                    }

                    in.close();

                    fo.flush();
                    fo.close();
                    Utils.showToast(context, "保存位置:"+filePath);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });


//        String filesDirPath = fileDir;
//        OkHttpClient okHttpClient = new OkHttpClient();
//        Request request = new Request.Builder()
//                .get()
//                .url(url)
//                .build();
//        Call call = okHttpClient.newCall(request);
//
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                //
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                // Log.d(TAG, "onResponse.");
//
//                InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
//                File target = new File(filesDirPath + "/" + FILE_NAME);
//                FileOutputStream fileOutputStream = new FileOutputStream(target);
//
//                try {
//                    byte[] buffer = new byte[2048];
//                    int len;
//                    while ((len = inputStream.read(buffer)) != -1) {
//                        fileOutputStream.write(buffer, 0, len);
//                        // Log.d(TAG, "read: " + len);
//                    }
//                    fileOutputStream.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

}

