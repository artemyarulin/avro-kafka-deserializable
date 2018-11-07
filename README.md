## avro-kafka-deserializable

Generate Java classes from Avro definitions which automatically has implementation of `Serialiser<T>`, `Deserialiser<T>` and `Serde<T>` and use it right away in Kafka and Kafka Streams

## How to use that CLI

Using Docker just run `artemyarulin/avro-kafka-deserializable`, mount your folder with Arvo JSON definitions to `srv` and Java files will be generated there as well

Example:
```bash
docker run -v $PWD/test/schemas:/srv artemyarulin/avro-kafka-deserializable
```

## Hot to use with maven/gradle

Both [maven-avro-plugin](https://issues.apache.org/jira/browse/AVRO-983) and [gradle-avro-plugin](https://github.com/commercehub-oss/gradle-avro-plugin#templatedirectory) supports custom template folder. Just take files from [templates](templates) folder, store it somewhere and point plugin to that

## How does it work

By using custom [Java Avro template](templates/record.vm) + for CLI avro-tools is used with small change https://github.com/apache/avro/pull/366

## Do I need Schema Registry?

Not at all, you can use Avro without Confluent Schema Registry, see [integration tests](test). But Schema Registry brings a lot of benefits, so think about it.

## Template changes

Here is a diff of changes that were done for `record.vm`

```diff
24c24,31
< import org.apache.avro.message.BinaryMessageEncoder;
---
> import org.apache.avro.generic.GenericDatumWriter;
> import org.apache.avro.generic.GenericRecord;
> import org.apache.avro.io.BinaryEncoder;
> import org.apache.avro.io.DatumReader;
> import org.apache.avro.io.DatumWriter;
> import org.apache.avro.io.Decoder;
> import org.apache.avro.io.DecoderFactory;
> import org.apache.avro.io.EncoderFactory;
25a33
> import org.apache.avro.message.BinaryMessageEncoder;
26a35,44
> import org.apache.avro.specific.SpecificData;
> import org.apache.avro.specific.SpecificDatumReader;
> import org.apache.kafka.common.serialization.Deserializer;
> import org.apache.kafka.common.serialization.Serde;
> import org.apache.kafka.common.serialization.Serializer;
> 
> import java.io.ByteArrayOutputStream;
> import java.io.IOException;
> import java.util.Arrays;
> import java.util.Map;
37c55
< public class ${this.mangle($schema.getName())}#if ($schema.isError()) extends org.apache.avro.specific.SpecificExceptionBase#else extends org.apache.avro.specific.SpecificRecordBase#end implements org.apache.avro.specific.SpecificRecord {
---
> public class ${this.mangle($schema.getName())}#if ($schema.isError()) extends org.apache.avro.specific.SpecificExceptionBase#else extends org.apache.avro.specific.SpecificRecordBase#end implements org.apache.avro.specific.SpecificRecord, Deserializer<${this.mangle($schema.getName())}>, Serializer<${this.mangle($schema.getName())}>, Serde<${this.mangle($schema.getName())}> {
43a62,128
>   @Override
>   public void configure(Map<String, ?> configs, boolean isKey) {
> 
>   }
> 
>   @Override
>   public void close() {
> 
>   }
> 
>   @Override
>   public Serializer<${this.mangle($schema.getName())}> serializer() {
>     return this;
>   }
> 
>   @Override
>   public Deserializer<${this.mangle($schema.getName())}> deserializer() {
>     return this;
>   }
> 
>   @Override
>   public byte[] serialize(String topic, ${this.mangle($schema.getName())} data) {
>     try {
>       byte[] result = null;
> 
>       if (data != null) {
> 
>         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
>         BinaryEncoder binaryEncoder =
>                 EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
> 
>         DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(data.getSchema());
>         datumWriter.write(data, binaryEncoder);
> 
>         binaryEncoder.flush();
>         byteArrayOutputStream.close();
> 
>         result = byteArrayOutputStream.toByteArray();
>       }
>       return result;
>     } catch (IOException ex) {
>       throw new IllegalArgumentException(
>               "Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
>     }
>   }
> 
>   @Override
>   public ${this.mangle($schema.getName())} deserialize(String topic, byte[] data) {
>     try {
>       ${this.mangle($schema.getName())} result = null;
> 
>       if (data != null) {
> 
>         DatumReader<GenericRecord> datumReader =
>                 new SpecificDatumReader<>(${this.mangle($schema.getName())}.class.newInstance().getSchema());
>         Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
> 
>         result = (${this.mangle($schema.getName())}) datumReader.read(null, decoder);
>       }
>       return result;
>     } catch (Exception ex) {
>       throw new IllegalArgumentException(
>               "Can't deserialize data '" + Arrays.toString(data) + "' from topic '" + topic + "'", ex);
>     }
>   }
> 
> 
```
