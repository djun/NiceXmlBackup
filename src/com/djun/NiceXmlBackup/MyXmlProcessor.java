package com.djun.NiceXmlBackup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

public class MyXmlProcessor {

	private Context mContext;
	private Handler mHandler;
	private ProgressDialog progressDialog;

	private XmlPullParser parser = null;
	private FileInputStream fIS = null;

	// for xml
	private int tagState = STATE_NONE;
	private boolean inItem = false;
	static final int STATE_NONE = 100;
	static final int STATE_SMSS = 101;
	static final int STATE_CONTACT = 102;
	static final int STATE_CALLLOG = 103;
	// for jeyo's xml
	static final String TAG_JEYO = "jeyo";
	static final String TAG_SMSS = "smss";
	static final String TAG_CONTACT = "contacts";
	static final String TAG_CALLLOG = "calllogs";
	// static final String TAG_OPTION="options";
	static final String TAG_ITEM = "item";
	// ---smss
	static final String TAG_SENDER = "sender"; // name+number
	static final String TAG_RECEIVER = "receiver"; // name+number
	static final String TAG_MSGCLASS = "msgClass"; // ֻ��������ࣺIPM.SMStext
	static final String TAG_SUBJECT = "subject";
	// static final String TAG_BODY = "body";
	static final String TAG_DELIVERTIME = "delivertime";
	static final String TAG_LASTMODIFYTIME = "lastmodifytime";
	// ---contact
	static final String TAG_FIRSTNAME = "firstname";
	static final String TAG_LASTNAME = "lastname";
	static final String TAG_MIDDLENAME = "middlename";
	static final String TAG_MOBILETELEPHONENUMBER = "mobiletelephonenumber";
	static final String TAG_HOMETELEPHONENUMBER = "hometelephonenumber";
	static final String TAG_HOME2TELEPHONENUMBER = "home2telephonenumber";
	static final String TAG_HOMEFAXNUMBER = "homefaxnumber";
	static final String TAG_BUSINESSTELEPHONENUMBER = "businesstelephonenumber";
	static final String TAG_BUSINESS2TELEPHONENUMBER = "business2telephonenumber";
	static final String TAG_BUSINESSFAXNUMBER = "businessfaxnumber";
	static final String TAG_EMAIL1 = "email1address";
	static final String TAG_EMAIL2 = "email2address";
	static final String TAG_EMAIL3 = "email3address";
	static final String TAG_COMPANYNAME = "companyname";
	static final String TAG_BODY = "body"; // �������ظ� // ���Ǳ�ע��
	// ---calllog
	static final String TAG_STARTTIME = "starttime";
	static final String TAG_ENDTIME = "endtime";
	static final String TAG_OUTGOING = "outgoing";
	static final String TAG_CONNECTED = "connected";
	static final String TAG_NUMBER = "number"; // name+number

	// for dialog
	static final int CREATE_PROGRESS_DIALOG = 1;
	static final int UPDATE_PROGRESS = 2;
	static final int SET_PROGRESS_TEXT = 3;
	static final int DISMISS_PROGRESS_DIALOG = 4;

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
					createProgressDialog();
					break;
				case UPDATE_PROGRESS:
					// TODO setProgress(value);
					break;
				case SET_PROGRESS_TEXT:
					// TODO setProgressText(str);
					break;
				case DISMISS_PROGRESS_DIALOG:
					dismissProgressDialog();
					// TODO ��ʾ�������Ի���
					break;
				default:
					super.handleMessage(msg);
				}
			}
		};
	}

	// ׼����xml
	public void readyToReadXmlFile(String fileFullName) {
		try {
			fIS = new FileInputStream(fileFullName);
			parser.setInput(fIS, "UTF-8");
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			cancelReadXmlFile();
		} catch (XmlPullParserException e) {
			// e.printStackTrace();
			cancelReadXmlFile();
		}
	}

	// ���������xml����
	public void processingReadingXmlFile() {
		final SmssItem sItem = new SmssItem();
		final ContactItem cItem = new ContactItem();
		final CallLogItem clItem = new CallLogItem();

		try {
			int event;
			event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				switch (event) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					switch (tagState) {
					case STATE_SMSS:
						if (TAG_ITEM.equals(parser.getName())) {
							sItem.resetData();
						} else if (TAG_SENDER.equals(parser.getName())) {
							sItem.sender = formatTargetNumber(parser.nextText());
						} else if (TAG_RECEIVER.equals(parser.getName())) {
							sItem.receiver = formatTargetNumber(parser
									.nextText());
						} else if (TAG_MSGCLASS.equals(parser.getName())) {
							if (!SmssItem.IPM_SMSTEXT.equals(parser.nextText())) { // �Ƕ���Ϣ������
								inItem = false;
							} else {
								sItem.msgClass = SmssItem.IPM_SMSTEXT;
							}
						} else if (TAG_SUBJECT.equals(parser.getName())) {
							sItem.subject = parser.nextText();
						} else if (TAG_DELIVERTIME.equals(parser.getName())) {
							sItem.deliverTime = formatDateString(parser
									.nextText());
						} else if (TAG_LASTMODIFYTIME.equals(parser.getName())) {
							sItem.lastModifyTime = formatDateString(parser
									.nextText());
						}

						break;
					case STATE_CONTACT:
						if (TAG_ITEM.equals(parser.getName())) {
							cItem.resetData();
						}
						// TODO δ���
						break;
					case STATE_CALLLOG:
						if (TAG_ITEM.equals(parser.getName())) {
							clItem.resetData();
						} else if (TAG_STARTTIME.equals(parser.getName())) {
							clItem.startTime = formatDateString(parser
									.nextText());
						} else if (TAG_ENDTIME.equals(parser.getName())) {
							clItem.endTime = formatDateString(parser.nextText());
						} else if (TAG_OUTGOING.equals(parser.getName())) {
							final String str = parser.nextText();
							if ("1".equals(str)) {
								clItem.outGoing = true;
							} else {
								clItem.outGoing = false;
							}
						} else if (TAG_CONNECTED.equals(parser.getName())) {
							final String str = parser.nextText();
							if ("1".equals(str)) {
								clItem.connected = true;
							} else {
								clItem.connected = false;
							}
						} else if (TAG_NUMBER.equals(parser.getName())) {
							clItem.number = formatTargetNumber(parser
									.nextText());
						}

						break;
					case STATE_NONE:
						if (TAG_SMSS.equals(parser.getName())) {
							tagState = STATE_SMSS;
						} else if (TAG_CONTACT.equals(parser.getName())) {
							tagState = STATE_CONTACT;
						} else if (TAG_CALLLOG.equals(parser.getName())) {
							tagState = STATE_CALLLOG;
						}
						break;
					}
					break;
				case XmlPullParser.END_TAG:
					if (TAG_SMSS.equals(parser.getName())
							|| TAG_CONTACT.equals(parser.getName())
							|| TAG_CALLLOG.equals(parser.getName())) {
						tagState = STATE_NONE;
					} else if (TAG_ITEM.equals(parser.getName())) {
						if (inItem) {
							// TODO �ύ���ݵ���Ӧ��
							switch (tagState) {
							case STATE_SMSS:
								break;
							case STATE_CONTACT:
								break;
							case STATE_CALLLOG:
								break;
							}
						}
						inItem = false;
					}
					break;
				}
				event = parser.next();
			}
		} catch (XmlPullParserException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	// ������xml
	public void finishReadXmlFile() {
		// TODO
		cancelReadXmlFile();
	}

	// ȡ����xml
	public void cancelReadXmlFile() {
		parser = null;
		fIS = null;
	}

	// ׼����xml
	public void readyToWriteXmlFile(String fileFullName) {
		// TODO
	}

	// ����Ҫд��xml����
	public void processingWritingXmlFile() {
		// TODO
	}

	// ����дxml
	public void finishWriteXmlFile() {
		// TODO
		cancelWriteXmlFile();
	}

	// ȡ��дxml
	public void cancelWriteXmlFile() {
		// TODO
	}

	// ���ڸ�ʽ�������еķ����ߡ������ߵ��ִ�����ȡ���еĺ�����Ϣ
	public String formatTargetNumber(String str) {
		return null;
	}

	// ���ڸ�ʽ������ʱ���ִ�
	public Date formatDateString(String str) {
		Date date;
		try {
			java.text.SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			date = format.parse(str);
		} catch (ParseException e) {
			// e.printStackTrace();
			date = null;
		}

		return date;
	}

	// �������ȶԻ���
	private void createProgressDialog() {
		// TODO
	}

	// ���ý��ȶԻ������ֵ
	private void setProgress(int value) {
		// TODO
	}

	// ���ý��ȶԻ�����ʾ����
	private void setProgressText(String text) {
		// TODO
	}

	// ȡ�����ȶԻ���
	private void dismissProgressDialog() {
		// TODO
	}

	// ��ʾ�Ի�����ʾ
	public void showDevelopingMessage(Context context, String title,
			String message) {
		final String BTN_OK = "OK";
		Dialog dialog = new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(BTN_OK,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		dialog.show();
	}

	// ��Ϣ��
	class SmssItem {
		// TODO
		String sender;
		String receiver;
		String msgClass;
		String subject;
		Date deliverTime;
		Date lastModifyTime;

		static final String IPM_SMSTEXT = "IPM.SMStext";

		// ��������
		public void resetData() {
			sender = null;
			receiver = null;
			msgClass = null;
			subject = null;
			deliverTime.setTime(0);
			lastModifyTime.setTime(0);
		}
	}

	// ��ϵ����
	class ContactItem {
		// TODO

		// ��������
		public void resetData() {

		}
	}

	// ͨ����¼��
	class CallLogItem {
		// TODO
		String number;
		Date startTime;
		Date endTime;
		boolean outGoing;
		boolean connected;

		// ��������
		public void resetData() {
			number = null;
			startTime.setTime(0);
			endTime.setTime(0);
			outGoing = false;
			connected = false;
		}
	}
}
