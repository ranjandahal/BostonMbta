package edu.umb.subway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class IncomingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(OutgoingReceiver.CUSTOM_INTENT)) {
			Log.v("CustomReceiver", "GOT THE INTENT");
		}
	}
}
