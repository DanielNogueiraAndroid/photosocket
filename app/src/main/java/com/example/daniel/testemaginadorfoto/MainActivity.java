package com.example.daniel.testemaginadorfoto;
//http://stackoverflow.com/questions/12273976/camera-tutorial-for-android-using-surfaceview

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends Activity implements SurfaceHolder.Callback {


    private final String tag = "VideoServer";
    //public class VideoServer extends Activity implements SurfaceHolder.Callback {
        TextView testView;
        Camera camera;
        SurfaceView surfaceView;
        SurfaceHolder surfaceHolder;
        android.hardware.Camera.PictureCallback rawCallback;
        android.hardware.Camera.ShutterCallback shutterCallback;
        android.hardware.Camera.PictureCallback jpegCallback;
    boolean connected;
        Button start, stop, capture;
    private String lastname;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            // para manter a tela ligada
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            start = (Button)findViewById(R.id.btn_start);
            start.setOnClickListener(new Button.OnClickListener()
            {
                public void onClick(View arg0) {
                    start_camera();
                }
            });
            stop = (Button)findViewById(R.id.btn_stop);
            capture = (Button) findViewById(R.id.capture);
            stop.setOnClickListener(new Button.OnClickListener()
            {
                public void onClick(View arg0) {
                    stop_camera();
                }
            });
            capture.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    captureImage();
                }
            });

            surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            rawCallback = new android.hardware.Camera.PictureCallback() {
                public void onPictureTaken(byte[] data, Camera camera) {
                    Log.d("Log", "onPictureTaken - raw");
                }
            };

            /** Handles data for jpeg picture */
            shutterCallback = new android.hardware.Camera.ShutterCallback() {
                public void onShutter() {
                    Log.i("Log", "onShutter'd");
                }
            };
            jpegCallback = new android.hardware.Camera.PictureCallback() {
                public void onPictureTaken(byte[] data, Camera camera) {
                    FileOutputStream outStream = null;
                    try {
                        lastname = String.format(
                                "/sdcard/%d.jpg", System.currentTimeMillis());
                        outStream = new FileOutputStream(lastname);
                        outStream.write(data);
                        outStream.close();
                        Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        send();
                    }
                    Log.d("Log", "onPictureTaken - jpeg");
                }
            };

        }

    private void send() {
        Log.d(tag, "send ");
        if (!connected) {
//        serverIpAddress = serverIp.getText().toString();
//        if (!serverIpAddress.equals("")) {
            Thread cThread = new Thread(new ClientThread());
            cThread.start();
            Log.d(tag, "start ");
            //    }
        }

    }


        private void captureImage() {
            // TODO Auto-generated method stub
            camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        }

    //  http://stackoverflow.com/questions/16602736/android-send-an-image-through-socket-programming

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

        private void start_camera()
        {
            try{
                camera = Camera.open();
            }catch(RuntimeException e){
                Log.e(tag, "init_camera: " + e);
                return;
            }
            Camera.Parameters param;
            param = camera.getParameters();
            //modify parameter
            param.setPreviewFrameRate(20);
            param.setPreviewSize(176, 144);
            camera.setParameters(param);
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                //camera.takePicture(shutter, raw, jpeg)
            } catch (Exception e) {
                Log.e(tag, "init_camera: " + e);
                return;
            }
        }

        private void stop_camera()
        {
            camera.stopPreview();
            camera.release();
        }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
        }

    public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
        }

    public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
        }

    public class ClientThread implements Runnable {
        //      boolean connected ;
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName("192.168.1.10");
                Log.d(tag, "C: Connecting...");
                Socket socket = new Socket(serverAddr, 345);
                connected = true;

                while (connected) {
                    try {


                    /*File myFile = new File (filepath);
                    byte [] mybytearray  = new byte [(int)myFile.length()];
                    FileInputStream fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(mybytearray,0,mybytearray.length);
                    OutputStream os = socket.getOutputStream();
                    Log.d("ClientActivity", "C: Sending command.");
                    //System.out.println("Sending...");
                    os.write(mybytearray,0,mybytearray.length);
                    os.flush();*/
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(lastname, options);
                        byte[] imgbyte = getBytesFromBitmap(bitmap);


                        Log.d(tag, "C: Sending command.");
                    /*PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                                .getOutputStream())), true);*/
                        // WHERE YOU ISSUE THE COMMANDS

                        OutputStream output = socket.getOutputStream();
                        Log.d(tag, "C: image writing.");

                        output.write(imgbyte);
                        output.flush();
                        // out.println("Hey Server!");
                        Log.d(tag, "C: Sent.");
                    } catch (Exception e) {
                        Log.e(tag, "S: Error", e);
                    }
                }
                socket.close();
                Log.d(tag, "C: Closed.");
            } catch (Exception e) {
                Log.e(tag, "C: Error", e);
                connected = false;
            }
        }
    }

    }


