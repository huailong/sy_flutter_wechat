package plugin.wechat.flutter.isanye.cn.syflutterwechat;

import com.maikg.city.wxapi.WXManager;

import org.json.JSONObject;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * SyFlutterWechatPlugin
 */
public class SyFlutterWechatPlugin implements MethodCallHandler {

    private Registrar registrar;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "sy_flutter_wechat");
        final SyFlutterWechatPlugin plugin = new SyFlutterWechatPlugin(registrar);
        channel.setMethodCallHandler(plugin);

        WXManager.getInstance().init(registrar.context());
    }

    private SyFlutterWechatPlugin(Registrar registrar) {
        this.registrar = registrar;
    }


    @Override
    public void onMethodCall(MethodCall call, Result result) {

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
                this.pay(call, result);
                break;
            case "auth":
                this.wxAuth(result);
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

        WXManager.getInstance().register(registrar.context(), appId, secret, new WXManager.ResultCallback() {
            @Override
            public void onHandle(boolean success) {
                result.success(success);
            }
        });
    }

    private void shareText(MethodCall call, final Result result) {
        String text = call.argument("text");
        String shareType = call.argument("shareType");

        WXManager.getInstance().shareText(text, shareType, new WXManager.ResultCallback() {
            @Override
            public void onHandle(boolean res) {
                result.success(res);
            }
        });
    }

    private void shareImage(final MethodCall call, final Result result) {
        final String imageUrl = call.argument("imageUrl");
        final String shareType = call.argument("shareType");

        WXManager.getInstance().shareImage(imageUrl, shareType, new WXManager.ResultCallback() {
            @Override
            public void onHandle(boolean res) {
                result.success(res);
            }
        });
    }

    private void shareWebPage(final MethodCall call, final Result result) {
        String title = call.argument("title");
        String description = call.argument("description");
        String imageUrl = call.argument("imageUrl");
        String webPageUrl = call.argument("webPageUrl");
        String shareType = call.argument("shareType");

        WXManager.getInstance().shareWebPage(title, description, imageUrl, webPageUrl, shareType, new WXManager.ResultCallback() {
            @Override
            public void onHandle(boolean success) {
                result.success(success);
            }
        });
    }

    //调起微信支付
    private void pay(MethodCall call, final Result result) {
        String appId = call.argument("appid");
        String partnerId = call.argument("partnerid");
        String prepayId = call.argument("prepayid");
        String packageValue = call.argument("package");
        String nonceStr = call.argument("noncestr");
        String timeStamp = call.argument("timestamp");
        String sign = call.argument("sign");
        
        WXManager.getInstance().pay(appId, partnerId, prepayId, packageValue, nonceStr, timeStamp, sign, new WXManager.DataCallback<Integer>() {
            @Override
            public void onGet(Integer data) {
                result.success(data);
            }
        });
    }

    //微信登录
    private void wxAuth(final Result result) {
        WXManager.getInstance().auth(new WXManager.DataCallback<JSONObject>() {
            @Override
            public void onGet(JSONObject data) {
                result.success(data);
            }
        });
    }
}
