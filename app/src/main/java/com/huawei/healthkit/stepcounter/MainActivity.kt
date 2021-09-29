package com.huawei.healthkit.stepcounter
import android.os.Bundle
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.hihealth.data.Scopes
import com.huawei.hms.support.api.entity.auth.Scope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.text.DateFormat.getTimeInstance
import java.util.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var healthKitAuthorizer: HealthKitAuthorizer
    private lateinit var stepsController: StepsDataController
    private var logStrings = ArrayDeque(List(15){ "" })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorize()
        displaySteps()
    }

    private fun authorize() {
        requestAuthorizationBtn.setOnClickListener {
            healthKitAuthorizer = HealthKitAuthorizer(
                scopes = listOf(
                    Scope(Scopes.HEALTHKIT_STEP_READ),
                    Scope(Scopes.HEALTHKIT_STEP_WRITE)
                    // add permissions you need
                ),
                printToLog = { message ->
                    printToLogView(HealthKitAuthorizer.TAG, message)
                }
            )
            if(!healthKitAuthorizer.permissionsGranted) {
                healthKitAuthorizer.requestAuth(this)
            } else {
                enableStartButton()
            }
        }
    }

    private fun displaySteps() {
        stepsCountText.text = "0"
        stepsController = StepsDataController(
            context = this,
            printToLog = { message ->
                printToLogView(StepsController.TAG, message)
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
        CoroutineScope(Main).launch {
            Log.d(tag, message)
            val time = getTimeInstance().format(Date())
            logStrings.addLast("$time: $message")
            logStrings.removeFirst()
            log_view.text = logStrings.joinToString(separator = "\n")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        healthKitAuthorizer.apply {
            onPermissionRequestResult(
                data = data,
                onSuccess = {
                    if(permissionsGranted) {
                        enableStartButton()
                    }
                    else {
                        printToLog("Missing some permissions")
                    }
                },
                onFailure = { result ->
                    printToLog("Authorization error result: $result")
                }
            )
        }
    }

    private fun enableStartButton() {
        startWalkButton.isEnabled = true
        requestAuthorizationBtn.isEnabled = false
        requestAuthorizationBtn.text = getString(R.string.authorization_success_text)
    }
}