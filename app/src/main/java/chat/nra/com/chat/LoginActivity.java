package chat.nra.com.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText userName;
    Button enterChat;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = (EditText)findViewById(R.id.user_name);
        enterChat = (Button)findViewById(R.id.enter);

        /*final Intent backIntent = getIntent();
        final boolean isBackground = backIntent.getBooleanExtra("isBackground",false);*/

        enterChat.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) { // 아이디를 입력 후 채팅방에 입장
                if(userName.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "이름을 입력하세요", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    Intent intent = new Intent(getApplication(),ChatActivity.class);

                    intent.putExtra("userName",userName.getText().toString() + " : "); // 입장한 아이디를 채팅액티비티에 넘김
                    startActivity(intent);
                }
            }
        });

    }
}
