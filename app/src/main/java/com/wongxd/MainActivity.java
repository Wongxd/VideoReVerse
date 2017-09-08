package com.wongxd;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wongxd.video.PermissionsActivity;
import com.wongxd.video.PreviewActivity;
import com.wongxd.video.R;
import com.wongxd.video.ShootActivity;
import com.wongxd.video.helper.PermissionHelper;

import static com.wongxd.video.PermissionsActivity.REQUEST_CODE;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_CHOOSE = 10001;

    private PermissionHelper mPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_main);

        mPermissionHelper = new PermissionHelper(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionHelper.lacksPermissions(PermissionsActivity.PERMISSIONS)) {
            PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PermissionsActivity.PERMISSIONS);
        } else {
            //有权限

            Button btnShoot = (Button) findViewById(R.id.btn_shoot);
            Button btnSelect = (Button) findViewById(R.id.btn_select);

            btnSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectVideo();
                }
            });

            btnShoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, ShootActivity.class));
                }
            });
        }
    }

    private void selectVideo() {

//        Matisse.from(MainActivity.this)
//                .choose(MimeType.of(MimeType.valueOf("")))
//                .countable(true)
//                .maxSelectable(1)
//                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
//                .thumbnailScale(0.85f)
//                .imageEngine(new GlideEngine())
//                .forResult(REQUEST_CODE_CHOOSE);

        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_CODE_CHOOSE);
    }


// private   List<Uri> mSelected;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
//            mSelected = Matisse.obtainResult(data);
//            Log.d("Matisse", "mSelected: " + mSelected);
//        }


        if (resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
            selectVideo();
            return;

        } else if (resultCode == -100) {
            finish();

        }

        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String videoPath = cursor.getString(columnIndex);
            cursor.close();

            Intent i = new Intent(this, PreviewActivity.class);
            i.putExtra("path", videoPath);
            startActivity(i);

        }
    }
}
