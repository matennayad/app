package com.matennayad.gamebox;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
    GameView view;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(9,13,27));
        getWindow().setNavigationBarColor(Color.rgb(9,13,27));
        view=new GameView(this);
        setContentView(view);
    }
    @Override public void onBackPressed(){if(view.screen!=0)view.goHome();else super.onBackPressed();}

    static class Game {
        int id,type; String title,icon,cat;
        Game(int id,String title,String icon,String cat,int type){this.id=id;this.title=title;this.icon=icon;this.cat=cat;this.type=type;}
    }

    class GameView extends View {
        final int BG=Color.rgb(9,13,27), SUR=Color.rgb(18,24,43), SUR2=Color.rgb(25,34,59),
                TXT=Color.rgb(247,249,255), MUTED=Color.rgb(150,163,190), ACC=Color.rgb(124,92,255),
                GREEN=Color.rgb(46,216,163), RED=Color.rgb(255,92,122), GOLD=Color.rgb(255,200,87),
                BLUE=Color.rgb(72,156,255);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Handler h=new Handler(Looper.getMainLooper());
        Random rnd=new Random();
        SharedPreferences prefs;
        ArrayList<Game> games=new ArrayList<>();
        String[] cats={"הכול","מהירות","חשיבה","שניים","ארקייד","זיכרון","אתגרים","מבחנים","מסתורין","זמן","קלפים"};
        int screen=0,selectedCat=0,gameId=-1,score=0,best=0,round=0,state=0,answer=0,a=0,b=0;
        long start;
        float scrollY=0f,downX,downY,lastY;
        boolean dragging=false,running=false;
        float tx=300,ty=300;
        int taps=0,lives=3,catcherX=300,fallingX=300,fallingY=180,colorIndex=0,seqPos=0,seqLen=3;
        int[] board=new int[9],memory=new int[12],seq=new int[12];
        String input="",message="";
        boolean playerTurn=true;

        GameView(Context c){super(c);prefs=getSharedPreferences("scores",MODE_PRIVATE);buildGames();}

        void addGame(int id,String t,String icon,String cat,int type){games.add(new Game(id,t,icon,cat,type));}
        void buildGames(){
            addGame(1,"תגובה בזק","⚡","מהירות",1);
            addGame(2,"לחץ 30","👆","מהירות",0);
            addGame(3,"עצור ב־10","⏱️","מהירות",14);
            addGame(4,"תפוס את הנקודה","🎯","מהירות",5);
            addGame(5,"שבירת שיא","🔥","מהירות",0);
            addGame(6,"לחיצה כפולה","✌️","מהירות",0);
            addGame(7,"מסך ירוק","🟢","מהירות",1);
            addGame(8,"המתן ואז לחץ","⏳","מהירות",1);
            addGame(9,"100 לחיצות","💥","מהירות",0);
            addGame(10,"אתגר 20 שניות","🚀","מהירות",14);
            addGame(11,"נחש את המספר","🔢","חשיבה",4);
            addGame(12,"חשבון בזק","🧮","חשיבה",3);
            addGame(13,"מי גדול יותר?","📈","חשיבה",12);
            addGame(14,"מצב הפוך","🔄","חשיבה",8);
            addGame(15,"צבע נכון","🌈","חשיבה",8);
            addGame(16,"מצא את השונה","🔎","חשיבה",9);
            addGame(17,"רצף סודי","🔐","חשיבה",7);
            addGame(18,"תיבה מסתורית","📦","חשיבה",13);
            addGame(19,"מספר מסתורי","🎲","חשיבה",4);
            addGame(20,"אתגר חיבור","➕","חשיבה",3);
            addGame(21,"אבן נייר מספריים","✊","שניים",11);
            addGame(22,"איקס עיגול","❌","שניים",10);
            addGame(23,"קלף גבוה","🃏","שניים",12);
            addGame(24,"דו־קרב לחיצות","⚔️","שניים",0);
            addGame(25,"קרב צבעים","🎨","שניים",8);
            addGame(26,"מי מגיב ראשון","🏁","שניים",1);
            addGame(27,"ארבעה בתור","🟡","שניים",10);
            addGame(28,"מלך המספרים","👑","שניים",12);
            addGame(29,"קרב חשבון","🧠","שניים",3);
            addGame(30,"דו־קרב זיכרון","🧠","שניים",2);
            addGame(31,"תפוס מטבעות","🪙","ארקייד",13);
            addGame(32,"התחמק","🚀","ארקייד",6);
            addGame(33,"פונג קטן","🏓","ארקייד",6);
            addGame(34,"מגן החללית","🛸","ארקייד",6);
            addGame(35,"נופלים מהשמיים","☄️","ארקייד",6);
            addGame(36,"תפוס את הכדור","⚾","ארקייד",13);
            addGame(37,"שער הזהב","🥅","ארקייד",5);
            addGame(38,"ברח מהמכשול","🏃","ארקייד",6);
            addGame(39,"מטאור אחרון","🌠","ארקייד",6);
            addGame(40,"ריצת אינסוף","🏃‍♂️","ארקייד",0);
            addGame(41,"זכור צבעים","🧠","זיכרון",2);
            addGame(42,"זכור מספרים","🔢","זיכרון",7);
            addGame(43,"זכור אורות","💡","זיכרון",2);
            addGame(44,"זוגות","🃏","זיכרון",2);
            addGame(45,"זיכרון 4x3","🧩","זיכרון",2);
            addGame(46,"רצף 1־9","🔢","זיכרון",7);
            addGame(47,"זכור 5 צעדים","👣","זיכרון",2);
            addGame(48,"זכור את האמצע","🎯","זיכרון",2);
            addGame(49,"הדפוס הסודי","🌀","זיכרון",7);
            addGame(50,"זיכרון מהיר","⚡","זיכרון",2);
            addGame(51,"צבע או מילה","🌈","אתגרים",8);
            addGame(52,"אדום או כחול","🔴","אתגרים",8);
            addGame(53,"שונה בשורה","🟩","אתגרים",9);
            addGame(54,"המספר החסר","❓","אתגרים",7);
            addGame(55,"בחר נכון","✅","אתגרים",8);
            addGame(56,"כן או לא","✔️","אתגרים",8);
            addGame(57,"הכיוון הנכון","🧭","אתגרים",9);
            addGame(58,"מצא את המרכז","🎯","אתגרים",5);
            addGame(59,"תפוס את האור","💡","אתגרים",5);
            addGame(60,"החלטה מהירה","⚡","אתגרים",1);
            addGame(61,"מבחן תגובה","⚡","מבחנים",1);
            addGame(62,"מבחן דיוק","🎯","מבחנים",5);
            addGame(63,"מבחן זיכרון","🧠","מבחנים",2);
            addGame(64,"מבחן חשבון","🧮","מבחנים",3);
            addGame(65,"מבחן מספרים","🔢","מבחנים",4);
            addGame(66,"מבחן צבעים","🎨","מבחנים",8);
            addGame(67,"מבחן רצף","🔐","מבחנים",7);
            addGame(68,"מבחן מהירות","🏎️","מבחנים",0);
            addGame(69,"מבחן סבלנות","🧘","מבחנים",14);
            addGame(70,"מבחן ריכוז","👁️","מבחנים",9);
            addGame(71,"נחש את הקוד","🔑","מסתורין",4);
            addGame(72,"הקופסה האבודה","📦","מסתורין",13);
            addGame(73,"המספר הסודי","🕵️","מסתורין",4);
            addGame(74,"שלוש דלתות","🚪","מסתורין",13);
            addGame(75,"איזו תיבה?","🎁","מסתורין",13);
            addGame(76,"מצא את האוצר","💎","מסתורין",5);
            addGame(77,"קוד בן 4 ספרות","🔐","מסתורין",4);
            addGame(78,"המבוך הסודי","🌀","מסתורין",7);
            addGame(79,"החפץ הנכון","🧿","מסתורין",9);
            addGame(80,"הקלף המסתורי","🃏","מסתורין",12);
            addGame(81,"מספר מול זמן","⏳","זמן",14);
            addGame(82,"10 שניות בדיוק","🎯","זמן",14);
            addGame(83,"20 שניות","⏱️","זמן",14);
            addGame(84,"30 שניות","⌛","זמן",14);
            addGame(85,"דקת הזהב","🥇","זמן",14);
            addGame(86,"טיימר הפוך","🔻","זמן",14);
            addGame(87,"כמה מהר?","⚡","זמן",1);
            addGame(88,"עצור על 5","🖐️","זמן",14);
            addGame(89,"עצור על 15","🕐","זמן",14);
            addGame(90,"אתגר הזמן","🔥","זמן",14);
            addGame(91,"נחש את הקלף","🃏","קלפים",12);
            addGame(92,"מי שלף גבוה","♠️","קלפים",12);
            addGame(93,"קלף זהב","🌟","קלפים",12);
            addGame(94,"קלף מסתורי","🂠","קלפים",12);
            addGame(95,"שלוף 10","🎴","קלפים",12);
            addGame(96,"קרב קלפים","⚔️","קלפים",12);
            addGame(97,"אס או לא","🅰️","קלפים",12);
            addGame(98,"מספר הקלף","🔢","קלפים",12);
            addGame(99,"מלכת הקלפים","👸","קלפים",12);
            addGame(100,"קלף אחרון","🏁","קלפים",12);
        }

        void bold(float s,int c){p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextSize(s);p.setColor(c);}
        void normal(float s,int c){p.setTypeface(Typeface.create("sans",Typeface.NORMAL));p.setTextSize(s);p.setColor(c);}
        void center(Canvas c,String s,float x,float y){c.drawText(s,x-p.measureText(s)/2,y,p);}
        void rr(Canvas canvas,float l,float t,float r,float b,float rad,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);canvas.drawRoundRect(l,t,r,b,rad,rad,p);}
        void circle(Canvas canvas,float x,float y,float r,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);canvas.drawCircle(x,y,r,p);}
        String bestFor(int id){return String.valueOf(prefs.getInt("best_"+id,0));}
        void saveBest(){int old=prefs.getInt("best_"+gameId,0);if(score>old)prefs.edit().putInt("best_"+gameId,score).apply();best=Math.max(best,score);}
        void finishGame(String msg){running=false;saveBest();state=99;message=msg;h.removeCallbacksAndMessages(null);invalidate();}
        void goHome(){screen=0;running=false;scrollY=0;dragging=false;h.removeCallbacksAndMessages(null);invalidate();}

        @Override protected void onDraw(Canvas c){c.drawColor(BG);if(screen==0)drawHome(c);else drawGame(c);}

        void drawHome(Canvas c){
            normal(13,MUTED);c.drawText("GAMEBOX",24,28,p);
            bold(32,TXT);c.drawText("המשחקייה שלך",24,62,p);
            normal(14,MUTED);c.drawText("100 משחקים • אופליין • שיאים נשמרים",25,85,p);

            rr(c,getWidth()-118,18,getWidth()-20,68,20,SUR2);
            bold(20,GOLD);center(c,"★",getWidth()-91,50);
            normal(12,TXT);c.drawText("שיאים",getWidth()-69,52,p);

            // Featured card
            rr(c,20,100,getWidth()-20,182,26,ACC);
            bold(13,Color.WHITE);c.drawText("🔥 משחק היום",38,124,p);
            Game daily=games.get((int)(System.currentTimeMillis()/86400000L)%games.size());
            bold(21,TXT);c.drawText(daily.icon+"  "+daily.title,38,153,p);
            normal(12,Color.WHITE);c.drawText("לחץ כדי לשחק עכשיו",38,172,p);
            
            // Categories
            float cx=20;
            for(int i=0;i<cats.length;i++){
                float ww=Math.max(70,p.measureText(cats[i])+34);
                rr(c,cx,198,cx+ww,240,18,i==selectedCat?ACC:SUR);
                bold(13,i==selectedCat?TXT:MUTED);center(c,cats[i],cx+ww/2,225);
                cx+=ww+8;
                if(cx>getWidth()-70) break;
            }

            int top=258;
            int count=0;
            for(Game g:games)if(selectedCat==0||g.cat.equals(cats[selectedCat]))count++;
            int rows=(count+1)/2;
            float maxScroll=Math.max(0,top+rows*118-(getHeight()-14));
            if(scrollY>maxScroll)scrollY=maxScroll;

            c.save();
            c.clipRect(0,250,getWidth(),getHeight());
            c.translate(0,-scrollY);
            int row=0,col=0;
            for(Game g:games){
                if(selectedCat!=0&&!g.cat.equals(cats[selectedCat]))continue;
                float l=18+col*((getWidth()-51)/2f+15);
                float w=(getWidth()-51)/2f;
                float y=top+row*118;
                rr(c,l,y,l+w,y+105,24,SUR);
                rr(c,l+12,y+12,l+55,y+55,15,SUR2);
                bold(22,TXT);center(c,g.icon,l+33.5f,y+40);
                bold(16,TXT);c.drawText(g.title,l+14,y+77,p);
                normal(11,MUTED);c.drawText(g.cat+" • "+bestFor(g.id),l+14,y+95,p);
                col++;if(col==2){col=0;row++;}
            }
            c.restore();

            if(maxScroll>0){
                float track=300;
                float thumb=Math.max(52,track*(getHeight()/(getHeight()+maxScroll)));
                float sy=254+(track-thumb)*(scrollY/maxScroll);
                rr(c,getWidth()-7,sy,getWidth()-3,sy+thumb,4,SUR2);
            }
        }

        void drawGame(Canvas c){
            Game g=null;for(Game x:games)if(x.id==gameId)g=x;
            rr(c,16,16,68,66,18,SUR2);bold(28,TXT);center(c,"‹",42,51);
            bold(22,TXT);c.drawText(g.title,84,44,p);
            normal(12,MUTED);c.drawText("שיא "+best,84,64,p);
            if(state==99){drawEnd(c,g);return;}
            switch(g.type){
                case 0:tap(c);break;case 1:reaction(c);break;case 2:memoryGame(c);break;case 3:mathGame(c);break;
                case 4:guessGame(c);break;case 5:targetGame(c);break;case 6:dodgeGame(c);break;case 7:sequenceGame(c);break;
                case 8:colorGame(c);break;case 9:oddGame(c);break;case 10:ttt(c);break;case 11:rps(c);break;
                case 12:highCard(c);break;case 13:catchGame(c);break;default:timerGame(c);
            }
        }

        void intro(Canvas c,String text,String action){
            normal(17,MUTED);center(c,text,getWidth()/2,190);
            rr(c,45,270,getWidth()-45,345,22,ACC);bold(18,TXT);center(c,action,getWidth()/2,317);
        }
        void drawEnd(Canvas c,Game g){
            rr(c,22,145,getWidth()-22,560,30,SUR);
            bold(30,TXT);center(c,"סיימת! 🎉",getWidth()/2,220);
            normal(17,MUTED);center(c,g.title,getWidth()/2,252);
            bold(56,GOLD);center(c,""+score,getWidth()/2,332);
            normal(14,MUTED);center(c,"השיא החדש נשמר במכשיר",getWidth()/2,370);
            rr(c,50,410,getWidth()-50,475,20,ACC);bold(17,TXT);center(c,"שחק שוב",getWidth()/2,451);
            rr(c,50,490,getWidth()-50,550,20,SUR2);bold(17,TXT);center(c,"חזרה למשחקים",getWidth()/2,529);
        }

        void tap(Canvas c){if(state==0){intro(c,"כמה לחיצות תעשה לפני שהמטרה נעלמת?","התחל");return;}bold(24,TXT);center(c,taps+"",getWidth()/2,180);circle(c,getWidth()/2,370,115,ACC);bold(32,TXT);center(c,"לחץ!",getWidth()/2,382);}
        void reaction(Canvas c){if(state==0){intro(c,"כשהמסך הופך לירוק — לחץ מהר!","התחל");return;}rr(c,30,150,getWidth()-30,590,28,state==2?GREEN:RED);bold(32,TXT);center(c,state==2?"עכשיו!":"חכה...",getWidth()/2,365);}
        void memoryGame(Canvas c){normal(17,MUTED);center(c,"זכור את הסדר ואז חזור עליו",getWidth()/2,145);int[] cs={RED,GOLD,GREEN,ACC,BLUE,Color.MAGENTA};for(int i=0;i<6;i++){float x=72+(i%3)*132,y=245+(i/3)*130;circle(c,x,y,46,(state==1&&i>=Math.min(memIndex(),6))?SUR2:cs[i]);}normal(16,TXT);center(c,"רמה "+Math.max(1,round+1),getWidth()/2,525);}
        int memIndex(){return Math.max(0,score/100);}
        void mathGame(Canvas c){if(state==0){intro(c,"פתור 10 תרגילים. כל תשובה נכונה = 100","התחל");return;}bold(34,TXT);center(c,a+" + "+b+" = ?",getWidth()/2,205);normal(15,MUTED);center(c,"ניקוד "+score,getWidth()/2,245);for(int i=0;i<10;i++){int co=i%5,ro=i/5;rr(c,20+co*74,290+ro*75,82+co*74,345+ro*75,18,SUR2);bold(19,TXT);center(c,""+(i==9?0:i+1),51+co*74,326+ro*75);}}
        void guessGame(Canvas c){if(state==0){intro(c,"נחש מספר בין 1 ל־100","התחל");return;}bold(34,TXT);center(c,input.isEmpty()?"?":input,getWidth()/2,190);normal(15,MUTED);center(c,message,getWidth()/2,228);String[] n={"1","2","3","4","5","6","7","8","9","⌫","0","✓"};for(int i=0;i<12;i++){int co=i%3,ro=i/3;rr(c,45+co*105,270+ro*68,130+co*105,324+ro*68,17,i==11?GREEN:SUR2);bold(17,TXT);center(c,n[i],87+co*105,305+ro*68);}}
        void targetGame(Canvas c){if(state==0){intro(c,"תפוס 10 מטרות לפני שהן זזות","התחל");return;}circle(c,tx,ty,39,GOLD);circle(c,tx,ty,14,RED);bold(17,TXT);c.drawText("🎯 "+round+"/10",22,120,p);}
        void dodgeGame(Canvas c){rr(c,30,145,getWidth()-30,585,26,SUR);circle(c,catcherX,535,22,GREEN);circle(c,fallingX,fallingY,21,RED);bold(17,TXT);c.drawText("ניקוד "+score,32,120,p);}
        void sequenceGame(Canvas c){if(state==0){intro(c,"זכור את הרצף ולחץ לפי הסדר","התחל");return;}bold(35,TXT);center(c,""+seq[seqPos],getWidth()/2,200);normal(15,MUTED);center(c,"נכונים "+score+" • אורך "+seqLen,getWidth()/2,240);for(int i=0;i<9;i++){int co=i%3,ro=i/3;rr(c,35+co*112,290+ro*72,130+co*112,345+ro*72,18,SUR2);bold(18,TXT);center(c,""+(i+1),82+co*112,326+ro*72);}}
        void colorGame(Canvas c){if(state==0){intro(c,"בחר את צבע הטקסט, לא את המילה","התחל");return;}String[] n={"אדום","צהוב","ירוק","כחול","סגול"};int[] cs={RED,GOLD,GREEN,BLUE,ACC};bold(35,cs[colorIndex]);center(c,n[(colorIndex+2)%5],getWidth()/2,220);for(int i=0;i<5;i++){rr(c,16+i*74,315,88+i*74,375,18,cs[i]);bold(11,BG);center(c,n[i],52+i*74,351);}}
        void oddGame(Canvas c){if(state==0){intro(c,"מצא את העיגול השונה","התחל");return;}for(int i=0;i<9;i++){int co=i%3,ro=i/3;circle(c,70+co*135,250+ro*115,42,i==round%9?GREEN:SUR2);}}
        void ttt(Canvas c){normal(17,MUTED);center(c,playerTurn?"התור שלך":"המחשב חושב...",getWidth()/2,135);for(int i=0;i<9;i++){int co=i%3,ro=i/3;float l=55+co*105,t=175+ro*105;rr(c,l,t,l+85,t+85,18,SUR);if(board[i]!=0){bold(34,board[i]==1?ACC:GOLD);center(c,board[i]==1?"X":"O",l+42,t+58);}}}
        void rps(Canvas c){normal(18,MUTED);center(c,"בחר",getWidth()/2,170);rr(c,20,245,135,335,22,SUR2);rr(c,145,245,260,335,22,SUR2);rr(c,270,245,385,335,22,SUR2);bold(28,TXT);center(c,"✊",78,304);center(c,"✋",202,304);center(c,"✌",327,304);bold(20,TXT);center(c,message,getWidth()/2,410);}
        void highCard(Canvas c){normal(17,MUTED);center(c,"שלוף שני מספרים. הגבוה מנצח",getWidth()/2,160);rr(c,75,215,205,425,22,SUR2);rr(c,220,215,350,425,22,SUR2);bold(48,TXT);center(c,a==0?"?":""+a,140,335);center(c,b==0?"?":""+b,285,335);rr(c,75,465,350,530,20,ACC);bold(17,TXT);center(c,"שלוף",212,507);}
        void catchGame(Canvas c){rr(c,30,145,getWidth()-30,590,26,SUR);circle(c,fallingX,fallingY,18,GOLD);rr(c,catcherX-52,535,catcherX+52,562,12,ACC);bold(17,TXT);c.drawText("🪙 "+score+"   ❤️ "+lives,32,120,p);}
        void timerGame(Canvas c){if(state==0){intro(c,"כמה מהר תצליח לבצע את האתגר?","התחל");return;}long left=Math.max(0,60000-(System.currentTimeMillis()-start));bold(34,TXT);center(c,String.valueOf(left/1000),getWidth()/2,205);circle(c,getWidth()/2,365,108,ACC);bold(30,TXT);center(c,""+taps,getWidth()/2,377);}

        void startGame(int id){
            gameId=id;screen=1;state=0;score=0;best=prefs.getInt("best_"+id,0);round=0;taps=0;message="";input="";running=true;
            h.removeCallbacksAndMessages(null);
            Game g=games.get(id-1);
            if(g.type==4)answer=1+rnd.nextInt(100);
            if(g.type==10){Arrays.fill(board,0);state=1;playerTurn=true;}
            if(g.type>=5&&g.type<=6){state=1;catcherX=getWidth()/2;fallingY=180;fallingX=55+rnd.nextInt(Math.max(1,getWidth()-110));postLoop();}
            if(g.type==12)state=1;
            if(g.type==13){state=1;lives=3;catcherX=getWidth()/2;fallingY=180;fallingX=55+rnd.nextInt(Math.max(1,getWidth()-110));postLoop();}
            if(g.type==14){state=1;start=System.currentTimeMillis();postLoop();}
            if(g.type==0&&id%3==0){state=1;}
            invalidate();
        }

        void postLoop(){h.postDelayed(()->{if(running){tick();invalidate();postLoop();}},35);}
        void tick(){
            Game g=games.get(gameId-1);
            if(g.type==5){score=round*100;}
            if(g.type==6){fallingY+=7;if(fallingY>535){fallingY=180;fallingX=55+rnd.nextInt(Math.max(1,getWidth()-110));score++;}if(Math.abs(fallingX-catcherX)<42&&fallingY>500)finishGame("פגעת!");}
            if(g.type==13){fallingY+=5+score/20;if(fallingY>530){if(Math.abs(fallingX-catcherX)<75)score++;else lives--;fallingY=180;fallingX=55+rnd.nextInt(Math.max(1,getWidth()-110));if(lives<=0)finishGame("נגמרו החיים");}}
            if(g.type==14&&System.currentTimeMillis()-start>=60000)finishGame("נגמר הזמן");
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(screen==0){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;lastY=y;dragging=false;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){
                    if(Math.abs(y-downY)>8)dragging=true;
                    if(dragging&&y>245){float dy=y-lastY;scrollY=Math.max(0,scrollY-dy);lastY=y;invalidate();}
                    return true;
                }
                if(e.getAction()==MotionEvent.ACTION_UP){
                    if(!dragging){
                        if(y>=198&&y<=240){handleCategoryTap(x);return true;}
                        if(y>=100&&y<=182){Game daily=games.get((int)(System.currentTimeMillis()/86400000L)%games.size());startGame(daily.id);return true;}
                        float contentY=y+scrollY;if(contentY>=258){
                            int row=(int)((contentY-258)/118),col=x<getWidth()/2?0:1,idx=findVisibleIndex(row*2+col);
                            if(idx>=0){
                                float rowY=contentY-(258+row*118);
                                if(rowY>=0&&rowY<=105)startGame(games.get(idx).id);
                            }
                        }
                    }
                    return true;
                }
                return true;
            }
            if(y<80&&x<75&&e.getAction()==MotionEvent.ACTION_DOWN){goHome();return true;}
            if(state==99&&e.getAction()==MotionEvent.ACTION_DOWN){if(y>395&&y<480)startGame(gameId);else if(y>480)goHome();return true;}
            Game g=games.get(gameId-1);
            if(e.getAction()==MotionEvent.ACTION_MOVE&&(g.type==6||g.type==13)){catcherX=(int)x;return true;}
            if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;
            switch(g.type){
                case 0:tapTouch(y);break;case 1:reactionTouch();break;case 2:memoryTouch(x,y);break;case 3:mathTouch(x,y);break;
                case 4:guessTouch(x,y);break;case 5:targetTouch(x,y);break;case 6:catcherX=(int)x;break;case 7:sequenceTouch(x,y);break;
                case 8:colorTouch(x);break;case 9:oddTouch(x,y);break;case 10:tttTouch(x,y);break;case 11:rpsTouch(x,y);break;
                case 12:highTouch(y);break;case 13:catcherX=(int)x;break;default:timerTouch(y);break;
            }
            invalidate();return true;
        }

        void handleCategoryTap(float x){
            // category chips occupy the first visible row; calculate their widths exactly as drawHome
            float cx=20;
            for(int i=0;i<cats.length;i++){
                p.setTextSize(13);float ww=Math.max(70,p.measureText(cats[i])+34);
                if(x>=cx&&x<=cx+ww){selectedCat=i;scrollY=0;invalidate();return;}
                cx+=ww+8;if(cx>getWidth()-70)break;
            }
        }
        int findVisibleIndex(int n){
            int k=0;for(int i=0;i<games.size();i++){if(selectedCat!=0&&!games.get(i).cat.equals(cats[selectedCat]))continue;if(k==n)return i;k++;}return -1;
        }

        void tapTouch(float y){if(state==0){state=1;taps=0;}else if(y>250){taps++;score=taps;int target=(games.get(gameId-1).id%4==0)?20:30;if(taps>=target)finishGame("השגת את היעד!");}}
        void reactionTouch(){if(state==0){state=1;h.postDelayed(()->{if(running){state=2;start=System.nanoTime();invalidate();}},900+rnd.nextInt(2100));}else if(state==1)finishGame("לחצת מוקדם!");else{score=(int)((System.nanoTime()-start)/1000000);finishGame(score<250?"מהיר מאוד!":"יפה מאוד!");}}
        void memoryTouch(float x,float y){if(state==0){state=1;round=0;h.postDelayed(()->{if(running){state=2;invalidate();}},1000);return;}if(state==2){round++;score+=100;if(round>=5){finishGame("זיכרון מצוין!");}else{state=1;h.postDelayed(()->{if(running){state=2;invalidate();}},650);}}}
        void mathTouch(float x,float y){if(state==0){state=1;nextMath();return;}int co=(int)(x/74),ro=(int)((y-290)/75);if(co<0||co>4||ro<0||ro>1)return;int val=ro==0?co+1:(co==4?0:co+6);input+=val;if(input.length()>=2){int correct=a+b;if(Integer.parseInt(input)==correct)score+=100;input="";round++;if(round>=10)finishGame("10 תרגילים הושלמו");else nextMath();}}
        void nextMath(){a=1+rnd.nextInt(18);b=1+rnd.nextInt(18);}
        void guessTouch(float x,float y){if(state==0){state=1;return;}if(y<265)return;int i=(int)((x-45)/105)+(int)((y-270)/68)*3;if(i<0||i>=12)return;if(i==9){if(!input.isEmpty())input=input.substring(0,input.length()-1);return;}if(i==11){if(input.isEmpty())return;int n=Integer.parseInt(input);score++;if(n==answer)finishGame("ניחשת נכון!");else{message=n<answer?"יותר גבוה ↑":"יותר נמוך ↓";input="";if(score>=10)finishGame("נגמרו הניסיונות");}return;}int n=i==10?0:i+1;if(input.length()<3)input+=n;}
        void targetTouch(float x,float y){if(state==0){state=1;moveTarget();}else if(Math.hypot(x-tx,y-ty)<65){round++;score+=100;if(round>=10)finishGame("10 מטרות!");else moveTarget();}}
        void moveTarget(){tx=70+rnd.nextInt(Math.max(1,getWidth()-140));ty=220+rnd.nextInt(300);}
        void sequenceTouch(float x,float y){if(state==0){state=1;seqLen=3;seqPos=0;for(int i=0;i<12;i++)seq[i]=1+rnd.nextInt(9);h.postDelayed(()->{if(running){state=2;invalidate();}},850);return;}if(state==2){int co=Math.max(0,Math.min(2,(int)((x-35)/112))),ro=Math.max(0,Math.min(2,(int)((y-290)/72))),n=ro*3+co+1;if(n==seq[seqPos]){seqPos++;score++;if(seqPos>=seqLen){seqLen++;seqPos=0;if(seqLen>8)finishGame("הרצף הושלם!");else{state=1;h.postDelayed(()->{if(running){state=2;invalidate();}},850);}}}else finishGame("טעות ברצף");}}
        void colorTouch(float x){if(state==0){state=1;colorIndex=rnd.nextInt(5);return;}int chosen=Math.max(0,Math.min(4,(int)(x/74)));if(chosen==colorIndex){score+=10;colorIndex=rnd.nextInt(5);}else finishGame("בחירה לא נכונה");}
        void oddTouch(float x,float y){if(state==0){state=1;round=0;return;}int col=(int)((x-25)/135),ro=(int)((y-210)/115),pick=ro*3+col;if(pick==round%9){score+=100;round++;if(round>=10)finishGame("מצאת 10!");}else finishGame("זה לא היה השונה");}
        void tttTouch(float x,float y){if(y>535){Arrays.fill(board,0);playerTurn=true;score=0;return;}if(!playerTurn)return;int col=(int)((x-55)/105),ro=(int)((y-175)/105),i=ro*3+col;if(i>=0&&i<9&&board[i]==0){board[i]=1;int w=winner();if(w!=0){score=w==1?10:5;finishGame(w==1?"ניצחת!":"תיקו");return;}playerTurn=false;h.postDelayed(()->computerMove(),220);}}
        int winner(){int[][] q={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[] z:q)if(board[z[0]]!=0&&board[z[0]]==board[z[1]]&&board[z[1]]==board[z[2]])return board[z[0]];for(int z:board)if(z==0)return 0;return 3;}
        void computerMove(){ArrayList<Integer> f=new ArrayList<>();for(int i=0;i<9;i++)if(board[i]==0)f.add(i);if(!f.isEmpty())board[f.get(rnd.nextInt(f.size()))]=2;int w=winner();if(w!=0)finishGame(w==1?"ניצחת!":w==3?"תיקו":"המחשב ניצח");playerTurn=true;invalidate();}
        void rpsTouch(float x,float y){if(y<230||y>360)return;int me=x<135?0:x<260?1:2,op=rnd.nextInt(3),res=(me-op+3)%3;if(res==1){score++;message="ניצחת!";}else if(res==0)message="תיקו";else{message="הפסדת";if(score>0)score--;}}
        void highTouch(float y){if(y<430)return;a=1+rnd.nextInt(13);b=1+rnd.nextInt(13);if(a>b)score++;else if(a<b&&score>0)score--;if(score>=10)finishGame("10 ניצחונות!");}
        void timerTouch(float y){if(state==0){state=1;taps=0;start=System.currentTimeMillis();}else if(y>250)taps++;}
    }
}
