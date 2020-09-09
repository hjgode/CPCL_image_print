package com.sample.cpcl_image_print;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

public class PrintPCX_cpcl {

    Context context=null;
    final static String TAG="PCX2CPCL";
final static String testString="! 0 200 200 210 1\n" +
        "TEXT 4 0 30 40 Hello World\n" +
        "FORM\n" +
        "PRINT";

    public PrintPCX_cpcl (Context _ctx){

        context=_ctx;
    }

    byte[] GetPrintCMD(boolean getPCXonly) {
        ByteArrayOutputStream stream = null;

        ByteArrayOutputStream bos=null;
        InputStream inputStream=null;
        try {
            //inputStream= context.getAssets().open("portrait.pcx");
            inputStream= context.getAssets().open("logo.pcx");
//            inputStream= context.getAssets().open("portrait_gray.pcx");

            bos = readFileToString(inputStream);
            stream = new ByteArrayOutputStream();
/*
            stream.write(testString.getBytes("utf-8"));
*/
            if(!getPCXonly) {
                stream.write("! 0 200 200 300 1\r\n".getBytes("utf-8"));
                stream.write("PCX 20 0\r\n".getBytes("utf-8"));

            }
            stream.write(bos.toByteArray());
            Log.d(TAG, "written: "+bos.toByteArray().length);
            if(!getPCXonly) {
                //stream.write("ENDPCX\r\nFORM\r\nPRINT\r\n".getBytes("utf-8"));
                stream.write("\r\nFORM\r\nPRINT\r\n".getBytes("utf-8"));
            }

            return stream.toByteArray();
        }catch (Exception ex){
            Log.d(TAG, ex.getMessage());
        }
        return null;
    }

    public ByteArrayOutputStream readFileToString(InputStream is) {
        InputStreamReader isr = null;
        ByteArrayOutputStream bos = null;
        try {
            isr = new InputStreamReader(is);
            bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[2048];
            int n = 0;
            while (-1 != (n = is.read(buffer))) {
                bos.write(buffer, 0, n);
            }

            return bos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

    }
    Handler mHandler=null;
    final static String MSG_KEY="MSG_KEY";
    void sendMessage(String text){
        if(mHandler!=null) {
            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString(MSG_KEY, text);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        Log.d(TAG, text);
    }

    public void printBitmapBT(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

                //BluetoothDevice mBluetoothDevice = bluetoothManager.getAdapter().getRemoteDevice("00:06:66:03:84:C9".toUpperCase());
                BluetoothDevice mBluetoothDevice = bluetoothManager.getAdapter().getRemoteDevice("0C:A6:94:3A:24:2D".toUpperCase());
                BluetoothSocket _socket = null;
                final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                Log.d(TAG, "printBitmapBT started...");
                sendMessage("print started");
                try {
                    _socket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID_SPP);
                } catch (IOException e) {
                    Log.d(TAG, "printBitmapBT createRfcommSocketToServiceRecord Exception: " + e.getMessage());
                }
                try {
                    _socket.connect();
                } catch (IOException e) {
                    Log.d(TAG, "printBitmapBT connect Exception: " + e.getMessage());
                    try {
                        _socket.close();
                    } catch (IOException ex) {
                        Log.d(TAG, "printBitmapBT connect / close Exception (not already paired?): " + ex.getMessage());
                        sendMessage("Unable to connect to printer. Already paired?");
                    }
                }

                if (_socket != null && _socket.isConnected()) {
                    //Socket is connected, now we can obtain our IO streams
                    InputStream _inStream;
                    OutputStream _outStream;
                    //....
                    try {
                        _inStream = _socket.getInputStream();
                        _outStream = _socket.getOutputStream();
                        try {
/*
<!> {offset} <200> <200> {height} {qty}
where:
<!>: Use ‘!’ to begin a control session.
{offset}:The horizontal offset for the entire label. This value causes all fields to be offset horizontally
by the specified number of UNITS.
<200>:Horizontal resolution (in dots-per-inch).
<200>:Vertical resolution (in dots-per-inch).
{height}:The maximum height of the label.
 */
                            //get RLL for bitmap
                            byte [] buf=GetPrintCMD(true); //portrait.pcx is 136x184
                            if(buf!=null){
                                _outStream.write("! 0 203 203 300 1\r\n".getBytes("utf-8"));
                                _outStream.flush();
                                _outStream.write("TEXT 4 0 30 40 Hello World\r\n".getBytes());
                                Thread.sleep(500);
                                _outStream.write("PCX 10 10 ".getBytes("utf-8"));
                                _outStream.write(buf);
                                _outStream.flush();
                                Thread.sleep(500);
                                //_outStream.write("ENDPCX\r\nFORM\r\nPRINT\r\n".getBytes("utf-8"));
                                _outStream.write("\r\nFORM\r\nPRINT\r\n".getBytes("utf-8"));
                                _outStream.flush();
                                _outStream.close();
                            }else{
                                Log.d(TAG, "buffer is null");
                            }
                            _socket.close();
                        } catch (IOException e) {
                            //Error
                            Log.d(TAG, "printBitmapBT write Exception: " + e.getMessage());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        //Error
                        Log.d(TAG, "printBitmapBT getStream Exception: " + e.getMessage());
                    }
                }else{
                    Log.d(TAG, "printBitmapBT No socket or NOT connected");
                }
                sendMessage("print ended");
            }
        }).start();
    }
}
