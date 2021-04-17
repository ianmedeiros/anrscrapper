package data

data class ANRLine(
    val errorClassLine: String, //Represents only the class + line count. Ex: MyFooClass:32
    val errorLine: String //Represents the full line with the source of the ANR. Ex: at android.database.sqlite.SQLiteCursor.close (SQLiteCursor.java:205)
)