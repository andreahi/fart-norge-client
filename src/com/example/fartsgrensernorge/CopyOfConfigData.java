package com.example.fartsgrensernorge;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class CopyOfConfigData  extends Activity {
	boolean backpressed;
	CheckBox [] fylker = new CheckBox[20];
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configdata);
		fylker[0] = (CheckBox) findViewById(R.id.checkBox1);
		fylker[1] = (CheckBox) findViewById(R.id.checkBox2);
		fylker[2] = (CheckBox) findViewById(R.id.checkBox3);
		fylker[3] = (CheckBox) findViewById(R.id.checkBox4);
		fylker[4] = (CheckBox) findViewById(R.id.checkBox5);
		fylker[5] = (CheckBox) findViewById(R.id.checkBox6);
		fylker[6] = (CheckBox) findViewById(R.id.checkBox7);
		fylker[7] = (CheckBox) findViewById(R.id.checkBox8);
		fylker[8] = (CheckBox) findViewById(R.id.checkBox9);
		fylker[9] = (CheckBox) findViewById(R.id.checkBox10);
		fylker[10] = (CheckBox) findViewById(R.id.checkBox11);
		fylker[11] = (CheckBox) findViewById(R.id.checkBox12);
		fylker[12] = (CheckBox) findViewById(R.id.checkBox13);
		fylker[13] = (CheckBox) findViewById(R.id.checkBox14);
		fylker[14] = (CheckBox) findViewById(R.id.checkBox15);
		fylker[15] = (CheckBox) findViewById(R.id.checkBox16);
		fylker[16] = (CheckBox) findViewById(R.id.checkBox17);
		fylker[17] = (CheckBox) findViewById(R.id.checkBox18);
		fylker[18] = (CheckBox) findViewById(R.id.checkBox19);
		fylker[19] = (CheckBox) findViewById(R.id.checkBox20);
	

		//this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		/*Button btn = (Button)findViewById(R.id.button1);
        btn.setBackgroundResource(R.drawable.cloud);

        Button btn2 = (Button)findViewById(R.id.button2);
        btn2.setBackgroundResource(R.drawable.cloud);
		 */
		/*    
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
		 */

	}
	@Override
	protected void onDestroy(){	
		backpressed = true;
		super.onDestroy();
		Log.d("debug message", "in onDestroy()");
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.activity_babycall, menu);

		return true;
	}
	
	String checkboksToString(CheckBox [] checkboxes){
		String text = "";
		for (int i = 0; i < checkboxes.length; i++) {
			if(checkboxes[i].isChecked())
			text = text + "" + (i+1);
		}
		return text;
	}
	boolean swap;
	
	public void downloadData(final View view){
		new Thread(){
			public void run(){
				Socket s;
				try {

					String state = Environment.getExternalStorageState();

					if (!Environment.MEDIA_MOUNTED.equals(state)) {
						System.out.println("Storage not found");
						return;
					}
					if(swap){
						s = new Socket("188.64.45.149", 1234);
						swap = false;
					}
					else{
						s = new Socket("188.64.45.149", 1234);
						swap = true;
					}
					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					String getQuarry = "GET " + checkboksToString(fylker) + "\n";
					System.out.println("getQuarry: " + getQuarry);
					out.writeBytes(getQuarry);

					byte [] buffer= new byte[4];
					s.getInputStream().read(buffer);
					int size = 0;
					for (int i = 0; i < 4; i++)
						size |= (((int)buffer[i] & 0xff) << (i * 8));

					System.out.println("size: " + size);
					File sdCard = getExternalFilesDir("coordsdata");
					//File sdCard = Environment.getExternalStorageDirectory();
					
	

					File file = new File(sdCard, "datab.dat");
					file.delete();
					DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));


					int buffer_size = 10240;
					buffer = new byte[buffer_size];
					int totalBytesRead = 0;
					boolean printed = false;
					byte lastbyte = -1;
					while(totalBytesRead < size){
						int noBytesRead = 0;
						System.out.println("total bytes read" + totalBytesRead);

						while(noBytesRead < buffer_size && totalBytesRead < size){
							int nrRead = s.getInputStream().read(buffer, noBytesRead, buffer.length-noBytesRead);
							//System.out.println("returned by read: " + nrRead);
							lastbyte = buffer[noBytesRead];
							noBytesRead += (nrRead > 0) ? nrRead : 0;
							totalBytesRead += (nrRead > 0) ? nrRead : 0;
							if (backpressed) {
								System.out.println("last byte: " + buffer[noBytesRead-1]);
								System.out.println("last byte: " + buffer[noBytesRead-2]);
								System.out.println("last byte: " + buffer[noBytesRead-3]);
								System.out.println("last byte: " + buffer[noBytesRead-4]);
								System.out.println("total bytes read" + totalBytesRead);

								dos.close();
								return;
							}
						}
						if(!printed){
							System.out.println("first bytes: ");
							for (int i = 0; i < 8; i++) {
								System.out.println(buffer[i]);
							}
							
							int firstCoord = 0;
						
							firstCoord = 0;
							for (int i = 0; i < 4; i++)
								firstCoord |= (((int)buffer[3-i] & 0xff) << (i * 8));
							
							System.out.println("firstCoord: " + firstCoord);

							firstCoord = 0;
							for (int i = 0; i < 3; i++)
								firstCoord |= (((int)buffer[7-i] & 0xff) << (i * 8));
							
							System.out.println("secondCoord: " + firstCoord);


							printed = true;
						}
						for (int i = 0; i < noBytesRead; i++) {						
							dos.write(buffer[i]);
						}
				

					}
					System.out.println("total bytes read" + totalBytesRead);

					//   for (int i = 0; i < buffer.length; i++) {
					//		System.out.print(buffer[i]);
					//	}

					dos.close();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
		}.start();
	}
}
