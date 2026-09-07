package com.juanitos.lib.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.juanitos.R
import com.juanitos.lib.SavingsGoalProjection
import java.util.Locale

const val SAVINGS_GOAL_RISK_CHANNEL_ID = "savings_goal_risk"

/** Creates the risk-notification channel. Safe to call repeatedly (Android no-ops if unchanged). */
fun createSavingsGoalNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        SAVINGS_GOAL_RISK_CHANNEL_ID,
        context.getString(R.string.savings_goal_risk_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = context.getString(R.string.savings_goal_risk_channel_description)
    }
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}

/**
 * Posts the "at risk of missing your savings goal" notification for [accountName]. The caller is
 * responsible for checking [android.Manifest.permission.POST_NOTIFICATIONS] is granted (a no-op
 * check below API 33) and for deduping via [com.juanitos.data.money.entities.SavingsGoal.lastRiskNotifiedMonth].
 */
fun sendSavingsGoalRiskNotification(
    context: Context,
    notificationId: Int,
    accountName: String,
    projection: SavingsGoalProjection,
) {
    val notification = NotificationCompat.Builder(context, SAVINGS_GOAL_RISK_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(
            context.getString(R.string.savings_goal_risk_notification_title, accountName)
        )
        .setContentText(
            context.getString(
                R.string.savings_goal_risk_notification_text,
                String.format(Locale.US, "%.2f€", projection.projected),
                String.format(Locale.US, "%.2f€", projection.target),
            )
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(notificationId, notification)
}
