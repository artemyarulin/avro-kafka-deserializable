package app

import example.Company
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import java.util.Properties

fun createStream(kafkaServer: String): KafkaStreams {
    val props = Properties().apply {
        put(StreamsConfig.APPLICATION_ID_CONFIG, "example")
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer)
    }

    val builder = StreamsBuilder().apply {
        stream("from", Consumed.with(Serdes.String(), Company()))
            .peek { key, value -> println("Processing $key and $value") }
            .to("to", Produced.with(Serdes.String(), Company()))
    }
    return KafkaStreams(builder.build(), props)
}