package com.dairy.demo.model;

/**
 * Classifies what a Farmer supplies. This is deliberately a separate enum from
 * {@link MilkEntry.MilkType} (which is always exactly COW or BUFFALO for a given
 * milk entry) because a farmer can supply BOTH — in that case each individual
 * MilkEntry still records a single concrete milk type, chosen at entry time.
 */
public enum AnimalType {
    COW, BUFFALO, BOTH
}
