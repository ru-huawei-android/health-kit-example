package com.huawei.healthkit.stepcounter

import android.content.Context
import com.huawei.hmf.tasks.Task
import com.huawei.hms.hihealth.AutoRecorderController
import com.huawei.hms.hihealth.HiHealthOptions
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.*
import com.huawei.hms.hihealth.options.OnSamplePointListener
import com.huawei.hms.support.hwid.HuaweiIdAuthManager

class StepsController(
    context: Context,
    onStepsChanged: (steps: Int) -> Unit,
    val printToLog: (String) -> Unit = { }
) {
    var isActive: Boolean
    private val autoRecorderController: AutoRecorderController
    private val onSamplePointListener = OnSamplePointListener { samplePoint ->
        printToLog("steps = ${samplePoint.getFieldValue(Field.FIELD_STEPS)}")
        val stepsCount = samplePoint
            .getFieldValue(Field.FIELD_STEPS)
            .asIntValue()
        onStepsChanged(stepsCount)
    }

    init {
        isActive = false
        val options = HiHealthOptions.builder().build()
        val hwId = HuaweiIdAuthManager.getExtendedAuthResult(options)
        autoRecorderController = HuaweiHiHealth.getAutoRecorderController(context, hwId)
    }

    fun start() {
        isActive = true
        autoRecorderController
            .startRecord(DataType.DT_CONTINUOUS_STEPS_TOTAL, onSamplePointListener)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    printToLog("Started accessing sensor")
                } else {
                    printToLog("Start failed ${task.exception}")
                }
            }
    }

    fun stop() {
        isActive = false
        autoRecorderController.stopRecord(DataType.DT_CONTINUOUS_STEPS_TOTAL, onSamplePointListener)
            .addOnSuccessListener {
                printToLog("Stopped accessing sensor")
            }
            .addOnFailureListener {
                printToLog("Failed to stop accessing sensor $it")
            }
    }

    companion object {
        const val TAG = "StepsController"
    }
}