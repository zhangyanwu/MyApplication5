package com.example.mycomputer.myapplication;


import java.text.DecimalFormat;

import com.baidu.mapapi.map.MapView;
import com.cn.stepcounter.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;



@SuppressLint("HandlerLeak")
public class StepCounterActivity extends Activity {

	MapView mMapView = null;

	static ImageView iv1;
	static ImageView iv2;
	static ImageView iv3;
	static ImageView iv4;

	private TextView tv_show_step;

	private TextView tv_timer;

	private TextView tv_distance;
	private TextView tv_calories;
	private TextView tv_velocity;

	private Button btn_start;
	private Button btn_stop;



	private long timer = 0;   //用时
	private  long startTimer = 0;
	private  long tempTime = 0;
	private Double distance = 0.0;//行程
	private Double calories = 0.0; //热量
	private Double velocity = 0.0; //速度

	private int step_length = 0;
	private int weight = 0;
	private int total_step = 0;
	private Thread thread;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			countDistance();

			if (timer != 0 && distance != 0.0) {

				calories = weight * distance * 0.001;
				velocity = distance * 1000 / timer;
			} else {
				calories = 0.0;
				velocity = 0.0;
			}
			countStep();
			tv_show_step.setText(total_step + "");
			tv_distance.setText(formatDouble(distance));
			tv_calories.setText(formatDouble(calories));
			tv_velocity.setText(formatDouble(velocity));
			tv_timer.setText(getFormatTime(timer));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		iv1= (ImageView) findViewById(R.id.iv1);
		iv2= (ImageView) findViewById(R.id.iv2);
		iv3= (ImageView) findViewById(R.id.iv3);
		iv4= (ImageView) findViewById(R.id.iv4);

		mMapView = (MapView) findViewById(R.id.bmapView);

		if (SettingsActivity.sharedPreferences == null) {
			SettingsActivity.sharedPreferences = this.getSharedPreferences(
					SettingsActivity.SETP_SHARED_PREFERENCES,
					Context.MODE_PRIVATE);
		}
		if (thread == null) {

			thread = new Thread() {

				@Override
				public void run() {
					super.run();
					int temp = 0;
					while (true) {
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (StepCounterService.FLAG) {
							Message msg = new Message();
							if (temp != StepDetector.CURRENT_SETP) {
								temp = StepDetector.CURRENT_SETP;
							}
							if (startTimer != System.currentTimeMillis()) {
								timer = tempTime + System.currentTimeMillis()
										- startTimer;
							}
							handler.sendMessage(msg);
						}
					}
				}
			};
			thread.start();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mMapView.onResume();

		Log.i("APP", "on resuame.");
		addView();

		init();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}


	private void addView() {
		tv_show_step = (TextView) this.findViewById(R.id.show_step);

		tv_timer = (TextView) this.findViewById(R.id.timer);

		tv_distance = (TextView) this.findViewById(R.id.distance);
		tv_calories = (TextView) this.findViewById(R.id.calories);
		tv_velocity = (TextView) this.findViewById(R.id.velocity);

		btn_start = (Button) this.findViewById(R.id.start);
		btn_stop = (Button) this.findViewById(R.id.stop);

		Intent service = new Intent(this, StepCounterService.class);
		stopService(service);
		StepDetector.CURRENT_SETP = 0;
		tempTime = timer = 0;
		tv_timer.setText(getFormatTime(timer));
		tv_show_step.setText("0");
		tv_distance.setText(formatDouble(0.0));
		tv_calories.setText(formatDouble(0.0));
		tv_velocity.setText(formatDouble(0.0));

		handler.removeCallbacks(thread);

	}


	private void init() {

		step_length = SettingsActivity.sharedPreferences.getInt(
				SettingsActivity.STEP_LENGTH_VALUE, 70);
		weight = SettingsActivity.sharedPreferences.getInt(
				SettingsActivity.WEIGHT_VALUE, 50);

		countDistance();
		countStep();
		if ((timer += tempTime) != 0 && distance != 0.0) {
			calories = weight * distance * 0.001;

			velocity = distance * 1000 / timer;
		} else {
			calories = 0.0;
			velocity = 0.0;
		}

		tv_timer.setText(getFormatTime(timer + tempTime));

		tv_distance.setText(formatDouble(distance));
		tv_calories.setText(formatDouble(calories));
		tv_velocity.setText(formatDouble(velocity));

		tv_show_step.setText(total_step + "");

		btn_start.setEnabled(!StepCounterService.FLAG);
		btn_stop.setEnabled(StepCounterService.FLAG);

		if (StepCounterService.FLAG) {
			btn_stop.setText(getString(R.string.pause));
		} else if (StepDetector.CURRENT_SETP > 0) {
			btn_stop.setEnabled(true);
			btn_stop.setText(getString(R.string.cancel));
		}

	}

	private String formatDouble(Double doubles) {
		DecimalFormat format = new DecimalFormat("####.##");
		String distanceStr = format.format(doubles);
		return distanceStr.equals(getString(R.string.zero)) ? getString(R.string.double_zero)
				: distanceStr;
	}

	public void kaishi(View view) {
		Intent service = new Intent(this, StepCounterService.class);

			startService(service);
			btn_start.setEnabled(false);
			btn_stop.setEnabled(true);
			btn_stop.setText(getString(R.string.pause));
			startTimer = System.currentTimeMillis();
			tempTime = timer;
	}

	public  void zhanting(View view){
		Intent service = new Intent(this, StepCounterService.class);
			stopService(service);
			if (StepCounterService.FLAG && StepDetector.CURRENT_SETP > 0) {
				btn_stop.setText(getString(R.string.cancel));
			} else {
				StepDetector.CURRENT_SETP = 0;
				tempTime = timer = 0;

				btn_stop.setText(getString(R.string.pause));
				btn_stop.setEnabled(false);

				tv_timer.setText(getFormatTime(timer));

				tv_show_step.setText("0");
				tv_distance.setText(formatDouble(0.0));
				tv_calories.setText(formatDouble(0.0));
				tv_velocity.setText(formatDouble(0.0));

				handler.removeCallbacks(thread);
			}
			btn_start.setEnabled(true);
	}


	private String getFormatTime(long time) {
		time = time / 1000;
		long second = time % 60;
		long minute = (time % 3600) / 60;
		long hour = time / 3600;


		String strSecond = ("00" + second)
				.substring(("00" + second).length() - 2);
		String strMinute = ("00" + minute)
				.substring(("00" + minute).length() - 2);
		String strHour = ("00" + hour).substring(("00" + hour).length() - 2);

		return strHour + ":" + strMinute + ":" + strSecond;
	}

	public void paobu(View view){
		iv1.setImageResource(R.drawable.paodian);
		iv2.setImageResource(R.drawable.tianqiweidian);
		iv3.setImageResource(R.drawable.woweidian);
		iv4.setImageResource(R.drawable.shezhiweidian);
	}
	public void tianqi(View view){
		iv1.setImageResource(R.drawable.paoweidian);
		iv2.setImageResource(R.drawable.tianqidian);
		iv3.setImageResource(R.drawable.woweidian);
		iv4.setImageResource(R.drawable.shezhiweidian);
	}
	public void wo(View view){
		iv1.setImageResource(R.drawable.paoweidian);
		iv2.setImageResource(R.drawable.tianqiweidian);
		iv3.setImageResource(R.drawable.wodian);
		iv4.setImageResource(R.drawable.shezhiweidian);
	}
	public void shezhi(View view){
		iv1.setImageResource(R.drawable.paodian);
		iv2.setImageResource(R.drawable.tianqiweidian);
		iv3.setImageResource(R.drawable.woweidian);
		iv4.setImageResource(R.drawable.shezhiweidian);
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}


	private void countDistance() {
		if (StepDetector.CURRENT_SETP % 2 == 0) {
			distance = (StepDetector.CURRENT_SETP / 2) * 3 * step_length * 0.01;
		} else {
			distance = ((StepDetector.CURRENT_SETP / 2) * 3 + 1) * step_length * 0.01;
		}
	}


	private void countStep() {
		if (StepDetector.CURRENT_SETP % 2 == 0) {
			total_step = StepDetector.CURRENT_SETP;
		} else {
			total_step = StepDetector.CURRENT_SETP +1;
		}

		total_step = StepDetector.CURRENT_SETP;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}
}
