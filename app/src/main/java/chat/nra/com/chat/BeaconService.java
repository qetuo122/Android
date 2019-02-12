package chat.nra.com.chat;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BeaconService extends Service {

    private BeaconManager beaconManager;
    Intent intent;
    static int beaconRiss;
    com.estimote.coresdk.recognition.packets.Beacon nearestBeacon;
    private BeaconRegion region;

    SQLiteDatabase db;
    SQLiteOpenHelper helper;
    String dbState = "";

    @Override
    public void onCreate(){
        super.onCreate();
        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new BeaconRegion("monitored region", UUID.fromString("cc86b3c0-2bd1-413b-bf71-43e83c2f5bb1"),45057,57504));
            }
        });

       /* PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,"WAKELOCK");
        final boolean screen = pm.isScreenOn();*/

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons) {
                Log.w("login", "비콘 범위에 들어옴" + beacons.get(0).getRssi() );
                //현재시간 구하기
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String getTime = time.format(date);
                selectState();
                if(beacons.get(0).getRssi() > -70){ // 70의 범위에 들어온경우
                    if (dbState == "" || !dbState.contains("출근")) { // 데이터베이스에 퇴근중인경우
                        if (!isAlreadyRunActivity()) { //앱이 작동중인경우
                            Log.w("login", "기존에 어플이 실행중이지 않음 - 비콘 로그인" + beacons.get(0).getRssi() );
                            showNotification("김태원님이", "출근하였습니다." + getTime);
                            insert("김태원님이", "출근하였습니다", getTime);
                        }
                    }
                } else { // 70의 범위를 벗어난경우
                    if (dbState.contains("출근")) { // 데이터베이스에 이미 출근중인경우
                        if (!isAlreadyRunActivity()) { //앱이 작동중인경우
                            Log.w("login", "기존에 어플이 실행중이지 않음 - 비콘 로그인" + beacons.get(0).getRssi());
                            showNotification("김태원님이", "퇴근하였습니다." + getTime);
                            insert("김태원님이", "퇴근하였습니다", getTime);
                        }
                    }
                }
            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion) { //비콘사정거리를 벗어나는경우
                //현재시간 구하기
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String getTime = time.format(date);
                Log.w("exit", "비콘로그아웃");

                selectState();
                if (!dbState.contains("퇴근")) {
                    if (!isAlreadyRunActivity()) { // 앱이 작동중일경우
                        Log.w("logout", "기존에 어플이 실행중이지 않음 - 비콘 로그아웃");
                        showNotification("김태원님이", "퇴근하였습니다." + getTime);
                        insert("김태원님이", "퇴근하였습니다.", getTime);
                    }
                }
            }
        });
        /*beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List list) {
                if (!list.isEmpty()) {
                    nearestBeacon = (Beacon) list.get(0);
                    Log.d("Airport", "Nearest places: " + nearestBeacon.getRssi());
                    //beaconRiss = nearestBeacon.getRssi();
                    //tvId.setText(nearestBeacon.getRssi() + "");
                    if(beaconRiss > -70){
                        //현재시간 구하기
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                        String getTime = time.format(date);
                        selectState();
                        if(dbState == "" || !dbState.contains("출근")){
                            insert("김태원님이","출근하셨습니다",getTime);
                            showNotification("김태원님이","출근하셨습니다." + getTime);
                        }
                    } else {
                        //현재시간 구하기
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                        String getTime = time.format(date);
                        selectState();
                        if(!dbState.contains("퇴근")){
                            insert("김태원님이","퇴근하셨습니다",getTime);
                            showNotification("김태원님이","퇴근하셨습니다." + getTime);
                        }
                    }
                }
            }
        });*/
        helper = new MySQLiteOpenHelper(getApplicationContext(),
                "beacon2.db",
                null,
                1);
        /*beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener(){
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> list) {
                Log.w("login","비콘범위 잡음");
                //현재시간 구하기
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String getTime = time.format(date);

                Beacon nearestBeacon = list.get(0);
                if(nearestBeacon.getRssi() > -70){
                    beaconManager.startRanging(region);
                    if(isAlreadyRunActivity()){ //어플이 실행중인경우
                        Log.w("login","비콘로그인");
                        if(!screen){ // 어플이 실행중인데 화면이 꺼져있는경우
                            Log.w("start","비콘로그인 기존에 어플 실행중이나 화면 꺼져있음 - 비콘 로그인");
                            showNotification("들어옴", "김태원님이 출근하셨습니다. " + "거리 : " + list.get(0).getRssi() + " 시간 : " + getTime, "김태원님이 출근하셨습니다. 시간 : " +  getTime);
                            wakeLock.acquire(); // WakeLock 깨우기
                            wakeLock.release(); // WakeLock 해제
                        } else { // 어플이 실행중인데 화면이 켜져있는경우
                            Log.w("start","비콘로그인 기존에 어플 실행중 비콘 로그인");
                            intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("start","김태원님이 출근하셨습니다." + getTime);
                            startActivity(intent);
                        }
                    } else { // 어플이 실행중이지 않은경우
                        Log.w("start","비콘로그인 기존에 어플 실행 안하는중 비콘 로그인");
                        showNotification("들어옴", "김태원님이 출근하셨습니다. " + "거리 : " + list.get(0).getRssi() + " 시간 : " + getTime, "김태원님이 출근하셨습니다. 시간 : " +  getTime);
                        wakeLock.acquire(); // WakeLock 깨우기
                        wakeLock.release(); // WakeLock 해제
                    }
                }
                else if(nearestBeacon.getRssi() < -70){
                    beaconManager.startRanging(region);
                    if(isAlreadyRunActivity()){ //어플이 실행중인 경우
                        if(!screen){ //어플이 실행중이나 화면이 꺼져있는 경우
                            Log.w("start","비콘로그인 기존에 어플 실행중이나 화면 꺼져있음 - 비콘 로그인");
                            showNotification("나감", "김태원님이 퇴근하셨습니다. " + " 시간 : " + getTime, "김태원님이 퇴근하셨습니다. 시간 : " + getTime);
                            wakeLock.acquire(); // WakeLock 깨우기
                            wakeLock.release(); // WakeLock 해제
                        } else { //어플이 실행중인데 화면이 켜져있는경우
                            Log.w("start","비콘로그인 기존에 어플 실행중 비콘 로그인");
                            intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("start","김태원님이 퇴근하셨습니다." + getTime);
                            startActivity(intent);
                        }
                    } else { // 어플이 실행중이지 않은 경우
                        Log.w("start","비콘로그인 기존에 어플 실행 안하는중 비콘 로그인");
                        showNotification("나감", "김태원님이 퇴근하셨습니다. " + " 시간 : " + getTime, "김태원님이 퇴근하셨습니다. 시간 : " + getTime);
                        wakeLock.acquire(); // WakeLock 깨우기
                        wakeLock.release(); // WakeLock 해제
                    }

                }
            }
            @Override
            public void onExitedRegion(BeaconRegion region) {
                Log.w("loginout","비콘범위 아웃");
                //현재시간 구하기
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String getTime = time.format(date);

                if(isAlreadyRunActivity()){ // 어플이 실행중인경우
                    if(!screen){ // 어플이 실행중이나 화면이 꺼져있는경우
                        Log.w("start","비콘로그인 기존에 어플 실행중 화면 꺼져있음 비콘 종료");
                        showNotification("나감", "김태원님이 퇴근하셨습니다. " + " 시간 : " + getTime, "김태원님이 퇴근하셨습니다. 시간 : " + getTime);
                        wakeLock.acquire(); // WakeLock 깨우기
                        wakeLock.release(); // WakeLock 해제
                    } else { // 어플이 실행중이나 화면이 켜져있는경우
                        Log.w("start","비콘로그인 기존에 어플 실행중 비콘 종료");
                        intent = new Intent(getApplicationContext(),MainActivity.class);
                        showNotification("나감", "김태원님이 퇴근하셨습니다. " + " 시간 : " + getTime, "김태원님이 퇴근하셨습니다. 시간 : " + getTime);
                        startActivity(intent);
                    }
                } else { // 어플이 꺼져있는경우
                    Log.w("start","비콘로그인 기존에 어플 실행 안하는중 비콘 종료");
                    showNotification("나감", "김태원님이 퇴근하셨습니다. " + " 시간 : " + getTime, "김태원님이 퇴근하셨습니다. 시간 : " + getTime);
                    wakeLock.acquire(); // WakeLock 깨우기
                    wakeLock.release(); // WakeLock 해제
                }
            }
        });*/
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void insert(String name, String state, String time){
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("state",state);
        values.put("time",time);
        db.insert("beacon2",null,values);
    }

    public void selectState(){
        db = helper.getReadableDatabase();
        Cursor c = db.query("beacon2", null, null, null, null, null, null);
        c.moveToLast();
        if(c.getCount() == 0){
            dbState = "";
        } else {
            dbState = c.getString(c.getColumnIndex("state"));
        }
    }

    public void showNotification(String title, String message) {

        Intent notifyIntent = new Intent(this, MainActivity.class);
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

        Intent response = new Intent("Beacon");
        response.putExtra("serviceBeacon",title);
        sendBroadcast(response);
    }
    private boolean isAlreadyRunActivity() { // 앱이 실행중인지 아닌지 확인하는 메서드드
        boolean isAlreadyRunActivity = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List task_info = activityManager.getRunningTasks(9999);

        String activityName = "";
        for (int i = 0; i < task_info.size(); i++) {
            activityName = activityManager.getRunningTasks(9999).get(i).topActivity.getPackageName();
            Log.w("packageName","packageName" + activityName);
            if (activityName.startsWith("chat.nra.com.chat")) {
                isAlreadyRunActivity = true;
                break;
            } else {
                isAlreadyRunActivity = false;
            }
        }
        return isAlreadyRunActivity;
    }
}
