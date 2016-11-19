package edu.umb.subway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class OutgoingReceiver extends BroadcastReceiver {
	
	public static final String CUSTOM_INTENT = "edu.umb.cs443.intent.action.TEST";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("CustomReceiver", "HIT OUTGOING "+intent.getExtras().getString(TelephonyManager.EXTRA_STATE));
		Intent i = new Intent();
		i.setAction(CUSTOM_INTENT);
		context.sendBroadcast(i);
	}
}
