package com.example.okey101scorer.engine

/**
 * Gaddarlık seviyesini belirten Enum.
 * UI tarafında verilecek tepkilerin (renk, animasyon, titreşim) şiddetini belirler.
 */
enum class EventSeverity {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    ULTRA
}

/**
 * Masa etkinliklerinin listesi. Her eventin bir ağırlığı (weight) ve gaddarlık seviyesi (severity) vardır.
 * Ağırlık, eventin rastgele tetiklenme havuzunda kapladığı alanı ifade eder.
 */
enum class TableEvent(val weight: Int, val severity: EventSeverity) {
    /**
     * %85 ihtimalle hiçbir şey olmaz, oyun normal devam eder.
     */
    NONE(weight = 85, severity = EventSeverity.NONE),

    /**
     * Rastgele bir takıma bonus veya ceza.
     */
    MYSTERY_BOX(weight = 40, severity = EventSeverity.LOW),

    /**
     * Bu elde girilen tüm eksi/artı puanlar 2 ile çarpılır.
     */
    KAOS_ELI(weight = 30, severity = EventSeverity.MEDIUM),

    /**
     * Yancıların kaderini belirleyeceği çark açılır.
     */
    YANCI_IHTILALI(weight = 15, severity = EventSeverity.HIGH),

    /**
     * Kaybeden takıma (veya eksi yiyen takıma) risk teklifi: x2 mi, 0 mı?
     */
    CIFTE_KUMAR(weight = 10, severity = EventSeverity.HIGH),

    /**
     * ISTAKALARI DEĞİŞTİRİN! Bomba efektiyle yerler değişir.
     */
    GREAT_SWAP(weight = 5, severity = EventSeverity.ULTRA)
}
