package com.example.lvmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> projects = new ArrayList<>();
    private File baseDir;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Alkönyvtár a projektekhez
        baseDir = new File(getExternalFilesDir(null), "LVManager");
        if (!baseDir.exists()) baseDir.mkdirs();

        // Listview és adapter beállítása
        ListView projectListView = findViewById(R.id.project_list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, projects);
        projectListView.setAdapter(adapter);

        // ProjectActivity megnyitása érintéskor
        projectListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProject = projects.get(position);
            Intent intent = new Intent(this, ProjectActivity.class);
            intent.putExtra("projectName", selectedProject);
            startActivity(intent);
        });

        // Hosszan nyomáskor törlés ablak
        projectListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedProject = projects.get(position);
            showDeleteConfirmationDialog(selectedProject, position);
            return true; // Indicate that the long-click event was handled
        });

        // Új projekt hozzáadása
        Button addProjectBtn = findViewById(R.id.add_project_button);
        addProjectBtn.setOnClickListener(v -> promptNewProject());

        // Már létező mappák betöltése
        loadProjects();
    }

    // Létező projektek listába töltése
    private void loadProjects() {
        File[] files = baseDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) projects.add(file.getName());
            }
        }
        adapter.notifyDataSetChanged(); // ListView frissítése
    }

    // Törlés jóváhagyása
    private void showDeleteConfirmationDialog(String projectName, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Biztosan törli a projektet?")
                .setMessage("Törlés után nincs lehetőség a projekt visszaállítására!")
                .setPositiveButton("Igen", (dialog, which) -> {
                    File projectFolder = new File(baseDir, projectName);
                    if (deleteDirectory(projectFolder)) {
                        projects.remove(position);
                        adapter.notifyDataSetChanged(); // ListView frissítése
                        Toast.makeText(this, "Projekt törölve", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Nem sikerült törölni a projektet", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Nem", null)
                .show();
    }

    // Segítő metódus mappák törléséhez
    private boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }

    // Új mappa készítése párbeszédablak
    private void promptNewProject() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kérem adja meg a projekt nevét");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_new_project, null);
        builder.setView(customLayout);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String projectName = ((EditText) customLayout.findViewById(R.id.project_name_input)).getText().toString();
            if (!projectName.trim().isEmpty()) {
                File projectDir = new File(baseDir, projectName);
                if (projectDir.mkdirs()) {
                    projects.add(projectName);
                    adapter.notifyDataSetChanged();
                } else {
                    showErrorDialog("Nem sikerült létrehozni a projektet. Próbálja újra.");
                }
            } else {
                showErrorDialog("A projekt neve nem lehet üres.");
            }
        });

        builder.setNegativeButton("Mégsem", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Hibaüzenet
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Hiba")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

