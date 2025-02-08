package com.example.chat.firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chat.R
import com.example.chat.actividades.ChatActivity
import com.example.chat.models.Usuario
import com.example.chat.utils.Constantes
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MensajesServicio : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val usuario = Usuario()
        usuario.id = remoteMessage.data[Constantes.KEY_ID_USUARIO]
        usuario.nombre = remoteMessage.data[Constantes.KEY_NOMBRE]
        usuario.token = remoteMessage.data[Constantes.KEY_FCM_TOKEN]

        val notificationId = Random().nextInt()
        val channelId = "chat_message"

        val intent = Intent(this, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(Constantes.KEY_USUARIO, usuario)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_notificacion)
        builder.setContentTitle(usuario.nombre)
        builder.setContentText(remoteMessage.data[Constantes.KEY_MENSAJE])
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.data[Constantes.KEY_MENSAJE]))
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Chat Message"
            val channelDescription = "This notification channel is used for chat message notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManagerCompat.notify(notificationId, builder.build())
    }
}