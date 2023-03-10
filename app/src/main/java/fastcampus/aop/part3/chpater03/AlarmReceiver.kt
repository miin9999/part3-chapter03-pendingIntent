package fastcampus.aop.part3.chpater03

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver: BroadcastReceiver() {


    companion object{
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL_ID = "1000"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Broadcast에 우리가 준 펜딩 인텐트가 수신이 되었을 때 오는 콜백 함수

        createNotificationChannel(context)
        notifyNotification(context)
    }
    private fun createNotificationChannel(context: Context){

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ // O버전이 26
            // 세번째 인자는 이 noti가 얼마나 중요한 것인지에 대한 인자
            // 보통 default를 쓰긴하는데 진동,무음모드에서는 알람이 다르게 동작할 수 있음

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,"기상 알람",
                NotificationManager.IMPORTANCE_HIGH
            )

            NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)

        }

    }

    private fun notifyNotification(context: Context){
        with(NotificationManagerCompat.from(context)){
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("알람")
                .setContentText("일어날 시간입니다.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            notify(NOTIFICATION_ID,build.build())
        }
    }




}