package com.trackingworld.handsets;

import com.fgtit.data.ConversionsEx;
import com.fgtit.device.Constants;
import com.fgtit.device.FPModule;
import com.fgtit.fpcore.FPMatch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements OnWebClickListener {

    private final FPModule fpm = new FPModule();
    private WebView webView;
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private Bitmap capturedBitmap;

    private final byte[] bmpdata = new byte[Constants.RESBMP_SIZE];
    private int bmpsize = 0;

    private final byte[] refdata = new byte[Constants.TEMPLATESIZE * 2];

    private final byte[] matdata = new byte[Constants.TEMPLATESIZE * 2];
    private int matsize = 0;

    private String refstring = "";
    private String matstring = "";
    private static final int CAMERA_REQUEST = 1001;
    private static final int CAMERA_PERMISSION = 2001;
    private boolean wasFrontCamera;
    private int worktype = 0;

    private TextView tvDevStatu, tvFpStatu, tvFpData, tvFpType;
    private ImageView ivFpImage = null;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        initView();
        tvDevStatu.setText(String.valueOf(fpm.getDeviceType()));

        int i = fpm.InitMatch();
        Log.d("MainActivity", "i:" + i);

        fpm.SetContextHandler(this, mHandler);
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);
        checkCameraPermission();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.FPM_DEVICE:
                    switch (msg.arg1) {
                        case Constants.DEV_OK:
                            tvFpStatu.setText("Open Device OK");
                            break;
                        case Constants.DEV_FAIL:
                            tvFpStatu.setText("Open Device Fail");
                            break;
                        case Constants.DEV_ATTACHED:

                            tvFpStatu.setText("USB Device Attached");
                            break;
                        case Constants.DEV_DETACHED:
                            tvFpStatu.setText("USB Device Detached");
                            break;
                        case Constants.DEV_CLOSE:
                            tvFpStatu.setText("Device Close");
                            break;
                    }
                    break;
                case Constants.FPM_PLACE:
                    tvFpStatu.setText("Place Finger");
                    Toast.makeText(MainActivity.this, "Place Finger", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.FPM_LIFT:
                    tvFpStatu.setText("Lift Finger");
                    Toast.makeText(MainActivity.this, "Lift Finger", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.FPM_GENCHAR: {
                    if (msg.arg1 == 1) {
                        if (worktype == 0) {
                            tvFpStatu.setText("Generate Template OK");
                            matsize = fpm.GetTemplateByGen(matdata);
                            //选择模板类型
                            switch (radioGroup.getCheckedRadioButtonId()) {
                                case R.id.radio1:
                                    Log.d("data type", "type:" + ConversionsEx.getInstance().GetDataType(matdata));
                                    matstring = ConversionsEx.getInstance().ToAnsiIso(matdata, ConversionsEx.ANSI_378_2004, ConversionsEx.COORD_MIRRORV);
                                    //Log.d("test", "handleMessage: Test "+ Base64.encodeToString(matdata,0));
                                    //Log.d("test", "handleMessage: Test "+matstring);
                                    break;
                                case R.id.radio2:
                                    matstring = ConversionsEx.getInstance().ToAnsiIso(matdata, ConversionsEx.ISO_19794_2005, ConversionsEx.COORD_MIRRORV);
                                    //Log.d("test", "handleMessage: Test "+matstring);
                                    saveIsoBase64ToTxt(matstring);
                                    copyToClipboard(matstring);
                                    uploadDataToWeb(matstring);
                                    break;
                                case R.id.radio3:
                                    matstring = Base64.encodeToString(matdata, 0);
                                    //matstring = ConversionsEx.getInstance().ToAnsiIso(matdata, ConversionsEx.ISO_19794_2005, ConversionsEx.COORD_MIRRORV);
                                    Log.d("test-matstring", "handleMessage: Test " + matstring);
                                    break;
                            }
                            tvFpData.setText(matstring);
                            int sc = MatchIsoTemplateStr(refstring, matstring);
                            tvFpStatu.setText("Match Result:" + String.valueOf(sc) + "/" + String.valueOf(FPMatch.getInstance().MatchTemplate(refdata, matdata)));

                        } else {
                            tvFpStatu.setText("Enrol Template OK");
                            int refsize = fpm.GetTemplateByGen(refdata);
                            //显示传感器输出的指纹模板类型
                            tvFpType.setText("raw FP template type: " + String.valueOf(ConversionsEx.getInstance().GetDataType(refdata)));
                            //选择模板类型
                            switch (radioGroup.getCheckedRadioButtonId()) {
                                case R.id.radio1:
                                    //if(fpm.getDeviceType()==Constants.DEV_7_3G_SPI){
                                    refstring = ConversionsEx.getInstance().ToAnsiIso(refdata, ConversionsEx.ANSI_378_2004, ConversionsEx.COORD_MIRRORV);
                                    break;
                                case R.id.radio2:
                                    //if(fpm.getDeviceType()==Constants.DEV_7_3G_SPI){
                                    refstring = ConversionsEx.getInstance().ToAnsiIso(refdata, ConversionsEx.ISO_19794_2005, ConversionsEx.COORD_MIRRORV);
                                    //Log.d("test", "handleMessage: "+refstring);
                                    break;
                                case R.id.radio3:
                                    refstring = Base64.encodeToString(refdata, 0);
                                    Log.d("test-refstring", "handleMessage: Test " + refstring);
                                    break;
                            }
                            tvFpData.setText(refstring);
                        }
                    } else {
                        tvFpStatu.setText("Generate Template Fail");
                    }
                }
                break;
                case Constants.FPM_NEWIMAGE: {
                    bmpsize = fpm.GetBmpImage(bmpdata);
                    Bitmap bm1 = BitmapFactory.decodeByteArray(bmpdata, 0, bmpsize);
                    ivFpImage.setImageBitmap(bm1);
                    saveBitmap(bm1);
                }
                break;
                case Constants.FPM_TIMEOUT:
                    tvFpStatu.setText("Time Out");
                    break;
            }
        }
    };

    private void uploadDataToWeb(String matstring) {
        senData(matstring, new OnIsoDataSubmit() {
            @SuppressLint("NewApi")
            @Override
            public void onSubmit(String value) {
//                Toast.makeText(MainActivity.this, "FPM_NEWIMAGE1", Toast.LENGTH_SHORT).show();

                bmpsize = fpm.GetBmpImage(bmpdata);
                Bitmap bm1 = BitmapFactory.decodeByteArray(bmpdata, 0, bmpsize);
                String base64 = bm1 != null ? bitmapToBase64(bm1) : "";
                webView.evaluateJavascript(
                        "javascript:onBiometricCompletion('" + base64 + "')",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                // Toast.makeText(MainActivity.this, "onBiometricCompletion", Toast.LENGTH_SHORT).show();
                                Log.e("onBiometricCompletion", value);
                                Log.d("ISO_DATA", "onBiometricCompletion: " + value);
                            }
                        }
                );
            }
        });

    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private void senData(final String datas, final OnIsoDataSubmit callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                webView.post(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        webView.evaluateJavascript(
                                "collectIsoData(" + JSONObject.quote(datas) + ")",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        callback.onSubmit(value);
                                    }
                                }
                        );
                    }
                });
            }
        }).start();
    }

    private void saveBitmap(Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            File dir = new File(
                    getExternalFilesDir(null),
                    "Fingerprints"
            );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(
                    dir,
                    "finger_" + System.currentTimeMillis() + ".png"
            );

            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();

//            Toast.makeText(this,
//                    "Bitmap saved:\n" + file.getAbsolutePath(),
//                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(this,
//                    "Failed to save bitmap",
//                    Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText("copied_text", text);
        clipboard.setPrimaryClip(clip);

//        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("NewApi")
    private void saveIsoBase64ToTxt(String isoBase64) {
//        Toast.makeText(MainActivity.this, isoBase64, Toast.LENGTH_SHORT).show();
        try {
            String fileName = "finger_iso_" + System.currentTimeMillis() + ".txt";
            File file = new File(getExternalFilesDir(null), fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(isoBase64.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();

            Log.d("ISO_SAVE", "Saved at: " + file.getAbsolutePath());

        } catch (Exception e) {
//            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public int MatchIsoTemplateByte(byte[] piFeatureA, byte[] piFeatureB) {
        byte adat[] = new byte[512];
        byte bdat[] = new byte[512];
        int sc = 0;
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radio1:
                ConversionsEx.getInstance().AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ANSI_378_2004);
                ConversionsEx.getInstance().AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ANSI_378_2004);
                return FPMatch.getInstance().MatchTemplate(adat, bdat);
            case R.id.radio2:
                ConversionsEx.getInstance().AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005);
                ConversionsEx.getInstance().AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005);
                return FPMatch.getInstance().MatchTemplate(adat, bdat);
            case R.id.radio3:
                //如果硬件直接设置为ISO模板，则指纹模块直接返回ISO数据，需要将其转换成私有模板
				/*ConversionsEx.getInstance().AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005);
				ConversionsEx.getInstance().AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005);
				return FPMatch.getInstance().MatchTemplate(adat, bdat);
				*/
                //硬件设置为私有，指纹返回私有格式，直接传入比对函数
                return FPMatch.getInstance().MatchTemplate(piFeatureA, piFeatureB);
        }
        return 0;
    }

    public int MatchIsoTemplateStr(String strFeatureA, String strFeatureB) {
        byte piFeatureA[] = Base64.decode(strFeatureA, Base64.DEFAULT);
        byte piFeatureB[] = Base64.decode(strFeatureB, Base64.DEFAULT);

        return MatchIsoTemplateByte(piFeatureA, piFeatureB);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fpm.ResumeRegister();
        fpm.OpenDevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fpm.PauseUnRegister();
        fpm.CloseDevice();
    }

    private void initView() {
        webView = (WebView) findViewById(R.id.mainWebview);

        tvDevStatu = (TextView) findViewById(R.id.textView1);
        tvFpStatu = (TextView) findViewById(R.id.textView2);
        tvFpData = (TextView) findViewById(R.id.textView3);
        tvFpType = (TextView) findViewById(R.id.textView4);
        ivFpImage = (ImageView) findViewById(R.id.imageView1);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);

        final Button btn_enrol = (Button) findViewById(R.id.button1);
        final Button btn_capture = (Button) findViewById(R.id.button2);

        btn_enrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fpm.GenerateTemplate(2)) {
                    worktype = 1;
                } else {
//					Toast.makeText(MainActivity.this, "Busy", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fpm.GenerateTemplate(1)) {
                    worktype = 0;
                } else {
//					Toast.makeText(MainActivity.this, "Busy", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //checked radio3
        radioGroup.check(R.id.radio2);
        loadWebsite();
    }

    @SuppressLint("NewApi")
    private void loadWebsite() {
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
//        settings.setPluginState(WebSettings.PluginState.ON);
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();

        WebView.setWebContentsDebuggingEnabled(true);

        // REQUIRED for HTTPS (Google, Cloudflare, etc.)
//        webView.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return false;
//            }
//
//            // OPTIONAL: ignore SSL errors (⚠️ only for testing)
//            @Override
//            public void onReceivedSslError(
//                    WebView view,
//                    SslErrorHandler handler,
//                    SslError error
//            ) {
//                handler.proceed();
//            }
//        });

        // JS Bridge
        webView.addJavascriptInterface(new TWJSBridge(this, ""), "TWJSBridge");

        webView.getSettings().setSupportMultipleWindows(false);
        WebView.enableSlowWholeDocumentDraw();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error) {
                Log.e("WEBVIEW_ERR",
                        error.getErrorCode() + " : " + error.getDescription());
            }

            @Override
            public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceResponse errorResponse) {
                Log.e("HTTP_ERR",
                        errorResponse.getStatusCode() + "");
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("JS_CONSOLE", consoleMessage.message());
                return true;
            }
        });

        settings.setDatabaseEnabled(true);


        // Load URL
        webView.loadUrl(
                "https://mbibtest.askaribank.com.pk/AccountOpeningApp"
//                "https://0a091af61f05.ngrok-free.app/"
        );
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Processing image...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION
            );
            return false;
        }

        return true;
    }

    private void openCamera(boolean front) {
        boolean checkCameraPermission = checkCameraPermission();
        if (!checkCameraPermission) return;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        wasFrontCamera = front;

        if (front) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        }

        File imageFile = new File(getCacheDir(), "camera.jpg");
        imageUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                imageFile
        );

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("IsFrontCamera", front);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openCamera(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                capturedBitmap = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        imageUri
                );
                onImageCaptured(capturedBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void onImageCaptured(final Bitmap bitmap) {

        // UI thread → show loader
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress();
            }
        });

        // 🔵 BACKGROUND THREAD (heavy work)
        new Thread(new Runnable() {
            @Override
            public void run() {

                final String type = wasFrontCamera ? "FRONT_CAMERA" : "BACK_CAMERA";
                Log.e("onImageCaptured", type);

                String base64 = bitmap != null ? bitmapToBase64(bitmap) : "";

                // Escape base64 for JS safety
                base64 = base64.replace("\\", "\\\\")
                        .replace("'", "\\'")
                        .replace("\n", "");

                final String js =
                        "javascript:onImageReceived('" + type + "','" + base64 + "')";

                // 🔴 BACK TO UI THREAD (WebView + dialog)
                runOnUiThread(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {

                        webView.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {

                                dismissProgress(); // ✅ dismiss loader

                                Log.d("JS_CALLBACK",
                                        "onImageReceived returned: " + value);

                                if (value == null || "null".equals(value)) {
                                    Log.e("JS_CALLBACK", "JS execution failed");
                                    return;
                                }

                                if ("true".equalsIgnoreCase(
                                        value.replace("\"", ""))) {
                                    Log.d("JS_CALLBACK",
                                            "Image received successfully");
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    public void onWebButtonClicked(@NonNull String buttonId) {
//        Toast.makeText(this, "Button ID: " + buttonId, Toast.LENGTH_SHORT).show();
        Log.e("onWebButtonClicked", buttonId);
        if (buttonId.contains("FINGERPRINT")) {
            boolean generateTemplate = fpm.GenerateTemplate(1);
            Log.e("GenerateTemplate", "" + generateTemplate);

        } else if (buttonId.contains("FRONT_CAMERA")) {
            Toast.makeText(this, "Opening Front Camera" + buttonId, Toast.LENGTH_SHORT).show();
            openCamera(true);
        } else if (buttonId.contains("BACK_CAMERA")) {
            openCamera(false);
            Toast.makeText(this, "Opening Back Camera" + buttonId, Toast.LENGTH_SHORT).show();

        }
    }


}
