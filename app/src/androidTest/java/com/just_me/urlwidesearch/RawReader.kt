package com.just_me.urlwidesearch

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.InputStream

object RawReader {

  fun readRawFile(context: Context, resourceId: Int): String {
	 val inputStream = context.resources.openRawResource(resourceId);
	 val byteStream = ByteArrayOutputStream();
	 try {
		val buffer = ByteArray(inputStream.available())
		inputStream.read(buffer);
		byteStream.write(buffer);
		byteStream.close();
		inputStream.close();
	 } catch (ignored: Exception) {
	 }
	 return byteStream.toString();
  }
}