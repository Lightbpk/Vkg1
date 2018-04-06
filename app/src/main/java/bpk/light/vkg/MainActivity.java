package bpk.light.vkg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
Button btn_go,btn_goEasy,btn_goHard, btn_ok;
EditText kVol, textSong,FPS;
    SharedPreferences sP;
    SharedPreferences.Editor sPe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sP = PreferenceManager.getDefaultSharedPreferences(this);
        sPe = sP.edit();
        textSong = findViewById(R.id.textSong);
        kVol = findViewById(R.id.kVol);
        btn_ok = findViewById(R.id.btn_ok);
        btn_goEasy= findViewById(R.id.btn_goEasy);
        btn_goHard= findViewById(R.id.btn_goHard);
        btn_ok = findViewById(R.id.btn_ok);
        FPS = findViewById(R.id.FPS);
        btn_ok.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               sPe.putInt("kVol", Integer.parseInt(kVol.getText().toString()));
               sPe.putString("textSong",textSong.getText().toString());
               sPe.putInt("FPS",Integer.parseInt(FPS.getText().toString()));
               sPe.commit();
           }
       });
        btn_go = findViewById(R.id.btn_go);
        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Game.class);
                intent.putExtra("level",1);
                startActivity(intent);
            }
        });
        btn_goEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Game.class);
                intent.putExtra("level",2);
                startActivity(intent);
            }
        });
        btn_goHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Game.class);
                intent.putExtra("level",3);
                startActivity(intent);
            }
        });
        textSong.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sPe.putString("textSong",textSong.getText().toString());

                sPe.commit();
                Log.d(getString(R.string.LL),"textSong = "+textSong.getText().toString());
            }
        });

    }
}
