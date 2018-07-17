package org.fossasia.openevent.app.core.settings.restriction;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.mvp.view.BaseFragment;
import org.fossasia.openevent.app.core.main.MainActivity;
import org.fossasia.openevent.app.data.ticket.Ticket;
import org.fossasia.openevent.app.databinding.TicketSettingsFragmentBinding;
import org.fossasia.openevent.app.ui.ViewUtils;

import javax.inject.Inject;

public class CheckInRestrictions extends BaseFragment implements CheckInRestrictionView {

    private Context context;
    private long eventId;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private TicketSettingsViewModel ticketSettingsViewModel;

    private CheckInRestrAdapter ticketsAdapter;
    private TicketSettingsFragmentBinding binding;
    private SwipeRefreshLayout refreshLayout;

    public static CheckInRestrictions newInstance(long eventId) {
        CheckInRestrictions fragment = new CheckInRestrictions();
        Bundle args = new Bundle();
        args.putLong(MainActivity.EVENT_KEY, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();
        if (getArguments() != null)
            eventId = getArguments().getLong(MainActivity.EVENT_KEY);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupRecyclerView();
        setupRefreshListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.ticket_settings_fragment, container, false);

        ticketSettingsViewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(TicketSettingsViewModel.class);
        ticketSettingsViewModel.setEventId(eventId);

        ticketSettingsViewModel.getProgress().observe(this, this::showProgress);
        ticketSettingsViewModel.getError().observe(this, this::showError);
        ticketSettingsViewModel.getTickets().observe(this, (newTickets) -> {
            ticketsAdapter.setTickets(newTickets);
            checkRestrictAll();
        });
        ticketSettingsViewModel.getTicketUpdatedAction().observe(this, (aVoid) -> {
            checkRestrictAll();
        });

        ticketSettingsViewModel.loadTickets();

        binding.restrictAll.setOnClickListener(v -> {
            restrictAll(!binding.restrictAllCheckbox.isChecked());
        });
        binding.restrictAllCheckbox.setOnClickListener(v -> {
            //checkbox already checked
            restrictAll(binding.restrictAllCheckbox.isChecked());
        });

        return binding.getRoot();
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis") // Inevitable DD anomaly
    private void checkRestrictAll() {
        boolean restrictAll = true;

        for (Ticket ticket : ticketSettingsViewModel.getTickets().getValue()) {
            if (ticket.isCheckinRestricted == null || !ticket.isCheckinRestricted) {
                restrictAll = false;
                break;
            }
        }

        binding.restrictAllCheckbox.setChecked(restrictAll);
    }

    private void restrictAll(boolean toRestrict) {
        binding.restrictAllCheckbox.setChecked(toRestrict);
        ticketSettingsViewModel.updateAllTickets(toRestrict);
        ticketsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewUtils.setTitle(this, getString(R.string.tickets));
    }

    @Override
    protected int getTitle() {
        return R.string.check_in_restrictions;
    }

    private void setupRecyclerView() {
        ticketsAdapter = new CheckInRestrAdapter(ticketSettingsViewModel.getTickets().getValue(), ticketSettingsViewModel);

        RecyclerView recyclerView = binding.ticketsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(ticketsAdapter);
    }

    private void setupRefreshListener() {
        refreshLayout = binding.swipeContainer;
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            ticketSettingsViewModel.loadTickets();
        });
    }

    @Override
    public void showError(String error) {
        ViewUtils.showSnackbar(binding.getRoot(), error);
    }

    @Override
    public void showProgress(boolean show) {
        ViewUtils.showView(binding.progressBar, show);
    }

}
