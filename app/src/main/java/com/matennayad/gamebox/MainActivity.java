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
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(11,16,32));
        getWindow().setNavigationBarColor(Color.rgb(11,16,32));
        view = new GameView(this);
        setContentView(view);
    }
    @Override public void onBackPressed() {
        if (view.screen != 0) view.goHome(); else super.onBackPressed();
    }

    static class Game {
        String title, icon, cat; int id;
        Game(int id,String title,String icon,String cat){this.id=id;this.title=title;this.icon=icon;this.cat=cat;}
    }

    class GameView extends View {
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Handler h=new Handler(Looper.getMainLooper());
        Random rnd=new Random();
        SharedPreferences prefs;
        int screen=0,gameId=-1,score=0,best=0,round=0,state=0,answer=0,a=0,b=0;
        long start;
        float tx=300,ty=300;
        String message="",input="";
        boolean running=false,playerTurn=true;
        int[] board=new int[9],memory=new int[12],seq=new int[12];
        int memLen=2,memPos=0,seqLen=3,seqPos=0,lives=3,catcherX=300,fallingX=300,fallingY=200,taps=0,colorIndex=0;
        final int BG=Color.rgb(11,16,32),SUR=Color.rgb(18,26,47),SUR2=Color.rgb(25,35,61),TXT=Color.rgb(247,249,255),MUTED=Color.rgb(155,167,192),ACC=Color.rgb(124,92,255),GREEN=Color.rgb(46,216,163),RED=Color.rgb(255,92,122),GOLD=Color.rgb(255,200,87);
        ArrayList<Game> games=new ArrayList<>();

        GameView(Context c){super(c);prefs=getSharedPreferences("scores",MODE_PRIVATE);buildGames();setFocusable(true);}
        void buildGames(){
            games.add(new Game(1,"תגובה מהירה","⚡","מהירות")); games.add(new Game(2,"לחץ 30","👆","מהירות"));
            games.add(new Game(3,"עצור ב־10","⏱","מהירות")); games.add(new Game(4,"תפוס את הנקודה","🎯","מהירות"));
            games.add(new Game(5,"נחש את המספר","🔢","חשיבה")); games.add(new Game(6,"חשבון מהיר","🧮","חשיבה"));
            games.add(new Game(7,"זיכרון צבעים","🧠","חשיבה")); games.add(new Game(8,"רצף מספרים","🔐","חשיבה"));
            games.add(new Game(9,"צבע או מילה","🌈","חשיבה")); games.add(new Game(10,"מצא את השונה","🔎","חשיבה"));
            games.add(new Game(11,"איקס עיגול","❌","שניים")); games.add(new Game(12,"אבן נייר מספריים","✊","שניים"));
            games.add(new Game(13,"פונג","🏓","ארקייד")); games.add(new Game(14,"תפוס מטבעות","🪙","ארקייד"));
            games.add(new Game(15,"התחמק","🚀","ארקייד")); games.add(new Game(16,"מבוך","🌀","ארקייד"));
            games.add(new Game(17,"קלף גבוה","🃏","חשיבה")); games.add(new Game(18,"זוגות","🃏","חשיבה"));
            games.add(new Game(19,"מספר מסתורי","🎲","חשיבה")); games.add(new Game(20,"אתגר דקה","🔥","מהירות"));
        }
        void goHome(){screen=0;running=false;h.removeCallbacksAndMessages(null);invalidate();}
        void bold(float s,int col){p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextSize(s);p.setColor(col);}
        void normal(float s,int col){p.setTypeface(Typeface.create("sans",Typeface.NORMAL));p.setTextSize(s);p.setColor(col);}
        void center(Canvas c,String s,float x,float y){c.drawText(s,x-p.measureText(s)/2,y,p);}
        void round(Canvas c,float l,float t,float r,float b,float rad,int col){p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRoundRect(l,t,r,b,rad,rad,p);}
        void circle(Canvas c,float x,float y,float r,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
        String bestFor(int id){return String.valueOf(prefs.getInt("best_"+id,0));}
        void saveBest(){int old=prefs.getInt("best_"+gameId,0);if(score>old)prefs.edit().putInt("best_"+gameId,score).apply();best=Math.max(old,score);}
        void button(Canvas c,float l,float t,float r,float b,String s,int col){round(c,l,t,r,b,20,col);bold(17,TXT);center(c,s,(l+r)/2,(t+b)/2+6);}
        @Override protected void onDraw(Canvas c){c.drawColor(BG);if(screen==0)home(c);else game(c);}
        void home(Canvas c){
            bold(31,TXT);c.drawText("GameBox",24,52,p);normal(14,MUTED);c.drawText("20 משחקים • אופליין • שיאים נשמרים",25,78,p);
            round(c,getWidth()-120,25,getWidth()-22,72,22,SUR2);bold(15,GOLD);center(c,"🏆",getWidth()-92,55);normal(13,TXT);c.drawText("שיאים",getWidth()-72,59,p);
            int y=98,w=(getWidth()-55)/2,col=0;
            for(Game g:games){
                float l=20+col*(w+15),r=l+w;
                round(c,l,y,r,y+108,22,SUR);bold(25,TXT);c.drawText(g.icon,l+15,y+37,p);bold(16,TXT);c.drawText(g.title,l+15,y+66,p);
                normal(11,MUTED);c.drawText(g.cat+"  •  "+bestFor(g.id),l+15,y+89,p);
                col++;if(col==2){col=0;y+=119;}
            }
        }
        void header(Canvas c,String title){round(c,16,18,67,68,18,SUR2);bold(28,TXT);center(c,"‹",41,53);bold(22,TXT);c.drawText(title,83,49,p);normal(12,MUTED);c.drawText("שיא "+best,84,69,p);}
        void game(Canvas c){
            String title="משחק";for(Game g:games)if(g.id==gameId)title=g.title;header(c,title);
            if(state==99){end(c,title);return;}
            switch(gameId){case 1:reaction(c);break;case 2:tap30(c);break;case 3:stop10(c);break;case 4:target(c);break;case 5:guess(c);break;case 6:math(c);break;case 7:memory(c);break;case 8:sequence(c);break;case 9:color(c);break;case 10:odd(c);break;case 11:ttt(c);break;case 12:rps(c);break;case 13:pong(c);break;case 14:catchGame(c);break;case 15:dodge(c);break;case 16:maze(c);break;case 17:highCard(c);break;case 18:pairs(c);break;case 19:mystery(c);break;default:minute(c);}
        }
        void end(Canvas c,String title){round(c,25,145,getWidth()-25,565,28,SUR);bold(29,TXT);center(c,"סיימת! 🎉",getWidth()/2,215);normal(17,MUTED);center(c,title,getWidth()/2,250);bold(52,GOLD);center(c,""+score,getWidth()/2,330);normal(15,MUTED);center(c,"השיא שלך: "+best,getWidth()/2,367);button(c,50,410,getWidth()-50,475,"שחק שוב",ACC);button(c,50,490,getWidth()-50,550,"חזרה",SUR2);}
        void reaction(Canvas c){if(state==0){normal(18,MUTED);center(c,"כשהמסך ירוק — לחץ!",getWidth()/2,190);button(c,45,270,getWidth()-45,345,"התחל",ACC);}else{round(c,30,145,getWidth()-30,getHeight()-110,28,state==2?GREEN:RED);bold(30,TXT);center(c,state==2?"עכשיו!":"חכה...",getWidth()/2,360);}}
        void tap30(Canvas c){if(state==0){normal(18,MUTED);center(c,"לחץ 30 פעמים כמה שיותר מהר",getWidth()/2,190);button(c,45,270,getWidth()-45,345,"התחל",ACC);return;}bold(24,TXT);center(c,taps+" / 30",getWidth()/2,185);circle(c,getWidth()/2,360,105,ACC);bold(31,TXT);center(c,"לחץ!",getWidth()/2,371);}
        void stop10(Canvas c){if(state==0){normal(18,MUTED);center(c,"עצור בדיוק על 10.00 שניות",getWidth()/2,190);button(c,45,270,getWidth()-45,345,"התחל",ACC);return;}long ms=(System.nanoTime()-start)/1000000;bold(30,TXT);center(c,String.format(Locale.US,"%.2f",ms/1000.0),getWidth()/2,220);button(c,45,300,getWidth()-45,375,"עצור!",GREEN);}
        void target(Canvas c){if(state==0){normal(18,MUTED);center(c,"תפוס 10 מטרות",getWidth()/2,190);button(c,45,270,getWidth()-45,345,"התחל",ACC);return;}circle(c,tx,ty,38,GOLD);circle(c,tx,ty,16,RED);bold(18,TXT);c.drawText("מטרות "+round+"/10",25,125,p);}
        void guess(Canvas c){if(state==0){normal(18,MUTED);center(c,"מצא מספר בין 1 ל־100",getWidth()/2,180);button(c,45,255,getWidth()-45,330,"התחל",ACC);return;}bold(32,TXT);center(c,input.length()==0?"?":input,getWidth()/2,205);normal(15,MUTED);center(c,message,getWidth()/2,240);String[] ns={"1","2","3","4","5","6","7","8","9","⌫","0","✓"};for(int i=0;i<12;i++){int co=i%3,ro=i/3;button(c,45+co*105,285+ro*70,130+co*105,340+ro*70,ns[i],i==11?GREEN:SUR2);}}
        void math(Canvas c){if(state==0){normal(18,MUTED);center(c,"פתור 10 תרגילים",getWidth()/2,190);button(c,45,270,getWidth()-45,345,"התחל",ACC);return;}bold(35,TXT);center(c,a+" + "+b+" = ?",getWidth()/2,205);normal(16,MUTED);center(c,"ניקוד "+score,getWidth()/2,245);for(int i=0;i<10;i++){int co=i%5,ro=i/5;button(c,22+co*72,290+ro*75,82+co*72,345+ro*75,String.valueOf(i==9?0:i+1),SUR2);}}
        void memory(Canvas c){normal(17,MUTED);center(c,"זכור את הצבעים",getWidth()/2,150);int[] cs={RED,GOLD,GREEN,ACC,Color.CYAN,Color.MAGENTA};for(int i=0;i<6;i++){float x=75+(i%3)*130,y=235+(i/3)*130;circle(c,x,y,45,(state==1&&i>=memPos)?SUR2:cs[i]);}normal(16,TXT);center(c,"רמה "+(memLen-1),getWidth()/2,520);}
        void sequence(Canvas c){if(state==0){normal(18,MUTED);center(c,"זכור את המספרים לפי הסדר",getWidth()/2,180);button(c,45,270,getWidth()-45,345,"התחל",ACC);return;}bold(35,TXT);center(c,""+seq[seqPos],getWidth()/2,205);normal(16,MUTED);center(c,"נכונים: "+score,getWidth()/2,245);for(int i=0;i<9;i++){int co=i%3,ro=i/3;button(c,35+co*112,290+ro*72,130+co*112,345+ro*72,String.valueOf(i+1),SUR2);}}
        void color(Canvas c){if(state==0){normal(18,MUTED);center(c,"בחר את צבע הטקסט",getWidth()/2,180);button(c,45,270,getWidth()-45,345,"התחל",ACC);return;}String[] n={"אדום","צהוב","ירוק","כחול","סגול"};int[] cs={RED,GOLD,GREEN,Color.CYAN,ACC};bold(35,cs[colorIndex]);center(c,n[(colorIndex+2)%5],getWidth()/2,220);for(int i=0;i<5;i++)button(c,18+i*74,315,88+i*74,375,n[i],cs[i]);}
        void odd(Canvas c){if(state==0){normal(18,MUTED);center(c,"מצא את הריבוע השונה",getWidth()/2,180);button(c,45,270,getWidth()-45,345,"התחל",ACC);return;}for(int i=0;i<9;i++){int co=i%3,ro=i/3;circle(c,70+co*135,260+ro*115,42,i==round%9?GREEN:SUR2);}}
        void ttt(Canvas c){normal(17,MUTED);center(c,playerTurn?"התור שלך":"המחשב חושב...",getWidth()/2,145);for(int i=0;i<9;i++){int co=i%3,ro=i/3;float l=55+co*105,t=190+ro*105;round(c,l,t,l+85,t+85,18,SUR);if(board[i]!=0){bold(34,board[i]==1?ACC:GOLD);center(c,board[i]==1?"X":"O",l+42,t+58);}}button(c,55,535,getWidth()-55,600,"משחק חדש",SUR2);}
        int winner(){int[][] q={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[] z:q)if(board[z[0]]!=0&&board[z[0]]==board[z[1]]&&board[z[1]]==board[z[2]])return board[z[0]];for(int z:board)if(z==0)return 0;return 3;}
        void rps(Canvas c){normal(18,MUTED);center(c,"בחר",getWidth()/2,170);button(c,20,245,135,335,"✊",SUR2);button(c,145,245,260,335,"✋",SUR2);button(c,270,245,385,335,"✌",SUR2);bold(20,TXT);center(c,message,getWidth()/2,410);normal(16,MUTED);center(c,"ניצחונות "+score,getWidth()/2,445);}
        void pong(Canvas c){round(c,30,150,getWidth()-30,590,25,SUR);circle(c,tx,ty,12,GOLD);round(c,catcherX-55,535,catcherX+55,555,10,ACC);bold(18,TXT);c.drawText("נקודות "+score,35,125,p);}
        void catchGame(Canvas c){round(c,30,150,getWidth()-30,590,25,SUR);circle(c,fallingX,fallingY,18,GOLD);round(c,catcherX-50,540,catcherX+50,565,12,ACC);bold(18,TXT);c.drawText("🪙 "+score+"  ❤️ "+lives,35,125,p);}
        void dodge(Canvas c){round(c,30,150,getWidth()-30,590,25,SUR);circle(c,catcherX,535,22,GREEN);circle(c,fallingX,fallingY,22,RED);bold(18,TXT);c.drawText("ניקוד "+score,35,125,p);}
        void maze(Canvas c){normal(17,MUTED);center(c,"העבר את הנקודה ליעד",getWidth()/2,135);round(c,30,160,getWidth()-30,590,25,SUR);for(int i=0;i<6;i++){p.setColor(SUR2);p.setStrokeWidth(16);c.drawLine(70+i*75,190,70+i*75,330,p);c.drawLine(70,240+i*55,getWidth()-70,240+i*55,p);}circle(c,75,190,18,GREEN);circle(c,getWidth()-75,540,18,GOLD);}
        void highCard(Canvas c){normal(18,MUTED);center(c,"שלוף שני קלפים. הגבוה מנצח",getWidth()/2,165);round(c,75,220,205,430,22,SUR2);round(c,220,220,350,430,22,SUR2);bold(48,TXT);center(c,a==0?"?":""+a,140,340);center(c,b==0?"?":""+b,285,340);button(c,75,470,350,535,"שלוף",ACC);normal(16,MUTED);center(c,"ניצחונות "+score,getWidth()/2,575);}
        void pairs(Canvas c){normal(17,MUTED);center(c,"מצא זוגות",getWidth()/2,145);for(int i=0;i<12;i++){int co=i%4,ro=i/4;float l=30+co*88,t=180+ro*95;round(c,l,t,l+70,t+75,16,SUR2);if(memory[i]==2){bold(22,TXT);center(c,"✓",l+35,t+48);}}}
        void mystery(Canvas c){normal(18,MUTED);center(c,"פתח תיבה וצבור נקודות",getWidth()/2,165);for(int i=0;i<6;i++){int co=i%3,ro=i/3;button(c,30+co*120,240+ro*120,125+co*120,325+ro*120,"?",SUR2);}bold(22,GOLD);center(c,"ניקוד "+score,getWidth()/2,525);}
        void minute(Canvas c){if(state==0){normal(18,MUTED);center(c,"כמה לחיצות ב־60 שניות?",getWidth()/2,190);button(c,45,270,getWidth()-45,345,"התחל",ACC);return;}long left=Math.max(0,60000-(System.currentTimeMillis()-start));bold(28,TXT);center(c,String.valueOf(left/1000),getWidth()/2,205);circle(c,getWidth()/2,370,105,ACC);bold(30,TXT);center(c,""+taps,getWidth()/2,382);}

        void startGame(int id){gameId=id;screen=1;state=0;score=0;round=0;message="";input="";running=true;best=prefs.getInt("best_"+id,0);h.removeCallbacksAndMessages(null);if(id==5)answer=1+rnd.nextInt(100);if(id==11){Arrays.fill(board,0);state=1;playerTurn=true;}if(id==13||id==14||id==15){state=1;catcherX=getWidth()/2;fallingY=180;start=System.currentTimeMillis();postLoop();}if(id==17)state=1;if(id==18){state=1;Arrays.fill(memory,0);}if(id==19)state=1;invalidate();}
        void finish(String msg){running=false;saveBest();state=99;message=msg;h.removeCallbacksAndMessages(null);invalidate();}
        void postLoop(){h.postDelayed(()->{if(running){updateLoop();invalidate();postLoop();}},30);}
        void updateLoop(){if(gameId==13){tx+=4;ty+=2;if(tx>getWidth()-70||tx<70)tx=getWidth()-tx; if(ty>520)ty=190;score=(int)((System.currentTimeMillis()-start)/1000);}if(gameId==14){fallingY+=5+score/10;if(fallingY>530){if(Math.abs(fallingX-catcherX)<70)score++;else lives--;fallingY=180;fallingX=55+rnd.nextInt(Math.max(1,getWidth()-110));if(lives<=0)finish("נגמרו החיים");}}if(gameId==15){fallingY+=7;if(fallingY>540){fallingY=180;fallingX=55+rnd.nextInt(Math.max(1,getWidth()-110));score++;}if(Math.abs(fallingX-catcherX)<40&&fallingY>495)finish("פגעת במכשול");}if(gameId==20&&System.currentTimeMillis()-start>=60000)finish("הזמן נגמר");}
        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(screen==0){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;int row=(int)((y-98)/119),col=x<getWidth()/2?0:1,idx=row*2+col;if(y>=98&&idx>=0&&idx<games.size())startGame(games.get(idx).id);return true;}
            if(y<80&&x<75&&e.getAction()==MotionEvent.ACTION_DOWN){goHome();return true;}
            if(state==99&&e.getAction()==MotionEvent.ACTION_DOWN){if(y>400&&y<485)startGame(gameId);else if(y>485)goHome();return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE&&gameId>=13&&gameId<=15){catcherX=(int)x;return true;}
            if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;
            switch(gameId){case 1:reactionTouch();break;case 2:tapTouch(y);break;case 3:stopTouch(y);break;case 4:targetTouch(x,y);break;case 5:guessTouch(x,y);break;case 6:mathTouch(x,y);break;case 7:memoryTouch(x,y);break;case 8:sequenceTouch(x,y);break;case 9:colorTouch(x);break;case 10:oddTouch(x,y);break;case 11:tttTouch(x,y);break;case 12:rpsTouch(x,y);break;case 13:catcherX=(int)x;break;case 14:catcherX=(int)x;break;case 15:catcherX=(int)x;break;case 16:mazeTouch(x,y);break;case 17:highTouch(y);break;case 18:pairsTouch(x,y);break;case 19:mysteryTouch(x,y);break;case 20:minuteTouch(y);break;}invalidate();return true;
        }
        void reactionTouch(){if(state==0){state=1;h.postDelayed(()->{if(running&&gameId==1){state=2;start=System.nanoTime();invalidate();}},800+rnd.nextInt(2200));}else if(state==1)finish("לחצת מוקדם!");else{score=(int)((System.nanoTime()-start)/1000000);finish(score<250?"מהיר מאוד!":"יפה!");}}
        void tapTouch(float y){if(state==0){state=1;taps=0;}else if(y>250){taps++;score=taps;if(taps>=30)finish("30 לחיצות!");}}
        void stopTouch(float y){if(state==0){state=1;start=System.nanoTime();}else if(y>270){long ms=(System.nanoTime()-start)/1000000;score=(int)Math.max(0,1000-Math.abs(ms-10000));finish("סטייה "+String.format(Locale.US,"%.2f",Math.abs(ms-10000)/1000.0)+" שניות");}}
        void targetTouch(float x,float y){if(state==0){state=1;round=0;tx=70+rnd.nextInt(Math.max(1,getWidth()-140));ty=220+rnd.nextInt(300);}else if(Math.hypot(x-tx,y-ty)<65){round++;score+=100;tx=70+rnd.nextInt(Math.max(1,getWidth()-140));ty=220+rnd.nextInt(300);if(round>=10)finish("10 מטרות!");}}
        void guessTouch(float x,float y){if(state==0){state=1;return;}if(y<280)return;int i=(int)((x-45)/105)+(int)((y-285)/70)*3;if(i<0||i>=12)return;if(i==9){if(input.length()>0)input=input.substring(0,input.length()-1);return;}if(i==11){if(input.length()==0)return;int n=Integer.parseInt(input);score++;if(n==answer)finish("ניחשת!");else{message=n<answer?"יותר גבוה ↑":"יותר נמוך ↓";input="";if(score>=10)finish("נגמרו 10 ניסיונות");}return;}int n=i==10?0:i+1;if(input.length()<3)input+=n;}
        void nextMath(){a=1+rnd.nextInt(20);b=1+rnd.nextInt(20);}
        void mathTouch(float x,float y){if(state==0){state=1;round=0;score=0;nextMath();return;}int co=(int)(x/72),ro=(int)((y-290)/75);if(co<0||co>4||ro<0||ro>1)return;int val=ro==0?co+1:(co==4?0:co+6);input+=val;int ans=a+b;if(Integer.parseInt(input)==ans){score+=100;round++;input="";if(round>=10)finish("10 מתוך 10!");else nextMath();}else if(input.length()>=2){input="";round++;if(round>=10)finish("סיימת");else nextMath();}}
        void memoryTouch(float x,float y){if(state==0){state=1;memPos=0;for(int i=0;i<6;i++)memory[i]=i;h.postDelayed(()->{if(running){state=2;invalidate();}},1200);return;}if(state==2){int co=Math.max(0,Math.min(2,(int)((x-30)/130))),ro=Math.max(0,Math.min(1,(int)((y-180)/130))),pick=ro*3+co;if(pick==memPos){memPos++;score+=100;if(memPos>=3){memLen++;memPos=0;if(memLen>6){finish("זיכרון מעולה!");return;}state=1;h.postDelayed(()->{if(running){state=2;invalidate();}},900);}}else finish("טעות");}}
        void sequenceTouch(float x,float y){if(state==0){state=1;seqPos=0;for(int i=0;i<seq.length;i++)seq[i]=1+rnd.nextInt(9);h.postDelayed(()->{if(running){state=2;invalidate();}},900);return;}if(state==2){int co=Math.max(0,Math.min(2,(int)((x-35)/112))),ro=Math.max(0,Math.min(2,(int)((y-290)/72))),n=ro*3+co+1;if(n==seq[seqPos]){seqPos++;score++;if(seqPos>=seqLen){seqLen++;seqPos=0;if(seqLen>8){finish("הרצף הושלם!");return;}state=1;h.postDelayed(()->{if(running){state=2;invalidate();}},900);}}else finish("טעות ברצף");}}
        void colorTouch(float x){if(state==0){state=1;colorIndex=rnd.nextInt(5);return;}int chosen=Math.max(0,Math.min(4,(int)(x/74)));if(chosen==colorIndex){score+=10;colorIndex=rnd.nextInt(5);}else finish("בחירה לא נכונה");}
        void oddTouch(float x,float y){if(state==0){state=1;round=0;return;}int col=(int)((x-25)/135),ro=(int)((y-210)/115),pick=ro*3+col;if(pick==round%9){score+=100;round++;if(round>=10)finish("מצאת 10!");}else finish("זה לא היה השונה");}
        void tttTouch(float x,float y){if(y>535){Arrays.fill(board,0);playerTurn=true;score=0;state=1;return;}if(!playerTurn)return;int col=(int)((x-55)/105),ro=(int)((y-190)/105),i=ro*3+col;if(i>=0&&i<9&&board[i]==0){board[i]=1;int w=winner();if(w!=0){score=w==1?10:5;finish(w==1?"ניצחת!":"תיקו");return;}playerTurn=false;h.postDelayed(()->computerMove(),250);}}
        void computerMove(){ArrayList<Integer> f=new ArrayList<>();for(int i=0;i<9;i++)if(board[i]==0)f.add(i);if(!f.isEmpty())board[f.get(rnd.nextInt(f.size()))]=2;int w=winner();if(w!=0)finish(w==1?"ניצחת!":w==3?"תיקו":"המחשב ניצח");playerTurn=true;invalidate();}
        void rpsTouch(float x,float y){if(y<230||y>360)return;int me=x<135?0:x<260?1:2,op=rnd.nextInt(3),res=(me-op+3)%3;if(res==1){score++;message="ניצחת!";}else if(res==0)message="תיקו";else{message="הפסדת";if(score>0)score--;}}
        void mazeTouch(float x,float y){if(state==0){state=1;return;}if(x>getWidth()-120&&y>490){score=100;finish("הגעת ליעד!");}else score++;}
        void highTouch(float y){if(y<450)return;a=1+rnd.nextInt(13);b=1+rnd.nextInt(13);if(a>b)score++;else if(a<b&&score>0)score--;if(score>=10)finish("10 ניצחונות!");}
        void pairsTouch(float x,float y){int co=(int)((x-30)/88),ro=(int)((y-180)/95),i=ro*4+co;if(i<0||i>=12)return;if(memory[i]==2)return;memory[i]=2;score+=10;if(score>=60)finish("כל הזוגות נמצאו!");}
        void mysteryTouch(float x,float y){int co=(int)(x/120),ro=(int)((y-240)/120);if(co<0||co>2||ro<0||ro>1)return;score+=1+rnd.nextInt(100);if(score>=300)finish("הגעת ל־300!");}
        void minuteTouch(float y){if(state==0){state=1;taps=0;start=System.currentTimeMillis();postLoop();}else if(y>250)taps++;}
    }
}
