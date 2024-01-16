package com.dna.beyoureyes

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window

class CustomDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 바 숨김
        setContentView(R.layout.activity_alert_dialog_intake)

    }
}
