package org.sprint.catalogandprescription_service.messaging;

public final class PharmacyEventRoutingKeys {

    private PharmacyEventRoutingKeys() {
    }

    public static final String EXCHANGE = "pharmacy.events.exchange";

    public static final String ORDER_CREATED = "order.created";
    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String ORDER_STATUS_CHANGED = "order.status.changed";
    public static final String PRESCRIPTION_REVIEWED = "prescription.reviewed";
    public static final String INVENTORY_ADJUSTED = "inventory.adjusted";
}
