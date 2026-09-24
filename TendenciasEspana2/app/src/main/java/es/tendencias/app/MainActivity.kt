package es.tendencias.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import es.tendencias.app.data.TrendsRepositoryImpl
import es.tendencias.app.ui.TrendsScreen
import es.tendencias.app.ui.TrendsViewModel
import es.tendencias.app.ui.theme.TendenciasTheme

class MainActivity : ComponentActivity() {

    // Repositorio preparado para inyección de dependencias o instanciación directa
    private val trendsRepository = TrendsRepositoryImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TendenciasTheme {
                val viewModel: TrendsViewModel = viewModel {
                    TrendsViewModel(trendsRepository)
                }
                TrendsScreen(viewModel = viewModel)
            }
        }
    }
}
