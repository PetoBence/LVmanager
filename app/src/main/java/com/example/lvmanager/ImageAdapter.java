package com.example.lvmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<File> imageFiles;
    private LayoutInflater inflater;
    private String subFolderPath;

    public ImageAdapter(Context context, ArrayList<File> imageFiles, String subFolderPath) {
        this.context = context;
        this.imageFiles = imageFiles;
        this.subFolderPath = subFolderPath;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imageFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return imageFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.image_item_layout, parent, false);
        }

        // Kép fájl get
        File imageFile = imageFiles.get(position);

        // Imageview és gomb megkeresése a layoutban
        ImageView imageView = convertView.findViewById(R.id.image_view);
        Button deleteButton = convertView.findViewById(R.id.delete_button);

        // Kép dekódolása -> Bitman, ImageView hozzáadás
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imageView.setImageBitmap(bitmap);

        // A törlés gomb megnyitja a párbeszéd ablakot
        deleteButton.setOnClickListener(v -> {
            // Show a confirmation dialog for deletion
            new AlertDialog.Builder(context)
                    .setTitle("Biztosan törli a képet?")
                    .setPositiveButton("Igen", (dialog, which) -> {
                        if (imageFile.exists()) {
                            boolean deleted = imageFile.delete();
                            if (deleted) {
                                imageFiles.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context, "Kép törölve", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Nem sikerült törölni a képet", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Nem", null)
                    .show();
        });

        return convertView;
    }

    // Metódus hogy jóváhagyás nélkül rögtön mentse a képet a mappába (sajnos nem működik)
    public void addImage(File newImageFile) {
        imageFiles.add(newImageFile);
        notifyDataSetChanged();
    }
}



