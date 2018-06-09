package org.fossasia.openevent.app.core.event.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.fossasia.openevent.app.common.ContextManager;
import org.fossasia.openevent.app.common.Pipe;
import org.fossasia.openevent.app.data.Bus;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.databinding.EventLayoutBinding;

import java.util.List;

class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.EventRecyclerViewHolder> {

    private final List<Event> events;

    private final Bus bus;
    private final EventsPresenter eventsPresenter;
    private boolean sortByName;

    EventsListAdapter(List<Event> events, Bus bus, EventsPresenter eventsPresenter) {
        this.events = events;
        this.bus = bus;
        this.eventsPresenter = eventsPresenter;
    }

        @Override
    public EventRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        EventLayoutBinding binding = EventLayoutBinding.inflate(layoutInflater, parent, false);
        EventRecyclerViewHolder eventRecyclerViewHolder = new EventRecyclerViewHolder(binding);

        eventRecyclerViewHolder.onItemLongClick(eventsPresenter::toolbarEditMode);
        eventRecyclerViewHolder.onItemClick(eventsPresenter::resetToDefaultState);
        return eventRecyclerViewHolder;
    }

    @Override
    public void onBindViewHolder(final EventRecyclerViewHolder holder, int position) {
        final Event thisEvent = events.get(position);
        holder.bind(thisEvent);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setSortByName(boolean sortBy) {
        sortByName = sortBy;
    }

    //view holder class
    class EventRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final EventLayoutBinding binding;
        private Event event;
        private final long selectedEventId;
        private Pipe<Event> longClickAction;
        private Runnable onClick;

        EventRecyclerViewHolder(EventLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding
                .getRoot()
                .setOnClickListener(view -> {
                    if (eventsPresenter.isEditMode())
                        onClick.run();
                    else
                        bus.pushSelectedEvent(event);
                });

            binding
                .getRoot()
                .setOnLongClickListener(view -> {
                    if (longClickAction != null) {
                        longClickAction.push(event);
                    }
                    return true;
                });

            final Event selectedEvent = ContextManager.getSelectedEvent();
            selectedEventId = selectedEvent == null ? -1 : selectedEvent.getId();
        }

        public void bind(Event event) {
            this.event = event;
            binding.setEvent(event);
            binding.setSelectedEventId(selectedEventId);
            binding.executePendingBindings();
            binding.setEventsPresenter(eventsPresenter);
        }

        public void onItemLongClick(Pipe<Event> longClickAction) {
            this.longClickAction = longClickAction;
        }

        public void onItemClick(Runnable onClick) {
            this.onClick = onClick;
        }
    }
}
