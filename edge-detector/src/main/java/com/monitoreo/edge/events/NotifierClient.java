package com.monitoreo.edge.events;

import com.monitoreo.edge.domain.DetectionEvent;

public interface NotifierClient {
    void publish(DetectionEvent event);
}
