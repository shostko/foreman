package by.shostko.foreman.sample

import android.app.Application
import android.util.Log
import by.shostko.foreman.EasyLogger
import by.shostko.foreman.Foreman

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Foreman.logger = EasyLogger { Log.d("Foreman", it) }
    }
}