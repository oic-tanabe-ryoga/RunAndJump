package com.example.surfaceview3;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class MainActivity extends Activity {

    // view(「view」オブジェクトを格納する変数)の宣言
    private View view;

    //ハンドラを作成
    private Handler handler = new Handler();
    //ビューの再描画感覚（ミリ秒）
    private final static long MSEC = 40;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 「GameView」オブジェクト(ビュー)の作成
        view = new GameView(this);

        // アクティビティにビューを組み込む
        setContentView(view);

        //ビュー再描画タイマー
        //タイマーを作成
        Timer timer = new Timer(false);
        //「MSEC」ミリ秒おきにタスクを実行
        timer.schedule(new TimerTask(){
            public void run(){
                handler.post(new Runnable() {
                    public void run(){
                        view.invalidate();
                    }});
            }
        },0,MSEC);
    }


    // アクティビティ再開時に実行されるメソッド
    @Override
    protected void onResume(){
        super.onResume();
        // ゲームの状態を取得
        int state = ((GameView) view).getGameState();
        // プレイ中なら、BGM再生開始
        //if(state == 1) Sounds.playBGM();
    }

    // アクティビティ一時停止時に実行されるメソッド
    @Override
    protected void onPause(){
        super.onPause();
        // BGMを一時停止
        //Sounds.stopBGM();
    }
}

class GameView extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    Thread thread;
    boolean isAttached;

    //ゲームの状態を表す状態
    public final static int GAME_START = 0;
    public final static int GAME_PLAY = 1;
    public final static int GAME_OVER = 2;
    //ゲームの状態を保持する変数
    private int gameState;

    //カウント
    private int count =0;

    //プレイ時間（秒）
    private final static long TIME = 60;
    //ゲーム開始時刻
    private long gameStarted;
    //残り時間
    private long remainedTime;

    //背景画像を格納する変数を宣言
    private Bitmap bgImage;

    //スタート画面の背景画像を格納する変数
    private Bitmap startImage;

    //スタートボタンの画像を格納する変数
    private Bitmap startButton;

    private Bitmap retryButton;

    //アニメーションのフレーム数
    private int frameIndex = 0;
    // プレイヤー画像を格納する変数を宣言
    private Bitmap[] player = new Bitmap[8];
    // プレイヤーのX座標
    private int playerX;
    // プレイヤーのY座標
    private int playerY;

    //プレイヤーの上昇量
    private int playerVY;

    private Bitmap[] yuka = new Bitmap[2];

    //雲の画像を格納する変数
    private Bitmap cloud;
    //雲のX座標
    private int cloudX = 0;
    //雲のY座標
    private int cloudY = 0;
    //雲の水平方向の移動量
    private int cloudVX = -8;

    // 画面(Canvas)中央のX座標
    private int canvasCX;
    // 画面(Canvas)中央のY座標
    private int canvasCY;

    //
    private int canvasY;
    private int canvasX;


    //赤アイテムのX座標
    private int energyrX;
    //アイテムのY座標
    private int energyrY;
    //アイテムの水平方向の移動量
    private int energyrVX = -15;

    //青アイテムのX座標
    private int energybX;
    //アイテムのY座標
    private int energybY;
    //アイテムの水平方向の移動量
    private int energybVX = -40;

    //緑アイテムのX座標
    private int energygX;
    //アイテムのY座標
    private int energygY;
    //アイテムの水平方向の移動量
    private int energygVX = +1;

    private int jflag=0;



    //アイテム描画用のペイントオブジェクト作成
    Paint energyrPaint = new Paint();

    Paint energybPaint = new Paint();

    Paint energygPaint = new Paint();

    //スコアのラベルテキスト
    private final String scoreLabel = "SCORE:";
    //スコアを保持する変数
    private int score;

    //スコア描画用のペイントオブジェクトを作成
    Paint scorePaint = new Paint();

    //タイトル描画用のペイントオブジェクトを作成
    Paint titlePaint = new Paint();

    //残り時間描画用のペイントオブジェクト作成
    Paint timePaint = new Paint();




    private float screenWidth, screenHeight;

    public GameView(Context context) {
        super(context);

        // リソースオブジェクトを作成
        Resources res = this.getContext().getResources();

        //背景画像「bg.jpg」をビットマップに変換して変数「bgImage」に入れる
        bgImage = BitmapFactory.decodeResource(res, R.mipmap.bg1);

        //背景画像「cloud1.png」をビットマップに変換して変数「cloud」に入れる
        cloud = BitmapFactory.decodeResource(res,R.mipmap.cloud1);

        // 変数「player」の配列宣言
        player[0] = BitmapFactory.decodeResource(res, R.mipmap.bird1);
        player[1] = BitmapFactory.decodeResource(res, R.mipmap.bird2);
        player[2] = BitmapFactory.decodeResource(res, R.mipmap.bird3);
        player[3] = BitmapFactory.decodeResource(res, R.mipmap.bird4);
        player[4] = BitmapFactory.decodeResource(res, R.mipmap.bird5);
        player[5] = BitmapFactory.decodeResource(res, R.mipmap.bird6);
        player[6] = BitmapFactory.decodeResource(res, R.mipmap.bird7);
        player[7] = BitmapFactory.decodeResource(res, R.mipmap.bird8);

        yuka[0]= BitmapFactory.decodeResource(res,R.drawable.yuka);
        yuka[1]= BitmapFactory.decodeResource(res,R.drawable.yukakusa);

        //スタート画像の背景画像を「top」
        //変数「startImage」に入れる
        startImage = BitmapFactory.decodeResource(res,R.drawable.topp);
        //スタートボタンの画像「start」
        //変数「startBotton」に入れる
        startButton = BitmapFactory.decodeResource(res,R.mipmap.start);

        //リトライボタンの画像「retry」
        //変数「retry」に入れる
        retryButton = BitmapFactory.decodeResource(res,R.mipmap.retry);

        //アイテムの描画色を設定
        energyrPaint.setColor(Color.RED);
        //アンチエイリアンスを有効にする
        energyrPaint.setAntiAlias(true);

        //アイテムの描画色を設定
        energybPaint.setColor(Color.BLUE);
        //アンチエイリアンスを有効にする
        energybPaint.setAntiAlias(true);

        //アイテムの描画色を設定
        energygPaint.setColor(Color.GREEN);
        //アンチエイリアンスを有効にする
        energygPaint.setAntiAlias(true);

        //スコアの描画色を設定
        scorePaint.setColor(Color.YELLOW);
        //スコアのテキストサイズを設定
        scorePaint.setTextSize(64);
        //アンチエイリアンスを有効にする
        scorePaint.setAntiAlias(true);

        //残り時間の描画色を設定
        timePaint.setColor(Color.RED);
        //残り時間のテキストサイズを設定
        timePaint.setTextSize(48);
        //アンチエイリアスを有効にする
        timePaint.setAntiAlias(true);

        //ゲームの状態をゲームスタートに設定
        gameState = GAME_START;

        //[Sounds]クラスの初期化
        //Sounds.init(context);

        //[PlayLog]クラスを初期化
        //PlayLog.init(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isAttached=true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isAttached = false;
        while (thread.isAlive());
    }

    @Override
    public void run() {
        while (isAttached) {
            Log.v("SurfaceViewSample3","run");



            doDraw(getHolder());
        }
    }

    private void doDraw(SurfaceHolder holder){
        Canvas canvas = holder.lockCanvas();

        // この間にグラフィック描画のコードを記述する。

        // 画面(Canvas)中央のX座標を取得
        canvasCX = canvas.getWidth() / 2;
        Log.d("Canvasサイズ","canvasCX"+canvasCX);
        // 画面(Canvas)中央のY座標を取得
        canvasCY = canvas.getHeight() / 2;
        Log.d("Canvasサイズ","canvasCY"+canvasCY);
        canvasY=canvas.getHeight();
        canvasX=canvas.getWidth();


        switch(gameState) {

            case GAME_START:
                //画面サイズに応じて背景画像を拡大する
                bgImage = Bitmap.createScaledBitmap(bgImage,
                        canvas.getWidth() * 2, canvas.getHeight(), true);
                //「startScene」メソッドを実行
                startScene(canvas);
                break;


            case GAME_PLAY:
                // 「playScene」メソッドを実行
                playScene(canvas);
                break;


            case GAME_OVER:
                //「overScene」メソッド実行
                overScene(canvas);
                break;

        }




        // この間にグラフィック描画のコードを記述する。

        holder.unlockCanvasAndPost(canvas);
    }
    //スタート画面を作成するメソッド
    public void startScene(Canvas canvas){
        //スコア初期化
        score = 0;
        //背景画像を描画
        startImage = Bitmap.createScaledBitmap(
                startImage,canvas.getWidth(),
                canvas.getHeight(),true);
        canvas.drawBitmap(startImage, 0, 0, null);

        //アンチエイリアスを有効にする
        titlePaint.setAntiAlias(true);
        //タイトルの描画色
        titlePaint.setColor(Color.WHITE);
        //タイトルのテキストサイズ
        titlePaint.setTextSize(172);
        //タイトルのテキスト配置
        titlePaint.setTextAlign(Paint.Align.CENTER);

        //ハイスコアを描画
        titlePaint.setColor(Color.YELLOW);
        titlePaint.setTextSize(64);
        //canvas.drawText("High score:" + PlayLog.bestScore(),
        //canvasCX, canvasCY - 200, titlePaint);

        //タイトルテキストを描画
        canvas.drawBitmap(startButton,
                canvasCX - startButton.getWidth() / 2,
                canvasCY - startButton.getHeight() + 200, null);
    }

    //ゲーム終了画面を作成するメソッド
    public  void overScene(Canvas canvas){
        canvas.drawColor(Color.WHITE);

        //リトライボタンを描画
        canvas.drawBitmap(retryButton,
                canvasCX - retryButton.getWidth()/2,
                canvasCY - retryButton.getHeight(),null);
        //アンチエイリアスを有効にする
        titlePaint.setAntiAlias(true);
        //描画色
        titlePaint.setColor(Color.RED);
        //テキストサイズ
        titlePaint.setTextSize(120);
        //テキスト配置
        titlePaint.setTextAlign(Paint.Align.CENTER);
        //テキスト描画
        canvas.drawText("Time Up!",canvasCX,canvasCY - 200,titlePaint);

        titlePaint.setColor(Color.BLUE);
        titlePaint.setTextSize(64);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        //スコアを描画
        canvas.drawText("Your score:" + score, canvasCX, canvasCY + 200, titlePaint);
    }


    // 「playScene」メソッド
    public void playScene(Canvas canvas){

        //残り時間を取得
        remainedTime = TIME - (System.currentTimeMillis()-gameStarted)/1000;
        //残り時間が0より小さくなったら
        if(remainedTime<0){
            //BGMの停止
            //Sounds.stopBGM();
            //スコアのチェックと保存
            //PlayLog.logScore(score);
            //ゲームの状態をGAME_OVERに設定する
            gameState = GAME_OVER;

            //リターン
            return;
        }
        // 画面に背景画像を描画
        canvas.drawBitmap(bgImage,0,0,null);

        //アイテムをスクロール表示するための処理

        //アイテムをenergyVXずつ右から左に移動する
        energyrX += energyrVX;
        energyrY += Math.cos(energyrX / 60)*20;
        //画面から消えたまたはプレイヤーに当たれば
        if(energyrX<0||hitCheckr()){
            //また右から
            energyrX = canvas.getWidth() +20;
            //高さが画面の上半分にランダム出現
            energyrY = (int)Math.floor(Math.random()*canvasCY);

        }

        energybX += energybVX;
        //画面から消えたまたはプレイヤーに当たれば
        if(energybX<0||hitCheckb()){
            //また右から
            energybX = canvas.getWidth() +20;
            //高さが画面の上半分にランダム出現
            energybY = (int)Math.floor(Math.random()*canvasCY);

        }

        if(energygY<0)energygY =1;
        energygX += energygVX;
        //画面から消えたまたはプレイヤーに当たれば
        if(energygX>canvas.getWidth() +20||hitCheckg()){
            //また左から
            energygX = 0 ;
            //高さが画面の上半分にランダム出現
            energygY = (int)Math.floor(Math.random()*canvasCY);

        }

        //雲をスクロール表示するための処理

        //雲の座標を「cloudVX」の分だけ移動
        cloudX += cloudVX;

        //全体が消えたら
        if(cloudX< -cloud.getWidth()){
            //また右から
            cloudX = canvas.getWidth();
            //高さ（Y座標）は画面のランダム位置
            //雲表示
            cloudY = (int)Math.floor(Math.random() * (canvasCY/2-10));
        }

        //画面に雲描画
        canvas.drawBitmap(cloud, cloudX, cloudY, null);
        //画面にアイテムを描画
        canvas.drawCircle(energyrX, energyrY, 10, energyrPaint);

        canvas.drawCircle(energybX, energybY, 10, energybPaint);

        canvas.drawCircle(energygX, energygY, 10, energygPaint);


        for(int cc=0;cc<=1;cc++)
            for(int c=0;c<=31;c++)
            canvas.drawBitmap(yuka[0], canvasX - (c+1)* 40, canvasY -(cc+1)*40, null);
        for(int c=0;c<=31;c++)
            canvas.drawBitmap(yuka[1], canvasX - (c+1)* 40, canvasY -120, null);


        // プレイヤーの初期表示X座標を設定
        playerX = canvasCX - player[0].getWidth() / 2;
        // プレイヤーの初期表示Y座標を設定
        //playerY =canvasCY - player[0].getHeight() / 2;

        //プレイヤーをplayerVYずつ上昇させる
        playerY += playerVY;
        //画面上よりはみ出さないようにする
        if(playerY<0)playerY =0;

        //プレイヤーを下降する
        playerVY += 4;
        //元の位置より下に行かないようにする
        if(playerY>canvasCY) playerY = canvasCY;
        if(playerY==canvasCY)jflag=0;

        // 画面(Canvas)にプレイヤーを描画
        if(count++<3) {
            canvas.drawBitmap(player[frameIndex], playerX, playerY ,null);
        }else{
            canvas.drawBitmap(player[frameIndex], playerX, playerY ,null);
            frameIndex++;
            count=0;
        }
        if(frameIndex>7)frameIndex=0;

        //スコアを描画
        canvas.drawText(scoreLabel+score,10,100,scorePaint);

        //残り時間を描画
        canvas.drawText("Time"+remainedTime,10,200,timePaint);
    }

    // タッチイベント時に実行されるメソッド
    public boolean onTouchEvent(MotionEvent me){

        //タッチした場所のX座標を取得
        int x = (int)me.getX();
        //タッチした場所のY座標を取得
        int y = (int)me.getY();


        // タッチされたら
        if(me.getAction() == MotionEvent.ACTION_DOWN){
            //ゲームの状態を取得
            switch (gameState) {
                //ゲームスタートの時
                case GAME_START:
                    //スタートボタンが押されたら
                    if (buttonOn(startButton, x, y)) {
                        //ゲームの状態をGAME_PLAYに設定する
                        gameState = GAME_PLAY;
                        //BGM再生開始
                        //Sounds.playBGM();
                        //ゲーム開始時間を取得する
                        gameStarted = System.currentTimeMillis();
                    }
                    break;




                case GAME_PLAY:
                    // プレイヤーの上昇値を設定

                    if(jflag==1) {
                        if (0 <= y && y <= canvasCY) playerVY = -30;
                        else playerVY = 50;
                        jflag=2;
                    }
                    if(jflag==0) {
                        playerVY=-50;
                        jflag=1;
                    }
                    break;

                case GAME_OVER:
                    //リトライボタンが押されたら、
                    if (buttonOff(startButton, x, y)) {
                        //ゲームの状態をGAME_STARTに設定する
                        gameState = GAME_START;
                    }
                    break;
            }
        }
        // 呼び出し元に戻る
        return  true;
    }


    //ボタンがタッチされたかどうかチェックするメソッド
    public boolean buttonOn(Bitmap button,int x,int y){
        //ボタンのX座標を取得
        int posX = canvasCX - startButton.getWidth()/2;
        //ボタンのY座標を取得
        int posY = canvasCY - startButton.getHeight()+200;

        if(x>posX&&x<posX+startButton.getWidth()&&
                y>posY&&y<posY+startButton.getHeight()){
            //ボタンがタッチされたらtrueを返す
            return true;
        }else{
            //そうでなければfalseを返す
            return  false;
        }
    }

    //ボタンがタッチされたかどうかチェックするメソッド
    public boolean buttonOff(Bitmap button,int x,int y){
        //ボタンのX座標を取得
        int posX = canvasCX - startButton.getWidth()/2;
        //ボタンのY座標を取得
        int posY = canvasCY - startButton.getHeight();

        if(x>posX&&x<posX+startButton.getWidth()&&
                y>posY&&y<posY+startButton.getHeight()){
            //ボタンがタッチされたらtrueを返す
            return true;
        }else{
            //そうでなければfalseを返す
            return  false;
        }
    }


    // 衝突判定メソッド
    public boolean hitCheckr(){
        if(playerX < energyrX &&
                (playerX + player[0].getWidth()) > energyrX &&
                playerY < energyrY &&
                (playerY + player[0].getHeight()) > energyrY){
            //score加算
            score+=10;

            //効果音を再生
            //Sounds.playSE();
            // アイテムが中心座標が、プレイヤーの矩形領域の中なら「true」を返す
            return true;
        } else {
            return false;
        }

    }

    // 衝突判定メソッド
    public boolean hitCheckb(){
        if(playerX < energybX &&
                (playerX + player[0].getWidth()) > energybX &&
                playerY < energybY &&
                (playerY + player[0].getHeight()) > energybY){
            //score加算
            score+=20;


            //効果音を再生
            //Sounds.playSE();
            // アイテムが中心座標が、プレイヤーの矩形領域の中なら「true」を返す
            return true;
        } else {
            return false;
        }

    }

    public boolean hitCheckg(){
        if(playerX < energygX &&
                (playerX + player[0].getWidth()) > energygX &&
                playerY < energygY &&
                (playerY + player[0].getHeight()) > energygY){
            //score加算
            score+=100;


            //効果音を再生
            //Sounds.playSE();
            // アイテムが中心座標が、プレイヤーの矩形領域の中なら「true」を返す
            return true;
        } else {
            return false;
        }

    }



    // ゲームの状態を取得するメソッド
    public  int getGameState(){
        return  gameState;
    }
}