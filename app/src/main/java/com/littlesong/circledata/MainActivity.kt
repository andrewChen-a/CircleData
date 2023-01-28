package com.littlesong.circledata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.set_progress).setOnClickListener {
            val view = findViewById<CircleProcessView>(R.id.progress_view)
            if (view.currentValue.isEmpty() || view.currentValue.equals("0"))
                view.setValue("60", 100f)
            else {
                view.setValue("0", 100f)

            }
        }
    }
}