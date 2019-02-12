package chat.nra.com.chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button chat;
    Button beacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chat = (Button)findViewById(R.id.chat);
        beacon = (Button)findViewById(R.id.beacon);

        chat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(),LoginActivity.class); // 로그인화면으로 화면전환
                startActivity(intent);
            }
        });

        beacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent beacon = new Intent(getApplication(),Beacon.class); // 로그인화면으로 화면전환
                startActivity(beacon);
            }
        });
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
}
