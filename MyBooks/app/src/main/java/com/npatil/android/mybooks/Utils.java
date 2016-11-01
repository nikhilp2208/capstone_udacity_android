package com.npatil.android.mybooks;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by nikhil.p on 01/11/16.
 */

public class Utils {

    public static Book JsonToBook(String json) {
        Gson gson = new Gson();
        HashMap<String,BookData>bookDataMap = gson.fromJson(json, new TypeToken<HashMap<String,BookData>>(){}.getType());
        return getBookFromBookDataMap(bookDataMap);
    }

    private static Book getBookFromBookDataMap(HashMap<String, BookData> bookDataMap) {
        if(bookDataMap == null) return null;
        Set<String> keys = bookDataMap.keySet();
        if (keys.size() == 0) return null;

        String isbnKey = keys.iterator().next();
        String isbn = isbnKey.substring(isbnKey.indexOf(':') + 1);
        BookData bookData = bookDataMap.get(isbnKey);
        BookData.BookDetails details = bookData.getDetails();

        if (details == null) return null;

        Book book = new Book();
        book.setTitle(details.getTitle());
        book.setSubtitle(details.getSubtitle());
        book.setDescription(details.getDescription());
        if (isbn.length() == 10){
            book.setIsbn10(isbn);
            book.setIsbn13(details.getIsbn_13()!=null?details.getIsbn_13().get(0):null);
        } else {
            book.setIsbn13(isbn);
            book.setIsbn10(details.getIsbn_10()!=null?details.getIsbn_10().get(0):null);
        }
        return book;
    }
}
