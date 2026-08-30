package com.purplehillsbooks.schema;

import java.math.BigDecimal;

/** JsonRange */
public class JsonRange {

    public BigDecimal maximum;
    public BigDecimal minimum;
    public BigDecimal exclusiveMaximum;
    public BigDecimal exclusiveMinimum;

    public boolean isInRange(BigDecimal bigValue) {
        if (maximum != null && bigValue.compareTo(maximum) > 0) {
            return false;
        }
        if (minimum != null && bigValue.compareTo(minimum) < 0) {
            return false;
        }
        if (exclusiveMaximum != null && bigValue.compareTo(exclusiveMaximum) >= 0) {
            return false;
        }
        if (exclusiveMinimum != null && bigValue.compareTo(exclusiveMinimum) <= 0) {
            return false;
        }
        return true;
    }
}
