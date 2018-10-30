package kasibhatla.dev.internet_tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = "download-task";
    File dFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        boolean networkStatus = isExternalStorageWritable();
        String net = "";
        if(networkStatus == true){net = "available";}
        else{net = "Unavailable";}
        TextView contentText = findViewById(R.id.t1);
        contentText.setText("External Storage\nis " + net);

    }



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }

        return false;
    }


    public void createNewDir(View view){
        File parent = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools");
        File child = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools"
                + "/files");
        if(!parent.exists()){
            parent.mkdir();
            child.mkdir();
        }
    }

    public void deleteDir(View view){
        File target = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools"
                + "/files");
        if(target.exists()){
            target.delete();
        }
    }

    public void createNewFile(View view){
        File csvData = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools"
                + "/files/" + "data.csv");
        FileOutputStream os = null;
        String data = "Hi, this is a new file! and extension";
        try {
            os = new FileOutputStream(csvData);
            os.write(data.getBytes());
            os.close();
        }catch(Exception f){
            f.printStackTrace();
        }
    }


    public void deleteFile(View view){
        File target = new File(Environment.getExternalStorageDirectory() + "/Android/data/kasibhatla.dev.internet_tools"
                + "/files/" + "data.csv");
        if(target.exists()){
            target.delete();
        }

    }

    public String indexFileLink = "https://docs.google.com/spreadsheets/d/e/2PACX-1vThYRC0NfdfBEgJXJCLhzRlvM-CrBDaPW5vaHSjD3UIrjrJXKO1hyq5t7MmfGaFbRrH6JzhpNME_Wy0/pub?gid=1241912515&single=true&output=csv";
    protected void doInBackground(){
        //URL url = new URL();
        //URL url = new URL("https://www.amrood.com/index.htm?language=en#j2se");
    }





}

