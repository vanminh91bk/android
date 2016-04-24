package minh.thuan.alarmtrafficjam.communication;

import android.util.Log;

import com.google.gson.Gson;
import com.minhthuan.lib.network.ISocketStringEvent;
import com.minhthuan.lib.result.Protocol;
import com.minhthuan.lib.ultil.Global;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import minh.thuan.alarmtrafficjam.ulities.GMessage;
import minh.thuan.alarmtrafficjam.ulities.Ultil;

/**
 * Created by sev_user on 10/07/15.
 */
public class MyClient {

    public String serverIP;
    public int serverPort;
    private Socket socket;// = new Socket();
    private String TAG = "MyClient";
    private ISocketStringEvent mISocketStringEvent;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private ReceiveData receiveData;


    public MyClient(String ip, int port) {
        GMessage.showMessage(TAG, "MyClient");
        GMessage.showMessage(TAG, "IP: " + ip + " Port: " + port);
        this.serverIP = ip;
        this.serverPort = port;
    }


    public boolean connect() {

        GMessage.showMessage(TAG, "Connect to socket");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() {

                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(serverIP, serverPort), Global.DEFAULT_TIME_LIMIT_MS);
                    printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    return true;
                } catch (UnknownHostException e) {
                    GMessage.showMessage(TAG, e.getMessage());
                } catch (IOException e) {
                    GMessage.showMessage(TAG, e.getMessage());
                }
                return false;
            }
        };
        Future<Boolean> future = executor.submit(callable);
        boolean result = false;
        try {
            result = future.get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
        }
        executor.shutdown();
        if (result) {
            GMessage.showMessage(TAG, "Connect successfully!");
            receiveData = new ReceiveData();
            receiveData.start();
            return true;
        } else {
            GMessage.showMessage(TAG, "Connect fail!");
            return false;
        }
    }

    public void setListener(ISocketStringEvent mISocketStringEvent) {
        GMessage.showMessage(TAG, "setListener");
        this.mISocketStringEvent = mISocketStringEvent;
    }


    private boolean closeSocket() {
        if (isConnect()) {
            try {
                socket.close();
                return true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return false;
    }

    public boolean send(Protocol protocol) {
        if (!isConnect()) {
            if (!connect()) {
                GMessage.showMessage(TAG, "Don't have connect to server!");
                return false;
            }
        }
        String msg = new Gson().toJson(protocol);
        GMessage.showMessage(TAG, "Send: " + msg);
        printWriter.println(msg);
        return true;
    }


    public boolean send(File file) {
        Protocol protocol = new Protocol(Protocol.Type.UPDATE_AVATAR, null, Ultil.user);
        if (!send(protocol))
            return false;

        GMessage.showMessage(TAG, "Send file: " + file.getAbsolutePath());

        byte[] mybytearray = new byte[(int) file.length()];
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(mybytearray, 0, mybytearray.length);
            OutputStream os = socket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
        } catch (IOException e) {
            GMessage.showMessage(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    private boolean isConnect() {
        if (socket != null && socket.isConnected()) {
            return true;
        }
        return false;
    }


    public class ReceiveData extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                GMessage.showMessage(TAG, "Run thread receive data from tcp");
                while (isConnect()) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        GMessage.showMessage(TAG, line);
                        if (mISocketStringEvent != null) {
                            mISocketStringEvent.onDataReceive(line);
                        }
                    }
                }
            } catch (IOException e) {
                GMessage.showMessage(TAG, "close connection " + e.getMessage());
                closeSocket();
            }
        }
    }
}
