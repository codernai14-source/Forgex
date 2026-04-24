package com.atguigu.e_book;

public class TestBook {
    public static void main(String[] args) {
        Book book1 =new Book();
        book1.stock=10;
        book1.bookName="西游记";
        book1.price=10;
        book1.author="吴承恩";
        book1.showInfo();
        book1.sale(3);
        book1.showInfo();
        Book book2 =book1;
        book2.sale(2);
        book1.showInfo();
        new Book().showInfo();
        printBookInfo(new Book());
    }
    public static void printBookInfo(Book book){

        book.showInfo();
    }
}
