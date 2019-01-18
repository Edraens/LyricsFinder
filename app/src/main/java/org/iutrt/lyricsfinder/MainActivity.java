package org.iutrt.lyricsfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView inputArtist;
    private EditText inputTitle;
    private Button btnSearch;
    private boolean artistHasText = false;
    private boolean titleHasText = false;

    private String[] test = new String[]{"bonjour", "bonsoir"};

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

//        Test autosuggest :
//        inputArtist.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, test));

//        Création d'un textChangedListener pour les deux champs de texte afin de n'activer le bouton que si du texte a été entré (via la méthode changeButtonState) :
        inputArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 3) {
                    RequestArtist req = new RequestArtist();
                    try {
                        List<String> artists = req.execute(inputArtist.getText().toString()).get();
                        String[] artistsArray = new String[artists.size()];
                        artistsArray = artists.toArray(artistsArray);
                        inputArtist.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, artistsArray));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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

    @SuppressWarnings("Duplicates")
    private class RequestArtist extends AsyncTask<String, Void, List<String>> {
        protected List<String> doInBackground(String... artist) {
            List<String> response = request(artist[0]);
            return response;
        }

        protected List<String> request(String artist) {
            List<String> response = null;

            try {
                HttpURLConnection connection = null;
                URL url = new
                        URL("http://musicbrainz.org/ws/2/artist/?fmt=json&query=" + URLEncoder.encode(artist, "UTF-8"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String rawJson = "";
                String ligne = bufferedReader.readLine();
                while (ligne != null) {
                    rawJson += ligne;
                    ligne = bufferedReader.readLine();
                }
                JSONObject responseJSON = new JSONObject(rawJson);
                JSONArray arrayArtistsJSON = new JSONArray(responseJSON.getJSONArray("artists"));
                response = getArtistsFromJSON(arrayArtistsJSON);
            } catch (Exception e) {
                Log.d("Lookup", "nothing found");
            }
            return response;
        }

        private List<String> getArtistsFromJSON(JSONArray array) throws Exception {
            List<String> response = new ArrayList<String>();
            for (int i=0; i<array.length(); i++){
                String artist = array.getJSONObject(i).getString("name");
                response.add(artist);
            }
            return response;
        }

//        protected void onPostExecute(List<String> result) {
//
//        }
    }

    protected void btnClick(View v){
//        Définition de l'intent vers LyricsActivity contenant l'artiste et le titre et démarrage de l'activité :
        Intent lyricsActivity = new Intent(this, LyricsActivity.class);
        lyricsActivity.putExtra("artist", inputArtist.getText().toString());
        lyricsActivity.putExtra("title", inputTitle.getText().toString());
        startActivity(lyricsActivity);
    }
}
