package org.izv.pgc.chatbot;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.izv.pgc.chatbot.apibot.ChatterBot;
import org.izv.pgc.chatbot.apibot.ChatterBotFactory;
import org.izv.pgc.chatbot.apibot.ChatterBotSession;
import org.izv.pgc.chatbot.apibot.ChatterBotType;
import org.izv.pgc.chatbot.apibot.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private EditText editText;
    private String pregunta;
    private String respuestaBING;
    private String src;
    private volatile boolean chat = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);

        //src = "https://www.bing.com/ttranslatev3?isVertical=1&&IG=C7C2278972724E04852E45BF3FA519D1&IID=translator.5026.2";
        src = "https://www.bing.com/ttranslatev3?";

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pregunta = editText.getText().toString();
                textView.setText(textView.getText().toString() + "\nYo>" + pregunta);
                editText.setText("");
                Log.v("nose", "yo> " + pregunta);

                //post para traducir la pregunta
                new Traducir("es","en",pregunta).execute();

                Log.v("nose", "");
                //new Chat().execute();

                /*chat = !chat;
                if(chat) {
                    new Chat().execute();
                }*/
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void chat(String mensaje){
        try{
            ChatterBotFactory factory = new ChatterBotFactory();

            //ChatterBot bot1 = factory.create(ChatterBotType.CLEVERBOT);
            //ChatterBotSession bot1session = bot1.createSession();

            //String respuesta = bot1session.think(pregunta);
            //Log.v("chatbot", "bot1> " + respuesta);
            //textView.setText(textView.getText().toString() + "\n" + respuesta);

            ChatterBot bot2 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
            ChatterBotSession bot2session = bot2.createSession();

            String respuesta = bot2session.think(mensaje);
            Log.v("nose", "bot> " + respuesta);
            new Traducir("en", "es", respuesta).execute();

            //textView.setText(textView.getText().toString() + "\nBot>" + respuesta);

            //String s = "Hello";

            /*while (chat) {

               System.out.println("bot1> " + s);
                textView.setText(textView.getText()+"\nbot1>"+s);
                s = bot2session.think(s);

                System.out.println("bot2> " + s);
                textView.setText(textView.getText()+"\nbot2>"+s);
                s = bot1session.think(s);
            }*/

        }catch (Exception e){

        }
    }

    private class Chat extends AsyncTask <Void,Void,Void> {
        String traducido;
        public Chat(String mensaje){
            traducido = mensaje;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            chat(traducido);
            return null;
        }
    }

    private class Traducir extends AsyncTask <Void,Void,Void>{
        HashMap<String, String> httpBodyParams;
        private String mensaje, parametros;
        Boolean persona;

        public Traducir (String idiomaTraducir, String idiomaTraducido, String textoTraducir){
            if(idiomaTraducir.equals("es")){
                persona = true;
            }else {
                persona = false;
            }
            httpBodyParams = new HashMap<>();
            httpBodyParams.put("fromLang", idiomaTraducir);
            httpBodyParams.put("to", idiomaTraducido);
            httpBodyParams.put("text", textoTraducir);

            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : httpBodyParams.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");
                try {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                result.append("=");
                try {
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            parametros = result.toString();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                mensaje = Utils.postHttp("https://www.bing.com/ttranslatev3?", parametros);
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("xyz", "Error: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mensaje = decomposeJson(mensaje);
            Log.v("nose", "mensaje> " + mensaje);
            if(persona == true){
                new Chat(mensaje).execute();
            }else{
                textView.setText(textView.getText().toString() + "\nBot>" + mensaje);
            }

        }
    }

    // https://www.bing.com/ttranslatev3?isVertical=1&&IG=C7C2278972724E04852E45BF3FA519D1&IID=translator.5026.2
    // POST
    // fromLang=es
    // text=soy programador
    // to=en

    public String decomposeJson(String json){
        String translationResult = "Could not get";
        try {
            JSONArray arr = new JSONArray(json);
            JSONObject jObj = arr.getJSONObject(0);
            translationResult = jObj.getString("translations");
            JSONArray arr2 = new JSONArray(translationResult);
            JSONObject jObj2 = arr2.getJSONObject(0);
            translationResult = jObj2.getString("text");
        } catch (JSONException e) {
            translationResult = e.getLocalizedMessage();
        }
        return translationResult;
    }
}
