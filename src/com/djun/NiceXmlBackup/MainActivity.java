package com.djun.NiceXmlBackup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	private Main main = null;
	private Context mContext = null;
	private Intent chooserIntent;
	private EditText editTarget;
	private Button btnBrowse, btnBackup, btnRestore;

	//private Handler handler;

	private String targetFile = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		getConstance();
		findViewGroup();
		setListeners();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (main != null) {
			if (main.fileName != null) {
				targetFile = main.fileName;
				editTarget.setText(targetFile);
				editTarget.requestFocus();
				editTarget.setSelection(editTarget.getText().length());
			}
		}
	}

	private void getConstance() {
		main = (Main) this.getApplication();
		mContext = this;
		chooserIntent = new Intent(this, FileChooser.class);
	}

	private void findViewGroup() {
		editTarget = (EditText) findViewById(R.id.edit_target);
		btnBrowse = (Button) findViewById(R.id.button_browse);
		btnBackup = (Button) findViewById(R.id.button_backup);
		btnRestore = (Button) findViewById(R.id.button_restore);
	}

	private void setListeners() {
		btnBrowse.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(chooserIntent);
			}
		});

		btnBackup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO
				main.showDevelopingMessage(mContext);
			}
		});

		btnRestore.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO
				main.showDevelopingMessage(mContext);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}