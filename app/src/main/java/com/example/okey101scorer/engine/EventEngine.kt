package com.example.okey101scorer.engine

import kotlin.random.Random

/**
 * Masa etkinliklerinin tetiklenmesini ve zarların atılmasını yöneten merkezi motor.
 */
object EventEngine {

    /**
     * Yeni bir tur eklendiğinde çağrılır. Verilen zar algoritması ve gaddarlık filtresi
     * kullanılarak bir TableEvent döner.
     *
     * @param scoreDifference İki takım arasındaki mevcut toplam puan farkı.
     * @return Tetiklenen olay (TableEvent).
     */
    fun rollForNextRoundEvent(scoreDifference: Int): TableEvent {
        // 1. Aşama: %85 ihtimalle Event tetikleme kontrolü.
        // Oyunun suyunu çıkarmamak için eventler çok nadir olmalı.
        val initialRoll = Random.nextInt(1, 101) // 1..100
        if (initialRoll <= 85) {
            return TableEvent.NONE
        }

        // 2. Aşama: Event havuzu ve ağırlıkların ayarlanması.
        // Sadece gerçekleşebilecek eventleri (NONE hariç) içeren havuz:
        val eventPool = TableEvent.entries.filter { it != TableEvent.NONE }.toMutableList()
        
        // Dinamik ağırlıkları tutacağımız bir harita (Map)
        val dynamicWeights = eventPool.associateWith { it.weight }.toMutableMap()

        // 3. Aşama: Gaddarlık Filtresi
        // Eğer puan farkı 350'den azsa, adaleti sarsmamak adına GREAT_SWAP havuzdan çıkarılır
        // ve onun 5 puanlık ağırlığı MYSTERY_BOX'a aktarılır.
        if (scoreDifference < 350) {
            val greatSwapWeight = dynamicWeights[TableEvent.GREAT_SWAP] ?: 5
            dynamicWeights.remove(TableEvent.GREAT_SWAP)
            eventPool.remove(TableEvent.GREAT_SWAP)
            
            val currentMysteryBoxWeight = dynamicWeights[TableEvent.MYSTERY_BOX] ?: 40
            dynamicWeights[TableEvent.MYSTERY_BOX] = currentMysteryBoxWeight + greatSwapWeight
        }

        // 4. Aşama: Ağırlıklı Rastgele Seçim (Weighted Random)
        val totalWeight = dynamicWeights.values.sum()
        var randomWeight = Random.nextInt(0, totalWeight) // 0..totalWeight - 1

        for (event in eventPool) {
            val weight = dynamicWeights[event] ?: continue
            if (randomWeight < weight) {
                return event
            }
            randomWeight -= weight
        }

        // Güvenlik (Fallback) dönüşü
        return TableEvent.NONE
    }
}
