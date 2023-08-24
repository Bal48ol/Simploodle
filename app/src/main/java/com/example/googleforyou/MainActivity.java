package com.example.googleforyou;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.yandex.mobile.ads.banner.AdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.InitializationListener;
import com.yandex.mobile.ads.common.MobileAds;

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
    private RadioButton siteRadioButton;
    private RadioButton imageRadioButton;
    private RadioButton videoRadioButton;
    private RadioButton mapsRadioButton;
    private RadioButton newsRadioButton;
    private BannerAdView mBannerAdView;
    private static final String YANDEX_MOBILE_ADS_TAG = "YandexMobileAds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        searchButton = findViewById(R.id.searchButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        siteRadioButton = findViewById(R.id.siteRadioButton);
        imageRadioButton = findViewById(R.id.imageRadioButton);
        videoRadioButton = findViewById(R.id.videoRadioButton);
        mapsRadioButton = findViewById(R.id.mapsRadioButton);
        newsRadioButton = findViewById(R.id.newsRadioButton);

        siteRadioButton.setChecked(true);

        MobileAds.initialize(this, new InitializationListener() {
            @Override
            public void onInitializationCompleted() {
                Log.d(YANDEX_MOBILE_ADS_TAG, "SDK initialized");
            }
        });

        // Создание экземпляра mAdView.
        mBannerAdView = (BannerAdView) findViewById(R.id.banner_ad_view);
        String AdUnitId = "demo-banner-yandex"; // R-M-2733347-1
        mBannerAdView.setAdUnitId(AdUnitId);
        mBannerAdView.setAdSize(AdSize.BANNER_320x50);

        // Создание объекта таргетирования рекламы.
        AdRequest adRequest = new AdRequest.Builder().build();

        mBannerAdView.loadAd(adRequest);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String doodle = searchEditText.getText().toString().toLowerCase();
                String searchUrl1 = "https://search.yahoo.com/search?p=" + doodle;

                if(doodle.isEmpty()){
                    Toast.makeText(MainActivity.this, "Пожалуйста, введите что-нибудь :(", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(doodle.equals("порно") || doodle.equals("порн") || doodle.equals("порнография") || doodle.equals("секс") || doodle.equals("прон")
                        || doodle.equals("цыпочки") || doodle.equals("горячие девочки") || doodle.equals("сучки") || doodle.equals("анал") || doodle.equals("орал")
                        || doodle.equals("сиськи") || doodle.equals("чайлдпрон") || doodle.equals("чайлдпорн") || doodle.equals("porn") || doodle.equals("hot chicks")
                        || doodle.equals("hot girl") || doodle.equals("sexual") || doodle.equals("anal") || doodle.equals("хуй") || doodle.equals("пизда")
                        || doodle.equals("джигурда") || doodle.equals("большие сиськи") || doodle.equals("большие белые сиськи") || doodle.equals("большие черные сиськи")){
                    Toast.makeText(MainActivity.this, "Прости, котик-помощник не может помочь тебе с этим :(", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(imageRadioButton.isChecked()){
                    String imageUrl = "https://yandex.ru/images/search?text=" + doodle + "&lr=14";
                    openUrl(imageUrl);
                }
                else if(videoRadioButton.isChecked()){
                    String videoUrl = "https://www.youtube.com/results?search_query=" + doodle;
                    openUrl(videoUrl);
                }
                else if(mapsRadioButton.isChecked()) {
                    //firstLinkUrl = "https://yandex.ru/maps/14/city/search/" + doodle.replace(" ", "%20");
                    String mapsUrl = "https://2gis.ru/city/search/" + doodle.replace(" ", "%20");
                    openUrl(mapsUrl);
                }
                else if(newsRadioButton.isChecked()){
                    String newsUrl = "https://lenta.ru/search?query=" + doodle;
                    openUrl(newsUrl);
                }
                else if (siteRadioButton.isChecked()) {
                    String searchUrl = "https://www.google.com/search?q=" + doodle;

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
                                String firstLinkUrl = null;
                                if (htmlContent != null ) {
                                    ArrayList<String> links = extractLinks(htmlContent);
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
                                                if (skippedCount <= 3) {
                                                    continue;
                                                } else {
                                                    Log.d("Open link", "Open link: " + link);
                                                    openUrl(link);
                                                    break;
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
                animateClick(searchButton);
            }
        });
        showInstructionDialog();
    }

    private void openUrl(String url) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

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
                siteRadioButton.setChecked(true);
                imageRadioButton.setChecked(false);
                videoRadioButton.setChecked(false);
                mapsRadioButton.setChecked(false);
                newsRadioButton.setChecked(false);
            }
        }, 2500);
    }

    private void showInstructionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_instruction, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button skipButton = dialogView.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button rustoreButton = dialogView.findViewById(R.id.rustoreButton);
        rustoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://apps.rustore.ru/app/com.example.googleforyou";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        dialog.show();
    }
}