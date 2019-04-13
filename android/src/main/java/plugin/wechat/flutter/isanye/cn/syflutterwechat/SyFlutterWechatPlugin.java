package plugin.wechat.flutter.isanye.cn.syflutterwechat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.maikg.city.social.wxapi.WXManager;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * SyFlutterWechatPlugin
 */
public class SyFlutterWechatPlugin implements MethodCallHandler {

    private static final String TAG = "SyFlutterWechatPlugin>>";
    public static final String filterName = "wxCallback";
    private IWXAPI wxApi;
    private Registrar registrar;
    private static Result result;
    private static final int THUMB_SIZE = 150;

    public static final String loginFilterName = "wxAuthCallback1";


    //微信支付回调
    private static BroadcastReceiver wxpayCallbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (result == null || intent == null) {
                return;
            }
            Integer errCode = intent.getIntExtra("errCode", -3);
            Log.e(TAG, errCode.toString());
            result.success(errCode);
        }
    };

    //微信授权
    private static BroadcastReceiver wxAuthCallbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (result == null || intent == null) {
                return;
            }

            boolean isSuccess = intent.getBooleanExtra("isSuccess", false);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("isSuccess", isSuccess);
            if (isSuccess) {
                String openid = intent.getStringExtra("openId");
                String nickname = intent.getStringExtra("nickName");
                String headImgUrl = intent.getStringExtra("headImgUrl");
                String accessToken = intent.getStringExtra("accessToken");
                String sex = intent.getStringExtra("sex");
                paramMap.put("openId", openid);
                paramMap.put("nickName", nickname);
                paramMap.put("headImgUrl", headImgUrl);
                paramMap.put("accessToken", accessToken);
                paramMap.put("sex", sex);
                result.success(paramMap);
            } else {
                result.success(paramMap);
            }
        }
    };

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "sy_flutter_wechat");
        final SyFlutterWechatPlugin plugin = new SyFlutterWechatPlugin(registrar);
        channel.setMethodCallHandler(plugin);
        registrar.context().registerReceiver(wxpayCallbackReceiver, new IntentFilter(filterName));
        registrar.context().registerReceiver(wxAuthCallbackReceiver, new IntentFilter(loginFilterName));

        WXManager.getInstance().init(registrar.context());
    }

    private SyFlutterWechatPlugin(Registrar registrar) {
        this.registrar = registrar;
    }


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        SyFlutterWechatPlugin.result = null;
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "register":
                this.registerToWX(call, result);
                break;
            case "shareText":
                this.shareText(call, result);
                break;
            case "shareImage":
                this.shareImage(call, result);
                break;
            case "shareWebPage":
                this.shareWebPage(call, result);
                break;
            case "pay":
                SyFlutterWechatPlugin.result = result;
                this.pay(call);
                break;
            case "auth":
                SyFlutterWechatPlugin.result = result;
                this.wxAuth();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    //注册微信app id
    private void registerToWX(MethodCall call, final Result result) {
        String appId = call.argument("appId");
        String secret = call.argument("secret");

        wxApi = WXManager.getInstance().register(registrar.context(), appId, secret, new WXManager.ResultCallback() {
            @Override
            public void onHandle(boolean success) {
                result.success(success);
            }
        });
    }

    private void shareText(MethodCall call, Result result) {
//        String text = call.argument("text");
//        String shareType = call.argument("shareType");
//
//        WXTextObject textObj = new WXTextObject();
//        textObj.text = text;
//
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = textObj;
//        msg.description = text;
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.scene = _convertShareType(shareType);
//        req.message = msg;
//        //req.transaction = buildTransaction("");
//        boolean res = wxApi.sendReq(req);
//        result.success(res);
    }

    private void shareImage(final MethodCall call, final Result result) {
//        final String imageUrl = call.argument("imageUrl");
//        final String shareType = call.argument("shareType");
//        new Thread(new Runnable() {
//            WXMediaMessage msg = new WXMediaMessage();
//
//            @Override
//            public void run() {
//                try {
//                    Bitmap bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
//                    WXImageObject imageObject = new WXImageObject(bmp);
//                    Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
//
//                    msg.mediaObject = imageObject;
//                    bmp.recycle();
//                    msg.thumbData = SyFlutterWechatPlugin.bmpToByteArray(thumbBmp, true);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                SendMessageToWX.Req req = new SendMessageToWX.Req();
//                req.scene = _convertShareType(shareType);
//                req.message = msg;
//                boolean res = wxApi.sendReq(req);
//                result.success(res);
//            }
//        }
//        ).start();
    }

    private void shareWebPage(final MethodCall call, final Result result) {
        String title = call.argument("title");
        String description = call.argument("description");
        String imageUrl = call.argument("imageUrl");
        String webPageUrl = call.argument("webPageUrl");
        String shareType = call.argument("shareType");

        WXManager.getInstance().shareWebPage(title, description, imageUrl, webPageUrl, _convertShareType(shareType), new WXManager.ResultCallback() {
            @Override
            public void onHandle(boolean success) {
                result.success(success);
            }
        });
    }

    //调起微信支付
    private void pay(MethodCall call) {
//        PayReq req = new PayReq();
//        req.appId = call.argument("appid");
//        req.partnerId = call.argument("partnerid");
//        req.prepayId = call.argument("prepayid");
//        req.packageValue = call.argument("package");
//        req.nonceStr = call.argument("noncestr");
//        req.timeStamp = call.argument("timestamp");
//        req.sign = call.argument("sign");
//        wxApi.sendReq(req);
    }


    private static int _convertShareType(String shareType) {
        switch (shareType) {
            case "session":
                return SendMessageToWX.Req.WXSceneSession;
            case "timeline":
                return SendMessageToWX.Req.WXSceneTimeline;
            case "favorite":
                return SendMessageToWX.Req.WXSceneFavorite;
            default:
                return SendMessageToWX.Req.WXSceneSession;
        }
    }

    //微信登录
    private void wxAuth() {
//        SendAuth.Req req = new SendAuth.Req();
//        req.scope = "snsapi_userinfo";
//        req.state = "none";
//        wxApi.sendReq(req);
    }
}
