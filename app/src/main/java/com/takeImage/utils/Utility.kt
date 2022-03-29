package com.takeImage.utils

import android.content.Context
import android.os.Environment
import com.takeImage.utils.Constants.FOLDER_PATH
import com.takeImage.utils.Constants.NAME
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object Utility {

    fun createDirectory(context: Context) : File {

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/" + FOLDER_PATH + "/" + NAME)
        if (!file.isDirectory){
            file.mkdirs()
        }
        return file
    }

    fun copyStream(inputStream : InputStream?, outputStream: OutputStream) {
        val buffer = ByteArray(2084)
        var bytesRead : Int
        while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        inputStream?.close()
        outputStream.close()
    }

}