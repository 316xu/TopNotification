package cn.xujifa.topnotificationsample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import cn.xujifa.topnotification.TopNotification;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
    }

    public void info(View view) {
        TopNotification.Builder builder = new TopNotification.Builder(this);

        builder.setMessage("INFO")
                .create()
                .show();
    }

    public void warn(View view) {
        TopNotification.Builder builder = new TopNotification.Builder(this);

        builder.setMessage("warn")
                .setLevel(TopNotification.WARN)
                .create()
                .show();
    }

    public void clickable(View view) {
        TopNotification.Builder builder = new TopNotification.Builder(this);

        builder.setMessage("clickable")
                .setPositiveListener(new TopNotification.OnClickListener() {
                    @Override
                    public void onClick(TopNotification topnotification) {
                        Toast.makeText(mContext, "Click Ok", Toast.LENGTH_SHORT).show();
                        topnotification.dismiss();
                    }
                })
                .setNegativeListener(new TopNotification.OnClickListener() {
                    @Override
                    public void onClick(TopNotification topnotification) {
                        Toast.makeText(mContext, "Click Cancel", Toast.LENGTH_SHORT).show();
                        topnotification.dismiss();
                    }
                })
                .create()
                .show();
    }

    public void disableOutside(View view) {
        TopNotification.Builder builder = new TopNotification.Builder(this);

        builder.setMessage("Disable Outside touch")
                .setOutsideTouchable(false)
                .create()
                .show();
    }

    public void custom(View view) {
        TopNotification.Builder builder = new TopNotification.Builder(this);

        builder.setMessage("Custom")
                .setIconRes(R.mipmap.ic_launcher)
                .create()
                .show();
    }

    public void duration(View view) {
        TopNotification.Builder builder = new TopNotification.Builder(this);

        builder.setMessage("Duration")
                .setDuration(3000)
                .setIsProgressBarVisible(true)
                .create()
                .show();
    }
}
