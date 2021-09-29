package com.huawei.healthkit.stepcounter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.SettingController
import com.huawei.hms.hihealth.data.Scopes
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.text.DateFormat.getTimeInstance
import java.util.*
import com.huawei.hms.hihealth.result.HealthKitAuthResult

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var settingController: SettingController
    private lateinit var stepsController: StepsController
    private var logStrings = ArrayDeque(List(15){ "" })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorize()
        displaySteps()
    }

    private fun authorize() {
        settingController = HuaweiHiHealth.getSettingController(this)
        requestAuthorizationBtn.setOnClickListener {
            val authorizeIntent = settingController
                .requestAuthorizationIntent(
                    arrayOf(
                        Scopes.HEALTHKIT_STEP_READ,
                        Scopes.HEALTHKIT_STEP_WRITE
                    ),
                    true
                )
            startActivityForResult(
                authorizeIntent,
                REQUEST_AUTH
            )
        }
    }

    private fun displaySteps() {
        stepsCountText.text = "0"
        stepsController = StepsController(
            context = this,
            onStepsChanged = { steps ->
                CoroutineScope(Main).launch {
                    val stepsString = steps.toString()
                    stepsCountText.text = stepsString
                }
            },
            printToLog = { message ->
                CoroutineScope(Main).launch {
                    printToLogView(StepsController.TAG, message)
                }
            }
        )
        startWalkButton.setOnClickListener {
            if (!stepsController.isActive) {
                stepsController.start()
                startWalkButton.text = getString(R.string.stop_button_text)
            } else {
                stepsController.stop()
                startWalkButton.text = getString(R.string.start_button_text)
            }
        }
    }

    private fun printToLogView(tag: String, message: String) {
        Log.d(tag, message)
        val time = getTimeInstance().format(Date())
        logStrings.addLast("$time: $message")
        logStrings.removeFirst()
        log_view.text = logStrings.joinToString(separator = "\n")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_AUTH) {
            // Obtain the authorization response result from the intent.
            val result: HealthKitAuthResult? =
                settingController.parseHealthKitAuthResultFromIntent(data)
            if (result == null) {
                printToLogView(TAG, "authorization fail")
                return
            }
            if (result.isSuccess) {
                printToLogView(TAG, "authorization success")
                enableStartButton()
            } else {
                printToLogView(TAG, "authorization fail, errorCode: $result.errorCode")
            }
        }
    }

    private fun enableStartButton() {
        startWalkButton.isEnabled = true
        requestAuthorizationBtn.isEnabled = false
        requestAuthorizationBtn.text = getString(R.string.authorization_success_text)
    }

    companion object {
        const val TAG = "Authorization"
        const val REQUEST_AUTH = 1002
    }
}