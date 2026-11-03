package com.example.ui.insights

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Expense
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ThinkingConfig
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "AiInsightsViewModel"

sealed interface AiInsightsUiState {
    data object Idle : AiInsightsUiState
    data object Loading : AiInsightsUiState
    data class Streaming(val text: String) : AiInsightsUiState
    data class Success(val advice: String) : AiInsightsUiState
    data class Error(val message: String) : AiInsightsUiState
}

class AiInsightsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AiInsightsUiState>(AiInsightsUiState.Idle)
    val uiState: StateFlow<AiInsightsUiState> = _uiState.asStateFlow()

    // High thinking model configuration using gemini-4-argon with ThinkingLevel.HIGH
    private val model by lazy {
        val config = generationConfig {
            thinkingConfig = ThinkingConfig.Builder()
                .setThinkingLevel(ThinkingLevel.HIGH)
                .build()
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-4-argon",
            generationConfig = config
        )
    }

    fun generateSmartExpenseAudit(expenses: List<Expense>, monthlyLimit: Double) {
        if (expenses.isEmpty()) {
            _uiState.value = AiInsightsUiState.Success(
                "এখনো কোনো খরচ রেকর্ড করা হয়নি। কয়েকটি খরচ রেকর্ড করার পর এআই স্মার্ট অ্যানালাইসিস শুরু করতে পারবেন।"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = AiInsightsUiState.Loading
            val accumulated = StringBuilder()

            try {
                val expenseSummary = expenses.take(30).joinToString("\n") {
                    "- ${it.expenseDate}: ৳${it.amount} on '${it.title}' (খাত: ${it.categoryName}, দোকান: ${it.shopName.ifBlank { "N/A" }}, মাধ্যম: ${it.paymentMethod})"
                }

                val prompt = """
                    You are a financial advisor expert for the TakaTrack Bangladeshi expense tracking application.
                    Analyze the following real expense data recorded by the user:
                    Monthly Spending Limit: ৳$monthlyLimit BDT.
                    Recorded Expenses:
                    $expenseSummary

                    Provide a deep, constructive analysis in Bengali (বাংলা) covering:
                    ১. ব্যয়ের মূল ক্ষেত্র এবং অস্বাভাবিক/অপ্রয়োজনীয় খরচের সম্ভাবনা (Unnecessary/Unusual Spending Detection)
                    ২. খরচ কমানোর সুনির্দিষ্ট ৩টি বাস্তবসম্মত কৌশল (Actionable Saving Advice)
                    ৩. Spending Limit নিয়ন্ত্রণের সুপারিশ

                    Rules:
                    - Keep it clear, polite, and encouraging.
                    - All amounts in BDT (৳).
                    - Remember this is an Expense-Only tracker (no income or balance concepts).
                """.trimIndent()

                withTimeout(60_000L) {
                    model.generateContentStream(prompt).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        accumulated.append(chunkText)
                        _uiState.value = AiInsightsUiState.Streaming(accumulated.toString())
                    }
                }

                if (accumulated.isNotBlank()) {
                    _uiState.value = AiInsightsUiState.Success(accumulated.toString())
                } else {
                    _uiState.value = AiInsightsUiState.Error("এআই কোনো প্রতিক্রিয়া প্রদান করতে পারেনি। আবার চেষ্টা করুন।")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Generation timed out", e)
                _uiState.value = AiInsightsUiState.Error("বিশ্লেষণ করতে নির্ধারিত সময়ের বেশি সময় লাগছে। পুনরায় চেষ্টা করুন।")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Generation failed", e)
                _uiState.value = AiInsightsUiState.Error(
                    e.localizedMessage ?: "এআই বিশ্লেষণ প্রক্রিয়ায় সমস্যা দেখা দিয়েছে।"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AiInsightsUiState.Idle
    }
}
