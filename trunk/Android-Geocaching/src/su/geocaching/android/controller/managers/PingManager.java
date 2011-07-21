package su.geocaching.android.controller.managers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import su.geocaching.android.controller.Controller;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Jul 13, 2011
 * 
 */
public class PingManager {
    private static final String TAG = PingManager.class.getCanonicalName();
    private static final int TIME_INTERVAL = 1 * 60 * 1000; // interval between pings
    private static final int PING_URL_TIMEOUT = 30000; // timeout for checking reachable of PING_URL
    private static final String PING_URL = "http://pda.geocaching.su";
    private boolean isInternetConnected = true;
    private URL pingUrl;
    private volatile Thread pingThread;

    public PingManager() {
        try {
            pingUrl = new URL(PING_URL);
        } catch (MalformedURLException e) {
            LogManager.e(TAG, "PingManager init: malformed url (" + PING_URL + ")", e);
        }
    }

    public void start() {
        pingThread = new Thread(new PingServer());
        pingThread.start();
        pingThread.run();
        LogManager.d(TAG, "run ping thread");
    }

    public void stop() {
        pingThread = null;
        LogManager.d(TAG, "stop ping thread");
    }

    /**
     * @return the isInternetConnected
     */
    public boolean isInternetConnected() {
        return isInternetConnected;
    }

    /**
     * @param isInternetConnected
     *            the isInternetConnected to set
     */
    private synchronized void setInternetConnected(boolean isInternetConnected) {
        if (this.isInternetConnected && !isInternetConnected) {
            Controller.getInstance().getConnectionManager().getHandler().sendEmptyMessage(ConnectionManager.INTERNET_LOST);
        }
        if (!this.isInternetConnected && isInternetConnected) {
            Controller.getInstance().getConnectionManager().getHandler().sendEmptyMessage(ConnectionManager.INTERNET_FOUND);
        }
        this.isInternetConnected = isInternetConnected;
        LogManager.d(TAG, "set connected = " + isInternetConnected);
    }

    /**
     * TODO: what is better: syncronized log or log exceptions from other threads?
     * @author Grigory Kalabin. grigory.kalabin@gmail.com
     * @since Jul 14, 2011
     *
     */
    private class PingServer implements Runnable {
        
        public PingServer() {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            Thread thisThread = Thread.currentThread();
            while (pingThread == thisThread) {
                checkConnection();
                try {
                    Thread.sleep(TIME_INTERVAL);
                } catch (InterruptedException e) {
                }
            }
        }

        /**
         * Check reachable or not of server 
         */
        private void checkConnection() {
            boolean isConnected = true;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) pingUrl.openConnection();
                connection.setConnectTimeout(PING_URL_TIMEOUT);
                connection.setReadTimeout(PING_URL_TIMEOUT);
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    isConnected = false;
                } else {
                }
            } catch (IOException e) {
                isConnected = false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            setInternetConnected(isConnected);
        }
    }

}
