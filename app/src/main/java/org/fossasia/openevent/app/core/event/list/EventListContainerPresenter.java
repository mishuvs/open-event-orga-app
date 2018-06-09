package org.fossasia.openevent.app.core.event.list;

import org.fossasia.openevent.app.common.mvp.presenter.AbstractBasePresenter;
import org.fossasia.openevent.app.common.rx.Logger;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.data.event.EventRepository;
import org.fossasia.openevent.app.utils.service.DateService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import io.reactivex.Observable;
import timber.log.Timber;

import static org.fossasia.openevent.app.common.rx.ViewTransformers.dispose;
import static org.fossasia.openevent.app.common.rx.ViewTransformers.emptiable;
import static org.fossasia.openevent.app.common.rx.ViewTransformers.progressiveErroneousRefresh;

public class EventListContainerPresenter extends AbstractBasePresenter<EventListContainerView> {

    private final List<Event> events = new ArrayList<>();
    private final EventRepository eventsDataRepository;

    @Inject
    public EventListContainerPresenter(EventRepository eventsDataRepository) {
        this.eventsDataRepository = eventsDataRepository;
    }

    @Override
    public void start() {
        loadUserEvents(false);
    }

    public void loadUserEvents(boolean forceReload) {
        if (getView() == null)
            return;

        getEventSource(forceReload)
            .compose(dispose(getDisposable()))
            .compose(progressiveErroneousRefresh(getView(), forceReload))
            .toSortedList()
            .compose(emptiable(getView(), events))
            .subscribe(something -> {
                Logger.logSuccess(something);
            }, Logger::logError);
    }

    public List<Event> getEventsCategory(String constraint) {
        List<Event> selectedEvents = new ArrayList();
        for (Event event : events) {
            try {
                String category = DateService.getEventStatus(event);
                if (constraint.equalsIgnoreCase(event.getState()))
                    selectedEvents.add(event);
                else if (constraint.equalsIgnoreCase(category))
                    selectedEvents.add(event);
                else
                    selectedEvents.add(event);
            } catch (ParseException e) {
                Timber.e(e);
            }
        }
        return selectedEvents;
    }

    private Observable<Event> getEventSource(boolean forceReload) {
        if (!forceReload && !events.isEmpty() && isRotated())
            return Observable.fromIterable(events);
        else
            return eventsDataRepository.getEvents(forceReload);
    }
}
