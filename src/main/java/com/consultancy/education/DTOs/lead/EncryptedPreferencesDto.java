package com.consultancy.education.DTOs.lead;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * This DTO represents the structure of preferences that will be encrypted by
 * the frontend
 * and sent as a JSON string in the 'encryptedPreferences' field.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EncryptedPreferencesDto {
    String preferredCollege;
    String additionalNotes;
}
