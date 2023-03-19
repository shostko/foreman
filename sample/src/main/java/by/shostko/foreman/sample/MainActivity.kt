package by.shostko.foreman.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.shostko.foreman.Report
import by.shostko.foreman.Worker
import by.shostko.foreman.sample.ui.theme.SampleTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleWorkersScreen(viewModel)
        }
    }
}

@Composable
private fun SampleWorkersScreen(viewModel: MainViewModel) {
    SampleTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(text = "Worker #1:")
                SimpleWorkerRepresentation(viewModel.awesomeUnit)

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Worker #2:")
                SimpleWorkerRepresentation(viewModel.awesomeValue)

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Worker #3:")
                SimpleWorkerRepresentation(viewModel.parametrizedValue)
                Button(onClick = viewModel::onSomeEvent) {
                    Text(text = "Update param for #3")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Worker #4:")
                SimpleWorkerRepresentation(viewModel.loremValues)

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Worker #5:")
                SimpleWorkerRepresentation(viewModel.neverSucceed)

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Default Combined Workers 2,3,4,5:")
                SimpleWorkerRepresentation(viewModel.combinedDefault2345)

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Default Combined Workers 2,3,4:")
                SimpleWorkerRepresentation(viewModel.combinedDefault234)

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Default Combined Workers 4,3,2:")
                SimpleWorkerRepresentation(viewModel.combinedDefault432)

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Custom Combined Workers 2,3,4:")
                SimpleWorkerRepresentation(viewModel.combinedCustom)
            }
        }
    }
}

@Composable
private fun SimpleWorkerRepresentation(worker: Worker<*, *>) {
    Text(text = "TAG: ${worker.tag}")
    val state by worker.reportFlow.collectAsState()
    Box(
        modifier = Modifier
            .background(color = Color.LightGray.copy(alpha = 0.4F))
            .padding(4.dp),
    ) {
        when (val report = state) {
            is Report.Initial -> Text(text = "Pending")
            is Report.Working -> Text(text = "Working")
            is Report.Success -> Text(text = report.result.toString())
            is Report.Failed -> Text(text = "Failed: ${report.error}")
        }
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFF000000)
private fun SampleWorkersScreenPreview() {
    SampleWorkersScreen(MainViewModel(MainRepository()))
}
