package org.ogasimli.MovieBox.movie;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Review object class
 * Created by ogasimli on 23.07.2015.
 */
public class ReviewList {

    public ArrayList<Review> results;

    public static class Review implements Parcelable {

        @SerializedName("author")
        public String author;

        @SerializedName("content")
        public String content;

        public Review() {
        }

        private Review(Parcel in) {
            author = in.readString();
            content = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(author);
            dest.writeString(content);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Review> CREATOR = new Creator<Review>() {
            @Override
            public Review createFromParcel(Parcel in) {
                return new Review(in);
            }

            @Override
            public Review[] newArray(int size) {
                return new Review[size];
            }
        };
    }
}
