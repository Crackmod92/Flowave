package com.crackmod.flowave.domain.exceptions

import android.app.PendingIntent

/**
 * Кастомное исключение для передачи запроса на разрешение
 * из слоя данных в слой UI.
 */
class RecoverablePermissionException(
    val intent: PendingIntent,
    message: String
) : Exception(message)