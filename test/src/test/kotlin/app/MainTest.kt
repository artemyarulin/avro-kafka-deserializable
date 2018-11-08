package app

import example.one.Company
import example.two.User
import example.one.Department
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
    fun desirialisationCompany() {
        val company = Company("Example", Department.IT)
        val data = company.serialize("nop", company)
        val company2 = company.deserialize("nop", data)
        Assert.assertEquals(company, company2)
    }

    @Test
    fun desirialisationUser() {
        val user = User("John")
        val data = user.serialize("nop", user)
        val user2 = user.deserialize("nop", data)
        Assert.assertEquals(user, user2)
    }

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

        val item = Company("Example", Department.IT)
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
        Assert.assertEquals(item, received.first())
    }
}
