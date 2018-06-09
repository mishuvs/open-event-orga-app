package org.fossasia.openevent.app.core.event.list;

import org.fossasia.openevent.app.common.mvp.view.Emptiable;
import org.fossasia.openevent.app.data.event.Event;

import java.util.List;

public interface EventsView {

    void changeToEditMode(long id);
    void changeToNormalMode();
    void setEventsList(List<Event> events);
}
