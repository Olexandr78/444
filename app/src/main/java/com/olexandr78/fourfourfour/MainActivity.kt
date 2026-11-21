package com.olexandr78.fourfourfour

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
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
 night.addView(btn("Включить ночной вызов · 03:00",::enable),LinearLayout.LayoutParams(-1,dp(48)));gap(night,6);night.addView(btn("Отключить",::disable),LinearLayout.LayoutParams(-1,dp(48)));root.addView(night)
 gap(root,14);val about=card();about.addView(txt("Информация",15f,ink,true));gap(about,6);about.addView(txt("Очистка работает только с кэшем 444. Android не позволяет обычным приложениям тайно удалять данные других программ. Ночной сценарий включается вручную и отключается здесь.",12f,muted));root.addView(about)
 }
 private fun refresh(bar:ProgressBar){try{val s=StatFs(android.os.Environment.getDataDirectory().path);val total=s.totalBytes;val used=total-s.availableBytes;storage.text="${DecimalFormat("0.0").format(used/1e9)} ГБ / ${DecimalFormat("0.0").format(total/1e9)} ГБ";bar.progress=(used*100/total).toInt()}catch(_:Exception){storage.text="Недоступно"}}
 private fun enable(){if(Build.VERSION.SDK_INT>=31){val a=getSystemService(ALARM_SERVICE) as AlarmManager;if(!a.canScheduleExactAlarms()){startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));Toast.makeText(this,"Разреши точные будильники и нажми кнопку ещё раз",Toast.LENGTH_LONG).show();return}}
 if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),2);return}
 getSharedPreferences("444",0).edit().putBoolean("enabled",true).apply();schedule();Toast.makeText(this,"Включено: ближайшие 03:00",Toast.LENGTH_LONG).show()}
 private fun disable(){getSharedPreferences("444",0).edit().putBoolean("enabled",false).apply();cancelAlarm();(getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(444);Toast.makeText(this,"Ночной вызов отключён",Toast.LENGTH_SHORT).show()}
 private fun schedule(){val i=Intent(this,NightReceiver::class.java);val p=PendingIntent.getBroadcast(this,444,i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);val c=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,3);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1)};(getSystemService(ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,c.timeInMillis,p)}
 private fun cancelAlarm(){val p=PendingIntent.getBroadcast(this,444,Intent(this,NightReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);(getSystemService(ALARM_SERVICE) as AlarmManager).cancel(p)}
}

class NightReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,i:Intent?){if(!c.getSharedPreferences("444",0).getBoolean("enabled",false))return
 val nm=c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;val ch="444_calls"
 if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(NotificationChannel(ch,"Входящий вызов",NotificationManager.IMPORTANCE_HIGH).apply{lockscreenVisibility=Notification.VISIBILITY_PUBLIC;enableVibration(true)})
 val open=PendingIntent.getActivity(c,1,Intent(c,CallActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 val no=PendingIntent.getBroadcast(c,2,Intent(c,DeclineReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 val yes=PendingIntent.getActivity(c,3,Intent(c,CallActivity::class.java).putExtra("answer",true),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 val b=Notification.Builder(c,ch).setSmallIcon(android.R.drawable.sym_call_incoming).setContentTitle("444").setContentText("Входящий вызов").setCategory(Notification.CATEGORY_CALL).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true).setContentIntent(open).setFullScreenIntent(open,true)
 if(Build.VERSION.SDK_INT>=31)b.setStyle(Notification.CallStyle.forIncomingCall(Person.Builder().setName("444").build(),no,yes)) else {b.addAction(0,"Отклонить",no);b.addAction(0,"Ответить",yes)}
 nm.notify(444,b.build());val next=Intent(c,NightReceiver::class.java);val p=PendingIntent.getBroadcast(c,444,next,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);val cal=Calendar.getInstance().apply{add(Calendar.DAY_OF_YEAR,1);set(Calendar.HOUR_OF_DAY,3);set(Calendar.MINUTE,0);set(Calendar.SECOND,0)};(c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.timeInMillis,p)
 }
}
class DeclineReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent?){(c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(444)}}
class CallActivity:Activity(){
 private var ring:Ringtone?=null;private var vib:Vibrator?=null
 override fun onCreate(b:Bundle?){super.onCreate(b);window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);window.statusBarColor=Color.rgb(25,30,38);window.navigationBarColor=Color.rgb(25,30,38)
 val root=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER;setPadding(28,40,28,40);setBackgroundColor(Color.rgb(25,30,38))};setContentView(root)
 val state=TextView(this).apply{text=if(intent.getBooleanExtra("answer",false))"Вызов соединён" else "Входящий вызов";textSize=17f;gravity=Gravity.CENTER;setTextColor(Color.LTGRAY)}
 root.addView(TextView(this).apply{text="444";textSize=40f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD});root.addView(state)
 val spacer=View(this);root.addView(spacer,LinearLayout.LayoutParams(1,0,1f));val row=LinearLayout(this).apply{gravity=Gravity.CENTER}
 row.addView(Button(this).apply{text="Отклонить";setOnClickListener{closeCall()}})
 row.addView(Button(this).apply{text="Ответить";setOnClickListener{stopSound();state.text="Вызов соединён";root.removeView(row);root.addView(TextView(this@CallActivity).apply{text="Ты всё-таки ответил.";textSize=22f;gravity=Gravity.CENTER;setTextColor(Color.WHITE)})}});root.addView(row)
 if(!intent.getBooleanExtra("answer",false))startSound()
 }
 private fun startSound(){try{val u=RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE)?:RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);ring=RingtoneManager.getRingtone(this,u)?.also{if(Build.VERSION.SDK_INT>=28)it.isLooping=true;it.play()}}catch(_:Exception){}
 vib=if(Build.VERSION.SDK_INT>=31)(getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator else @Suppress("DEPRECATION") (getSystemService(VIBRATOR_SERVICE) as Vibrator)
 try{val p=longArrayOf(0,700,500,700,1000);if(Build.VERSION.SDK_INT>=26)vib?.vibrate(VibrationEffect.createWaveform(p,0))else @Suppress("DEPRECATION") vib?.vibrate(p,0)}catch(_:Exception){}}
 private fun stopSound(){try{ring?.stop()}catch(_:Exception){};ring=null;vib?.cancel()}
 private fun closeCall(){stopSound();(getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(444);finish()}
 override fun onDestroy(){stopSound();super.onDestroy()}
}
