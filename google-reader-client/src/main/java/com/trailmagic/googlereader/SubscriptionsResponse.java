package com.trailmagic.googlereader;

import java.util.List;

/**
 * Created by: oliver on Date: Dec 11, 2009 Time: 1:00:07 AM
 */
public class SubscriptionsResponse {
    List<Subscription> subscriptions;

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
