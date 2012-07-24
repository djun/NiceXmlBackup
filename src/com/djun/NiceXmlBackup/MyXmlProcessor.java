package com.djun.NiceXmlBackup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class MyXmlProcessor {

	private Context mContext;
	private Handler mHandler;
	private ProgressDialog progressDialog;

	// for dialog
	static final int CREATE_PROGRESS_DIALOG = 1;
	static final int UPDATE_PROGRESS = 2;
	static final int DISMISS_PROGRESS_DIALOG = 3;

	MyXmlProcessor(Context context) {
		this.mContext = context;
		init();
	}

	private void init() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CREATE_PROGRESS_DIALOG:
					break;
				case UPDATE_PROGRESS:
					break;
				case DISMISS_PROGRESS_DIALOG:
					break;
				default:
					super.handleMessage(msg);
				}
			}
		};
	}

	// TODO 处理xml的两个方法写在这里

	private void createProgressDialog() {

	}

	private void setProgress(int value) {

	}

	private void dismissProgressDialog() {

	}
}
