package com.huawei.healthkit.stepcounter

import android.content.Context
import com.huawei.hms.hihealth.DataController
import com.huawei.hms.hihealth.HiHealthOptions
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.DataType
import com.huawei.hms.hihealth.data.Field
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StepsDataController(
    context: Context,
    val printToLog: (String) -> Unit = { }
) {
    var steps = 0
    var isActive: Boolean = false
    private val dataController = HuaweiHiHealth.getDataController(context)

    fun start() {
        isActive = true
        CoroutineScope(IO).launch {
            while(isActive) {
                dataController.readLatestData(listOf(DataType.DT_CONTINUOUS_STEPS_DELTA))
                    .addOnSuccessListener {
                        it.values.forEach { value ->
                            steps += value.getFieldValue(Field.FIELD_STEPS_DELTA).toString().toInt()
                            printToLog("steps = $steps")
                        }
                    }
                    .addOnFailureListener {
                        printToLog("$it")
                    }
                delay(1000)
            }
        }
    }
    fun stop() {
        isActive = false
    }

    companion object {
        const val TAG = "StepsController"
    }
}