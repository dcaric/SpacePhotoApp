package com.paola.spacephotoapp.domain.model;

/**
 * This enum defines the different categories that a news article can belong to.
 * Enums are a special type of class that represent a fixed set of constants.
 * This ensures that news articles can only be assigned to one of these predefined categories,
 * making data consistent and easy to manage.
 */
public enum NewsCategory {
    SPACE,   // Represents news related to general space topics.
    MOON,    // Represents news specifically about the Moon.
    MARS,    // Represents news specifically about Mars.
    ARTEMIS, // Represents news related to the Artemis program.
    UNKNOWN  // Represents news that doesn't fit into any of the predefined categories.
}