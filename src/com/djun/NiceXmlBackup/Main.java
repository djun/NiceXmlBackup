package com.djun.NiceXmlBackup;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class Main extends Application {

	String fileName = null;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	// 显示开发提示信息
	public void showDevelopingMessage(Context context) {
		Dialog dialog = new AlertDialog.Builder(context)
				.setTitle(getString(R.string.hint_title))
				.setMessage(getString(R.string.developing_msg))
				.setPositiveButton(getString(R.string.ok_title),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		dialog.show();
	}

}
