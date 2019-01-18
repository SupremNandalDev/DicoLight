package in.notesmart.tourch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.logging.LoggingPermission;

public class MainActivity extends AppCompatActivity {

    private ImageView imageFlashlight;
    private static final int CAMERA_REQUEST = 50;
    private boolean flashLightStatus = false;
    TextView btn_disco;
    Boolean disc = false;
    int lim = 1000;
    SeekBar seekbar_disco;
    int maxPro = 8;
    int minPro = 1;
    int proPro = 4;
    Boolean runThread = false;
    CountDownTimer timer;
    CountDownTimer timer1;
    TextView stop_button;

    @Override
    protected void onStart() {
        super.onStart();


        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopThread();
            }
        });

        final boolean hasCameraFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        final boolean isEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        imageFlashlight.setEnabled(isEnabled);

        imageFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!disc){
                    if (flashLightStatus)
                        flashLightOff();
                    else
                        flashLightOn();
                }else{
                    StartThread();
                }
            }
        });
        btn_disco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!disc){
                    btn_disco.setTextColor(getColor(R.color.button_color));
                    disc = true;
                }else {
                    btn_disco.setTextColor(getColor(android.R.color.white));
                    disc = false;
                }
            }
        });
        seekbar_disco.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < minPro) {
                    seekBar.setProgress(minPro);
                }
                lim = 900 - (progress*100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                try{
                    StopThread();
                }catch (Exception  e){

                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (disc) StartThread();
                else
                    Toast.makeText(MainActivity.this, "Turn on Disco Light!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void StartThread(){
        runThread = true;
        DiscThread discThread = new DiscThread(lim);
        new Thread(discThread).start();
        stop_button.setVisibility(View.VISIBLE);
    }

    public void StopThread(){
        try{
            runThread = false;
            timer.cancel();
            timer1.cancel();
            flashLightOff();
            imageFlashlight.setEnabled(true);
            stop_button.setVisibility(View.GONE);
        }catch (Exception e){

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageFlashlight = findViewById(R.id.imageFlashlight);
        btn_disco = findViewById(R.id.btn_disco);
        seekbar_disco = findViewById(R.id.seekbar_disco);
        seekbar_disco.setMax(maxPro);
        seekbar_disco.setProgress(proPro);
        stop_button = findViewById(R.id.stop_button);


        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);

    }

    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
            imageFlashlight.setImageResource(R.drawable.active);
        } catch (CameraAccessException e) {
        }
    }

    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
            imageFlashlight.setImageResource(R.drawable.inactive);
        } catch (CameraAccessException e) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case CAMERA_REQUEST :
                if (grantResults.length > 0  &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageFlashlight.setEnabled(true);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    class DiscThread implements Runnable{
        int sp;
        Handler threadHandler = new Handler(Looper.getMainLooper());

        DiscThread(int sp){
            this.sp = sp;
        }


        @Override
        public void run() {
            threadHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (runThread){

                        flashLightOn();
                        imageFlashlight.setEnabled(false);
                        timer = new CountDownTimer(50000, sp) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                timer1 = new CountDownTimer(sp/2, 50) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        threadHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                flashLightOn();
                                            }
                                        });
                                    }
                                    @Override
                                    public void onFinish() {
                                        threadHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                flashLightOff();
                                            }
                                        });
                                    }
                                }.start();
                            }
                            @Override
                            public void onFinish() {
                                threadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        flashLightOff();
                                    }
                                });
                            }
                        }.start();
                    } else {
                        return;
                    }
                }
            });
            Looper.prepare();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StopThread();
    }
}