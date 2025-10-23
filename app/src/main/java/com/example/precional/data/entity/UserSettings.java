package com.example.precional.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_settings")
public class UserSettings {
    @PrimaryKey
    private int id = 1; // Solo habrá un registro de configuración

    private String userName;
    private int userAge;
    private String userGender;
    private boolean morningReminder;
    private boolean eveningReminder;
    private String morningTime; // HH:mm format
    private String eveningTime; // HH:mm format
    private boolean hasHypertension;
    private boolean hasDiabetes;
    private boolean isDarkModeEnabled;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactEmail; // Email del contacto de emergencia para envío de reportes
    private String n8nWebhookUrl; // URL del webhook de n8n
    private String aiApiKey;
    private boolean aiEnabled;

    // Constructor
    public UserSettings() {
        // Valores por defecto
        this.userName = "Usuario";
        this.userAge = 30;
        this.userGender = "other";
        this.morningReminder = false;
        this.eveningReminder = false;
        this.morningTime = "08:00";
        this.eveningTime = "20:00";
        this.hasHypertension = false;
        this.hasDiabetes = false;
        this.isDarkModeEnabled = false;
        this.emergencyContactName = "";
        this.emergencyContactPhone = "";
        this.emergencyContactEmail = "";
        this.n8nWebhookUrl = "https://primary-production-38135.up.railway.app/webhook-test/precion-arterial";
        this.aiApiKey = "";
        this.aiEnabled = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getUserAge() { return userAge; }
    public void setUserAge(int userAge) { this.userAge = userAge; }

    public String getUserGender() { return userGender; }
    public void setUserGender(String userGender) { this.userGender = userGender; }

    public boolean isMorningReminder() { return morningReminder; }
    public void setMorningReminder(boolean morningReminder) { this.morningReminder = morningReminder; }

    public boolean isEveningReminder() { return eveningReminder; }
    public void setEveningReminder(boolean eveningReminder) { this.eveningReminder = eveningReminder; }

    public String getMorningTime() { return morningTime; }
    public void setMorningTime(String morningTime) { this.morningTime = morningTime; }

    public String getEveningTime() { return eveningTime; }
    public void setEveningTime(String eveningTime) { this.eveningTime = eveningTime; }

    public boolean isHasHypertension() { return hasHypertension; }
    public void setHasHypertension(boolean hasHypertension) { this.hasHypertension = hasHypertension; }

    public boolean isHasDiabetes() { return hasDiabetes; }
    public void setHasDiabetes(boolean hasDiabetes) { this.hasDiabetes = hasDiabetes; }

    public boolean isDarkModeEnabled() { return isDarkModeEnabled; }
    public void setDarkModeEnabled(boolean darkModeEnabled) { this.isDarkModeEnabled = darkModeEnabled; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getEmergencyContactEmail() { return emergencyContactEmail; }
    public void setEmergencyContactEmail(String emergencyContactEmail) { this.emergencyContactEmail = emergencyContactEmail; }

    public String getN8nWebhookUrl() { return n8nWebhookUrl; }
    public void setN8nWebhookUrl(String n8nWebhookUrl) { this.n8nWebhookUrl = n8nWebhookUrl; }

    public String getAiApiKey() { return aiApiKey; }
    public void setAiApiKey(String aiApiKey) { this.aiApiKey = aiApiKey; }

    public boolean isAiEnabled() { return aiEnabled; }
    public void setAiEnabled(boolean aiEnabled) { this.aiEnabled = aiEnabled; }
}
