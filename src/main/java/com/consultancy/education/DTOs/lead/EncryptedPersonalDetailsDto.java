package com.consultancy.education.DTOs.lead;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * This DTO represents the structure of personal details that will be encrypted
 * by the frontend
 * and sent as a JSON string in the 'encryptedPersonalDetails' field.
 * 
 * Frontend should:
 * 1. Create this JSON object
 * 2. Encrypt it using AES-256 or similar
 * 3. Send the encrypted string to backend
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EncryptedPersonalDetailsDto {
    String alternatePhoneNumber;
    String dateOfBirth; // Format: dd/MM/yyyy
    String gender; // MALE, FEMALE, OTHER
    String fullAddress;
    String city;
    String state;
    String pincode;
}
