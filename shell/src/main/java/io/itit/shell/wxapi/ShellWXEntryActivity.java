package io.itit.shell.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hwangjr.rxbus.RxBus;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import java.util.HashMap;
import java.util.Map;

import io.itit.androidlibrary.Consts;
import io.itit.shell.ShellApp;

public class ShellWXEntryActivity extends Activity implements IWXAPIEventHandler {
	IWXAPI api;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ITIT","onCreate");
		api  = ShellApp.getWx(this);
		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		Log.d("ITIT","onReq");
		switch (req.getType()) {
			case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
				break;
			case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
				break;
			default:
				break;
		}
		finish();
	}

	@Override
	public void onResp(BaseResp resp) {
		int result = 0;
		String code = "";
		Log.d("ITIT","resp:"+ JSON.toJSONString(resp));
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			switch (resp.errCode) {
				case BaseResp.ErrCode.ERR_OK:
					//pay
					break;
				case BaseResp.ErrCode.ERR_USER_CANCEL:
					break;
				case BaseResp.ErrCode.ERR_AUTH_DENIED:
					break;
				default:
					break;
			}
		} else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
			switch (resp.errCode) {
				case BaseResp.ErrCode.ERR_OK:
					Map<String,String> resMap = new HashMap<>();
					resMap.put("funcName","weixinShare");
					resMap.put("errCode",resp.errCode+"");
					//UnityPlayer.UnitySendMessage("Canvas","callNativeResult", JSON.toJSONString(resMap));
					//share
					break;
				case BaseResp.ErrCode.ERR_USER_CANCEL:
					break;
				case BaseResp.ErrCode.ERR_AUTH_DENIED:
					break;
				default:
					break;
			}
		}else {
			switch (resp.errCode) {
				case BaseResp.ErrCode.ERR_OK:
					code = ((SendAuth.Resp) resp).code;
					Toast.makeText(getApplicationContext(), "登录成功！", Toast.LENGTH_SHORT).show();
					Log.d("ITIT",code);
					Map<String,String> resMap = new HashMap<>();
					resMap.put("funcName","weixinLogin");
					resMap.put("errCode","0");
					resMap.put("code",code);
					RxBus.get().post(Consts.BusAction.LoginSuccess,code);
					//UnityPlayer.UnitySendMessage("Canvas","callNativeResult", JSON.toJSONString(resMap));
					//login
					break;
				case BaseResp.ErrCode.ERR_USER_CANCEL:
					break;
				case BaseResp.ErrCode.ERR_AUTH_DENIED:
					break;
				default:
					break;
			}
		}
		finish();
	}


}