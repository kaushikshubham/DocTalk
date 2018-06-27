package com.kaushik.doctalk.view.viewModel;


import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;
import com.kaushik.doctalk.network.ApiClient;
import com.kaushik.doctalk.network.BaseApiService;
import com.kaushik.doctalk.network.dataModel.Data;
import com.kaushik.doctalk.network.dataModel.Model;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.kaushik.doctalk.utility.Utils.TAG;

public class Presenter extends ViewModel {
    private BaseApiService apiService;
    private PublishSubject<String> publishSubject = PublishSubject.create();
    private String searchString;
    private int pageNumber = 1;
    private int itemCount = 20;

    Presenter() {
        apiService = ApiClient.getClient().create(BaseApiService.class);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        apiService = null;
    }

    public DisposableObserver<TextViewTextChangeEvent> searchContactsTextWatcher() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                Log.d(TAG, "Search query: " + textViewTextChangeEvent.text());
                searchString = textViewTextChangeEvent.text().toString();
                pageNumber = 1;
                if (!searchString.isEmpty())
                    publishSubject.onNext(searchString);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }

    public Observable<Data> getData() {
        return publishSubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .switchMapSingle(new Function<String, Single<Data>>() {
                    @Override
                    public Single<Data> apply(String s) throws Exception {
                        return apiService.getUsersList(s, pageNumber, itemCount).map(new Function<Model, Data>() {

                            @Override
                            public Data apply(Model model) throws Exception {
                                Log.v(TAG, "Data : " + model);
                                return new Data(model.getUsers(), pageNumber);
                            }
                        })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());
                    }
                });
    }

    public void onStart() {
        pageNumber = 1;
        if (searchString != null && !searchString.isEmpty())
            publishSubject.onNext(searchString);
    }

    public void fetchMoreData() {
        ++pageNumber;
        if (searchString != null && !searchString.isEmpty())
            publishSubject.onNext(searchString);
    }
}
