package chat.nra.com.chat;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Beacon extends AppCompatActivity{

    private BeaconManager beaconManager; //
    private BeaconRegion region;
    private TextView tvId;
    com.estimote.coresdk.recognition.packets.Beacon nearestBeacon;
    static int beaconRiss;

    Intent intent;
    BroadcastReceiver receiver;
    static String serviceData;

    SQLiteDatabase db;
    SQLiteOpenHelper helper;
    String dbState = "";

    int id;
    String name;
    String state;
    String time;

    static boolean isBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        tvId = (TextView) findViewById(R.id.tvId);

        intent = new Intent(this, BeaconService.class);
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter("Beacon");

        registerReceiver(receiver, filter);
        startService(intent);

        helper = new MySQLiteOpenHelper(Beacon.this, "beacon.db", null, 1);

        region = new BeaconRegion("ranged region", UUID.fromString("cc86b3c0-2bd1-413b-bf71-43e83c2f5bb1"), 45057, 57504);
        beaconManager = new BeaconManager(this);

        Log.d("메서드 실행 전 백그라운드인지 확인","===========" + isBackground);
        isAppIsInBackground(getApplicationContext());
        Log.d("화면이 포그라운드인지 백그라운드인지 확인","===========" + isBackground);
        /*beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
                region = new BeaconRegion("ranged region", UUID.fromString("cc86b3c0-2bd1-413b-bf71-43e83c2f5bb1"), 45057, 57504);
            }
        });*/

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List list) { // 앱이 켜지면 계속해서 범위를 검색함
                if (!list.isEmpty()) {
                    nearestBeacon = (com.estimote.coresdk.recognition.packets.Beacon) list.get(0);
                    Log.d("Airport", "Nearest places: " + nearestBeacon.getRssi());
                    beaconRiss = nearestBeacon.getRssi();
                    //tvId.setText(nearestBeacon.getRssi() + "");
                    if (beaconRiss > -70) { // 70의 범위에 들어온 경우
                        Log.d("login", "=====start=====");
                        //현재시간 구하기
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                        String getTime = time.format(date);
                        selectState(); // 데이터베이스에서 출퇴근 상태를 불러옴
                        if (dbState == "" || !dbState.contains("출근")) { // 데이터베이스에 출근상태인경우
                            if(isBackground) { // 앱이 백그라운드 상태인 경우 (알람을 띄우고 데이터베이스 저장)
                                Log.d("login", "범위안에 해당 로그인 ===== 백그라운드상태");
                                showNotification("김태원님이", "출근하였습니다." + getTime);
                                insert("김태원님이", "출근하셨습니다", getTime);
                                tvId.setText(null); //화면의 text를 초기화한 후 select()로 insert된 데이터베이스의 데이터를 가져옴
                                select();
                            } else { // 포그라운드상태인 경우 (데이터베이스에만 저장)
                                Log.d("login", "범위안에 해당 로그인 ===== 포그라운드상태");
                                insert("김태원님이", "출근하셨습니다", getTime);
                                tvId.setText(null);
                                select();
                            }
                        }
                    } else { // 70의 범위에서 벗어난 경우
                        Log.d("login", "=====finish=====");
                        //현재시간 구하기
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                        String getTime = time.format(date);
                        selectState();
                        if (!dbState.contains("퇴근")) { // 데이터베이스에 출근상태인 경우
                            if(isBackground) { // 앱이 백그라운드상태인경우
                                Log.d("login", "범위안에서 벗어남 로그아웃 ===== 백그라운드상태" + isBackground);
                                showNotification("김태원님이", "퇴근하였습니다." + getTime);
                                insert("김태원님이", "퇴근하셨습니다", getTime);
                                tvId.setText(null);
                                select();
                            } else { // 앱이 포그라운드 상태
                                Log.d("login", "범위안에서 벗어남 로그아웃 ===== 포그라운드상태" + isBackground);
                                insert("김태원님이", "퇴근하셨습니다", getTime);
                                tvId.setText(null);
                                select();
                            }
                        }
                    }
                }
            }
        });
       /*Log.d("nearestBeacon", "nearestBeacon : " + beaconRiss);
            beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {

                @Override
                public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons) {
                }
                @Override
                public void onExitedRegion(BeaconRegion beaconRegion) {
                }
            });*/
        region = new BeaconRegion("ranged region", UUID.fromString("cc86b3c0-2bd1-413b-bf71-43e83c2f5bb1"), 45057, 57504);

        helper = new MySQLiteOpenHelper(getApplicationContext(),
                "beacon2.db",
                null,
                1);

        tvId.setText(null);
        select();
    }


    @Override
    protected  void onResume(){
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this); // 블루투스를 사용하게하는 메서드
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
                region = new BeaconRegion("ranged region", UUID.fromString("cc86b3c0-2bd1-413b-bf71-43e83c2f5bb1"), 45057, 57504);
            }
        });
        tvId.setText(null);
        select();
    }
    @Override
    protected void onPause(){
        isBackground = true; // 앱을 끄면 백그라운드 상태로 돌아감
        Log.d("onPause상태 백그라운드인지 확인","확인 : ========" + isBackground);
        SystemRequirementsChecker.checkWithDefaultDialogs(this); // 블루투스를 사용하게하는 메서드
        try {
            unregisterReceiver(receiver);

        } catch(IllegalArgumentException e){
        }
        super.onPause();
    }
    public void insert(String name, String state, String time){ // 데이터베이스에 출퇴근 현황을 기록하는 메서드
        db = helper.getWritableDatabase(); // 데이터베이스를 불러옴

        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("state",state);
        values.put("time",time);
        db.insert("beacon2",null,values);
    }

    public void selectState(){ // 데이터베이스에서 출근상태인지 퇴근상태인지 가져오는 메서드
        db = helper.getReadableDatabase();
        Cursor c = db.query("beacon2", null, null, null, null, null, null);
        c.moveToLast();
        if(c.getCount() == 0){
            dbState = "";
        } else {
            dbState = c.getString(c.getColumnIndex("state"));
        }
    }
    public void select(){ // 데이터베이스의 내용을 전부 가져오는 메서드
        db = helper.getReadableDatabase();
        Cursor c = db.query("beacon2",null,null,null,null,null,null);

        c.moveToLast(); // 데이터의 맨 끝으로 감

        if(c.getCount() == 0){
            tvId.setText("등록된 출퇴근 기록이 없습니다.\n");
        } else {
            id = c.getInt(c.getColumnIndex("id"));
            name = c.getString(c.getColumnIndex("name"));
            state = c.getString(c.getColumnIndex("state"));
            time = c.getString(c.getColumnIndex("time"));
            Log.d("db", "id: " + id + ", name : " + name + ", state : " + state
                    + ", time : " + time);
            tvId.append(id + " " + name + " " + state + " " + time + "\n");

            while(c.moveToPrevious()){ // 맨 끝의 데이터를 가져온 후 가져올 데이터가 있을경우
                id = c.getInt(c.getColumnIndex("id"));
                name = c.getString(c.getColumnIndex("name"));
                state = c.getString(c.getColumnIndex("state"));
                time = c.getString(c.getColumnIndex("time"));
                Log.d("db 로그 while", "id: " + id + ", name : " + name + ", state : " + state
                        + ", time : " + time);
                tvId.append(id + " " + name + " " + state + " " + time + "\n");
            }
        }
    }

    public void showNotification(String title, String message) {

        Intent notifyIntent = new Intent(getApplicationContext(), Beacon.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}
                , PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notification.defaults |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
    class Receiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            serviceData = intent.getStringExtra("serviceBeacon");
            isBackground = false;
            tvId.setText(null);
            select();
        }
    }
    // 현재 앱이 포그라운드인지 백그라운드인지 검사하는 메서드 ( 백그라운드상태면 true, 포그라운드상태면 false로 리턴)
    private boolean isAppIsInBackground(Context context) {
        isBackground = true; // 백그라운드상태라고 초기화
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isBackground = false;
            }
        }
        Log.d("메서드 내 에서 백그라운드 확인","확인 : ======= " + isBackground);
        return isBackground;
    }
}
