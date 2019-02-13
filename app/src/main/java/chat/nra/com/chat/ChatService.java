package chat.nra.com.chat;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatService extends Service {

    //파이어베이스 데이터베이스를 가져옴
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference("message");

    Intent service;
    static boolean isBackground;
    static String lastData;
    static String lastDataInService;
    static String userName;

    static String dbName;
    static String dbMessage;
    static String dbTime;

    //채팅액티비티에서 프리퍼런스로 넘어온 데이터를 받아오기위한 메서드
    private void load() {
        SharedPreferences appData = getSharedPreferences("appData", MODE_PRIVATE); //save()메서드에서 프리퍼런스 이름을 appData로 설정했기때문에 일치시킴
        // SharedPreferences 객체.get타입( 저장된 이름, 기본값 )
        // 저장된 이름이 존재하지 않을 시 기본값
        isBackground = appData.getBoolean("isBackground",false);
        userName = appData.getString("userName", "");
        lastData = appData.getString("lastData", "");
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //서비스가 재시작 될 경우 메서드
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        // 프리퍼런스에서 보낸 데이터값을 받아옴
        // intent가 null일경우 앱에 오류가 생겨 프리퍼런스로 받아오기 설정해봤음
        load();
        Log.d("서비스에서 프리퍼런스 값체크",isBackground + userName + lastData);
           /* //채팅액티비티에서 intent한 데이터를 가져옴
            isBackground = intent.getBooleanExtra("isBackground",false);
            Log.d("널값체크",isBackground + "");
            lastData = intent.getStringExtra("lastData");
            userName = intent.getStringExtra("userName");
            Log.d("chatService intent",isBackground + "");*/

        //백그라운드 상태에서 가장 최근의 데이터의 키값 1개를 가져오는 리스너
        databaseReference.child("message").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){ // 파이어베이스에 데이터가 없을경우 오류가 생기므로 null인지 확인
                    lastDataInService = dataSnapshot.getChildren().iterator().next().getKey(); //가장 최근의 데이터의 키값
                    dbMessage = dataSnapshot.getChildren().iterator().next().getValue(ChatVO.class).getMessage(); // 알람이 울릴때 데이터베이스의 내용을 붙여서 알람 설정하기위해 저장
                    dbName = dataSnapshot.getChildren().iterator().next().getValue(ChatVO.class).getUserName();
                    dbTime = dataSnapshot.getChildren().iterator().next().getValue(ChatVO.class).getTime();

                    Log.d("마지막데이터 키값 확인",lastDataInService);
                    Log.d("마지막데이터 벨류값 확인", "메시지 : " + dbMessage + " 시간 : " + dbTime + " 이름 : " + dbName);

                    if(isBackground){ //앱이 백그라운드 상태일 경우
                        if(!lastData.equals(lastDataInService)){ // 앱을 중지시켰을 당시 가장 최근 데이터와 백그라운드상태에서의 가장 최근데이터를 비교(이걸 안하면 앱에서 뒤로가기를 눌렀을 경우 데이터가 변경되었다고 인식되어 알람이 붙음)
                            // 비교했을때 데이터가 다르면 알람이 울림
                            showNotification("새로운 메시지가 도착했습니다.", dbName + dbMessage);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return super.onStartCommand(intent,flags,startId);
    }

    // 알람을 울리는 메서드
    public void showNotification(String title, String message) {

        Intent notifyIntent = new Intent(getApplicationContext(),ChatActivity.class);
        notifyIntent.putExtra("userName",userName); // 알람을 누르면 가장 마지막에 접속되었었던 아이디로 채팅방에 입장
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

}
