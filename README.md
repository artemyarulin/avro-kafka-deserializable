## avro-kafka-deserializable

Generate Java classes from Avro definitions which automatically has implementation of `Serialiser<T>`, `Deserialiser<T>` and `Serde<T>` and use it right away in Kafka and Kafka Streams

## How to use that

Using Docker. Just run `artemyarulin/avro-kafka-deserializable`, mount your folder with Arvo JSON definitions to `srv` and Java files will be generated there as well

Example:
```bash
docker run -v $PWD/test/schemas:/srv artemyarulin/avro-kafka-deserializable
```

## How does it work

By using custom [Java Avro template](templates/record.vm) + avro-tools CLI compile call + https://github.com/apache/avro/pull/366

## Do I need Schema Registry?

Not at all, you can use Avro without Confluent Schema Registry, see [integration tests](test). But Schema Registry brings a lot of benefits, so think about it.
