package me.dio.copa.catar.features

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dio.copa.catar.core.BaseViewModel
import me.dio.copa.catar.domain.model.MatchDomain
import me.dio.copa.catar.domain.usecase.DisableNotificationUseCase
import me.dio.copa.catar.domain.usecase.EnableNotificationUseCase
import me.dio.copa.catar.domain.usecase.GetMatchesUseCase
import me.dio.copa.catar.remote.NotFoundException
import me.dio.copa.catar.remote.UnexpectedException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMatchesUseCase: GetMatchesUseCase,
    private val disableNotification: DisableNotificationUseCase,
    private val enableNotification: EnableNotificationUseCase
) : BaseViewModel<StateUIMain, ActionUIMain>(StateUIMain()) {
    //Iniciando a função assim que a classe é instanciada
    init {
        fetchMatches()
    }
    private fun fetchMatches() = viewModelScope.launch {
        getMatchesUseCase()
            .flowOn(Dispatchers.Main)
            .catch {
                when (it) {
                    is NotFoundException -> sendAction(ActionUIMain.MatchesNotFound(it.message ?: "No message Error "))
                    is UnexpectedException -> {ActionUIMain.Unexpexted}
                }
            }.collect {listMatches ->
                setState {
                    copy(listMatches)
                }
            }
    }
    fun toggleNotification(match: MatchDomain){
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Main) {
                    val action = if (match.notificationEnabled) {
                        disableNotification(match.id)
                        ActionUIMain.DisableNotification(match)
                    } else {
                        enableNotification(match.id)
                        ActionUIMain.EnableNotification(match)
                    }
                    sendAction(action)
                }
            }
        }
    }
}

//Aqui eu estou criando uma data class que tem uma lista que receber o estado de lista vazia;
data class StateUIMain(
    val matches: List<MatchDomain> = emptyList()
)

sealed interface ActionUIMain {
    data class MatchesNotFound(val message: String) : ActionUIMain
    object Unexpexted : ActionUIMain
    data class EnableNotification(val match: MatchDomain) : ActionUIMain
    data class DisableNotification(val match: MatchDomain) : ActionUIMain
}