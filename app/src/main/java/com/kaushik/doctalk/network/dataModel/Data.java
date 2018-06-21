package com.kaushik.doctalk.network.dataModel;

import java.util.List;

public class Data {

    final List<User> list;
    int pageNumber = 1;

    public Data(List<User> list, int pageCount) {
        this.list = list;
        this.pageNumber = pageCount;
    }

    public List<User> getList() {
        return list;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
