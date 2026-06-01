package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.Team
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object PredictionService {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val outcomeAdapter = moshi.adapter(AiPredictionOutcome::class.java)

    suspend fun getAiPrediction(
        homeTeam: Team,
        awayTeam: Team,
        extraContext: String,
        poissonHomeScore: String,
        poissonHomeProb: Double,
        poissonDrawProb: Double,
        poissonAwayProb: Double
    ): AiPredictionOutcome {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key is missing or default. Pastikan Anda mengaturnya di panel Secrets di AI Studio.")
        }

        val prompt = """
            Kamu adalah sistem kecerdasan buatan (Machine Learning) ahli analisis sepak bola dan statistik olahraga.
            Tugasmu adalah menganalisis pertandingan sepak bola antara dua tim berikut dan memprediksi hasil akhir secara akurat.

            TIM KANDANG (HOME):
            - Nama: ${homeTeam.name}
            - Negara: ${homeTeam.country}
            - Rating Serangan (Attack Rating): ${homeTeam.attackRating} (Rata-rata gol yang dicetak per game, basis 1.0)
            - Rating Bertahan (Defense Rating): ${homeTeam.defenseRating} (Rata-rata gol kemasukan per game, basis 1.0, makin rendah makin bagus)
            - Keunggulan Kandang (Home Advantage Factor): ${homeTeam.homeAdvantage}
            - Performa Terakhir (5 match terakhir): ${homeTeam.recentForm} (W = menang, D = seri, L = kalah)

            TIM TANDANG (AWAY):
            - Nama: ${awayTeam.name}
            - Negara: ${awayTeam.country}
            - Rating Serangan: ${awayTeam.attackRating}
            - Rating Bertahan: ${awayTeam.defenseRating}
            - Performa Terakhir (5 match terakhir): ${awayTeam.recentForm}

            ANALISIS STATISTIK POISSON:
            - Prediksi Skor Poisson: $poissonHomeScore
            - Probabilitas Menang Kandang (Poisson): ${(poissonHomeProb * 100).toInt()}%
            - Probabilitas Seri (Poisson): ${(poissonDrawProb * 100).toInt()}%
            - Probabilitas Menang Tandang (Poisson): ${(poissonAwayProb * 100).toInt()}%

            CATATAN EKSTRA SECARA REAL-TIME DARI USER (Konteks tambahan seperti cidera pemain, kepelatihan dsb):
            ${if (extraContext.isNotBlank()) extraContext else "Tidak ada catatan tambahan."}

            Gabungkan algoritma statistik Poisson di atas dengan wawasan taktis sepak bola modern Anda untuk memberikan analisis Machine Learning yang paling komprehensif.

            Harap balas dengan format JSON murni bermutu tinggi berikut. Pastikan JSON valid dan tidak ada teks pengantar atau penutup di luar JSON.
            JSON format:
            {
              "homeWinProbability": 0.XX, (probabilitas kemenangan kandang, nilai antara 0.0 sampai 1.0)
              "drawProbability": 0.XX, (probabilitas seri, nilai antara 0.0 sampai 1.0)
              "awayWinProbability": 0.XX, (probabilitas kemenangan tandang, nilai antara 0.0 sampai 1.0)
              "predictedHomeScore": X, (angka prediktif skor kandang, Integer)
              "predictedAwayScore": Y, (angka prediktif skor tandang, Integer)
              "detailedAnalysis": "Teks analisis mendalam bahasa Indonesia..."
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.4f,
                responseMimeType = "application/json"
            )
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val responseBody = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Tidak ada respon dari model Gemini.")

        return try {
            val cleanJson = sanitizeJson(responseBody)
            outcomeAdapter.fromJson(cleanJson) ?: getFallbackOutcome()
        } catch (e: Exception) {
            Log.e("PredictionService", "Error parsing AI response", e)
            getFallbackOutcome(e.message ?: "Parsing error")
        }
    }

    private fun sanitizeJson(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removeSuffix("```")
            .trim()
    }

    private fun getFallbackOutcome(error: String = ""): AiPredictionOutcome {
        return AiPredictionOutcome(
            homeWinProbability = 0.45,
            drawProbability = 0.30,
            awayWinProbability = 0.25,
            predictedHomeScore = 2,
            predictedAwayScore = 1,
            detailedAnalysis = "Gagal memproses analisis otomatis secara sempurna. Harap periksa koneksi atau kunci API Anda. Detail: $error"
        )
    }
}
