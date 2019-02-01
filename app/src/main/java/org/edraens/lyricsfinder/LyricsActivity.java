package org.edraens.lyricsfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LyricsActivity extends AppCompatActivity {

    private String[] song = new String[2];
    private TextView textLyrics;
    private ProgressBar progress;
    private boolean inFavs;

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
        setTitle(title+" - "+artist);

//        Modification de la taille des paroles en fonction des paramètres :
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
        String size = prefs.getString("fontSize", "14");
        textLyrics.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(size));



//        La chanson est-elle en favoris ?
        int index = isInFavorites();
        if (index >= 0) inFavs = true;
        else inFavs = false;

//        Récupération des lyrics auprès de l'API lyrics.ovh OU depuis un favori sauvegardé :
        if (inFavs){
            try {
                JSONArray lyricsArray = new JSONArray(prefs.getString("fav_lyrics", ""));
                progress.setVisibility(View.GONE);
                textLyrics.setText(lyricsArray.get(index).toString());
                Log.d("Lyrics", "recuperes depuis fav");
            } catch (JSONException e) {
                Log.d("Error", "Fav error");
            }
        }
        else {
            Request req = new Request();
            req.execute(song);
        }

    }

//    Méthode permettant l'ajout de la chanson actuellement affichée à l'historique
    @SuppressWarnings("Duplicates")
    private void appendHistory() {
        JSONArray history_artists;
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
//            Si des titres sont déjà dans l'historique on crée un JSONArray à partir de ce qui existe déjà, sinon on en fait un nouveau
            String raw = prefs.getString("history_artists", "");
            if (raw.equals("")) history_artists = new JSONArray();
            else history_artists = new JSONArray(raw);
            history_artists.put(song[0]);
            prefs.edit().putString("history_artists", history_artists.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("History error", "Cannot parse history");
        }
//        De même pour le titre
        JSONArray history_titles;
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
            String raw = prefs.getString("history_titles", "");
            if (raw.equals("")) history_titles = new JSONArray();
            else history_titles = new JSONArray(raw);
            history_titles.put(song[1]);
            prefs.edit().putString("history_titles", history_titles.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("History error", "Cannot parse history");
        }
    }

//    Tâche async permettant de récupérer les paroles de la chanson demandée
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
//                        On retire les quote de l'URL sinon l'API ne renvoie rien
                        URL("https://api.lyrics.ovh/v1/" + URLEncoder.encode(artist.replace("'", " "), "UTF-8") + "/" + URLEncoder.encode(title.replace("'", " "), "UTF-8"));
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
            }
//            En cas d'erreur, le code d'erreur est spécifié à la place des paroles
            catch (FileNotFoundException e) {
                response = new StringBuilder("404");
            } catch (IOException e) {
                response = new StringBuilder("noInternet");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response.toString();
        }

//        Les paroles sont contenues dans le string "lyrics" renvoyé par l'API. Extraction :
        private String getLyricsFromJSON(JSONObject jso) throws Exception {
            String response;
            response = jso.getString("lyrics");
            return response;
        }

        protected void onPostExecute(String result) {
//            Récupération d'éventuels codes d'erreurs, et affichage de notifications en conséquence pour prévenir l'utilisateur (paroles non trouvées, pas de connexion..)
            if (result.equals("404")) {
                Toast.makeText(LyricsActivity.this, R.string.lyrics_404, Toast.LENGTH_SHORT).show();
                LyricsActivity.this.finish();
            } else if (result.equals("noInternet")) {
                Toast.makeText(LyricsActivity.this, R.string.internet_issue, Toast.LENGTH_SHORT).show();
                LyricsActivity.this.finish();
            } else {
                progress.setVisibility(View.GONE);
                result = result.replaceAll("\\n\\n\\n+", "\n\n");
                textLyrics.setText(result);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
//                Si l'historique est activé et que la chanson n'est PAS en favoris, on voit pour ajouter le titre à l'historique:
                if (prefs.getBoolean("historyEnabled", true) && !inFavs) {
//                Si le titre est déjà la dernière entrée dans l'historique, on ajoute pas (pour éviter les doublons)
                    String rawArtists = prefs.getString("history_artists", "");
                    String rawTitles = prefs.getString("history_titles", "");
//                    Si l'historique est vide, on ajoute
                    if (rawArtists.equals("") || rawTitles.equals("")) {
                        appendHistory();
                    } else {
//                        Sinon, on vérifie si la chanson n'est pas déjà en dernier dans l'historique
                        try {
//                        Récupération de l'historique
                            JSONArray history_artists = new JSONArray(rawArtists);
                            JSONArray history_titles = new JSONArray(rawTitles);
//                        Si le dernier titre de l'historique n'est pas celui qu'on voit actuellement et n'est pas en favoris, alors on l'ajoute. Sinon on fait rien.
                            if (!(history_artists.getString(history_artists.length() - 1).equalsIgnoreCase(song[0]) && history_titles.getString(history_titles.length() - 1).equalsIgnoreCase(song[1]))) {
//                                Toutes les conditions sont réunies, on déclenche l'ajout
                                appendHistory();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON", "JSON parse error");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

//    Affichage du bouton d'ajout aux favoris si la chanson n'y est pas, ou inversement
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem add = menu.findItem(R.id.menulyrics_add);
        MenuItem del = menu.findItem(R.id.menulyrics_del);
        if (inFavs) {
            del.setVisible(true);
        } else {
            add.setVisible(true);
        }
        return true;
    }

//    Création du menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_lyrics, menu);
        return true;
    }

//    Gestion d'un clic sur un item du menu
    @SuppressWarnings("Duplicates")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menulyrics_add:
//                Ajout de l'artiste
                JSONArray fav_artists;
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
                    String raw = prefs.getString("fav_artists", "");
//                    Si il n'y a pas de favoris, on créé un nouvel JSONArray, sinon on le créé à partir de ceux existants
                    if (raw.equals("")) fav_artists = new JSONArray();
                    else fav_artists = new JSONArray(raw);
//                    Ajout de l'artiste au JSONArray
                    fav_artists.put(song[0]);
//                    Conversion du JSONArray en string pour le stocker dans les SharedPreferences
                    prefs.edit().putString("fav_artists", fav_artists.toString()).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Fav error", "Cannot parse favs");
                }
//                Ajout du titre (même processus que ci-dessus)
                JSONArray fav_titles;
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
                    String raw = prefs.getString("fav_titles", "");
                    if (raw.equals("")) fav_titles = new JSONArray();
                    else fav_titles = new JSONArray(raw);
                    fav_titles.put(song[1]);
                    prefs.edit().putString("fav_titles", fav_titles.toString()).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Fav error", "Cannot parse favs");
                }
//                Ajout des paroles (de même)
                JSONArray fav_lyrics;
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
                    String raw = prefs.getString("fav_lyrics", "");
                    if (raw.equals("")) fav_lyrics = new JSONArray();
                    else fav_lyrics = new JSONArray(raw);
                    fav_lyrics.put(textLyrics.getText().toString());
                    prefs.edit().putString("fav_lyrics", fav_lyrics.toString()).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Fav error", "Cannot parse favs");
                }
//                Recréation de l'activité dans le but d'actualiser le bouton du menu
                recreate();
                Toast.makeText(this, R.string.favs_add_notification, Toast.LENGTH_SHORT).show();
                return (true);

            case R.id.menulyrics_del:
//               Récupération de l'id de la chanson dans les array de favoris
                int index = isInFavorites();

//                Suppression de l'artiste à partir de l'index récupéré plus haut
                JSONArray fav_artists_del;
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
                    String raw = prefs.getString("fav_artists", "");
                    fav_artists_del = new JSONArray(raw);
                    fav_artists_del.remove(index);
                    prefs.edit().putString("fav_artists", fav_artists_del.toString()).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Fav error", "Cannot parse favs");
                }
//                Suppression du titre (de même)
                JSONArray fav_titles_del;
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
                    String raw = prefs.getString("fav_titles", "");
                    fav_titles_del = new JSONArray(raw);
                    fav_titles_del.remove(index);
                    prefs.edit().putString("fav_titles", fav_titles_del.toString()).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Fav error", "Cannot parse favs");
                }
//                Suppression des paroles (de même)
                JSONArray fav_lyrics_del;
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
                    String raw = prefs.getString("fav_lyrics", "");
                    fav_lyrics_del = new JSONArray(raw);
                    fav_lyrics_del.remove(index);
                    prefs.edit().putString("fav_lyrics", fav_lyrics_del.toString()).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Fav error", "Cannot parse favs");
                }
//                Recréation de l'activité pour actualiser le menu
                recreate();
                Toast.makeText(this, R.string.favs_del_notification, Toast.LENGTH_SHORT).show();
                return (true);
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

//    Méthode permettant de récupérer l'index de la chanson dans les tableaux de favoris. Renvoie -1 si la chanson n'est pas en favoris
    @SuppressWarnings("Duplicates")
    private int isInFavorites() {
        int index;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LyricsActivity.this);
        String rawArtists = prefs.getString("fav_artists", "[]");
        String rawTitles = prefs.getString("fav_titles", "[]");
        if (rawArtists.equals("[]") || rawTitles.equals("[]")) {
            index = -1;
        } else {
            try {
//                        Récupération des favoris
                JSONArray fav_artists = new JSONArray(rawArtists);
                JSONArray fav_titles = new JSONArray(rawTitles);
                index = -1;
//                On parcourt le tableau des artistes
                for (int i = 0; i < fav_artists.length(); i++) {
//                    Si l'artiste de la chanson actuelle correspond à celui de l'index du tableau artistes des favoris, on vérifie si ce même index correspond au titre dans le tableau des titres
                    if (fav_artists.get(i).toString().equalsIgnoreCase(song[0])) {
                        if (fav_titles.get(i).toString().equalsIgnoreCase(song[1])){
//                            Si l'artiste et le titre de la chanson sont présents tous les deux au même index des tableaux de favoris, alors la chanson actuelle est en favoris. On retourne donc l'index.
                            index = i;
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("JSON", "JSON parse error");
                e.printStackTrace();
                index = -1;
            }
        }
        return index;
    }

}
