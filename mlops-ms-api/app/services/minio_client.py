from minio import Minio
from minio.error import S3Error


class MinioStorage:
    def __init__(self):
        self.client = Minio(
            endpoint="minio:9000",
            access_key="minioadmin",
            secret_key="minioadmin",
            secure=False,
        )

    def upload_file(self, bucket_name: str, object_name: str, file_path: str):
        return self.client.fput_object(bucket_name, object_name, file_path)
