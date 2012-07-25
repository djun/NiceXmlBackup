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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Xml;

public class MyXmlProcessor {

	private Context mContext;
	private ContentResolver mResolver;
	private Handler mHandler;
	private ProgressDialog progressDialog;

	private XmlPullParser parser = null;
	private FileInputStream fIS = null;

	private ContentValues values = null;

	// for xml
	private int tagState = STATE_NONE;
	private int folderId = 0;
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
	static final String ATTR_FOLDERID = "folderid";
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

	// for system content
	static final String SMS_URI_ALL = "content://sms/";
	static final String SMS_URI_INBOX = "content://sms/inbox";
	static final String SMS_URI_SENT = "content://sms/sent";
	static final String SMS_URI_DRAFT = "content://sms/draft";
	static final String SMS_URI_OUTBOX = "content://sms/outbox";
	static final String SMS_URI_FAILED = "content://sms/failed";
	static final String SMS_URI_QUEUED = "content://sms/queued";
	static final String COL_ADDRESS = "address";
	static final String COL_DATE = "date";
	// static final String COL_DATE2 = "date2";
	static final String COL_TYPE = "type";
	static final String COL_BODY = "body";
	static final String COL_PROTOCOL = "protocol";
	static final String COL_READ = "read";
	static final String COL_STATUS = "status";
	static final String COL_LOCKED = "locked";
	static final String COL_SEEN = "seen";
	static final int TYPE_ALL = 0;
	static final int TYPE_INBOX = 1;
	static final int TYPE_SENT = 2;
	static final int TYPE_DRAFT = 3;
	static final int TYPE_OUTBOX = 4;
	static final int TYPE_FAILED = 5;
	static final int TYPE_QUEUED = 6;

	// for dialog
	static final int CREATE_PROGRESS_DIALOG = 1;
	static final int UPDATE_PROGRESS = 2;
	static final int SET_PROGRESS_TEXT = 3;
	static final int DISMISS_PROGRESS_DIALOG = 4;

	MyXmlProcessor(Context context) {
		this.mContext = context;
		mResolver = mContext.getContentResolver();
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
			parser = Xml.newPullParser();
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
							sItem.folderId = folderId;
							inItem = true;
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
							inItem = true;
						}
						// TODO δ���
						break;
					case STATE_CALLLOG:
						if (TAG_ITEM.equals(parser.getName())) {
							clItem.resetData();
							inItem = true;
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
						// TODO δ����
						break;
					case STATE_NONE:
						if (TAG_SMSS.equals(parser.getName())) {
							final int count = parser.getAttributeCount();
							for (int i = 0; i < count; ++i) {
								if (ATTR_FOLDERID.equals(parser
										.getAttributeName(i))) {
									folderId = Integer.parseInt(parser
											.getAttributeValue(i));
									break;
								}
							}
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
								// debug
								System.out.println("SMS---");
								if (sItem.sender != null)
									System.out.println("sender: "
											+ sItem.sender);
								if (sItem.receiver != null)
									System.out.println("receiver: "
											+ sItem.receiver);
								if (sItem.msgClass != null)
									System.out.println("msgClass: "
											+ sItem.msgClass);
								if (sItem.subject != null)
									System.out.println("subject: "
											+ sItem.subject);
								if (sItem.deliverTime != null)
									System.out.println("deliverTime: "
											+ sItem.deliverTime.toString());
								if (sItem.lastModifyTime != null)
									System.out.println("lastModifyTime: "
											+ sItem.lastModifyTime.toString());
								insertSmssRecord(sItem);
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
		String rtStr = str;
		final int ltIndex = str.lastIndexOf("<");
		final int gtIndex = str.lastIndexOf(">");
		if (ltIndex >= 0 && gtIndex >= 0) {
			rtStr = str.substring(ltIndex + 1, gtIndex);
		}
		return rtStr;
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

	// ���Ӷ��ż�¼
	public boolean insertSmssRecord(SmssItem item) {
		if (values == null) {
			values = new ContentValues();
		}
		values.clear();

		// address
		if (item.folderId == SmssItem.FID_INBOX) {
			values.put(COL_ADDRESS, item.sender);
		} else {
			values.put(COL_ADDRESS, item.receiver);
		}
		values.put(COL_BODY, item.subject);
		// date
		if (item.folderId == SmssItem.FID_DRAFT) {
			values.put(COL_DATE, String.valueOf(item.lastModifyTime.getTime()));
			// values.put(COL_DATE2,
			// String.valueOf(item.lastModifyTime.getTime()));
		} else {
			values.put(COL_DATE, String.valueOf(item.deliverTime.getTime()));
			// values.put(COL_DATE2,
			// String.valueOf(item.deliverTime.getTime()));
		}
		// protocol
		values.put(COL_PROTOCOL, "0"); // protocol=SMS
		// read
		values.put(COL_READ, "1"); // read=true
		// status
		values.put(COL_STATUS, "-1"); // status=-1
		// locked
		values.put(COL_LOCKED, "0"); // locked=false
		// seen
		values.put(COL_SEEN, "1"); // seen=true
		// type
		if (item.folderId == SmssItem.FID_INBOX) {
			values.put(COL_TYPE, String.valueOf(TYPE_INBOX));
			mResolver.insert(Uri.parse(SMS_URI_INBOX), values);
		} else if (item.folderId == SmssItem.FID_SENT) {
			values.put(COL_TYPE, String.valueOf(TYPE_SENT));
			mResolver.insert(Uri.parse(SMS_URI_SENT), values);
		} else if (item.folderId == SmssItem.FID_DRAFT) {
			values.put(COL_TYPE, String.valueOf(TYPE_DRAFT));
			mResolver.insert(Uri.parse(SMS_URI_DRAFT), values);
		} else {
			values.put(COL_TYPE, String.valueOf(TYPE_ALL));
			mResolver.insert(Uri.parse(SMS_URI_ALL), values);
		}

		return false;
		// ��ע
		// ���봦��address date (date2) protocol read status body locked seen
		// ���о���thread_id toa person date_sent sc_toa service_center (sim_slot)
	}

	// ���Ӷ��ż�¼
	public boolean insertContactRecord(ContactItem item) {
		if (values == null) {
			values = new ContentValues();
		}
		values.clear();

		return false;
	}

	// ���Ӷ��ż�¼
	public boolean insertCallLogRecord(CallLogItem item) {
		if (values == null) {
			values = new ContentValues();
		}
		values.clear();

		return false;
	}

	// �������ȶԻ���
	private void createProgressDialog() {
		progressDialog = new ProgressDialog(mContext);
		progressDialog.setCancelable(false);
	}

	// ���ý��ȶԻ������ֵ
	private void setProgress(int value) {
		progressDialog.setProgress(value);
	}

	// ���ý��ȶԻ�����ʾ����
	private void setProgressText(String text) {
		progressDialog.setMessage(text);
	}

	// ȡ�����ȶԻ���
	private void dismissProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
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
		int folderId;
		String sender;
		String receiver;
		String msgClass;
		String subject;
		Date deliverTime;
		Date lastModifyTime;

		static final int FID_INBOX = 0;
		static final int FID_SENT = 2;
		static final int FID_DRAFT = 4;
		static final String IPM_SMSTEXT = "IPM.SMStext";

		// ��������
		public void resetData() {
			folderId = FID_INBOX;
			sender = null;
			receiver = null;
			msgClass = null;
			subject = null;
			deliverTime = null;
			lastModifyTime = null;
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
			startTime = null;
			endTime = null;
			outGoing = false;
			connected = false;
		}
	}
}
