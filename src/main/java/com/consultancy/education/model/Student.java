package com.consultancy.education.model;

import com.consultancy.education.enums.Gender;
import com.consultancy.education.enums.GraduationLevel;
import com.consultancy.education.enums.ActiveStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "students")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_level")
    GraduationLevel graduationLevel;

    @Column(name = "date_of_birth")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status")
    ActiveStatus profileActiveStatus;

    @Column(name = "alternate_phone_number")
    String alternatePhoneNumber;

    @Column(name = "profile_completion", nullable = false, columnDefinition = "INT DEFAULT 0")
    Integer profileCompletion;

    @Column(name = "aadhaar_card_file")
    String aadhaarCardFile;

    @Column(name = "passport_file")
    String passportFile;

    @Column(name = "pan_card_file")
    String panCardFile;

    @Column(name = "birth_certificate_file")
    String birthCertificateFile;

    @JoinColumn
    @OneToOne
    Seo seo;

    @JoinColumn
    @OneToOne
    User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Address> addresses;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    List<StudentEducation> studentEducations;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    List<AbroadExam> abroadExams;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Certification> certifications;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Project> projects;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    Finance finance;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    Wishlist wishlist;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    AdmissionDocument admissionDocument;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    List<StudentCollegeCourseRegistration> studentCollegeCourseRegistrations;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    List<StudentEventRegistration> studentEventRegistrations;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Communication> communications;

}
