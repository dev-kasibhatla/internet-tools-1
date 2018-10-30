package kasibhatla.dev.internet_tools;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = "download-task";
    File dFile;
    ProgressBar mProgressBar;
    File sdcard = Environment.getExternalStorageDirectory();
    public String packageName = "kasibhatla.dev.internet_tools";
    public String downloadLocation = sdcard + "/Android/data/" + packageName + "/files/";
    public String recentDownload = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        boolean networkStatus = isExternalStorageWritable();
        String net = "";
        if (networkStatus == true) {
            net = "available";
        } else {
            net = "Unavailable";
        }
        TextView contentText = findViewById(R.id.t1);
        contentText.setText("External Storage\nis " + net);

        //setting progressbar properties
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);


    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }

        return false;
    }


    public void createNewDir(View view) {
        File parent = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools");
        File child = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools"
                + "/files");
        if (!parent.exists() || !child.exists()) {
            parent.mkdir();
            child.mkdir();
        }
    }

    public void deleteDir(View view) {
        File target = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools"
                + "/files");
        if (target.exists()) {
            target.delete();
        }
    }

    public void createNewFile(View view) {
        File csvData = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools"
                + "/files/" + "data.csv");
        FileOutputStream os = null;
        String data = "Hi, this is a new file! and extension";
        try {
            os = new FileOutputStream(csvData);
            os.write(data.getBytes());
            os.close();
        } catch (Exception f) {
            f.printStackTrace();
        }
    }


    public void deleteFile(View view) {
        File target = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools"
                + "/files/" + "data.csv");
        if (target.exists()) {
            target.delete();
        }

    }

    public void downloadFIle(View view){
        final DownloadTask download= new DownloadTask(DownloadActivity.this);
        download.execute(indexFileLink);
    }

    public void readDownloadedFile(View view){
        File file = new File (downloadLocation + recentDownload);
        FileInputStream in = null;
        String dataRead = "";
        char c; byte b[] = {0};
        int i = 0;
        try{
            in = new FileInputStream(file);
            while ((i = in.read()) != -1){
                c = (char)i;
                dataRead = dataRead + Character.toString(c);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        TextView printThis = findViewById(R.id.textRead);
        printThis.setText(dataRead);
    }




    public String indexFileLink = "https://docs.google.com/spreadsheets/d/e/2PACX-1vThYRC0NfdfBEgJXJCLhzRlvM-CrBDaPW5vaHSjD3UIrjrJXKO1hyq5t7MmfGaFbRrH6JzhpNME_Wy0/pub?gid=1241912515&single=true&output=csv";


     // usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
    public class DownloadTask extends AsyncTask<String, Integer, String> {
       //

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(downloadLocation + "data.csv");
                recentDownload = "data.csv";
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();

            mProgressBar = findViewById(R.id.progressBar);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressBar = findViewById(R.id.progressBar);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressBar = findViewById(R.id.progressBar);
            mProgressBar.setVisibility(View.INVISIBLE);
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
        }

    }
}