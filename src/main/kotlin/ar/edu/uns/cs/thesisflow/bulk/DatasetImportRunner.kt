package ar.edu.uns.cs.thesisflow.bulk

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.dataset.import", name = ["enabled"], havingValue = "true")
class DatasetImportRunner(
    private val datasetImporter: LegacyDatasetImporter,
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(DatasetImportRunner::class.java)

    override fun run(args: ApplicationArguments?) {
        logger.info("Legacy dataset import enabled - starting import process")
        datasetImporter.import()
        logger.info("Legacy dataset import completed")
    }
}
