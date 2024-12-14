package com.example.lvmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;

public class ProjectActivity extends AppCompatActivity {
    private String projectName;
    private File projectDir;
    private ArrayList<String> subFolders = new ArrayList<>();
    private ListView subFolderListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        projectName = getIntent().getStringExtra("projectName");
        projectDir = new File(getExternalFilesDir(null), "LVManager/" + projectName);

        Button addSubfolderBtn = findViewById(R.id.add_subfolder_button);
        subFolderListView = findViewById(R.id.subfolder_list_view);

        // Meglévő mappák betöltése
        loadSubFolders();

        addSubfolderBtn.setOnClickListener(v -> promptNewSubfolder());

        // Érintés funckció kezelése
        subFolderListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSubfolder = subFolders.get(position);
            File subfolderPath = new File(projectDir, selectedSubfolder);
            openSubfolderActivity(subfolderPath.getAbsolutePath());
        });

        // Törlés
        subFolderListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedSubfolder = subFolders.get(position);
            showDeleteConfirmationDialog(selectedSubfolder, position);
            return true;
        });
    }

    // Meglévő mappák betöltése ListView-be
    private void loadSubFolders() {
        File[] files = projectDir.listFiles();
        if (files != null) {
            subFolders.clear();  // Clear the list before adding new data
            for (File file : files) {
                if (file.isDirectory()) {
                    subFolders.add(file.getName());
                }
            }
        }

        // ListView -> projektek megjelenítése
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subFolders);
        subFolderListView.setAdapter(adapter);
    }

    // SubFolderActivity megnyitása
    private void openSubfolderActivity(String subFolderPath) {
        Intent intent = new Intent(this, SubFolderActivity.class);
        intent.putExtra("subFolderPath", subFolderPath);
        startActivity(intent);
    }

    // Törlés jóváhagyása
    private void showDeleteConfirmationDialog(String subfolderName, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Biztosan törli a mappát?")
                .setMessage("A mappa visszaállítása nem lehetséges a törlés után!")
                .setPositiveButton("Igen", (dialog, which) -> {
                    File subfolder = new File(projectDir, subfolderName);
                    if (deleteDirectory(subfolder)) {
                        subFolders.remove(position);
                        ((ArrayAdapter) subFolderListView.getAdapter()).notifyDataSetChanged(); // Listview frissítése
                        Toast.makeText(this, "Mappa törölve", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Nem sikerült törölni a mappát", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Nem", null)
                .show();
    }

    // Metódos a mappa, és a mappában lévő tartalmak törlésére
    private boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();  // Mappa törlése
    }

    // Párbeszédablak új mappa létrehozásához
    private void promptNewSubfolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("LV szekrény hozzáadása");

        // Inflate custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_subfolder, null);
        builder.setView(dialogView);

        EditText inputSerialNumber = dialogView.findViewById(R.id.input_serial_number);
        inputSerialNumber.setHint("Sorozatszám");
        EditText inputSeriesNumber = dialogView.findViewById(R.id.input_series_number);
        inputSeriesNumber.setHint("Szériaszám");

        builder.setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String serialNumber = inputSerialNumber.getText().toString().trim();
                    String seriesNumber = inputSeriesNumber.getText().toString().trim();

                    if (serialNumber.isEmpty() || seriesNumber.isEmpty()) {
                        Toast.makeText(this, "Mindkét mezőt ki kell tölteni!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String subFolderName = serialNumber + "_" + seriesNumber;
                    File subFolder = new File(projectDir, subFolderName);

                    if (subFolder.mkdirs()) {
                        subFolders.add(subFolderName);
                        Toast.makeText(this, "Mappa hozzáadva!", Toast.LENGTH_SHORT).show();
                        ((ArrayAdapter) subFolderListView.getAdapter()).notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Hiba a mappa hozzáadása során", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Mégsem", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}



