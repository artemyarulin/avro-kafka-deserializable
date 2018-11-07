package app

import example.Company
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.utils.Time
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils
import org.apache.kafka.test.TestUtils
import org.junit.Assert
import org.junit.Test

class MainTest {
    @Test
    fun integration() {
        val kafka = {
            EmbeddedKafkaCluster(1).apply {
                start(); createTopics(
                    "from",
                    "to"
            )
            }
        }()

        val item = Company("Example")
        IntegrationTestUtils.produceValuesSynchronously(
                "from",
                listOf(item),
                TestUtils.producerConfig(
                        kafka.bootstrapServers(),
                        StringSerializer().javaClass,
                        Company::class.java
                ), Time.SYSTEM
        )
        createStream(kafka.bootstrapServers()).start()
        val received = IntegrationTestUtils.waitUntilMinValuesRecordsReceived<Company>(
                TestUtils.consumerConfig(
                        kafka.bootstrapServers(),
                        StringDeserializer::class.java,
                        Company::class.java
                ), "to", 1
        )
        Assert.assertEquals(1, received.size)
        println("RESULT ${received.first()}")
        Assert.assertEquals(item, received.first())
    }
}