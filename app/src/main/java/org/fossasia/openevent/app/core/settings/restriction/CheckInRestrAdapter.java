package org.fossasia.openevent.app.core.settings.restriction;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.data.ticket.Ticket;
import org.fossasia.openevent.app.databinding.TicketSettingLayoutBinding;

import java.util.List;

public class CheckInRestrAdapter extends RecyclerView.Adapter<CheckInRestrAdapter.TicketViewHolder> {

    private List<Ticket> tickets;
    private final TicketSettingsViewModel viewModel;

    public CheckInRestrAdapter(List<Ticket> tickets, TicketSettingsViewModel viewModel) {
        this.tickets = tickets;
        this.viewModel = viewModel;
    }

    @Override
    public TicketViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new TicketViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                R.layout.ticket_setting_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(TicketViewHolder ticketViewHolder, int position) {
        ticketViewHolder.bind(tickets.get(position));
    }

    @Override
    public int getItemCount() {
        return tickets == null ? 0 : tickets.size();
    }

    public void setTickets(List<Ticket> newTickets) {
        tickets = newTickets;
        notifyDataSetChanged();
    }

    class TicketViewHolder extends RecyclerView.ViewHolder {
        private final TicketSettingLayoutBinding binding;

        TicketViewHolder(TicketSettingLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Ticket ticket) {
            binding.setTicket(ticket);
            View.OnClickListener listener = v -> {
                ticket.isCheckinRestricted = ticket.isCheckinRestricted == null || !ticket.isCheckinRestricted;
                binding.ticketCheckbox.setChecked(ticket.isCheckinRestricted);
                viewModel.updateTicket(ticket);
                binding.executePendingBindings();
            };
            itemView.setOnClickListener(listener);
            binding.ticketCheckbox.setOnClickListener(listener);
        }
    }

}

