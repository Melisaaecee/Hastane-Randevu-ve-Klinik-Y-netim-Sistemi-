package com.hospital.management.Event;

import com.hospital.management.Entity.Penalty;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PenaltyEvent extends ApplicationEvent {
    private final Penalty penalty;

    public PenaltyEvent(Object source, Penalty penalty) {
        super(source);
        this.penalty = penalty;
    }
}