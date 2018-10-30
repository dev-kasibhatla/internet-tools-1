package kasibhatla.dev.internet_tools;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * Android Drive Quickstart activity. This activity takes a photo and saves it in Google Drive. The
 * user is prompted with a pre-made dialog which allows them to choose the file location.
 */
public class MainActivity extends Activity {

    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;


    private GoogleSignInClient mGoogleSignInClient;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private Bitmap mBitmapToSave;

    public int amILoggedIn = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        TextView netStat = findViewById(R.id.netStat);

        if (networkInfo != null && networkInfo.isConnected()) {
            netStat.setText("Connected");
        } else {
            netStat.setText("Disconnected");
        }
    }


    /** Start sign in activity. */

    private void signIn() {
        Log.i(TAG, "Start sign in");
        GoogleSignInClient GoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }


    /** Build a Google SignIn client. */
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    /** Create a new file and save it to Drive. */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;

        mDriveResourceClient
                .createContents()
                .continueWithTask(
                        new Continuation<DriveContents, Task<Void>>() {
                            @Override
                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                return createFileIntentSender(task.getResult(), image);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed to create new contents.", e);
                            }
                        });
    }

    public void uploadToDrive(){
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
       // final File file = File(this.getFilesDir())

    }





    /**
     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
     * CreateFileActivityOptions} for user to create a new photo in Drive.
     */
    private Task<Void> createFileIntentSender(DriveContents driveContents, Bitmap image) {
        Log.i(TAG, "New contents created.");
        // Get an output stream for the contents.
        OutputStream outputStream = driveContents.getOutputStream();
        // Write the bitmap data from it.
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
        try {
            outputStream.write(bitmapStream.toByteArray());
        } catch (IOException e) {
            Log.w(TAG, "Unable to write file contents.", e);
        }

        // Create the initial metadata - MIME type and title.
        // Note that the user will be able to change the title later.
        MetadataChangeSet metadataChangeSet =
                new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg")
                        .setTitle("Android Photo.png")
                        .build();


        // Set up options to configure and display the create file activity.
        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        new Continuation<IntentSender, Void>() {
                            @Override
                            public Void then(@NonNull Task<IntentSender> task) throws Exception {
                                startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
                                return null;
                            }
                        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView netStat = findViewById(R.id.netStat);
        String request = "Request Code: " + requestCode + " res code: " + resultCode;
        netStat.setText(request);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i(TAG, "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    amILoggedIn = 1;
                    Log.i(TAG, "Signed in successfully.");
                    Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                    // Use the last signed in account here since it already has a Drive scope.
                    mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Build a drive resource client.
                    mDriveResourceClient =
                            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Start camera.


                }
                break;
            case REQUEST_CODE_CAPTURE_IMAGE:
                Log.i(TAG, "capture image request code");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Image captured successfully.");
                    // Store the image data as a bitmap for writing later.
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                    saveFileToDrive();
                }
                break;
            case REQUEST_CODE_CREATOR:
                Log.i(TAG, "creator request code");
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    Toast.makeText(this, "Image Uploaded!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public void checkStatus (View view){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        TextView netStat = findViewById(R.id.netStat);

       /* Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                netStat.setText("Refreshing");
            }
        }, 100);*/

        if (networkInfo != null && networkInfo.isConnected()) {
            netStat.setText("Connected");
        } else {
            netStat.setText("Disconnected");
        }

    }
    public void uploadImage(View view){
        if(amILoggedIn == 0){
            Toast.makeText(this, "Please Sign in First", Toast.LENGTH_SHORT).show();
        }
        else{
            startActivityForResult(
                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
        }


    }

    public void uploadfile(View view){
        if(amILoggedIn == 0){
            Toast.makeText(this, "Please Sign in First", Toast.LENGTH_SHORT).show();
        }
        else{
           }

       // File file = new File(this.getFilesDir(), "myFile");
        FileOutputStream outputStream;
        String fileContents = "Can you read this";
        try {
            outputStream = openFileOutput("myFile.txt", Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
            Toast.makeText(this, "File Created", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void readFile(View view){
        /*
        * To read an existing file, call openFileInput(name), passing the name of the file.
          You can get an array of all your app's file names by calling fileList().
        * */
        String fileRead = "";
        FileInputStream inputStream;
        try {
            inputStream = openFileInput("myFile.txt");
            byte b[] = {0};
            int i; char c;
            int index = 0;
            while((i = inputStream.read())!= -1){
                c = (char)i;
                fileRead = fileRead + Character.toString(c);
                index++;
              //  if(index > 10){break;}
            }

            inputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        TextView f = findViewById(R.id.textLoading);
        f.setText(fileRead);
    }

    public void moreAppend(View view){
        FileOutputStream outputStream;
        String fileContents = " new text added ";
        try{
            outputStream = openFileOutput("myFile.txt", Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
            Toast.makeText(this, "Appended", Toast.LENGTH_SHORT).show();
        } catch(Exception f){
            f.printStackTrace();
        }

    }


    public void justSignIn(View view){
        if(amILoggedIn == 0){
            TextView textView = findViewById(R.id.textLoading);
            textView.setText("Loading");
            signIn();
            textView.setText("Complete");        }
        else{
            Toast.makeText(this, "You are already logged in", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadTaskSwitch(View view){
        Intent i = new Intent(MainActivity.this, DownloadActivity.class);
        startActivity(i);

    }

}
