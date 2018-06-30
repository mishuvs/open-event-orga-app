package org.fossasia.openevent.app.core.event.list;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.databinding.library.baseAdapters.BR;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.mvp.view.BaseFragment;
import org.fossasia.openevent.app.core.event.create.CreateEventActivity;
import org.fossasia.openevent.app.data.Bus;
import org.fossasia.openevent.app.data.ContextUtils;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.databinding.FragmentEventListBinding;
import org.fossasia.openevent.app.ui.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import static org.fossasia.openevent.app.core.event.list.EventsViewModel.SORTBYDATE;
import static org.fossasia.openevent.app.core.event.list.EventsViewModel.SORTBYNAME;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link EventListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class EventListFragment extends BaseFragment implements EventsView {
    @Inject
    ContextUtils utilModel;

    @Inject
    Bus bus;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private EventsViewModel eventsViewModel;

    private FragmentEventListBinding binding;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;

    private EventsListAdapter eventListAdapter;
    private RecyclerView.AdapterDataObserver adapterDataObserver;

    private Context context;
    private boolean initialized;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p>
     * parameters can be added in future if required so
     * which can be passed in bundle.
     *
     * @return A new instance of fragment EventListFragment.
     */
    public static EventListFragment newInstance() {
        return new EventListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_list, container, false);

        eventsViewModel = ViewModelProviders.of(this, viewModelFactory).get(EventsViewModel.class);

        binding.tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        eventListAdapter.getFilter().filter("live");
                        break;
                    case 1:
                        eventListAdapter.getFilter().filter("past");
                        break;
                    case 2:
                        eventListAdapter.getFilter().filter("draft");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //do nothing
            }
        });


        return binding.getRoot();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_events, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sortByEventName:
                sortEvents(SORTBYNAME);
                return true;
            case R.id.sortByEventDate:
                sortEvents(SORTBYDATE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortEvents(int sortBy) {
        eventsViewModel.sortBy(sortBy);
        eventListAdapter.setSortByName(sortBy == SORTBYNAME);
        binding.setVariable(BR.events, eventsViewModel.getEvents());
        binding.executePendingBindings();
        eventListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupRecyclerView();
        setupRefreshListener();
        eventsViewModel.loadUserEvents(false);
        binding.setEvents(eventsViewModel.getEvents().getValue());

        initialized = true;

        binding.createEventFab.setOnClickListener(view -> openCreateEventFragment());

        eventsViewModel.getProgress().observe(this, this::showProgress);
        eventsViewModel.getError().observe(this, this::showError);
        eventsViewModel.getSuccess().observe(this, this::onRefreshComplete);
        eventsViewModel.getEvents().observe(this, (events) -> {
            eventListAdapter.updateList(events);

            binding.tab.getTabAt(
                binding.tab.getSelectedTabPosition()
            ).select();

            if (events.isEmpty())
                this.showEmptyView(true);
            else
                this.showEmptyView(false);
        });
    }

    public void openCreateEventFragment() {
        Intent intent = new Intent(getActivity(), CreateEventActivity.class);
        startActivity(intent);
    }

    @Override
    protected int getTitle() {
        return R.string.events;
    }

    @Override
    public void onStop() {
        super.onStop();
        refreshLayout.setOnRefreshListener(null);
        eventListAdapter.unregisterAdapterDataObserver(adapterDataObserver);
    }

    private void setupRecyclerView() {
        if (!initialized) {
            eventListAdapter = new EventsListAdapter(eventsViewModel.getEvents().getValue(), bus, this);
            eventListAdapter.getFilter().filter("live");

            recyclerView = binding.eventRecyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(eventListAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            StickyRecyclerHeadersDecoration decoration = new StickyRecyclerHeadersDecoration(eventListAdapter);
            recyclerView.addItemDecoration(decoration);

            adapterDataObserver = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    decoration.invalidateHeaders();
                }
            };
        }
        eventListAdapter.registerAdapterDataObserver(adapterDataObserver);
    }

    private void setupRefreshListener() {
        refreshLayout = binding.swipeContainer;
        refreshLayout.setColorSchemeColors(utilModel.getResourceColor(R.color.color_accent));
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            eventsViewModel.loadUserEvents(true);
        });
    }

    @Override
    public void showProgress(boolean show) {
        ViewUtils.showView(binding.progressBar, show);
    }

    @Override
    public void onRefreshComplete(boolean success) {
        if (success) {
            ViewUtils.showSnackbar(recyclerView, R.string.refresh_complete);
        }
    }

    @Override
    public void showResults(List<Event> events) {
        binding.setVariable(BR.events, events);
        binding.executePendingBindings();
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
    public void openSalesSummary(Long eventId) {
        DialogFragment fragment = SalesSummaryFragment.newInstance(eventId);
        fragment.show(getFragmentManager(), "summary");
    }

    @Override
    public void closeSalesSummary() {
        DialogFragment fragment = ((DialogFragment) getFragmentManager().findFragmentByTag("summary"));
        if (fragment != null)
            fragment.dismiss();
    }
}
