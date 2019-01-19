package org.edraens.lyricsfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LyricsActivity extends AppCompatActivity {

    private String[] song = new String[2];
    private TextView textLyrics;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

//        Récupération des views
        textLyrics = findViewById(R.id.lyrics_viewLyrics);
        progress = findViewById(R.id.lyrics_progress);

//        Ajout du bouton "back" dans l'ActionBar :
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);



//        Récupération de l'intent appelé dans MainActivity, de l'artiste et du titre :
        Intent mainIntent = getIntent();
        String artist = mainIntent.getStringExtra("artist");
        String title = mainIntent.getStringExtra("title");
        song[0] = artist;
        song[1] = title;

//        Affichage de l'artiste et du titre dans l'activité :
        setTitle(title + " - " + artist);

//        Modification de la taille des paroles en fonction des paramètres :
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String size = prefs.getString("fontSize", "Medium");
        textLyrics.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(size));


//        Récupération des lyrics auprès de l'API lyrics.ovh via une tâche asynchrone :
        Request req = new Request();
        req.execute(song);

    }

    @SuppressWarnings("Duplicates")
    private class Request extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... song) {
            return request(song[0], song[1]);
        }

        String request(String artist, String title) {
            StringBuilder response = new StringBuilder();

            try {
                HttpURLConnection connection;
                URL url = new
                        URL("https://api.lyrics.ovh/v1/" + URLEncoder.encode(artist, "UTF-8") + "/" + URLEncoder.encode(title, "UTF-8"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String ligne = bufferedReader.readLine();
                while (ligne != null) {
                    response.append(ligne);
                    ligne = bufferedReader.readLine();
                }
                JSONObject lyricsJSON = new JSONObject(response.toString());
                response = new StringBuilder(getLyricsFromJSON(lyricsJSON));
            } catch (FileNotFoundException e){
                response = new StringBuilder("404");
            }
            catch (IOException e) {
                response = new StringBuilder("noInternet");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return response.toString();
        }

        private String getLyricsFromJSON(JSONObject jso) throws Exception {
            String response;
            response = jso.getString("lyrics");
            return response;
        }

        protected void onPostExecute(String result){
            if (result.equals("404")) {
                Toast.makeText(LyricsActivity.this, R.string.lyrics_404, Toast.LENGTH_SHORT).show();
                LyricsActivity.this.finish();
            }
            else if (result.equals("noInternet")){
                Toast.makeText(LyricsActivity.this, R.string.internet_issue, Toast.LENGTH_SHORT).show();
                LyricsActivity.this.finish();
            }
            else {
                progress.setVisibility(View.GONE);
                result = result.replaceAll("\\n\\n\\n+", "\n\n");
                textLyrics.setText(result);
            }
        }

    }

}
