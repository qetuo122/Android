package chat.nra.com.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    ArrayList<ChatVO> list = new ArrayList<>();
    private String userName;
    private ListView chatView;
    private EditText chatEdit;
    private Button chatSend;
    Intent service;

    static boolean isBackground;
    static String lastData;


    //파이어베이스의 데이터베이스를 가져옴
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference("message");

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        chatView = (ListView)findViewById(R.id.chat_view);
        chatEdit = (EditText)findViewById(R.id.chat_edit);
        chatSend = (Button)findViewById(R.id.chat_send);

        Intent intent = getIntent(); // 로그인액티비티에서 접속한 아이디를 가져옴 + 서비스의 push에서 보낸 아이디값도 받음
        userName = intent.getStringExtra("userName");

        isBackground = false; // 채팅어플이 켜진상태이므로 백그라운드는 false
        Log.d("==== intent 이름 ====",userName + "");
        Log.d("==== 채팅 background ====",isBackground + "");

        //채팅서비스에 백그라운드인지 포그라운드인지 데이터를 보냄
        service = new Intent(this,ChatService.class);
        service.putExtra("isBackground",isBackground);
        service.putExtra("userName",userName);
        //서비스단 시작(서비스는 단순히 백그라운드인지 포그라운드인지를 체크하여 알람을 붙일지 말지 결정하기위해 만듦
        startService(service);

        //파이어베이스의 데이터베이스에서 가장 최근의 데이터중 키값을 하나 가져오기 위한 리스너( 서비스(백그라운드상태)의 최근 데이터와 비교하기위해)
        databaseReference.child("message").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("dataSnapshot 확인",dataSnapshot.getChildren().iterator().hasNext() + "");
                if(dataSnapshot.getChildren().iterator().hasNext()){ // 파이어베이스에 저장된 메시지가 없을경우 오류가 걸리므로 설정
                    lastData = dataSnapshot.getChildren().iterator().next().getKey();
                    //dbMessage = dataSnapshot.getChildren().iterator().next().getValue();

                    Log.d("마지막데이터 확인",lastData);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        // 채팅화면이 나타나는 메서드 시작
        enterChat(userName);

        //채팅 보내기버튼을 누르면 chatVO에 사용자이름, 내용, 시간을 저장하고 파이어베이스 데이터베이스에 저장함
        chatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chatEdit.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"채팅내용을 입력하세요",Toast.LENGTH_LONG).show();
                } else {
                    Date today = new Date();
                    SimpleDateFormat nowTime = new SimpleDateFormat("a K:mm");

                    ChatVO chatVO = new ChatVO(userName, chatEdit.getText().toString(), nowTime.format(today));
                    databaseReference.child("message").push().setValue(chatVO); // 객체를 데이터베이스에 저장
                    chatEdit.setText(""); // 데이터베이스에 저장이 완료됐으므로 보낼 메시지 칸은 빈칸으로 변함
                }
            }
        });
    }

    @Override
    protected void onResume() {
        isBackground = false;
        save(); // 앱이 부활했기때문에 false로 설정된 백그라운드데이터를 프리퍼런스에 저장
        startService(service);
        super.onResume();
    }

    @Override
    protected void onPause(){
        isBackground = true; // 어플이 꺼져있는 상태이므로 백그라운드는 true
        Log.d("onPause상태 백그라운드인지 확인","확인 : ========" + isBackground);
        Log.d("onPause상태 마지막데이터 확인","확인 : ========" + lastData);
        /*service.putExtra("isBackground",isBackground); // 서비스에 백그라운드인지 포그라운드인지 데이터 넘김
        service.putExtra("lastData",lastData); // 어플을 껐을때 가장 최근의 메시지와 서비스에서의 가장 최근메시지를 비교하기위해 서비스에 데이터 넘김
        service.putExtra("userName",userName);*/
        save();
        startService(service); // 앱이 정지상태에서도 서비스를 계속 돌리기 위해
        super.onPause();
    }

    /*@Override
    protected void onDestroy() {
        isBackground = true;
        save(); //백그라운드데이터를 트루라고 설정했기때문에 프리퍼런스에 트루로 저장
        stopService(service); // 어플이 완전히 종료되면 서비스 종료
        super.onDestroy();
    }*/

    //채팅화면에서 채팅내용을 보여주기위한 메서드
    private void enterChat(String userName){

        final ChatAdapter adapter = new ChatAdapter(getApplicationContext(), R.layout.activity_chatlist, list, userName); // 어뎁터에 채팅방에 입장시 아이디도 함께 넘김
        //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        ((ListView)findViewById(R.id.chat_view)).setAdapter(adapter);
        ((ListView)findViewById(R.id.chat_view)).setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); // 화면을 넘어가는 데이터가 있으면 스크롤바가 생김
        chatView.setSelection(adapter.getCount() - 1); // 기본으로 접속했을때 데이터의 맨 마지막으로 포커싱 맞춤

        adapter.registerDataSetObserver(new DataSetObserver() { // 데이터가 추가될 경우 맨 마지막으로 포커싱 업데이트
            @Override
            public void onChanged() {
                super.onChanged();
                chatView.setSelection(adapter.getCount() - 1);
            }
        });

        // 데이터베이스에 메시지가 추가될 경우 어뎁터리스트(화면에 보여질 리스트)에 데이터 추가
        databaseReference.child("message").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addMessage(dataSnapshot, adapter); // 화면에 보여질 데이터메서드
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    //화면에 보여질 데이터 메서드
    private void addMessage(DataSnapshot dataSnapshot, ChatAdapter adapter){
        ChatVO chatVO = dataSnapshot.getValue(ChatVO.class); // 데이터베이스에서 가져온 데이터를 객체로 저장
        list.add(chatVO); // 화면에 보여질 어뎁터 리스트에 데이터 저장
        adapter.notifyDataSetChanged();
    }

    // 프리퍼런스에 저장하는 메서드
    // 프리퍼런스에 데이터를 임시로 저장하면 앱이 삭제되기 전까지 데이터 유지
    // intent로 데이터를 넘기다 보니 intent로 넘길 데이터가 없으면 오류가 걸려서 프리퍼런스로 데이터를 넘겨봄

    private void save() {
        // SharedPreferences 객체만으론 저장 불가능 Editor 사용
        SharedPreferences sharedPreferences = getSharedPreferences("appData", MODE_PRIVATE); // 프리퍼런스의 이름을 appData로 설정
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 에디터객체.put타입( 저장시킬 이름, 저장시킬 값 )
        // 저장시킬 이름이 이미 존재하면 덮어씌움
        editor.putBoolean("isBackground", isBackground);
        editor.putString("userName", userName);
        editor.putString("lastData", lastData);

        Log.d("save값 확인", isBackground + "");
        // apply, commit 을 안하면 변경된 내용이 저장되지 않음
        editor.apply();
    }
}
