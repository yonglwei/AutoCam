package com.hunter.camservice.video;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.hunter.camservice.R;
import com.hunter.camservice.common.Utilities;

public class VideoListActivity extends Activity {

    /**
     * Tag of class.
     */
    private static final String TAG = "VideoListActivity";

    /**
     * Temporary file name.
     */
    private static final String TMP = "_T_m_P.mp4";

    /**
     * Extension of video files.
     */
    private static final String MP4 = ".mp4";

    /**
     * Item list for video files.
     *
     */
    private ListView listView;

    /**
     * List for video files.
     */
    private ArrayList<File> files = new ArrayList<File>();

    /**
     * List for selected video files.
     */
    private ArrayList<Integer> selectedFiles = new ArrayList<Integer>();

    /**
     * Initialization method.
     * @param savedInstanceState
     */
   
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(onItemClick);

        deleteTmpFile();
        actualize(false);
    }

    /**
     * On resume to this activity method.
     */
   
    protected void onResume() {
        super.onResume();

        this.deleteTmpFile();
    }

    /**
     * Initialization menu method.
     * @param menu
     * @return
     */
   
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.video_list, menu);
        return true;
    }

    /**
     * On menu item click method.
     * @param item
     * @return
     */
   
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_delete) {

            deleteSelected();
            return true;

        } else if (id == R.id.action_actualize) {

            actualize(true);
            return true;

        } else if (id == R.id.action_rename) {

            rename();
            return true;

        } else if (id == R.id.action_play) {

            playSelectedFiles();
            return true;

        } else if (id == R.id.action_merge) {

            mergeSelectedFiles();
            return true;
        } else if (id == R.id.action_info) {

            infoFiles();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Listener for click on item.
     */
    private OnItemClickListener onItemClick = new OnItemClickListener() {

        private Toast toast;

       
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (selectedFiles.contains(position)) {

                selectedFiles.remove(selectedFiles.indexOf(position));
            } else {

                selectedFiles.add(position);
            }

            if (toast == null) {
                toast = Toast.makeText(VideoListActivity.this, "", Toast.LENGTH_SHORT);
            }

            if (selectedFiles.size() == 0) {

                toast.setText("No file selected.");
                toast.show();
            } else {
                StringBuilder sb = new StringBuilder();

                sb.append("ORDER: ");

                for (int i = 0; i < selectedFiles.size(); i++) {
                    sb.append("\n(");
                    sb.append((i + 1));
                    sb.append(") - ");
                    sb.append(files.get(selectedFiles.get(i)).getName());
                    sb.append(" ");
                }

                toast.setText(sb);
                toast.show();
            }
        }
    };

    /**
     * Method for deleting temporary file.
     */
    private void deleteTmpFile() {

        File tmp = new File(Utilities.getAppDir(getApplicationContext()), TMP);

        if (tmp.exists()) {
            if (!tmp.delete()) {
                Log.e(TAG, "Temporary file cannot be deleted.");
            }
        }
    }

    /**
     * Method for extract names from list of files.
     * @param files List of files.
     * @return List of names.
     */
    private ArrayList<String> getNames(ArrayList<File> files) {

        ArrayList<String> names = new ArrayList<String>();

        for (File f : files) {
            names.add(f.getName());
        }

        return names;
    }

    /**
     * Method for getting video files from video folder.
     * @return List of video files.
     */
    private ArrayList<File> getVideoFiles() {

        ArrayList<File> list = new ArrayList<File>();

        File mediaStorageDir = Utilities.getAppDir(getApplicationContext());

        if (mediaStorageDir.exists()) {

            File[] fList = mediaStorageDir.listFiles();

            if (fList != null) {
                for (File f : fList) {

                    if (f.isFile() && f.getName().endsWith(".mp4")) {
                        list.add(f);
                    }
                }
            }
        }

        return list;
    }

    /**
     * Method for actualization of list of files.
     * @param showToast True if toast should be displayed.
     */
    private void actualize(boolean showToast) {

        selectedFiles.clear();
        files.clear();
        files = getVideoFiles();
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, getNames(files)));

        if (showToast) {
            Toast.makeText(VideoListActivity.this, "Actualized.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Delete selected video files.
     */
    private void deleteSelected() {

        if (selectedFiles.size() != 0) {
            for (Integer i : selectedFiles) {
                if (!files.get(i).delete()) {
                    Log.e(TAG, "Problem with deleting file - " + files.get(i).getPath());
                }
            }

            actualize(false);
            Toast.makeText(VideoListActivity.this, "Deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(VideoListActivity.this, "No file selected.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Rename selected video file.
     */
    private void rename() {

        if (selectedFiles.size() != 1) {

            Toast.makeText(VideoListActivity.this, "Renaming requires one file selected.", Toast.LENGTH_SHORT).show();
        } else {

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            final EditText input = new EditText(this);
            alertDialog.setView(input);
            alertDialog.setTitle("Rename");
            alertDialog.setMessage("Enter the new name without extension.");
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

               
                public void onClick(DialogInterface dialog, int which) {

                    String newName = input.getText().toString().trim();

                    File newFile = new File(Utilities.getAppDir(getApplicationContext()), newName + MP4);

                    if (newFile.exists()) {
                        Toast.makeText(VideoListActivity.this, "File with the name already exists.", Toast.LENGTH_SHORT).show();
                    } else if (newName.length() == 0) {
                        Toast.makeText(VideoListActivity.this, "Cannot rename file with zero characters.", Toast.LENGTH_SHORT).show();
                    } else {
                        File oldFile = files.get(selectedFiles.get(0));

                        if (oldFile.renameTo(newFile)) {
                            actualize(false);
                            Toast.makeText(VideoListActivity.this, "File was renamed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

               
                public void onClick(DialogInterface dialog, int which) {

                    alertDialog.cancel();
                }
            });

            alertDialog.show();
        }
    }

    /**
     * Merge selected video files.
     */
    private void mergeSelectedFiles() {

        if (selectedFiles.size() > 1) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            final EditText input = new EditText(this);

            alertDialog.setView(input);
            alertDialog.setTitle("Merge.");
            alertDialog.setMessage("Enter the new name without extension.");
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

               
                public void onClick(DialogInterface dialog, int which) {

                    String newName = input.getText().toString().trim();
                    File newFile = new File(Utilities.getAppDir(getApplicationContext()), newName + MP4);

                    if (newFile.exists()) {
                        Toast.makeText(VideoListActivity.this, "File with the name already exists.", Toast.LENGTH_SHORT).show();
                    } else if (newName.length() == 0) {
                        Toast.makeText(VideoListActivity.this, "Cannot create file with zero characters.", Toast.LENGTH_SHORT).show();
                    } else if (!checkVideoResolution()) {
                            Toast.makeText(VideoListActivity.this, "Files must have same resolution.", Toast.LENGTH_SHORT).show();
                    } else {
                        new MergeFiles().execute(newName + MP4);
                    }
                }
            });

            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

               
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.cancel();
                }
            });

            alertDialog.show();
        } else {
            Toast.makeText(VideoListActivity.this, "Select at least two files.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Play selected video files.
     */
    private void playSelectedFiles() {

        if (selectedFiles.size() == 0) {

            Toast.makeText(VideoListActivity.this, "Select at least one file.", Toast.LENGTH_SHORT).show();
            return;
        } else if (!checkVideoResolution()) {
            Toast.makeText(VideoListActivity.this, "Files must have same resolution.", Toast.LENGTH_SHORT).show();
            return;
        }

        new PlayFiles().execute(TMP);
    }

    /**
     * Displays info about selected file.
     */
    private void infoFiles() {

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Info");
        if (selectedFiles.size() == 0) {
            alertDialog.setMessage("No files selected.");
        } else {

            StringBuilder message = new StringBuilder();

            for (Integer i : selectedFiles) {
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

                metaRetriever.setDataSource(files.get(i).getPath());
                String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

                message.append(files.get(i).getName() + " - " + width + " x " + height + "\n");
            }
            alertDialog.setMessage(message);
        }

            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

               
                public void onClick(DialogInterface dialog, int which) {

                    alertDialog.cancel();
                }
            });

            alertDialog.show();

    }

    /**
     * Class for merging files.
     */
    private class MergeFiles extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog mergeDialog;

        protected void onPreExecute() {
            mergeDialog = ProgressDialog.show(VideoListActivity.this, "Please wait... ", "... merging files.", false);
        }

        protected void onPostExecute(Boolean success) {

            if (success) {
                mergeDialog.dismiss();
                actualize(false);
                Toast.makeText(VideoListActivity.this, "File was created.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(VideoListActivity.this, "Merging error.", Toast.LENGTH_SHORT).show();
            }
        }

       
        protected Boolean doInBackground(String... params) {

            if (params.length == 1) {

                mergeSelectedFiles(params[0]);
                return true;

            } else {
                Log.e(TAG, "Missing output file name parametr.");
                return false;
            }
        }

    }

    /**
     * Class for playing files.
     */
    private class PlayFiles extends AsyncTask<String, Void, File> {

        private ProgressDialog mergeDialog;

        protected void onPreExecute() {
            mergeDialog = ProgressDialog.show(VideoListActivity.this, "Please wait... ", "... merging files.", false);
        }

        protected void onPostExecute(File result) {
            mergeDialog.dismiss();

            Intent intent = new Intent(VideoListActivity.this, VideoPlayer.class);
            intent.putExtra("videoFilePath", result.getPath());
            startActivity(intent);
        }

       
        protected File doInBackground(String... params) {

            if (params.length == 1) {

                if (selectedFiles.size() == 1) {
                    return files.get(selectedFiles.get(0));
                } else {
                    return mergeSelectedFiles(params[0]);
                }
            } else {
                Log.e(TAG, "Missing output file name parametr.");
                return null;
            }
        }
    }

    private File mergeSelectedFiles(String newName) {

        File output = new File(Utilities.getAppDir(getApplicationContext()), newName);

        ArrayList<Movie> inMovies = new ArrayList<Movie>();

        for (Integer i : selectedFiles) {
            try {
                inMovies.add(MovieCreator.build(files.get(i).getPath()));
            } catch (IOException e) {
                Log.e(TAG, "Problem with building movies: " + e.getMessage());
            }
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (audioTracks.size() > 0) {

            try {
                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            } catch (IOException e) {
                Log.e(TAG, "Problem with adding audio tracks: " + e.getMessage());
            }
        }

        if (videoTracks.size() > 0) {

            try {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            } catch (IOException e) {
                Log.e(TAG, "Problem with adding video tracks: " + e.getMessage());
            }
        }

        Container out = new DefaultMp4Builder().build(result);

        try {
            FileChannel fc = new RandomAccessFile(output.getPath(), "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
        } catch (IOException e) {
            Log.e(TAG, "Problem with creating output: " + e.getMessage());
        }

        return output;
    }

    /**
     * Check if selected files have same resolution.
     * @return True if selected files have same resolution.
     */
    private boolean checkVideoResolution() {
        if (selectedFiles.size() > 0) {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

            metaRetriever.setDataSource(files.get(selectedFiles.get(0)).getPath());
            String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

            for (Integer i : selectedFiles) {
                MediaMetadataRetriever metaRetrieverSec = new MediaMetadataRetriever();

                metaRetrieverSec.setDataSource(files.get(i).getPath());
                String height2 = metaRetrieverSec.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                String width2 = metaRetrieverSec.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

                if (!width.equals(width2) || !height.equals(height2) ) {
                    return false;
                }
            }
        }

        return true;
    }
}

