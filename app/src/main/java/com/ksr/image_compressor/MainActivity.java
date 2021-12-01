package com.ksr.image_compressor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    public static final int RESULT_IMAGE = 101;

    ImageView imageOriginal, imageCompressed;
    TextView textOriginal, textCompressed, textQuality;
    EditText editTextHeight, editTextWidth;
    SeekBar seekQuality;
    Button btnPick, btnCompress;
    File originalImage, compressedImage;
    private static String filePath;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Compressed_Files");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();

        imageOriginal = findViewById(R.id.imageOriginal);
        imageCompressed = findViewById(R.id.imageCompressed);
        textOriginal = findViewById(R.id.textOriginal);
        textCompressed = findViewById(R.id.textCompressed);
        textQuality = findViewById(R.id.textQuality);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWidth = findViewById(R.id.editTextWidth);
        seekQuality = findViewById(R.id.seekQuality);
        btnPick = findViewById(R.id.btnPick);
        btnCompress = findViewById(R.id.btnCompress);


        filePath = path.getAbsolutePath();
        if (!path.exists()){
            path.mkdirs();
        }

        seekQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textQuality.setText("Quality: "+ i);
                seekQuality.setMax(100);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quality = seekQuality.getProgress();
                int width = Integer.parseInt(editTextWidth.getText().toString());
                int height = Integer.parseInt(editTextHeight.getText().toString());

                try {
                    compressedImage = new Compressor(MainActivity.this)
                            .setMaxWidth(width)
                            .setMaxHeight(height)
                            .setQuality(quality)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(filePath)
                            .compressToFile(originalImage);

                    File finalFile = new File(filePath,originalImage.getName());
                    Bitmap finalBitmap = BitmapFactory.decodeFile(finalFile.getAbsolutePath());
                    imageCompressed.setImageBitmap(finalBitmap);
                    textCompressed.setText("Size: "+ Formatter.formatShortFileSize(MainActivity.this, finalFile.length()));
                    Toast.makeText(MainActivity.this, "Image compressed & saved succesfully!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error occured while compressing!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            btnCompress.setVisibility(View.VISIBLE);
            final Uri imgUri = data.getData();
            try {
                final InputStream imgInputStream = getContentResolver().openInputStream(imgUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imgInputStream);
                imageOriginal.setImageBitmap(selectedImage);
                originalImage = new File(imgUri.getPath().replace("raw/",""));
                textOriginal.setText("Size: "+ Formatter.formatShortFileSize(this, originalImage.length()));
                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void askPermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
}