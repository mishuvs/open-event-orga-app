package org.fossasia.openevent.app.core.settings.restriction;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import org.fossasia.openevent.app.common.livedata.SingleEventLiveData;
import org.fossasia.openevent.app.common.rx.Logger;
import org.fossasia.openevent.app.data.ticket.Ticket;
import org.fossasia.openevent.app.data.ticket.TicketRepository;
import org.fossasia.openevent.app.utils.ErrorUtils;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class TicketSettingsViewModel extends ViewModel {

    private final MutableLiveData<List<Ticket>> tickets = new MutableLiveData<>();
    private final TicketRepository ticketRepository;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<Boolean> progress = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final SingleEventLiveData<Void> ticketUpdatedAction = new SingleEventLiveData<>();

    private long eventId;

    @Inject
    public TicketSettingsViewModel(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public void loadTickets() {

        compositeDisposable.add(ticketRepository.getTickets(eventId, true)
            .toSortedList()
            .doOnSubscribe(disposable -> progress.setValue(true))
            .doFinally(() -> progress.setValue(false))
            .subscribe(newTickets -> tickets.setValue(newTickets),
                throwable -> error.setValue(ErrorUtils.getMessage(throwable).toString())));
    }

    public void updateTicket(Ticket ticket) {

        compositeDisposable.add(
            ticketRepository
                .updateTicket(ticket)
                .doOnSubscribe(disposable -> progress.setValue(true))
                .doFinally(() -> progress.setValue(false))
                .subscribe(updatedTicket -> {
                    ticketUpdatedAction.call();
                }, throwable -> error.setValue(ErrorUtils.getMessage(throwable).toString())));
    }

    public void updateAllTickets(boolean toRestrict) {

        compositeDisposable.add(
            Observable.fromIterable(tickets.getValue())
                .doOnSubscribe(disposable -> progress.setValue(true))
                .doFinally(() -> progress.setValue(false))
                .subscribe(ticket -> {
                    ticket.isCheckinRestricted = toRestrict;
                    updateTicket(ticket);
                }, Logger::logError)
        );
    }

    public MutableLiveData<List<Ticket>> getTickets() {
        return tickets;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getProgress() {
        return progress;
    }

    public SingleEventLiveData<Void> getTicketUpdatedAction() {
        return ticketUpdatedAction;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
}
