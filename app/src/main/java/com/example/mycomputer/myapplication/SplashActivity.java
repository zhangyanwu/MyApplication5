package com.example.mycomputer.myapplication;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;

import com.cn.stepcounter.R;


public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		
		if (StepCounterService.FLAG || StepDetector.CURRENT_SETP > 0) {
			Intent intent = new Intent(SplashActivity.this, StepCounterActivity.class);
			startActivity(intent);
			this.finish();
		} else {
			new CountDownTimer(2000L, 1000L)
			{
				public void onFinish()
				{
					Intent intent = new Intent();
					intent.setClass(SplashActivity.this, StepCounterActivity.class);
					Bundle bundle = new Bundle();
					bundle.putBoolean("run", false);
					intent.putExtras(bundle);
					startActivity(intent);
					overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
					finish();
				}
				public void onTick(long paramLong)
				{
				}
			}
			.start();
		}
	}
}

