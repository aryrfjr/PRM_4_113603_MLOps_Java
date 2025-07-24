from kafka import KafkaProducer
from airflow.exceptions import AirflowFailException
import json

########################################################################
#
# Helpers for Kafka messaging.
#
########################################################################


def send_kafka_message(message):

    try:
        json.dumps(message)
    except TypeError as e:
        raise AirflowFailException(f"Kafka message is not JSON serializable: {e}")

    producer = KafkaProducer(
        bootstrap_servers="kafka:9092",
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
    )

    producer.send(  # NOTE: see application-properties of service mlops-api
        "airflow-events", message
    )

    producer.flush()
