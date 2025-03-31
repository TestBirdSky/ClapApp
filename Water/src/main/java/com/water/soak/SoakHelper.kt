package com.water.soak

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.SecretKeySpec

/**
 * Date：2025/3/24
 * Describe:
 */
object SoakHelper {
    private var mSoakK by LakeStore(def = "ClapWater109232#")

    private val ALGORITHM = "AES"
    private val TRANSFORMATION = "AES/ECB/PKCS5Padding"

    /**
     * 解密文件
     * @param inputFile 输入文件路径（加密文件）
     * @param outputFile 输出文件路径（解密后文件）
     * @param key 密钥的字节数组
     * @throws Exception 异常
     */
    @JvmStatic
    @Throws(Exception::class)
    fun decryptFile(inputStream: InputStream, outputFile: String, key: ByteArray) {
        val secretKeySpec = SecretKeySpec(key, ALGORITHM)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        BufferedInputStream(inputStream).use { fis ->
            CipherInputStream(fis, cipher).use { cis ->
                FileOutputStream(outputFile).use { fos ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while ((cis.read(buffer).also { bytesRead = it }) != -1) {
                        fos.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }

    private fun getStr(context: Context, id: Int): InputStream {
        return context.resources.openRawResource(id)
    }

//    private fun getIS(context: Context, assetName: String): InputStream {
//        return when (assetName) {
//            "ic_water" -> {
//                return getStr(context, R.drawable.ic_water)
//            }
//            "demo" -> {
//                return getStr(context, R.drawable.ic_ice_snow)
//            }
//            "black_tras_t" -> {
//                return getStr(context, R.drawable.black_tras_3)
//            }
//
//            else -> {
//                return getStr(context, R.drawable.ice6_dra_white)
//            }
//        }
//    }

    suspend fun handSoakInfo(context: Context, assetName: String, num: Int = 5): Boolean {
        return withContext(Dispatchers.IO) {
            val fileNameS = "${context.dataDir}/Cache/soak.so"
            File("${context.dataDir}/Cache").mkdirs()
            val soFile = File(fileNameS)
            try {
                if (soFile.exists()) {
                    if (soFile.length() < 5000000) {
                        soFile.delete()
                    }
                }
                if (!soFile.exists()) {
                    soFile.createNewFile()
                    val inputStream: InputStream =
                        BufferedInputStream(context.assets.open(assetName))
                    // 3. 解密文件（示例使用简单的XOR解密，需替换实际算法）
                    decryptFile(inputStream, fileNameS, mSoakK.toByteArray())
                }
                // 5. 加载so库 这里可以加入解密后的md5文件校验
                TideHelper.log("size--> ${soFile.length()}")
                if (soFile.length() < 5000000) {
                    soFile.delete()
                    delay(3000)
                    if (num > 0) {
                        return@withContext handSoakInfo(context, assetName, num - 1)
                    }
                } else {
                    soFile.setReadOnly()
                    System.load(fileNameS)
                    delay(500)
                    File(fileNameS).delete()
                    return@withContext true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            false
        }

    }

}