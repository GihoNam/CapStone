package gujc.serviceexample;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class RealService extends Service {
    private Thread mainThread;
    public static Intent serviceIntent = null;
    private Socket mSocket;
    public RealService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;
       final JSONObject data = new JSONObject();
        showToast(getApplication(), "Start Service");


        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("aa hh:mm");
                boolean run = true;
                while (run) {
                    try {
                        mSocket = IO.socket("");
                        mSocket.connect();
                        try{
                            data.put("key1",getIP());               //IP를 가져오는 부분
                            mSocket.emit("event-name",data);
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                        mSocket.on("chat-message",onMessageRecived);
                    }
                       /* Thread.sleep(1000 * 60 * 1); // 1 minute
                        Date date = new Date();
                        showToast(getApplication(), sdf.format(date));*/
                       catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mainThread.start();

        return START_NOT_STICKY;
    }

    public static String getIP(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        serviceIntent = null;
        setAlarmTimer();
        Thread.currentThread().interrupt();

        if (mainThread != null) {
            mainThread.interrupt();
            mainThread = null;
        }
    }

    private Emitter.Listener onMessageRecived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject receivedData = (JSONObject) args[0];
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmRecever.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

}
