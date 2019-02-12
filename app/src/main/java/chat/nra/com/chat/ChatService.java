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

        //채팅액티비티에서 intent한 데이터를 가져옴
        isBackground = intent.getBooleanExtra("isBackground",false);
        lastData = intent.getStringExtra("lastData");
        userName = intent.getStringExtra("userName");
        Log.d("chatService intent",isBackground + "");


        /*databaseReference.child("message").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastDataInService = dataSnapshot.getChildren().iterator().next().getKey();
                Log.d("서비스에서 마지막데이터 확인",lastDataInService);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/

        //백그라운드 상태에서 가장 최근의 데이터의 키값 1개를 가져오는 리스너
        databaseReference.child("message").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastDataInService = dataSnapshot.getChildren().iterator().next().getKey(); //가장 최근의 데이터의 키값
                Log.d("value 데이터 확인",lastDataInService);
                if(isBackground){ //앱이 백그라운드 상태일 경우
                    if(!lastData.equals(lastDataInService)){ // 앱을 중지시켰을 당시 가장 최근 데이터와 백그라운드상태에서의 가장 최근데이터를 비교
                        // 비교했을때 데이터가 다르면 알람이 울림
                        showNotification("알람", "새로운 메시지가 도착했습니다.");
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
        notifyIntent.putExtra("userName",userName);
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
