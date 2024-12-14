package com.example.lvmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SubFolderActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 3;

    private String subFolderPath;
    private ListView imagesListView;
    private ArrayList<File> imageFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subfolder);

        // Almappa elérési út
        subFolderPath = getIntent().getStringExtra("subFolderPath");

        // Kamera megnyitása gomb inicializálása
        Button openCameraBtn = findViewById(R.id.open_camera_button);
        openCameraBtn.setOnClickListener(v -> openCamera());

        // Listview és kép fájl lista inicializálása
        imagesListView = findViewById(R.id.images_list_view);
        imageFiles = new ArrayList<>();

        // Képek betöltése és megjelenítése
        loadImagesFromSubfolder();
    }

    private void openCamera() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Megfelelő engedélyek esetén kamera alkalmazás megnyitása
            startCameraIntent();
        }
    }

    // Kamera alkalmazás indítása
    private void startCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera nem elérhető", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            // Get the image bitmap from the camera intent
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            // Kép mentése az almappába
            saveImageToSubfolder(imageBitmap);
        }
    }

    // Készített kép mentése az almappába
    private void saveImageToSubfolder(Bitmap imageBitmap) {
        if (subFolderPath == null) {
            Toast.makeText(this, "Hibás elérési útvonal", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fájl készítése a mappában és ellenőrzése
        File subFolder = new File(subFolderPath);
        if (!subFolder.exists() && !subFolder.mkdirs()) {
            Toast.makeText(this, "Nem sikerült létrehozni a mappát", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fájl készítése a képnek a mappában
        String imageName = "image_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(subFolder, imageName);

        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            // Tömörítés és kép mentése fájlba
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Kép elmentve!", Toast.LENGTH_SHORT).show();

            // Mentés után a képek újra betöltése
            loadImagesFromSubfolder();
        } catch (IOException e) {
            Log.e("SubFolderActivity", "Hiba a kép mentése során", e);
            Toast.makeText(this, "Hiba a kép mentése során", Toast.LENGTH_SHORT).show();
        }
    }

    // A mappában lévő összes kép betöltése
    private void loadImagesFromSubfolder() {
        if (subFolderPath == null) {
            return;
        }

        // Előző lista törlése
        imageFiles.clear();

        // File objektum létrehozása az almappában
        File subFolder = new File(subFolderPath);
        if (subFolder.exists()) {
            File[] files = subFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Ellenőrzés hogy a fájl JPG v. PNG
                    if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
                        imageFiles.add(file);
                    }
                }
            }
        }

        // ListView adapter létrehozása
        ImageAdapter imageAdapter = new ImageAdapter(this, imageFiles, subFolderPath);
        imagesListView.setAdapter(imageAdapter);
    }

}



