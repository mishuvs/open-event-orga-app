package org.fossasia.openevent.app.core.event.list;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.mvp.view.BaseChildFragment;
import org.fossasia.openevent.app.core.event.create.CreateEventActivity;
import org.fossasia.openevent.app.data.Bus;
import org.fossasia.openevent.app.data.ContextUtils;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.databinding.FragmentEventListBinding;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;

import static org.fossasia.openevent.app.core.event.list.EventsPresenter.SORTBYDATE;
import static org.fossasia.openevent.app.core.event.list.EventsPresenter.SORTBYNAME;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link EventListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class EventListFragment extends BaseChildFragment<EventsPresenter> implements EventsView {
    @Inject
    ContextUtils utilModel;

    @Inject
    Bus bus;

    @Inject
    Lazy<EventsPresenter> presenterProvider;

    private FragmentEventListBinding binding;
    private RecyclerView recyclerView;

    private EventsListAdapter eventListAdapter;

    private List<Event> events = new ArrayList<>();

    private Context context;
    private boolean initialized;
    private boolean editMode;
    private long id;

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
        editMode = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_list, container, false);

        return binding.getRoot();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_events, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItemSort = menu.findItem(R.id.sort);
        MenuItem menuItemEdit = menu.findItem(R.id.edit);
        menuItemSort.setVisible(!editMode);
        menuItemEdit.setVisible(editMode);
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
            case R.id.edit:
                Intent intent = new Intent(getActivity(), CreateEventActivity.class);
                intent.putExtra(CreateEventActivity.EVENT_ID, id);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortEvents(int sortBy) {
        getPresenter().sortBy(sortBy);
        eventListAdapter.setSortByName(sortBy == SORTBYNAME);
        binding.setVariable(BR.events, events);
        binding.executePendingBindings();
        eventListAdapter.notifyDataSetChanged();
    }

    @Override
    public Lazy<EventsPresenter> getPresenterProvider() {
        return presenterProvider;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupRecyclerView();
        getPresenter().attach(this);
        binding.setEvents(events);
        getPresenter().start();

        initialized = true;

        binding.createEventFab.setOnClickListener(view -> openCreateEventFragment());
    }

    public void openCreateEventFragment() {
        Intent intent = new Intent(getActivity(), CreateEventActivity.class);
        startActivity(intent);
    }

    private void setupRecyclerView() {
        if (!initialized) {
            eventListAdapter = new EventsListAdapter(events, bus, getPresenter());
            binding.bottomNavigation.setSelectedItemId(R.id.action_live);

            recyclerView = binding.eventRecyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(eventListAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    @Override
    public void changeToEditMode(long id) {
        editMode = true;
        this.id = id;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void changeToNormalMode() {
        editMode = false;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void setEventsList(List<Event> events) {
        this.events = events;
        getPresenter().setEvents(events);
        binding.setVariable(BR.events, events);
        binding.executePendingBindings();
        eventListAdapter.notifyDataSetChanged();
    }
}
