package org.iutrt.lyricsfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LyricsActivity extends AppCompatActivity {

    private String artist;
    private String title;
    private String[] song = new String[2];
    private TextView textInfo;
    private TextView textLyrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

//        Récupération des views
        textInfo = findViewById(R.id.lyrics_textInfo);
        textLyrics = findViewById(R.id.lyrics_textLyrics);

//        Récupération de l'intent appelé dans MainActivity, de l'artiste et du titre :
        Intent mainIntent = getIntent();
        artist = mainIntent.getStringExtra("artist");
        title = mainIntent.getStringExtra("title");
        song[0] = artist;
        song[1] = title;

//        Affichage de l'artiste et du titre dans l'activité :
        textInfo.setText(artist + " - " + title);

//        Récupération des lyrics auprès de l'API lyrics.ovh via une tâche asynchrone :
        Request req = new Request();
        req.execute(song);

    }

    private class Request extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... song) {
            String response = request(song[0], song[1]);
            return response;
        }

        protected String request(String artist, String title) {
            String response = "";

            try {
                HttpURLConnection connection = null;
                URL url = new
                        URL("https://api.lyrics.ovh/v1/" + URLEncoder.encode(artist, "UTF-8") + "/" + URLEncoder.encode(title, "UTF-8"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String ligne = bufferedReader.readLine();
                while (ligne != null) {
                    response += ligne;
                    ligne = bufferedReader.readLine();
                }
                JSONObject lyricsJSON = new JSONObject(response);
                response = getLyricsFromJSON(lyricsJSON);
            } catch (IOException e) {
                response = "problème de connexion ";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        private String getLyricsFromJSON(JSONObject jso) throws Exception {
            String response = "";
            response = jso.getString("lyrics");
            return response;
        }

        protected void onPostExecute(String result){
            textLyrics.setText(result);
        }

    }

}
