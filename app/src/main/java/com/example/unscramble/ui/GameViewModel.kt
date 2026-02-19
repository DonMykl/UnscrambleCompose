package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    //Stateflow is a data holder observables flow that emits the current and new state updates
    private val _uiState = MutableStateFlow(GameUiState())
    var userGuess by mutableStateOf("")
        private set
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
// The asStateFLow makes the state flow a read-only state flow
    private lateinit var currentWord: String
    //Set of words used in the game
    private var usedWords: MutableSet<String> = mutableSetOf()
    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()
        if(usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        }else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }

    }
    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        //Shuffle the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }
    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }
    init {
        resetGame()
    }
    fun checkUserGuess() {
        if(userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        }else {
            //User's guess is wrong show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
                //The copy() method allows us to copy an object allowing us to alter some of it's properties while keeping the rest unchanged
            }
        }
        //Reset user guess
        updateUserGuess("")
    }
    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }
    fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            //Last round of the game, update isGameOver to true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score =updatedScore,
                    isGameOver = true
                )
            }
        } else {
            //Normal round of the game
        _uiState.update { currentState ->
            currentState.copy(
                isGuessedWordWrong = false,
                currentScrambledWord = pickRandomWordAndShuffle(),
                score = updatedScore,
                currentWordCount = currentState.currentWordCount.inc(),
            )
           }
        }
    }
    fun skipWord() {
        updateGameState(_uiState.value.score)
        //Reset user guess
        updateUserGuess("")
    }
}