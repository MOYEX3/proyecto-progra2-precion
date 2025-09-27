package com.example.precional.service;

import com.example.precional.data.entity.BloodPressureRecord;

import java.util.Random;

public class AIRecommendationService {

    private static final String[] NORMAL_RECOMMENDATIONS = {
        "✅ Presión normal. Mantén tu rutina de ejercicio y alimentación saludable.",
        "✅ Excelentes niveles. Continúa hidratándote bien y durmiendo 7-8 horas.",
        "✅ Tu presión está perfecta. Sigue con frutas, verduras y ejercicio regular.",
        "✅ Niveles ideales. Mantén el estrés controlado con respiración profunda.",
        "✅ Presión en rango óptimo. Continúa con tus hábitos saludables actuales."
    };

    private static final String[] ELEVATED_RECOMMENDATIONS = {
        "⚠️ Presión elevada. Reduce sal, aumenta caminatas de 30 min diarios.",
        "⚠️ Niveles altos. Más verduras, menos procesados. Mantente hidratado.",
        "⚠️ Atención necesaria. Practica meditación 10 min/día, limita cafeína.",
        "⚠️ Presión subiendo. Come plátanos (potasio), evita comida rápida.",
        "⚠️ Requiere cuidado. Duerme mejor, camina más, controla el estrés."
    };

    private static final String[] HIGH_RECOMMENDATIONS = {
        "🚨 Presión alta. Consulta tu médico pronto. Mientras: menos sal, más descanso.",
        "🚨 Niveles preocupantes. Visita al doctor esta semana. Evita alcohol y tabaco.",
        "🚨 Atención urgente. Agenda cita médica. Come ligero, camina suave.",
        "🚨 Presión peligrosa. Ve al médico YA. Relájate, respira profundo, descansa.",
        "🚨 Riesgo alto. Consulta profesional inmediatamente. Hidrátate, evita estrés."
    };

    private static final String[] DASHBOARD_TIPS = {
        "💡 Tip: Mide tu presión a la misma hora cada día para mayor precisión.",
        "📝 Consejo: Lleva registro constante para mejor seguimiento médico.",
        "💪 Recuerda: 30 minutos de caminata diaria mejoran la presión arterial.",
        "🥗 Tip saludable: Más potasio (plátanos, espinacas) ayuda a controlar la presión.",
        "😌 Bienestar: 5 minutos de respiración profunda reducen el estrés diario."
    };

    public static String getRecommendation(BloodPressureRecord record) {
        if (record == null) {
            return getDashboardTip();
        }

        String status = record.getPressureStatus();
        Random random = new Random();

        switch (status) {
            case "normal":
                return NORMAL_RECOMMENDATIONS[random.nextInt(NORMAL_RECOMMENDATIONS.length)];
            case "elevated":
                return ELEVATED_RECOMMENDATIONS[random.nextInt(ELEVATED_RECOMMENDATIONS.length)];
            case "high":
                return HIGH_RECOMMENDATIONS[random.nextInt(HIGH_RECOMMENDATIONS.length)];
            default:
                return getDashboardTip();
        }
    }

    public static String getDashboardTip() {
        Random random = new Random();
        return DASHBOARD_TIPS[random.nextInt(DASHBOARD_TIPS.length)];
    }

    public static String getRecommendationForMultipleReadings(java.util.List<BloodPressureRecord> records) {
        if (records == null || records.isEmpty()) {
            return getDashboardTip();
        }

        // Calcular promedio de las últimas lecturas
        double avgSystolic = records.stream().mapToInt(BloodPressureRecord::getSystolic).average().orElse(0);
        double avgDiastolic = records.stream().mapToInt(BloodPressureRecord::getDiastolic).average().orElse(0);

        if (avgSystolic < 120 && avgDiastolic < 80) {
            return "📊 Promedio semanal excelente. Mantén estos hábitos saludables.";
        } else if (avgSystolic < 140 && avgDiastolic < 90) {
            return "📊 Promedio elevado. Ajusta ejercicio y dieta. Sin medicamentos por cuenta propia.";
        } else {
            return "📊 Promedio alto. Consulta a tu médico para evaluación profesional completa.";
        }
    }

    public static String getShortTipForDashboard(BloodPressureRecord record) {
        if (record == null) {
            return "Registra tu presión diariamente para mejor control de salud.";
        }

        String status = record.getPressureStatus();
        switch (status) {
            case "normal":
                return "Tu presión está perfecta. Sigue así con ejercicio y buena alimentación.";
            case "elevated":
                return "Presión un poco alta. Reduce sal, camina más, controla el estrés.";
            case "high":
                return "Presión elevada. Consulta tu médico pronto. Descansa y aliméntate bien.";
            default:
                return "Mantén un estilo de vida saludable para controlar tu presión arterial.";
        }
    }
}
