package com.example.googleforyou;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateClick(searchButton);
                String doodle = searchEditText.getText().toString();
                String searchUrl = "https://www.google.com/search?q=" + doodle; // search.yahoo.com
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<String> future = executor.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        try {
                            URL url = new URL(searchUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line);
                            }
                            reader.close();
                            return stringBuilder.toString();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                });

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(searchUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line);
                            }
                            reader.close();
                            String htmlContent = stringBuilder.toString();
                            if (htmlContent != null) {
                                ArrayList<String> links = extractLinks(htmlContent);
                                String firstLinkUrl = null;
                                for (String link : links) {
                                    Log.d("Links", "link: " + link);
                                    assert doodle != null;

                                    char[] alphabet = new char[26]; // Создаем массив длиной 26 (количество букв в английском алфавите)
                                    for (int i = 0; i < 26; i++) {
                                        alphabet[i] = (char) ('a' + i); // Заполняем массив буквами английского алфавита
                                    }

                                    if (link.startsWith("https://" + doodle.toLowerCase()) || link.startsWith("https://www." + doodle.toLowerCase())
                                            || link.startsWith("http://" + doodle.toLowerCase()) || link.startsWith("http://www." + doodle.toLowerCase())) {
                                        Log.d("Service", "Service: " + doodle);
                                        firstLinkUrl = link;
                                        break;
                                    }
                                    else {
                                        for (char letter : alphabet) {
                                            if (link.startsWith("https://" + letter + ".www." + doodle.toLowerCase()) || link.startsWith("https://" + letter + "." + doodle.toLowerCase())) {
                                                Log.d("Service", "Service + char: " + doodle);
                                                firstLinkUrl = link;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (firstLinkUrl != null) {
                                    openUrl(firstLinkUrl);
                                }
                                else {
                                    int skippedCount = 0; // Счетчик пропущенных непустых ссылок
                                    for (int i = 0; i < links.size(); i++) {
                                        String link = links.get(i);
                                        if (!link.isEmpty()) {
                                            skippedCount++;
                                            if (skippedCount <= 5) {
                                                continue; // Пропускаем первую и вторую непустую ссылку
                                            } else {
                                                Log.d("Link арбуз", "Link арбуз: " + link);
                                                openUrl(link);
                                                break; // Прерываем цикл после открытия третьей непустой ссылки
                                            }
                                        }
                                    }


                                }

                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            String poiskUrl = "https://yandex.ru/search/?text=" + doodle;
                            openUrl(poiskUrl);
                        }
                    }
                });
            }
        });
    }

    private void openUrl(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private ArrayList<String> extractLinks(String htmlContent) {
        ArrayList<String> links = new ArrayList<>();
        Document doc = Jsoup.parse(htmlContent);
        Elements linkElements = doc.select("a[href]");
        for (Element linkElement : linkElements) {
            String linkUrl = linkElement.absUrl("href");
            links.add(linkUrl);
        }
        return links;
    }

    private void animateClick(View view) {
        Animation animation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(100);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(animation);

        loadingProgressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingProgressBar.setVisibility(View.GONE);
                searchEditText.setText("");
            }
        }, 2500);
    }
}