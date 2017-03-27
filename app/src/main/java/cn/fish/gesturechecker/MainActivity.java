package cn.fish.gesturechecker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import cn.fish.gesturechecker.view.AlphabetDiscView;

public class MainActivity extends AppCompatActivity {

    private AlphabetDiscView mAdv;
    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdv = (AlphabetDiscView) findViewById(R.id.adv);
        mTv = (TextView) findViewById(R.id.tv);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTv.setText("");
            }
        });
        initEvent();
    }

    private void initEvent() {
        mAdv.setOnLetterDisced(new AlphabetDiscView.LetterCallback() {
            @Override
            public void onDisced(AlphabetDiscView.Letter letter) {
                appendTV(letter.toString() + "->");
            }
        });
    }

    private void appendTV(String str) {
        mTv.setText(mTv.getText() + str);
    }
}
