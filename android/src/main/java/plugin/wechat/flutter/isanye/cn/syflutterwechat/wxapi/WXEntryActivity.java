package plugin.wechat.flutter.isanye.cn.syflutterwechat.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;


import cz.msebera.android.httpclient.Header;
import plugin.wechat.flutter.isanye.cn.syflutterwechat.SyFlutterWechatPlugin;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "WXEntryActivity";
    //微信支付
    private IWXAPI iwxapi;

    private String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";

    private String GetUserInfoRequest = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iwxapi = StateManager.getAPi();
        iwxapi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        iwxapi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.e(TAG,"req");
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.e(TAG,"微信授权回调");
        if(baseResp != null){
            if(baseResp.getType() == ConstantsAPI.COMMAND_SENDAUTH){
                switch (baseResp.errCode){
                    case BaseResp.ErrCode.ERR_OK:
                        Log.e(TAG, "成功");
                        String code = ((SendAuth.Resp) baseResp).code;
                        String get_access_token = getCodeRequest(code);
                        getAccessToken(get_access_token);
                        break;
                    default:
                        Log.e(TAG, "取消");
                        break;
                }
            }
        }

        finish();
    }

    private void getAccessToken(String url){
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers,response);
                try {
                    if (!response.equals("")) {
                        String access_token = response.getString("access_token");
                        String openid = response.getString("openid");
                        String get_user_info_url = getUserInfoUrl(access_token, openid);
                        getUserInfo(get_user_info_url, access_token);
                    }else{
                        sendResult(false,"" , "", "", "", "");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void getUserInfo(String url, final String accessToken){
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers,response);
                try {
                    if (!response.equals("")) {
                        String openid = response.getString("openid");
                        String nickname = response.getString("nickname");
                        String headImgUrl = response.getString("headimgurl");
                        String sex = response.getString("sex");

                        sendResult(true, openid, nickname, headImgUrl, accessToken, sex);
                    }else{
                        sendResult(false, "", "", "", "", "");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendResult(boolean isSuccess, String openId, String nickName, String headImgUrl, String accessToken, String sex){
        Intent i = new Intent(SyFlutterWechatPlugin.loginFilterName);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("isSuccess", isSuccess);
        i.putExtra("openId", openId);
        i.putExtra("nickName", nickName);
        i.putExtra("headImgUrl", headImgUrl);
        i.putExtra("accessToken", accessToken);
        i.putExtra("sex", sex);
        WXEntryActivity.this.sendBroadcast(i);
    }

    /**
     * 获取用户个人信息的URL（微信）
     *
     * @param access_token
     *            获取access_token时给的
     * @param openid
     *            获取access_token时给的
     * @return URL
     */
    private String getUserInfoUrl(String access_token, String openid) {
        String result;
        GetUserInfoRequest = GetUserInfoRequest.replace("ACCESS_TOKEN",
                urlEncodeUTF8(access_token));
        GetUserInfoRequest = GetUserInfoRequest.replace("OPENID", urlEncodeUTF8(openid));
        result = GetUserInfoRequest;
        return result;
    }

    /**
     * 获取access_token的URL（微信）
     *
     * @param code
     *            授权时，微信回调给的
     * @return URL
     */
    private String getCodeRequest(String code) {
        String result;
        GetCodeRequest = GetCodeRequest.replace("APPID",
                urlEncodeUTF8(StateManager.wxAppId));
        GetCodeRequest = GetCodeRequest.replace("SECRET",
                urlEncodeUTF8(StateManager.wxSecret));
        GetCodeRequest = GetCodeRequest.replace("CODE", urlEncodeUTF8(code));
        result = GetCodeRequest;
        return result;
    }

    private String urlEncodeUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
