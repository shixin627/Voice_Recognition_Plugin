package com.skaiwalk.voice_recognition.service

import java.util.*

class MyObservable private constructor() : Observable() {
    var textResult = ""
    fun setData(data: String) {
        if (this.textResult != data) {
            //假如資料有變動
            this.textResult = data
            setChanged()
            //設定有改變
        }
        notifyObservers()
        //發送給觀察者
    }

    companion object {
        val instance = MyObservable()
    }
}