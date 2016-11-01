package com.npatil.android.mybooks;

import java.util.List;

/**
 * Created by nikhil.p on 01/11/16.
 */

public class BookData {
    private BookDetails details;

    public BookDetails getDetails() {
        return details;
    }

    public void setDetails(BookDetails details) {
        this.details = details;
    }

    public class BookDetails {
        private String title;
        private String subtitle;
        private String description;
        private List<String> isbn_13;
        private List<String> isbn_10;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getIsbn_13() {
            return isbn_13;
        }

        public void setIsbn_13(List<String> isbn_13) {
            this.isbn_13 = isbn_13;
        }

        public List<String> getIsbn_10() {
            return isbn_10;
        }

        public void setIsbn_10(List<String> isbn_10) {
            this.isbn_10 = isbn_10;
        }
    }
}
