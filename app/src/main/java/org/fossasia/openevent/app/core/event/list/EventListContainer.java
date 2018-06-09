package org.fossasia.openevent.app.core.event.list;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.databinding.library.baseAdapters.BR;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.mvp.view.BaseFragment;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.databinding.FragmentEventListContainerBinding;
import org.fossasia.openevent.app.ui.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventListContainer extends BaseFragment<EventListContainerPresenter> implements EventListContainerView {

    private static String[] tabTitle = new String[]{"live", "past", "draft"};

    FragmentEventListContainerBinding binding;
    FragmentPagerAdapter pagerAdapter;
    EventListFragment listFragment;

    @Inject
    Lazy<EventListContainerPresenter> presenterProvider;

    public static EventListContainer newInstance() {
        return new EventListContainer();
    }

    @Override
    public Lazy<EventListContainerPresenter> getPresenterProvider() {
        return presenterProvider;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_list_container, container, false);

        listFragment = EventListFragment.newInstance();

        pagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                listFragment.setEventsList(getPresenter().getEventsCategory(tabTitle[position]));
                return listFragment;
            }

            @Override
            public int getCount() {
                return tabTitle.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return super.getPageTitle(position);
            }
        };

        binding.pager.setAdapter(pagerAdapter);
        binding.tabs.setupWithViewPager(binding.pager);

        return binding.getRoot();
    }

    @Override
    public void showProgress(boolean show) {
        ViewUtils.showView(binding.progressBar, show);
    }

    @Override
    public void onRefreshComplete(boolean success) {
        if (success) {
            ViewUtils.showSnackbar(binding.pager, R.string.refresh_complete);
            listFragment.getPresenter().resetToDefaultState();
        }
    }

    @Override
    public void showResults(List<Event> events) {
        binding.pager.setCurrentItem(0);
    }

    @Override
    public void showEmptyView(boolean show) {
        ViewUtils.showView(binding.emptyView, show);
    }

    @Override
    public void showError(String error) {
        ViewUtils.showSnackbar(binding.getRoot(), error);
    }


    @Override
    protected int getTitle() {
        return R.string.events;
    }
}
