package org.iutrt.lyricsfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText inputArtist;
    private EditText inputTitle;
    private Button btnSearch;
    private boolean artistHasText = false;
    private boolean titleHasText = false;

    @SuppressWarnings("Duplicates")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Récupération des views :
        inputArtist = findViewById(R.id.main_inputArtist);
        inputTitle = findViewById(R.id.main_inputTitle);
        btnSearch = findViewById(R.id.main_btnSearch);

//        Désactivation du bouton de recherche et mise en place du focus sur le champ Artiste :
        btnSearch.setEnabled(false);
        inputArtist.requestFocus();

//        Création d'un textChangedListener pour les deux champs de texte afin de n'activer le bouton que si du texte a été entré (via la méthode changeButtonState) :
        inputArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) artistHasText = true;
                else artistHasText = false;
                changeButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) titleHasText = true;
                else titleHasText = false;
                changeButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void changeButtonState(){
//        Vérifie que les deux champs ont du texte et active/désactive le bouton en fonction :
        if (artistHasText && titleHasText) btnSearch.setEnabled(true);
        else btnSearch.setEnabled(false);
    }

    protected void btnClick(View v){
//        Définition de l'intent vers LyricsActivity contenant l'artiste et le titre et démarrage de l'activité :
        Intent lyricsActivity = new Intent(this, LyricsActivity.class);
        lyricsActivity.putExtra("artist", inputArtist.getText().toString());
        lyricsActivity.putExtra("title", inputTitle.getText().toString());
        startActivity(lyricsActivity);
    }
}
