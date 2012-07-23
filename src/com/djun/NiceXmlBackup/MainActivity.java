package com.djun.NiceXmlBackup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	private Main main = null;
	private Intent chooserIntent;
	private EditText editTarget;
	private Button btnBrowse;

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
		chooserIntent = new Intent(this, FileChooser.class);
	}

	private void findViewGroup() {
		editTarget = (EditText) findViewById(R.id.edit_target);
		btnBrowse = (Button) findViewById(R.id.button_browse);
	}

	private void setListeners() {
		btnBrowse.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(chooserIntent);
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK){
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}