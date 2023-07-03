import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date

class SQLiteFileManager(private val context: Context, private val databaseUri: Uri) {
    fun generateSqlFile(): String {
        val dbName = getDatabaseNameFromUri(databaseUri)

        try {
            val filePath = context.getDatabasePath(dbName).path
            val outputFile = File(filePath)

            if (outputFile.exists()) {
                return dbName
            }

            val inputStream: InputStream? = context.contentResolver.openInputStream(databaseUri)
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream?.read(buffer).also { bytesRead = it ?: 0 } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            inputStream?.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dbName
    }

    private fun getDatabaseNameFromUri(uri: Uri): String {
        return uri.path?.substring(uri.path?.lastIndexOf("/")!! + 1) ?: ""
    }

    fun downloadDatabase() {
        val dbName = getDatabaseNameFromUri(databaseUri)
        val filePath = context.getDatabasePath(dbName).path
        val outputFile = File(filePath)

        if (!outputFile.exists()) {
            return
        }

        val exportedFile = createExportedFile()

        try {
            outputFile.copyTo(exportedFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createExportedFile(): File {
        val exportedFileName = "exported_${Date().time}.db"
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDirectory, exportedFileName)
    }
}
