package com.project.videoeditor.codecs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.arthenica.mobileffmpeg.Config;
import com.project.videoeditor.R;
import com.project.videoeditor.VideoInfo;
import com.project.videoeditor.support.UtilUri;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsMPEG4Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsMPEG4Fragment extends Fragment {
    private View viewPointer;
    private static final int FOLDERPICKER_CODE = 101;
    private String selectedFormat;
    private TextView countdownText;
    private LinearLayout settingsEncodeLayout;
    static private VideoInfo videoInfo;
    private ProgressBar progressBar;
    public SettingsMPEG4Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SettingsMPEG4Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsMPEG4Fragment newInstance(VideoInfo _videoInfo) {
        SettingsMPEG4Fragment fragment = new SettingsMPEG4Fragment();
        videoInfo = _videoInfo;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        countdownText = (TextView) getActivity().findViewById(R.id.textView_Countdown);
//        settingsEncodeLayout = (LinearLayout) getActivity().findViewById(R.id.settingsEncodeLayout);
//        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_mpeg4, container, false);
        view.findViewById(R.id.buttonSelectFolderPath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPath();
            }
        });
        view.findViewById(R.id.buttonRunEncode_MPEG4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    RunEncode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Spinner spinner = (Spinner) view.findViewById(R.id.Spinner_FormatVideoFile);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.formats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        selectedFormat = "mp4";

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Получаем выбранный объект
                selectedFormat = (String)parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
        viewPointer = view;
        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FOLDERPICKER_CODE:
                    Uri uri = data.getData();
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    String path2 = UtilUri.getPath(getContext(), docUri);
                    ((EditText)viewPointer.findViewById(R.id.editText_FolderPath)).setText(path2);
                    break;
            }
        }
    }
    private void SelectPath()
    {
        try {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            DocumentFile file = DocumentFile.fromFile( Environment.getDataDirectory());
            //i.putExtra(EXTRA_INITIAL_URI,file.getUri());
            startActivityForResult(Intent.createChooser(i, "Choose directory"), FOLDERPICKER_CODE);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    private void RunEncode() throws Exception {
        String framerateVideo = ((EditText)viewPointer.findViewById(R.id.editText_Framerate)).getText().toString();
        String bitrateVideo = ((EditText)viewPointer.findViewById(R.id.editText_Bitrate)).getText().toString();
        String formatVideo = ((Spinner)viewPointer.findViewById(R.id.Spinner_FormatVideoFile)).getSelectedItem().toString();
        String folderPathVideo = ((EditText)viewPointer.findViewById(R.id.editText_FolderPath)).getText().toString();
        String filenameVideo = ((EditText)viewPointer.findViewById(R.id.editText_Filename)).getText().toString();

        int valueVideoQuality = ((IndicatorSeekBar)viewPointer.findViewById(R.id.IndicatorSeekBar_VideoQuality)).getProgress();
        int valueAudioQuality = ((IndicatorSeekBar)viewPointer.findViewById(R.id.IndicatorSeekBar_AudioQuality)).getProgress();
        Handler handler = new Handler();
        settingsEncodeLayout.setVisibility(View.INVISIBLE);
        countdownText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        ActionEditor.EncodeMPEG4(videoInfo.getPath(),folderPathVideo + "\\"+filenameVideo+"."+formatVideo,valueVideoQuality,valueAudioQuality,bitrateVideo,framerateVideo);
        handler.post(new Runnable() {
            @Override
            public void run() {

                double encodeSpeed = Config.getLastReceivedStatistics().getSpeed();
                long ms = (long)((videoInfo.getDuration() - Config.getLastReceivedStatistics().getTime()) / encodeSpeed);
                long secs = TimeUnit.MILLISECONDS.toSeconds(ms)  % 60;
                long hours = TimeUnit.MILLISECONDS.toHours(ms)  % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(ms)  % 60;

                countdownText.setText(String.format(Locale.getDefault(),
                        "%d:%02d:%02d", hours, minutes, secs));
                if(secs > 0) {
                    handler.postDelayed(this, 1000);
                }
                else {
                    settingsEncodeLayout.setVisibility(View.VISIBLE);
                    countdownText.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


    }
}
