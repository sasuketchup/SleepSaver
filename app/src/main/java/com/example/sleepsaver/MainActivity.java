package com.example.sleepsaver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.net.sip.SipSession;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.database.DatabaseUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.sleepsaver.FileSelectionDialog.OnFileSelectListener;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnFileSelectListener {

    // 年月日時刻を扱う変数
    int year = 0;
    int month = 0;
    int date = 0;
    int hour = 0;
    int minute = 0;

    // 新しいAPIレベル用のオリジナルタイムピッカー
    TimePicker originalTimePicker;

    // ピッカーの時刻を変更したときに変数に代入するようにするために、TimePickerDialogを継承したクラス
    public class CustomTimePickerDialog extends TimePickerDialog {

        public CustomTimePickerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
            super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker view, int s_hour, int s_minute) {
            hour = s_hour;
            minute = s_minute;
        }
    }

    Calendar calendar;
    Calendar cal_now;
    Calendar cal_spec;
    Calendar cal_spec1;
    Calendar cal_spec2;

    LinearLayout varRecordLay;

    LinearLayout varDateLay;
    LinearLayout varGULay;
    LinearLayout varGTBLay;
    LinearLayout varSTLay;

    // スクロールの移動距離の合計
    int sum_diffY = 0;
    // 強調するデータの位置
    int data_position = 0;

    TimeHandler timeHandler = new TimeHandler();
    PrefActivity prefActivity = new PrefActivity();

    // 音声認識のリクエストコード
    private static final int REQUEST_CODE = 1000;

    // ファイルメニューIDの定数(CSV入力用)
    private static final int MENUID_FILE = 0;
    // メンバー変数(初期フォルダ)
    private String m_strInitialDir = Environment.getExternalStorageDirectory().getPath();
    // ファイル選択ダイアログ
    FileSelectionDialog selectionDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // timeHandler = (TimeHandler) this.getApplication();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                return true;
//            }
//        });

        SharedPreferences sp = getSharedPreferences("pref", MODE_PRIVATE);
        // 表示範囲を取得
        int results = sp.getInt("results", 0);
        // 起床→就寝切り替え時刻を取得
        int stay_up_line = sp.getInt("stay_up_line", 1200);
        int hour_line = timeHandler.number_to_time(stay_up_line)[0];

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        // データの行数を取得
        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");

        // データが1行以上あるとき実行
        if(idCount > 0) {
            // 記録し忘れがある場合、差分を埋める
            // 更新されない現象があるため3回繰り返す
            for (int i=0; i<3; i++) {
                timeHandler.fillForget(db, MainActivity.this);
            }
        }else{
            // データが空のとき実行
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(this));

            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            date = calendar.get(Calendar.DATE);

            timeHandler.insertTime(db, 0, year, month, date, -1, -1, -1, -1);
        }

        // データの行数を再取得
        idCount = DatabaseUtils.queryNumEntries(db, getString(R.string.date_table));

        // データと表示件数の差分
        int diff_id = 0;

        // 指定日1～2の時、表示範囲を決めるための変数
        int diff_now_spec1 = (int) (idCount - 1);
        int diff_now_spec2 = 0;

        // 指定日1～指定日2(resultsが-2)の場合
        if (results == -2) {
            // 今日と指定日1、2の差日数を計算
            diff_now_spec1 = timeHandler.spec12_today(db, MainActivity.this, false)[0];
            diff_now_spec2 = timeHandler.spec12_today(db, MainActivity.this, false)[1];
        }

        // 指定日～今日(resultsが-1)の場合に指定日と今日の差分を計算
        if (results == -1) {
            // 指定日と今日の差を計算し、表示件数に代入
            results = timeHandler.spec_today(db, MainActivity.this, false);
        }

        // 表示件数を計算
        if (results > 0) {
            if (idCount > results) {
                diff_id = (int) (idCount - results);
                idCount = results;
            }
        }

        varRecordLay = (LinearLayout) findViewById(R.id.RecordLayout);

        varDateLay = (LinearLayout) findViewById(R.id.DateLayout);
        varGULay = (LinearLayout) findViewById(R.id.GULayout);
        varGTBLay = (LinearLayout) findViewById(R.id.GTBLayout);
        varSTLay = (LinearLayout) findViewById(R.id.STLayout);

        Cursor cursor = db.query("DateTable", new String[] {"id", "year", "month", "date"}, null, null, null, null, null);
        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

//        long idCount = DatabaseUtils.queryNumEntries(db, "DateTable");
        long idGU = DatabaseUtils.queryNumEntries(db, "GetUpTable");
        long idGTB = DatabaseUtils.queryNumEntries(db, "GoToBedTable");

//        LinearLayout[] timeLayout = new LinearLayout[(int) idCount];

        TextView[] textDate = new TextView[(int) idCount];
        final TextView[] textGU = new TextView[(int) idCount];
        final TextView[] textGTB = new TextView[(int) idCount];
        TextView[] textST = new TextView[(int) idCount - 1];
        // 月の変わるタイミングを調べるためにmonthのみ配列
        int[] month = new int[(int) idCount];

        // カーソルを最新の記録へ
        cursor.moveToLast();
        cursor1.moveToLast();
        cursor2.moveToLast();

        // ボタン無効化のために最新の記録取得
        int latestGU = cursor1.getInt(1);
        int latestGTB = cursor2.getInt(1);

        // 記録を表示
        for(int i=0;i<idCount;i++){
//            timeLayout[i] = new LinearLayout(this);
            textDate[i] = new TextView(this);
            textGU[i] = new TextView(this);
            textGTB[i] = new TextView(this);

            // 曜日を準備
            String[] week_name = {"<font color=\"red\">日</font>", "月", "火", "水", "木", "金", "<font color=\"blue\">土</font>"};

            int year = cursor.getInt(1);
            month[i] = cursor.getInt(2);
            int date = cursor.getInt(3);

            // 日付をセットして曜日を取得
            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(year, month[i] - 1, date);
            int week = calendar1.get(Calendar.DAY_OF_WEEK) - 1;

            String timeGUSt = "--:--";
            String timeGTBSt = "--:--";
            String timeSTSt = "--:--";

            int hourGU = -1;
            int minuteGU = -1;
            if(i < idGU){
                hourGU = cursor1.getInt(1);
                minuteGU = cursor1.getInt(2);

                timeGUSt = timeHandler.timeString(hourGU, minuteGU);
            }

            if(i < idGTB) {
                int hourGTB = cursor2.getInt(1);
                int minuteGTB = cursor2.getInt(2);

                timeGTBSt = timeHandler.timeString(hourGTB, minuteGTB);
            }

            // 表示されるうち最新の記録と月が変わったときのみ年月を表示
            if (i == diff_now_spec2) {
                textDate[i].setText(Html.fromHtml(year + "年" + month[i] + "月<br><big>" + date + "日(" + week_name[week] + ")</big>"));
            } else {
                textDate[i].setText(Html.fromHtml("<big>" + date + "日(" + week_name[week] + ")</big>"));
            }
            if (i > 0) {
                if (month[i] != month[i - 1]) {
                    textDate[i].setText(Html.fromHtml(year + "年" + month[i] + "月<br><big>" + date + "日(" + week_name[week] + ")</big>"));
                }
            }

            textGU[i].setText(timeGUSt);
            textGTB[i].setText(timeGTBSt);
//            textDate[i].setWidth(convertDp2Px(80));

            textDate[i].setHeight(150);
            textGU[i].setHeight(150);
            textGTB[i].setHeight(150);

//            textGU[i].setWidth(convertDp2Px(100));
//            textGTB[i].setWidth(convertDp2Px(100));
//            textDate[i].setGravity(Gravity.TOP);
            textGU[i].setGravity(Gravity.RIGHT);
            textGTB[i].setGravity(Gravity.RIGHT);
            textGU[i].setTextSize(30);
            textGTB[i].setTextSize(30);
            cursor.moveToPrevious();
            cursor1.moveToPrevious();
            cursor2.moveToPrevious();

            if (i < idCount - 1) {
                textST[i] = new TextView(this);

                // 睡眠時間計算のため、一つ前の就寝時刻を取得
                int hourGTBPrevious = cursor2.getInt(1);
                int minuteGTBPrevious = cursor2.getInt(2);

                // 起床・就寝の値が揃っているとき
                if (hourGTBPrevious != -1 && hourGU != -1) {
                    int dateST = 0;
                    // 日付を跨いだ場合(起床→就寝切り替え時刻より遅い(かつ24時より前の)場合は24時を跨ぐので引かれる方の日付をプラス1)
                    if (hourGTBPrevious > hour_line) {
                        dateST = 1;
                    }

                    Calendar calST = Calendar.getInstance();
                    calST.set(0, 0, dateST, hourGU, minuteGU);
                    calST.add(Calendar.HOUR, 0 - hourGTBPrevious);
                    calST.add(Calendar.MINUTE, 0 - minuteGTBPrevious);

                    int hourST = calST.get(Calendar.HOUR_OF_DAY);
                    int minuteST = calST.get(Calendar.MINUTE);

                    timeSTSt = timeHandler.timeString(hourST, minuteST);
                }
                textST[i].setText(timeSTSt);
                textST[i].setTextSize(30);
                textST[i].setHeight(150);
                textST[i].setGravity(Gravity.RIGHT);

                if (diff_now_spec2 <= i && i <= (diff_now_spec1 - 1)) {
                    varSTLay.addView(textST[i]);
                }
            }

//            timeLayout[i].setOrientation(LinearLayout.HORIZONTAL);
//            timeLayout[i].addView(textDate[i]);
//            timeLayout[i].addView(textGU[i]);
//            timeLayout[i].addView(textGTB[i]);
//            varRecordLay.addView(timeLayout[i], 0);
            if (diff_now_spec2 <= i && i <= diff_now_spec1) {
                varDateLay.addView(textDate[i]);
                varGULay.addView(textGU[i]);
                varGTBLay.addView(textGTB[i]);
            }
        }
        cursor.close();
        cursor1.close();
        cursor2.close();

        // 睡眠時間の表示位置調整のための空のテキストビュー
        TextView emptyST = new TextView(this);
        emptyST.setHeight(75);
        varSTLay.addView(emptyST, 0);

        // 既に最新の記録がある場合記録ボタンを無効化
        if (latestGU != -1) {
            findViewById(R.id.GUbtn).setEnabled(false);
        }
        if (latestGTB != -1) {
            findViewById(R.id.GTBbtn).setEnabled(false);
        }

        data_position += diff_now_spec2;
        textGU[data_position].setTextSize(34);
        textGTB[data_position].setTextSize(34);
        textGU[data_position].setPaintFlags(textGU[data_position].getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textGTB[data_position].setPaintFlags(textGTB[data_position].getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        // スクロールしたときの処理
        final ObservableScrollView obScrollView = (ObservableScrollView) findViewById(R.id.scrollView);
        obScrollView.setOnScrollViewListener(
                new ObservableScrollView.ScrollViewListener() {
                    @Override
                    public void onScrollChanged(int x, int y, int oldX, int oldY) {
                        int diffY = y - oldY;
                        sum_diffY += diffY;
                        Log.d("diff", String.valueOf(diffY));
                        Log.d("sum", String.valueOf(sum_diffY));
                        if (Math.abs(sum_diffY) >= 150) {

                            // 強調する記録箇所の移動個数
                            int move_position = sum_diffY / 150;
                            Log.d("move_position", String.valueOf(move_position));

                            // if (sum_diffY > 0) {
                                data_position += move_position;
                                textGU[data_position - move_position].setTextSize(30);
                                textGTB[data_position - move_position].setTextSize(30);
                                textGU[data_position - move_position].setPaintFlags(textGU[data_position - move_position].getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                                textGTB[data_position - move_position].setPaintFlags(textGTB[data_position - move_position].getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                                sum_diffY = 0;
                                // sum_diffY -= (150 * move_position);
                            // } else {
//                                data_position--;
//                                textGU[data_position + 1].setTextSize(30);
//                                textGTB[data_position + 1].setTextSize(30);
//                                textGU[data_position + 1].setPaintFlags(textGU[data_position + 1].getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
//                                textGTB[data_position + 1].setPaintFlags(textGTB[data_position + 1].getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
//                                sum_diffY += 150;
                            // }
                            textGU[data_position].setTextSize(34);
                            textGTB[data_position].setTextSize(34);
                            textGU[data_position].setPaintFlags(textGU[data_position].getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            textGTB[data_position].setPaintFlags(textGTB[data_position].getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        }
                    }
                }
        );

        // 睡眠データボタンの処理
        findViewById(R.id.data_page).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, DataActivity.class);
                        intent.putExtra("Update", false);
                        startActivity(intent);
                    }
                }
        );

        // グラフボタンの処理
        findViewById(R.id.graph_page).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                        startActivity(intent);
                    }
                }
        );

        // 音声入力ボタンの処理
        findViewById(R.id.voice_input).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 音声認識のIntentインスタンス
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        // 認識する言語を指定(日本語)
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());
                        // 候補数
                        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                        // 案内を表示
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "「おはよう」または「おやすみ」\nと話しかけてください");
                        // インテント発行
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                }
        );

        // デバッグ用
//        findViewById(R.id.debug).setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(MainActivity.this, PopUpActivity.class);
//                        startActivity(intent);
//                    }
//                }
//        );

        // 設定ボタンの処理
        findViewById(R.id.Settings).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, PrefActivity.class);
                        startActivity(intent);
                    }
                }
        );

        // 起床時刻ボタンの処理
        findViewById(R.id.GUbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recordTime(false, db, -1);
                    }
                }
        );

        // 就寝時刻ボタンの処理
        findViewById(R.id.GTBbtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recordTime(true, db, -1);
                    }
                }
        );

        // 記録時刻の修正・削除(長押し)
        for (int i=0; i<idCount; i++) {
            // 起床時刻の修正・削除
            final int finalI = (int) (idCount - i) - 1 + diff_id;
            textGU[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    recordTime(false, db, finalI);
                    return true;
                }
            });

            // 就寝時刻の修正・削除
            final int finalI1 = (int) (idCount - i) - 1 + diff_id;
            textGTB[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    recordTime(true, db, finalI1);
                    return true;
                }
            });
        }
    }

    // 睡眠データ
    public void startDataPage() {
        Intent intent = new Intent(MainActivity.this, DataActivity.class);
        intent.putExtra("Update", false);
        startActivity(intent);
    }
    // グラフ
    public void startGraphPage() {
        Intent intent = new Intent(MainActivity.this, GraphActivity.class);
        startActivity(intent);
    }
    // 音声入力
    public void startVoiceInput() {
        // 音声認識のIntentインスタンス
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // 認識する言語を指定(日本語)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());
        // 候補数
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        // 案内を表示
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "「おはよう」または「おやすみ」\nと話しかけてください");
        // インテント発行
        startActivityForResult(intent, REQUEST_CODE);
    }
    // 設定
    public void startSetting() {
        Intent intent = new Intent(MainActivity.this, PrefActivity.class);
        startActivity(intent);
    }
    // CSV入出力
    public void startCSV() {
        String[] inoutCSVStr = {"期間を指定してCSVファイルを出力", "CSVファイルを指定して読み込み(上書き)"};
        new AlertDialog.Builder(MainActivity.this).setTitle(null).setItems(inoutCSVStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // 出力
                        prefActivity.resultsWhich = prefActivity.setChoices(0);
                        prefActivity.setRange(MainActivity.this, 1);
                        break;
                    case 1: // 入力
                        // ダイアログオブジェクト
                        selectionDialog = new FileSelectionDialog(MainActivity.this, new OnFileSelectListener() {
                            @Override
                            public void onFileSelect(final File file) {
                                // 拡張子を取得
                                int point = file.getPath().lastIndexOf(".");
                                if (point != -1) { // 拡張子がある場合
                                    String extension = file.getPath().substring(point + 1);
                                    // 拡張子が"csv"かどうか判定
                                    if (extension.equals("csv")) { // csvであれば確認ダイアログを表示
                                        // ファイル名を取得
                                        final String fileName = file.getName();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setMessage(fileName + "を読み込んでデータを上書きします。\n現在のデータはすべて消去されます。よろしいですか？");
                                        builder.setPositiveButton(
                                                "OK",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        try {
                                                            // CSVファイルの読み込み
                                                            FileInputStream inputStream = new FileInputStream(file.getPath());
                                                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                                            String line;

                                                            MyOpenHelper helper = new MyOpenHelper(MainActivity.this);
                                                            final SQLiteDatabase db = helper.getWritableDatabase();

                                                            // ここで既存のテーブルの内容をすべて削除
                                                            db.execSQL("delete from DateTable;");
                                                            db.execSQL("delete from GetUpTable;");
                                                            db.execSQL("delete from GoToBedTable;");

                                                            // 新しく保存するためのid
                                                            int id = 0;

                                                            // 反対から挿入するために一旦idをカウント
                                                            while ((line = bufferedReader.readLine()) != null) {
                                                                id++;
                                                            }

                                                            // idをデクリメントしながらテーブルに挿入する
                                                            while ((line = bufferedReader.readLine()) != null) {

                                                                // idをデクリメント
                                                                id--;

                                                                // カンマで区切る
                                                                String[] rowDataStr = line.split(",");
                                                                int[] rowData = new int[rowDataStr.length];
                                                                // キャスト
                                                                for (int i = 0; i < rowDataStr.length; i++) {
                                                                    rowData[i] = Integer.parseInt(rowDataStr[i]);
                                                                }
                                                                // テーブルに挿入
                                                                timeHandler.insertTime(db, id, rowData[1], rowData[2], rowData[3], rowData[4], rowData[5], rowData[6], rowData[7]);
                                                            }
                                                        } catch (FileNotFoundException e) {
                                                            e.printStackTrace();
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }

                                                        // アクティビティをリスタート
                                                        finish();
                                                        overridePendingTransition(0, 0);
                                                        startActivity(getIntent());
                                                        overridePendingTransition(0, 0);
                                                    }
                                                }
                                        );
                                        builder.setNegativeButton(
                                                "戻る",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        selectionDialog.show(new File(m_strInitialDir));
                                                    }
                                                }
                                        );
                                        builder.show();
                                    } else { // csvでなければ警告ダイアログを表示
                                        // timeHandler.showDialog(MainActivity.this, 2, "", "CSVファイルではありません。\nCSVファイルを選択してください。", "戻る");
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setMessage("CSVファイルではありません。\nCSVファイルを選択してください。");
                                        builder.setPositiveButton(
                                                "戻る",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        selectionDialog.show(new File(m_strInitialDir));
                                                    }
                                                }
                                        );
                                        builder.show();
                                    }
                                    // Toast.makeText(MainActivity.this, "File Type : " + extension, Toast.LENGTH_LONG).show();
                                }
                                m_strInitialDir = file.getParent();
                            }
                        });
                        selectionDialog.show(new File(m_strInitialDir));
                        break;
                }
            }
        }).show();
    }

    // ダイアログでファイルが選択されたときに呼び出されるメソッド
    public void onFileSelect(File file) {
        Toast.makeText(this, "File Selected : " + file.getPath(), Toast.LENGTH_LONG).show();
        m_strInitialDir = file.getParent();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        // MainActivity.timeHandler = (TimeHandler) this.getApplicationContext();

        switch (id) {
            case R.id.data_page: // 睡眠データ
                startDataPage();
                break;
            case R.id.graph_page: // グラフ
                startGraphPage();
                break;
            case R.id.voice_input: // 音声入力
                startVoiceInput();
                break;
            case R.id.settings: // 設定
                startSetting();
                break;
            case R.id.io_csv: // CSV入出力
                startCSV();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    // 音声認識の結果を受け取る
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        MyOpenHelper helper = new MyOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor1 = db.query("GetUpTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);
        Cursor cursor2 = db.query("GoToBedTable", new String[] {"id", "hour", "minute"}, null, null, null, null, null);

        cursor1.moveToLast();
        cursor2.moveToLast();

        // 最新の記録を取得
        int latestGU = cursor1.getInt(1);
        int latestGTB = cursor2.getInt(1);

        cursor1.close();
        cursor2.close();

        long id = DatabaseUtils.queryNumEntries(db, "DateTable") - 1;

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(this));

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // 認識結果をArrayListで取得
            ArrayList<String> results_data = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            // 候補が一つ以上ある場合
            if (results_data.size() > 0) {
                // 「おはよう」の場合
                if (results_data.get(0).equals("おはよう")) {
                    if (latestGU == -1) { // 最新の記録が空の場合
                        // 記録し更新
                        timeHandler.updateTime(false, db, (int) id, year, month, date, hour, minute);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    } else { // 最新の記録がある場合
                        timeHandler.showDialog(MainActivity.this, 0, "既に起きています！", "", "OK");
                    }
                } else if (results_data.get(0).equals("おやすみ")) { // 「おやすみ」の場合
                    if (latestGTB == -1) { // 最新の記録が空の場合
                        // 記録し更新
                        timeHandler.updateTime(true, db, (int) id, year, month, date, hour, minute);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    } else { // 最新の記録がある場合
                        timeHandler.showDialog(MainActivity.this, 0, "既に寝ています！", "", "OK");
                    }
                } else { // 「おはよう」でも「おやすみ」でもない場合
                    timeHandler.showDialog(MainActivity.this, 0, "「" + results_data.get(0) + "」", "もう一度ボタンを押し\n「おはよう」または「おやすみ」\nと発音してください！", "OK");
                }
            } else { // 候補がない場合
                timeHandler.showDialog(MainActivity.this, 0, "認識に失敗しました", "", "OK");
            }
        }
    }

    // 起床or就寝時刻ボタンまたは記録のテキストビューを押したときに呼ばれるメソッド
    public void recordTime(final boolean sleep, final SQLiteDatabase db, final int i) {

        // 押し忘れ入力時のデフォルト時刻を取得するために設定から取得
        SharedPreferences sp = getSharedPreferences("pref", MODE_PRIVATE);
        int default_time;
        // 起床→就寝切り替え時刻を取得
        int stay_up_line = sp.getInt("stay_up_line", 1200);
        int hour_line = timeHandler.number_to_time(stay_up_line)[0];

        String sleepText;
        if (sleep == false) {
            sleepText = "起床";
            default_time = sp.getInt("default_gu", 0);
        }else {
            sleepText = "就寝";
            default_time = sp.getInt("default_gtb", 0);
        }

        String updateORadd = "記録";

        // 現在の日付と時刻を取得
        calendar = Calendar.getInstance();
        // 該当する時刻の場合加算or減算
        calendar.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(this));

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        date = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        String tableName;
        if (i != -1) {
            if (sleep == false) {
                tableName = "GetUpTable";
            }else {
                tableName = "GoToBedTable";
            }

            updateORadd = "修正";

            Cursor cursor0 = db.query("DateTable", new String[] {"id", "year", "month", "date"}, "id=" + i, null, null, null, null);
            cursor0.moveToFirst();
            year = cursor0.getInt(1);
            month = cursor0.getInt(2);
            date = cursor0.getInt(3);
            cursor0.close();

            Cursor cursor = db.query(tableName, new String[] {"id", "hour", "minute"}, null, null, null, null, null);
            cursor.moveToPosition(i);
            hour = cursor.getInt(1);
            minute = cursor.getInt(2);
            cursor.moveToPrevious();
            int weekOrLength = i - 1;
            if (weekOrLength > 7) {
                weekOrLength = 7;
            }
            int[] past_hour = new int[weekOrLength];
            int[] past_minute = new int[weekOrLength];
            for (int j = 0; j < weekOrLength; j++) {
                past_hour[j] = cursor.getInt(1);
                past_minute[j] = cursor.getInt(2);
                cursor.moveToPrevious();
            }
            cursor.close();

            // 空の時
            if (minute == -1) {
                int defaultWhich = (default_time - (default_time % 10000)) / 10000;
                switch (defaultWhich) {
                    case 0: // 現在時刻
                        hour = calendar.get(Calendar.HOUR_OF_DAY);
                        minute = calendar.get(Calendar.MINUTE);
                        break;
                    case 1: // 前日
                        for (int j = 0; j < weekOrLength; j++) { // 空でない日が見つかるまで繰り返す
                            if (past_minute[j] != -1) { // 空でないとき
                                hour = past_hour[j];
                                minute = past_minute[j];
                                break;
                            }
                        }
                        if (minute == -1) { // 空でない日がなかった場合は自分で指定した時刻に
                            hour = timeHandler.number_to_time(default_time % 10000)[0];
                            minute = timeHandler.number_to_time(default_time % 10000)[1];
                        }
                        break;
                    case 2: // 過去1週間の平均
                        int sum = 0;
                        int count = 0;
                        for (int j = 0; j < weekOrLength; j++) {
                            if (past_minute[j] != -1) {
                                count++;

                                if (sleep && past_hour[j] < hour_line) { // 就寝の場合かつ0時を過ぎている場合24を加算
                                    past_hour[j] += 24;
                                }

                                // 時を分に換算し合計に加算
                                sum += (past_hour[j] * 60) + past_minute[j];
                            }
                        }
                        // 分母が0でないとき
                        if (count > 0) {
                            int average = sum / count;
                            minute = average % 60;
                            hour = (average - minute) / 60;

                            if (hour >= 24) { // 24時以上の場合24を引いて戻す
                                hour -= 24;
                            }
                        } else { // すべて空で平均が計算できなかった場合は自分で指定した時刻に
                            hour = timeHandler.number_to_time(default_time % 10000)[0];
                            minute = timeHandler.number_to_time(default_time % 10000)[1];
                        }
                        break;
                    case 3: // 自分で指定
                        hour = timeHandler.number_to_time(default_time % 10000)[0];
                        minute = timeHandler.number_to_time(default_time % 10000)[1];
                        break;
                }
            }
        }

        // APIレベルによってタイムピッカーの表示方法を分ける
        if (Build.VERSION.SDK_INT >= 23) {
            // 自作のタイムピッカーを表示
            LayoutInflater inflater = getLayoutInflater();
            View originalDialog = inflater.inflate(R.layout.dialog_original_time_picker, (ViewGroup) findViewById(R.id.dialog_root));

            originalTimePicker = originalDialog.findViewById(R.id.originalTimePicker);
            originalTimePicker.setIs24HourView(true);
            originalTimePicker.setHour(hour);
            originalTimePicker.setMinute(minute);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(originalDialog);
            builder.setTitle(year + "年" + month + "月" + date + "日の" + sleepText + "時刻");
            builder.setPositiveButton(
                    updateORadd + "する",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            long idNumber;
                            if (i == -1) {
                                idNumber = DatabaseUtils.queryNumEntries(db, "DateTable") - 1;
                            }else {
                                idNumber = i;
                            }

                            timeHandler.updateTime(sleep, db, (int) idNumber, year, month, date, originalTimePicker.getHour(), originalTimePicker.getMinute());

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);

                            dialog.dismiss();
                        }
                    }
            );
            if (i != -1) {
                builder.setNeutralButton(
                        "削除する",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                timeHandler.updateTime(sleep, db, i, year, month, date, -1, -1);

                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);

                                dialog.dismiss();
                            }
                        }
                );
            }
            builder.setNegativeButton(
                    "キャンセル",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );
            builder.show();
        } else {
            // タイムピッカーを表示
            final CustomTimePickerDialog timePickerDialog;
            final CustomTimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int s_hour, int s_minute) {

                }
            };

//            int dialogTheme = TimePickerDialog.THEME_HOLO_LIGHT;
//            if (Build.VERSION.SDK_INT >= 23) {
//                dialogTheme = R.style.Theme_AppCompat_DayNight_Dialog;
//            }
            timePickerDialog = new CustomTimePickerDialog(MainActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, listener, hour, minute, true);

            timePickerDialog.setTitle(year + "年" + month + "月" + date + "日の" + sleepText + "時刻");
            timePickerDialog.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    updateORadd + "する",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            long idNumber;
                            if (i == -1) {
                                idNumber = DatabaseUtils.queryNumEntries(db, "DateTable") - 1;
                            } else {
                                idNumber = i;
                            }

                            timeHandler.updateTime(sleep, db, (int) idNumber, year, month, date, hour, minute);

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);

                            dialog.dismiss();
                        }
                    }
            );

            if (i != -1) {
                timePickerDialog.setButton(
                        DialogInterface.BUTTON_NEUTRAL,
                        "削除する",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                timeHandler.updateTime(sleep, db, i, year, month, date, -1, -1);

                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);

                                dialog.dismiss();
                            }
                        }
                );
            }

            timePickerDialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    "キャンセル",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Negative Button がクリックされた時の動作
                            dialog.dismiss();
                        }
                    }
            );
            timePickerDialog.show();
        }
    }

    // 指定日と今日の差分を計算するメソッド
//    public int spec_today(SQLiteDatabase db, Context context, boolean restart) {
//        int spec_year;
//        int spec_month;
//        int spec_date;
//        // アクティビティ初回起動時
//        if (!restart) {
//            // 指定日をDBから取得
//            Cursor cursor = db.query("RangeTable", new String[]{"id", "year", "month", "date"}, null, null, null, null, null);
//            cursor.moveToFirst();
//            spec_year = cursor.getInt(1);
//            spec_month = cursor.getInt(2);
//            spec_date = cursor.getInt(3);
//            cursor.close();
//        } else { // アクティビティ更新時
//            spec_year = PrefActivity.spec_year;
//            spec_month = PrefActivity.spec_month;
//            spec_date = PrefActivity.spec_date;
//        }
//        // 取得した日付をセット
//        Calendar cal_spec = Calendar.getInstance();
//        cal_spec.set(spec_year, spec_month, spec_date);
//        // 今日の日付を取得
//        Calendar cal_now = Calendar.getInstance();
//        cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context));
//        // 指定日と今日の差を計算し、返す
//        return timeHandler.cal_diff_Days(cal_now, cal_spec) + 1;
//    }

    // 指定日1と今日、指定日2と今日の差分を計算するメソッド
//    public int[] spec12_today(SQLiteDatabase db, Context context, boolean restart) {
//        int spec_year1;
//        int spec_month1;
//        int spec_date1;
//        int spec_year2;
//        int spec_month2;
//        int spec_date2;
//        // アクティビティ初回起動時
//        if (!restart) {
//            // 指定日1、2をDBから取得
//            Cursor cursor = db.query("RangeTable", new String[]{"id", "year", "month", "date"}, null, null, null, null, null);
//            cursor.moveToPosition(1);
//            spec_year1 = cursor.getInt(1);
//            spec_month1 = cursor.getInt(2);
//            spec_date1 = cursor.getInt(3);
//            cursor.moveToNext();
//            spec_year2 = cursor.getInt(1);
//            spec_month2 = cursor.getInt(2);
//            spec_date2 = cursor.getInt(3);
//            cursor.close();
//        } else {
//            spec_year1 = PrefActivity.spec_year1;
//            spec_month1 = PrefActivity.spec_month1;
//            spec_date1 = PrefActivity.spec_date1;
//            spec_year2 = PrefActivity.spec_year2;
//            spec_month2 = PrefActivity.spec_month2;
//            spec_date2 = PrefActivity.spec_date2;
//        }
//        // 取得した日付をセット
//        Calendar cal_spec1 = Calendar.getInstance();
//        cal_spec1.set(spec_year1, spec_month1, spec_date1);
//        Calendar cal_spec2 = Calendar.getInstance();
//        cal_spec2.set(spec_year2, spec_month2, spec_date2);
//        // 今日の日付を取得
//        Calendar cal_now = Calendar.getInstance();
//        cal_now.add(Calendar.DAY_OF_MONTH, timeHandler.compareTime(context));
//        // 今日と指定日1、2の差日数を計算し配列として返す
//        return new int[]{timeHandler.cal_diff_Days(cal_now, cal_spec1), timeHandler.cal_diff_Days(cal_now, cal_spec2)};
//    }

    // dpをpxに変換するメソッド
    public int convertDp2Px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        int px = (int) (dp * scale);
        return px;
    }
}
