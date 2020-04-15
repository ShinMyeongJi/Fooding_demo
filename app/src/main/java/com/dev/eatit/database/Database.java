package com.dev.eatit.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.dev.eatit.model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper{

    private static final String DB_NAME = "FoodingDB.db";
    private static final int DB_VER = 1;

    public Database(Context context){
        super(context, DB_NAME, null, DB_VER);
    }

    //cart 정보 get
    public List<Order> getCart(){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ID", "ProductId", "ProductName", "Quantity", "Price", "Discount", "Image"};
        String sqlTable = "OrderDetail";

        queryBuilder.setTables(sqlTable);
        Cursor c = queryBuilder.query(db, sqlSelect, null, null, null, null, null);

        final List<Order> result = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                result.add(
                        new Order(
                        c.getInt(c.getColumnIndex("ID")),
                        c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("Quantity")),
                        c.getString(c.getColumnIndex("Price")),
                        c.getString(c.getColumnIndex("Discount")),
                        c.getString(c.getColumnIndex("Image"))
                ));
            }while(c.moveToNext());
        }
        return result;
    }


    //db에 주문 정보 put
    public void addCart(Order order){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO OrderDetail(ProductId, ProductName, Quantity, Price, Discount, Image) VALUES ('%s', '%s', '%s', '%s', '%s', '%s');",
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount(),
                order.getImage()
        );
        db.execSQL(query);
    }

    public void cleanCart(){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail");
        db.execSQL(query);
    }

    //Favorites
    public void addToFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(FoodId) VALUES('%s');", foodId);
        db.execSQL(query);

    }

    public void removeFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE FoodId='%s';", foodId);
        db.execSQL(query);
    }

    public boolean isFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE FoodId='%s';", foodId);
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    public void updateCart(Order order){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity = %s WHERE ID = %d", order.getQuantity(), order.getID());
        db.execSQL(query);
    }


}
