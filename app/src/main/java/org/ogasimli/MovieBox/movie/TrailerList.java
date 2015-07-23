package org.ogasimli.MovieBox.movie;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Trailer object class
 * Created by ogasimli on 23.07.2015.
 */
public class TrailerList {

    public ArrayList<Trailer> results;

    public static class Trailer implements Parcelable {

        @SerializedName("key")
        public String key;

        @SerializedName("name")
        public String name;

        @SerializedName("size")
        public String size;

        public Trailer (){}

        private Trailer (Parcel in){
            key = in.readString();
            name = in.readString();
            size = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(key);
            dest.writeString(name);
            dest.writeString(size);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
            @Override
            public Trailer createFromParcel(Parcel in) {
                return new Trailer(in);
            }

            @Override
            public Trailer[] newArray(int size) {
                return new Trailer[size];
            }
        };

        /**
         * Helper method to build youtube link.
         */
        public String getYoutubeLink() {
            return "https://www.youtube.com/watch?v=" + this.key;
        }
    }
}
