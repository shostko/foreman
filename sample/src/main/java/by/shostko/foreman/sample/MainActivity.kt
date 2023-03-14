@file:OptIn(ExperimentalCoroutinesApi::class)

package by.shostko.foreman.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import by.shostko.foreman.Foreman
import by.shostko.foreman.Worker
import by.shostko.foreman.combineWith
import by.shostko.foreman.sample.ui.theme.SampleTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {

    private val worker1 = Foreman.prepare<String>("W1") {
        delay(2000L)
        "some result of worker #1"
    }

    private val worker2 = Foreman.prepare(
        tag = "W2",
        task = flowOf("1", "2", "3")
            .onEach { delay(4000L) }
            .flatMapConcat {
                flowOf("${it}A", "${it}B", "${it}C")
                    .onEach { delay(1000L) }
            }
    )

    private val worker3 = Foreman.prepare<String>("W3") {
        delay(10000L)
        throw UnsupportedOperationException("Test error for Worker 3")
    }

    private val workerCombined = worker1.combineWith(worker2).combineWith(worker3, tag = "Combined")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        SimpleWorkerRepresentation(worker1)
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleWorkerRepresentation(worker2)
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleWorkerRepresentation(worker3)
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleWorkerRepresentation(workerCombined)
                    }
                }
            }
        }
        worker1.launch(lifecycleScope)
        worker2.launch(lifecycleScope)
        worker3.launch(lifecycleScope)
    }
}

@Composable
private fun SimpleWorkerRepresentation(worker: Worker<*,*>){
    val report by worker.reportFlow.collectAsState()
    Text(text = "${worker.tag}: $report")
}

