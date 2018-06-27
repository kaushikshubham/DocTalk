package com.kaushik.doctalk.view;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.kaushik.doctalk.R;
import com.kaushik.doctalk.adapter.OnLoadMoreListener;
import com.kaushik.doctalk.adapter.UsersAdapter;
import com.kaushik.doctalk.network.dataModel.Data;
import com.kaushik.doctalk.view.viewModel.Presenter;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.kaushik.doctalk.utility.Utils.TAG;

public class MainActivity extends AppCompatActivity implements OnLoadMoreListener {

    @BindView(R.id.input_search)
    EditText inputSearch;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private Unbinder unbinder;

    private CompositeDisposable disposable = new CompositeDisposable();
    private UsersAdapter mAdapter;
    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);
        initRecyclerView();

        presenter = ViewModelProviders.of(this).get(Presenter.class);

        DisposableObserver observer = getSearchObserver();
        disposable.add(presenter.getData().subscribeWith(observer));
        disposable.add(getSearchEventObservable(inputSearch));
        disposable.add(observer);

        presenter.onStart();
    }

    private void initRecyclerView() {
        mAdapter = new UsersAdapter(this, recyclerView,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);
    }

    private DisposableObserver<Data> getSearchObserver() {
        return new DisposableObserver<Data>() {
            @Override
            public void onNext(Data data) {
                Log.e(TAG, "onNext: " + data);
                mAdapter.setData(data);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.v(TAG, "onComplete ");
            }
        };
    }

    @Override
    protected void onDestroy() {
        disposable.clear();
        unbinder.unbind();
        mAdapter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLoadMore() {
        Log.v(TAG,"onLoadMore called");
        presenter.fetchMoreData();
    }

    private Disposable getSearchEventObservable(EditText inputSearch) {
        return RxTextView.textChangeEvents(inputSearch)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(presenter.searchContactsTextWatcher());
    }
}
