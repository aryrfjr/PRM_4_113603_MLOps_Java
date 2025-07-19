package org.doi.prmv4p113603.mlops.data;

public enum SortDirection {

    ASC,
    DESC;

    public boolean isAsc() {
        return this == ASC;
    }

    public boolean isDesc() {
        return this == DESC;
    }

    public static SortDirection fromString(String value) {
        try {
            return SortDirection.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid sort direction: " + value + ". Use 'asc' or 'desc'.");
        }
    }

}
