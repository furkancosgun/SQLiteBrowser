import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.sqlitebrowser.Domain.UseCase.ValidationResult
import com.example.sqlitebrowser.Model.TableContent
import java.util.Locale

class DBHelper(context: Context, databaseName: String) :
    SQLiteOpenHelper(context, databaseName, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun executeSQL(sqlCode: String): TableContent {
        val result = TableContent()
        try {
            val db: SQLiteDatabase
            if (isModificationQuery(sqlCode)) {
                db = writableDatabase
                db.execSQL(sqlCode)
                result.columns.add("OK")
                result.cells.add(" ")
            } else {
                db = readableDatabase
                val cursor = db.rawQuery(sqlCode, null)
                cursor?.let {
                    while (cursor.moveToNext()) {
                        for (i in 0 until cursor.columnCount) {
                            result.cells.add(cursor.getString(i))
                        }
                    }
                    result.columns = cursor.columnNames.toMutableList()
                    cursor.close()
                }
            }
            db.close()
        } catch (_: Exception) {
        }
        return result
    }

    private fun isModificationQuery(sqlCode: String): Boolean {
        val modifiedKeywords = arrayOf("INSERT", "UPDATE", "DELETE")
        val uppercaseSqlCode = sqlCode.uppercase(Locale.getDefault()).trim()
        for (keyword in modifiedKeywords) {
            if (uppercaseSqlCode.startsWith(keyword)) {
                return true
            }
        }
        return false
    }

    fun validateQuery(sqlCode: String): ValidationResult {
        val result = ValidationResult(true)
        try {
            val db = readableDatabase
            db.rawQuery(sqlCode, null)
            db.close()
        } catch (e: Exception) {
            result.success = false
            result.errorMessage = e.localizedMessage
        }
        return result
    }
}
