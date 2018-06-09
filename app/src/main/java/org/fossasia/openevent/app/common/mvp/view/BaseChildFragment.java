package org.fossasia.openevent.app.common.mvp.view;

import android.support.v4.app.Fragment;

import org.fossasia.openevent.app.OrgaApplication;
import org.fossasia.openevent.app.common.mvp.presenter.BasePresenter;

import dagger.Lazy;

public abstract class BaseChildFragment<P extends BasePresenter> extends Fragment {

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    protected Lazy<P> getPresenterProvider() {
        return null;
    }

    @SuppressWarnings("PMD.NullAssignment")
    public P getPresenter() {
        Lazy<P> provider = getPresenterProvider();
        return (provider == null) ? null : provider.get();
    }

    @Override
    public void onStop() {
        super.onStop();
        P presenter = getPresenter();
        if (presenter != null)
            presenter.detach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OrgaApplication.getRefWatcher(getActivity()).watch(this);
    }
}
