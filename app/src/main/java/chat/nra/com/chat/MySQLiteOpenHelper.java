package chat.nra.com.chat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String sql = "create table beacon2 (" +
                    "id integer primary key autoincrement, " +
                    "name text, " +
                    "state text, " +
                    "time text);";
        db.execSQL(sql);
    }

    @Override
    public  void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String sql = "drop table if exists beacon2";
        db.execSQL(sql);

        onCreate(db);
    }
}
