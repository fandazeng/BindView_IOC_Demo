package zeng.fanda.com.bindview_ioc_demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import zeng.fanda.com.annotation.BindView;
import zeng.fanda.com.annotation.OnClick;
import zeng.fanda.com.library.Butterknife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_ioc)
    TextView mIOC;

    @BindView(R.id.tv_other_ioc)
    TextView mOtherIOC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Butterknife.bind(this);
        mIOC.setText("绑定成功");
        mOtherIOC.setText("再次绑定成功");
    }

}
