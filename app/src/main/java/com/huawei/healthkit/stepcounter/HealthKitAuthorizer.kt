package com.huawei.healthkit.stepcounter

import android.app.Activity
import android.content.Intent
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult

class HealthKitAuthorizer(
    private val scopes: List<Scope>,
    val printToLog: (String) -> Unit = { }
) {
    companion object {
        private const val REQUEST_AUTH = 1002
        const val TAG = "HealthKitAuthorizer"
        private var account: AuthHuaweiId? = HuaweiIdAuthManager.getAuthResult()
    }

    fun requestAuth(activity: Activity) {
        val authParamsHelper = HuaweiIdAuthParamsHelper()
        val authParams = authParamsHelper
            .setIdToken()
            .setScopeList(scopes)
            .createParams()
        val huaweiIdAuthService = HuaweiIdAuthManager
            .getService(activity.applicationContext, authParams)
        activity.startActivityForResult(huaweiIdAuthService.signInIntent, REQUEST_AUTH)
    }

    val permissionsGranted: Boolean
        get() = (account != null && HuaweiIdAuthManager.containScopes(account, scopes))

    fun onPermissionRequestResult(
        data: Intent?,
        onSuccess: () -> Unit,
        onFailure: (HuaweiIdAuthResult) -> Unit
    ) {
        val result = HuaweiIdAuthAPIManager
            .HuaweiIdAuthAPIService
            .parseHuaweiIdFromIntent(data)
        if(!result.isSuccess) {
            printToLog("Failed to sign in")
            onFailure(result)
        }
        printToLog("Sign in succeeded")
        account = HuaweiIdAuthAPIManager
                .HuaweiIdAuthAPIService
                .parseHuaweiIdFromIntent(data)
                .huaweiId
        if(account == null) {
            onFailure(result)
            return
        }
        onSuccess()
    }
}