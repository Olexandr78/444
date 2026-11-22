package com.olexandr78.fourfourfour

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import java.text.DecimalFormat
import java.util.Calendar

class MainActivity : Activity() {
 private val blue=Color.rgb(35,116,220); private val ink=Color.rgb(30,34,42); private val muted=Color.rgb(112,120,132)
 private lateinit var root:LinearLayout; private lateinit var storage:TextView; private lateinit var result:TextView
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 private fun txt(s:String,n:Float,c:Int=ink,b:Boolean=false)=TextView(this).apply{text=s;textSize=n;setTextColor(c);if(b)typeface=Typeface.DEFAULT_BOLD}
 private fun card()=LinearLayout(this).apply{orientation=1;setPadding(dp(18),dp(16),dp(18),dp(16));background=GradientDrawable().apply{setColor(Color.WHITE);cornerRadius=dp(18).toFloat()};elevation=dp(2).toFloat()}
 private fun gap(p:LinearLayout,h:Int){p.addView(View(this),LinearLayout.LayoutParams(1,dp(h)))}
 private fun btn(s:String,fn:()->Unit)=Button(this).apply{text=s;isAllCaps=false;setTextColor(Color.WHITE);background=GradientDrawable().apply{setColor(blue);cornerRadius=dp(12).toFloat()};setOnClickListener{fn()}}
 override fun onCreate(b:Bundle?){super.onCreate(b);window.statusBarColor=Color.rgb(246,248,251);window.navigationBarColor=Color.WHITE;window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
 if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
 root=LinearLayout(this).apply{orientation=1;setPadding(dp(18),dp(18),dp(18),dp(24));setBackgroundColor(Color.rgb(246,248,251))}
 setContentView(ScrollView(this).apply{addView(root)})
 root.addView(txt("手机管家",27f,ink,true));root.addView(txt("设备清理 · 存储管理",13f,muted));gap(root,18)
 val disk=card();disk.addView(txt("内部存储空间",16f,ink,true));gap(disk,8);storage=txt("Загрузка…",25f,ink,true);disk.addView(storage)
 val bar=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{max=100;progressTintList=android.content.res.ColorStateList.valueOf(blue)}
 disk.addView(bar,LinearLayout.LayoutParams(-1,dp(8)));gap(disk,8);disk.addView(txt("Данные файловой системы Android",11f,muted));root.addView(disk);refresh(bar)
 gap(root,14);val clean=card();clean.addView(txt("Очистка устройства",18f,ink,true));gap(clean,5);clean.addView(txt("Проверка временных файлов и состояния приложения.",13f,muted));gap(clean,10);result=txt("Готово к проверке",13f,muted);clean.addView(result);gap(clean,8);clean.addView(btn("Сканировать",{
 val bytes=cacheDir.walkTopDown().filter{it.isFile}.sumOf{it.length()};result.text="Найдено ${DecimalFormat("0.0").format(bytes/1024.0)} КБ кэша приложения"
 }),LinearLayout.LayoutParams(-1,dp(48)));root.addView(clean)
 gap(root,14);val night=card();night.addView(txt("Ночной вызов",18f,ink,true));gap(night,5);night.addView(txt("Сценарий на 03:00. После включения Android покажет входящий CallStyle-вызов, будет использован системный рингтон и вибрация.",13f,muted));gap(night,10)
 night.addView(btn("Включить ночной вызов · 03:00",::enable),LinearLayout.LayoutParams(-1,dp(48)));gap(night,6);night.addView(btn("Тестовый вызов через 1 минуту",::testCall),LinearLayout.LayoutParams(-1,dp(48)));gap(night,6);night.addView(btn("Отключить",::disable),LinearLayout.LayoutParams(-1,dp(48)));root.addView(night)
 gap(root,14);val about=card();about.addView(txt("Информация",15f,ink,true));gap(about,6);about.addView(txt("Очистка работает только с кэшем 444. Android не позволяет обычным приложениям тайно удалять данные других программ. Ночной сценарий включается вручную и отключается здесь.",12f,muted));root.addView(about)
 }
 private fun refresh(bar:ProgressBar){try{val s=StatFs(android.os.Environment.getDataDirectory().path);val total=s.totalBytes;val used=total-s.availableBytes;storage.text="${DecimalFormat("0.0").format(used/1e9)} ГБ / ${DecimalFormat("0.0").format(total/1e9)} ГБ";bar.progress=(used*100/total).toInt()}catch(_:Exception){storage.text="Недоступно"}}
 private fun enable(){if(Build.VERSION.SDK_INT>=31){val a=getSystemService(ALARM_SERVICE) as AlarmManager;if(!a.canScheduleExactAlarms()){startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));Toast.makeText(this,"Разреши точные будильники и нажми кнопку ещё раз",Toast.LENGTH_LONG).show();return}}
 if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),2);return}
 getSharedPreferences("444",0).edit().putBoolean("enabled",true).apply();schedule();Toast.makeText(this,"Включено: ближайшие 03:00",Toast.LENGTH_LONG).show()}
 private fun testCall(){if(Build.VERSION.SDK_INT>=31){val a=getSystemService(ALARM_SERVICE) as AlarmManager;if(!a.canScheduleExactAlarms()){startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));Toast.makeText(this,"Разреши точные будильники, затем нажми тест ещё раз",Toast.LENGTH_LONG).show();return}}
 if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),3);return}
 val i=Intent(this,NightReceiver::class.java).putExtra("test",true);val p=PendingIntent.getBroadcast(this,445,i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);(getSystemService(ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+60_000,p);Toast.makeText(this,"Тестовый вызов запланирован через 1 минуту",Toast.LENGTH_LONG).show()}
 private fun disable(){getSharedPreferences("444",0).edit().putBoolean("enabled",false).apply();cancelAlarm();(getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(444);Toast.makeText(this,"Ночной вызов отключён",Toast.LENGTH_SHORT).show()}
 private fun schedule(){val i=Intent(this,NightReceiver::class.java);val p=PendingIntent.getBroadcast(this,444,i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);val c=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,3);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1)};(getSystemService(ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,c.timeInMillis,p)}
 private fun cancelAlarm(){val p=PendingIntent.getBroadcast(this,444,Intent(this,NightReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);(getSystemService(ALARM_SERVICE) as AlarmManager).cancel(p)}
}

class NightReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,i:Intent?){val test=i?.getBooleanExtra("test",false)==true;if(!test&&!c.getSharedPreferences("444",0).getBoolean("enabled",false))return
 val nm=c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;val ch="444_calls"
 if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(NotificationChannel(ch,"Входящий вызов",NotificationManager.IMPORTANCE_HIGH).apply{lockscreenVisibility=Notification.VISIBILITY_PUBLIC;enableVibration(true)})
 val open=PendingIntent.getActivity(c,1,Intent(c,CallActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 val no=PendingIntent.getBroadcast(c,2,Intent(c,DeclineReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 val yes=PendingIntent.getActivity(c,3,Intent(c,CallActivity::class.java).putExtra("answer",true),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 val b=Notification.Builder(c,ch).setSmallIcon(android.R.drawable.sym_call_incoming).setContentTitle("444").setContentText("Входящий вызов").setCategory(Notification.CATEGORY_CALL).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true).setContentIntent(open).setFullScreenIntent(open,true)
 if(Build.VERSION.SDK_INT>=31)b.setStyle(Notification.CallStyle.forIncomingCall(Person.Builder().setName("444").build(),no,yes)) else {b.addAction(0,"Отклонить",no);b.addAction(0,"Ответить",yes)}
 nm.notify(444,b.build());if(test)return;val next=Intent(c,NightReceiver::class.java);val p=PendingIntent.getBroadcast(c,444,next,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);val cal=Calendar.getInstance().apply{add(Calendar.DAY_OF_YEAR,1);set(Calendar.HOUR_OF_DAY,3);set(Calendar.MINUTE,0);set(Calendar.SECOND,0)};(c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.timeInMillis,p)
 }
}
class DeclineReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent?){(c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(444)}}
class CallActivity:Activity(){
 private var ring:Ringtone?=null
 private var vib:Vibrator?=null
 private val handler=Handler(Looper.getMainLooper())
 private var connectedAt=0L
 private var muted=false
 private var speaker=false
 private lateinit var root:LinearLayout
 private lateinit var timer:TextView
 private val timerTick=object:Runnable{
  override fun run(){
   if(connectedAt==0L)return
   val seconds=((SystemClock.elapsedRealtime()-connectedAt)/1000).toInt()
   timer.text=String.format("%02d:%02d",seconds/60,seconds%60)
   handler.postDelayed(this,1000)
  }
 }
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 private fun label(s:String,size:Float,color:Int,bold:Boolean=false)=TextView(this).apply{
  text=s;textSize=size;setTextColor(color);gravity=Gravity.CENTER
  if(bold)typeface=Typeface.DEFAULT_BOLD
 }
 private fun circle(color:Int)=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(color)}
 private fun actionButton(text:String,color:Int,onClick:()->Unit)=TextView(this).apply{
  this.text=text;textSize=15f;gravity=Gravity.CENTER;setTextColor(Color.WHITE)
  background=circle(color);isClickable=true;isFocusable=true;setOnClickListener{onClick()}
 }
 private fun imageButton(asset:String,x:Int,y:Int,w:Int,h:Int,round:Boolean,onClick:()->Unit):ImageView{
  val encoded=assets.open(asset).bufferedReader().use{it.readText()}
  val bytes=Base64.decode(encoded,Base64.DEFAULT)
  val source=BitmapFactory.decodeByteArray(bytes,0,bytes.size)
  val crop=Bitmap.createBitmap(source,x,y,w,h)
  return ImageView(this).apply{
   setImageBitmap(crop);scaleType=ImageView.ScaleType.FIT_XY
   background=GradientDrawable().apply{
    shape=if(round)GradientDrawable.OVAL else GradientDrawable.RECTANGLE
    if(!round)cornerRadius=dp(28).toFloat()
   }
   clipToOutline=true;isClickable=true;isFocusable=true;setOnClickListener{onClick()}
  }
 }
 override fun onCreate(b:Bundle?){
  super.onCreate(b)
  window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  val answered=intent.getBooleanExtra("answer",false)
  if(answered)showActive() else showIncoming()
 }
 private fun base(bg:Int,lightBars:Boolean){
  window.statusBarColor=bg;window.navigationBarColor=bg
  window.decorView.systemUiVisibility=if(lightBars)View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
  root=LinearLayout(this).apply{
   orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
   setPadding(dp(24),dp(28),dp(24),dp(24));setBackgroundColor(bg)
  }
  setContentView(root)
 }
 private fun showIncoming(){
  val bg=Color.rgb(247,235,246)
  base(bg,true)
  root.addView(label("ВХОДЯЩИЙ ВЫЗОВ",13f,Color.rgb(112,99,112)))
  val space=View(this);root.addView(space,LinearLayout.LayoutParams(1,0,0.7f))
  val avatar=FrameLayout(this).apply{
   background=circle(Color.rgb(191,160,190))
   elevation=dp(4).toFloat()
  }
  avatar.addView(label("444",38f,Color.WHITE,true),FrameLayout.LayoutParams(dp(132),dp(132),Gravity.CENTER))
  root.addView(avatar,LinearLayout.LayoutParams(dp(132),dp(132)))
  val spacer=View(this);root.addView(spacer,LinearLayout.LayoutParams(1,dp(26)))
  root.addView(label("444",34f,Color.rgb(42,34,43)))
  root.addView(label("Неизвестный номер",16f,Color.rgb(111,99,111)))
  val mid=View(this);root.addView(mid,LinearLayout.LayoutParams(1,0,0.9f))
  val buttons=LinearLayout(this).apply{gravity=Gravity.CENTER;orientation=LinearLayout.HORIZONTAL}
  val decline=imageButton("call_incoming.jpg.b64",53,613,64,64,true){closeCall()}
  val answer=imageButton("call_incoming.jpg.b64",254,613,64,64,true){connectCall()}
  buttons.addView(decline,LinearLayout.LayoutParams(dp(76),dp(76)).apply{marginEnd=dp(76)})
  buttons.addView(answer,LinearLayout.LayoutParams(dp(76),dp(76)))
  root.addView(buttons)
  val captions=LinearLayout(this).apply{gravity=Gravity.CENTER;orientation=LinearLayout.HORIZONTAL}
  captions.addView(label("Отклонить",13f,Color.rgb(62,54,62)),LinearLayout.LayoutParams(dp(152),dp(38)))
  captions.addView(label("Ответить",13f,Color.rgb(62,54,62)),LinearLayout.LayoutParams(dp(152),dp(38)))
  root.addView(captions)
  if(Build.VERSION.SDK_INT>=21)root.addView(label(" ",10f,Color.GRAY))
  startSound()
 }
 private fun connectCall(){
  stopSound()
  (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(444)
  connectedAt=SystemClock.elapsedRealtime()
  showActive()
 }
 private fun showActive(){
  val bg=Color.rgb(35,20,16)
  base(bg,false)
  root.addView(label("00:00",16f,Color.rgb(220,205,197)).also{timer=it})
  root.addView(label("444",38f,Color.WHITE))
  root.addView(label("Мобильный вызов",14f,Color.rgb(190,174,166)))
  val spacer=View(this);root.addView(spacer,LinearLayout.LayoutParams(1,0,1f))
  val controls=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER}
  fun control(title:String,x:Int,click:()->Unit){
   val col=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER}
   val button=imageButton("call_active.jpg.b64",x,574,72,72,true,click)
   col.addView(button,LinearLayout.LayoutParams(dp(66),dp(66)))
   col.addView(label(title,12f,Color.WHITE),LinearLayout.LayoutParams(dp(76),dp(34)))
   controls.addView(col,LinearLayout.LayoutParams(0,dp(108),1f))
  }
  control("Клавиатура",24){Toast.makeText(this,"Клавиатура вызова",Toast.LENGTH_SHORT).show()}
  control("Микрофон",108){muted=!muted;Toast.makeText(this,if(muted)"Микрофон выключен" else "Микрофон включён",Toast.LENGTH_SHORT).show()}
  control("Динамик",192){speaker=!speaker;Toast.makeText(this,if(speaker)"Динамик включён" else "Динамик выключен",Toast.LENGTH_SHORT).show()}
  control("Ещё",275){Toast.makeText(this,"Дополнительные действия",Toast.LENGTH_SHORT).show()}
  root.addView(controls)
  val gap=View(this);root.addView(gap,LinearLayout.LayoutParams(1,dp(28)))
  val end=imageButton("call_active.jpg.b64",105,707,162,59,false){closeCall()}
  root.addView(end,LinearLayout.LayoutParams(dp(144),dp(62)))
  if(connectedAt==0L)connectedAt=SystemClock.elapsedRealtime()
  handler.removeCallbacks(timerTick);timerTick.run()
 }
 private fun startSound(){
  try{
   val u=RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE)?:RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
   ring=RingtoneManager.getRingtone(this,u)?.also{if(Build.VERSION.SDK_INT>=28)it.isLooping=true;it.play()}
  }catch(_:Exception){}
  vib=if(Build.VERSION.SDK_INT>=31)(getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator else @Suppress("DEPRECATION") (getSystemService(VIBRATOR_SERVICE) as Vibrator)
  try{
   val p=longArrayOf(0,700,500,700,1000)
   if(Build.VERSION.SDK_INT>=26)vib?.vibrate(VibrationEffect.createWaveform(p,0))else @Suppress("DEPRECATION") vib?.vibrate(p,0)
  }catch(_:Exception){}
 }
 private fun stopSound(){
  try{ring?.stop()}catch(_:Exception){}
  ring=null;vib?.cancel()
 }
 private fun closeCall(){
  stopSound();handler.removeCallbacks(timerTick)
  (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(444)
  finish()
 }
 override fun onDestroy(){
  stopSound();handler.removeCallbacks(timerTick);super.onDestroy()
 }
}
