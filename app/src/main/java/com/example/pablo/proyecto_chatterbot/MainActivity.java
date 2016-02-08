package com.example.pablo.proyecto_chatterbot;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private ArrayList<String> text;
    private TextView tv;
    private int RESULT_SPEECH = 0;
    private TextToSpeech tts;
    private String texto;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        iv=(ImageView)findViewById(R.id.imageView);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Escuchar();
                //Debido a que mi dispositivo falla a la hora de usar el intent de stt he tenido que hacer las pruebas a base
                //de escribir en el chat.

/*              String frase = et.getText().toString();
                tv.append("\n" + frase);
                et.setText("");
                Tarea t = new Tarea();
                t.execute(frase);*/
            }
        });
    }

    //Intent que reconoce la voz
    public void Escuchar() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //Debido a que el español no esta como lenguaje, he dejado la voz con entonacion inglesa
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        i.putExtra(RecognizerIntent.
                        EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                10000);
        try {
            startActivityForResult(i, RESULT_SPEECH);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    R.string.sinstt,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SPEECH) { //Si estamos hablando nosotros
            if (resultCode == RESULT_OK && null != data) {
                //Lo que reconozca por voz se guarda en un array de posibles palabras, siendo la primera
                // por lo general la más acertada, asi que es la que utilizamos
                text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                tv.append("\n" + text.get(0));
                Tarea t = new Tarea();
                t.execute(text.get(0));
            }
        } else { //Si el que habla es el bot
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            //Aunque no funcione (o almenos en mi movil), segun me he documentado esto
                            //deberia valer para que tuviese entonacion española
                            tts.setLanguage(new Locale("es", "ES"));
                            tts.setPitch(0); //tono
                            tts.setSpeechRate(0); //velocidad
                            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            ArrayList<String> textos = data.getStringArrayListExtra(
                                    RecognizerIntent.EXTRA_RESULTS);
                            for (int i = 0; i < textos.size(); i++) {
                                tv.setText(textos.get(i));
                            }
                        }
                    }
                });
            }
        }
    }

    public class Tarea extends AsyncTask<String, Integer, String> {
        private int CTE=1;
            @Override
            protected String doInBackground(String... params) {
                //Conectamos con el bot
                ChatterBotFactory factory = new ChatterBotFactory();
                ChatterBot bot1 = null;
                try {
                    bot1 = factory.create(ChatterBotType.CLEVERBOT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Creamos sesion con el bot
                ChatterBotSession bot1session = bot1.createSession();
                String s = params[0] + "";
                String respuesta = "";
                try {
                    respuesta = bot1session.think(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return respuesta;
            }

            @Override
            protected void onPostExecute(String respuesta) {
                tv.append("\n" + respuesta);
                texto = respuesta;
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(intent, CTE);
            }
        }
    }

