package com.djun.NiceXmlBackup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class FileChooser extends Activity {

	private Main main = null;
	private LayoutInflater inflater = null;
	private EditText editFileName;
	private Button btnOk;
	private TextView textPath;
	private ListView listFiles;

	private File[] files = null;
	private ArrayList<File> fileList = null;
	private LinkedList<String> paths = null;

	static final String DEFAULT_PATH = "/";
	static final String PATH_RETURN = "..";
	static final String[] fileEndings = new String[] { ".xml" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_chooser);

		getConstance();
		findViewGroup();
		setListeners();

		init();
	}

	private void init() {
		paths.clear();
		visitPath(DEFAULT_PATH);
	}

	private void getConstance() {
		main = (Main) this.getApplication();
		inflater = this.getLayoutInflater();
		if (fileList == null) {
			fileList = new ArrayList<File>();
		}
		if (paths == null) {
			paths = new LinkedList<String>();
		}
	}

	private void findViewGroup() {
		editFileName = (EditText) findViewById(R.id.edit_filename);
		btnOk = (Button) findViewById(R.id.btn_ok);
		textPath = (TextView) findViewById(R.id.text_path);
		listFiles = (ListView) findViewById(R.id.list_file);
	}

	private void setListeners() {
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final StringBuilder sb=new StringBuilder();
				sb.append(editFileName.getText().toString());
				if (!checkEndsWithInStringArray(sb.toString(), fileEndings)){
					sb.append(".xml");
				}
				final String path=paths.getLast();
				if (path.equals(DEFAULT_PATH)){
					sb.insert(0, path);
				} else{
					sb.insert(0, path+"/");
				}
				
				main.fileName=sb.toString();
				finish();
			}
		});

		listFiles.setOnItemClickListener(fileItemClickListener);
	}

	public File[] getFiles(String path) {
		if (path == null || path.equals(""))
			return null;

		final File file = new File(path);
		File[] files = null;
		if (file != null) {
			files = file.listFiles();
		}

		return files;
	}

	private boolean checkEndsWithInStringArray(String checkItsEnd,
			String[] fileEndings) {
		for (String aEnd : fileEndings) {
			if (checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}

	Comparator<File> comp=new Comparator<File>() {
		public int compare(File lhs, File rhs) {
			final String l=lhs.getName();
			final String r=rhs.getName();
			return l.compareTo(r);
		}
	};
	
	private void FilesArrayToFilesArrayList(File[] files) {
		if (fileList == null) {
			fileList = new ArrayList<File>();
		}
		fileList.clear();

		if (files!=null){
			for (File file : files) {
				if (file.isDirectory()
						|| checkEndsWithInStringArray(file.getName(), fileEndings)) {
					fileList.add(file);
				}
			}
		}
		
		Collections.sort(fileList, comp);
	}

	private void visitPath(String path){
		if (!path.equals(PATH_RETURN))
			paths.addLast(path);
		else {
			if (paths.size()>1){
				paths.removeLast();
			}
		}
		
		final String realPath=paths.getLast();
		System.out.println("path="+realPath);//debug
		files = getFiles(realPath);
		FilesArrayToFilesArrayList(files);
		listFiles.setAdapter(new FileListAdapter(fileList));
		setShowPath(realPath);
	}
	
	private void setShowPath(String path) {
		if (path != null)
			textPath.setText(path);
		else
			textPath.setText("");
	}

	static class ViewHolder {
		TextView text;
		// File info;
	}

	class FileListAdapter extends BaseAdapter {

		//private Context mContext;
		private ArrayList<File> list;

		public FileListAdapter(ArrayList<File> list) {
			this.list = list;
		}
		
		/*
		public FileListAdapter(Context mContext, ArrayList<File> list) {
			this.mContext = mContext;
			this.list = list;
		}*/

		public int getCount() {
			return list.size();
		}

		public File getItem(int position) {
			return list.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.fileitem, null);
			}

			ViewHolder holder = null;
			if (convertView.getTag() != null) {
				holder = (ViewHolder) convertView.getTag();
			} else {
				holder = new ViewHolder();
				holder.text = (TextView) convertView
						.findViewById(R.id.file_item);
				convertView.setTag(holder);
			}

			final File file = list.get(position);
			final StringBuilder sb = new StringBuilder();
			if (file.isDirectory()) {
				sb.append(getString(R.string.folder_prefix));
			}
			sb.append(file.getName());
			holder.text.setText(sb.toString());
			// holder.info = file;

			return convertView;
		}
	}

	OnItemClickListener fileItemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final File file=fileList.get(position);
			if (file.isDirectory()){
				visitPath(file.getPath());
			} else{
				editFileName.setText(file.getName());
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK){
			if (paths.size()>1){
				visitPath(PATH_RETURN);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
