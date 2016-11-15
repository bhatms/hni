package org.hni.events.service;

/**
 * Created by walmart on 11/14/16.
 * Handles events coming in to HNI.
 * For now these events will take the shape of SMS messages.
 * ### These are strings for now, but eventually should be Event POJOs
 */
public interface EventService {

    public void handleEvent(Event event);
}
