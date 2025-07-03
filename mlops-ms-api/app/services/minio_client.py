from minio import Minio
from minio.error import S3Error
import os


class MinioStorage:

    def __init__(self):
        self.client = Minio(
            endpoint="minio:9000",
            access_key="minioadmin",
            secret_key="minioadmin",
            secure=False,
        )

    def upload_file(self, bucket_name: str, object_name: str, file_path: str):

        if not self.client.bucket_exists(bucket_name):
            self.client.make_bucket(bucket_name)

        return self.client.fput_object(bucket_name, object_name, file_path)

    def upload_directory(
        self, local_dir: str, bucket_name: str, minio_prefix: str
    ) -> int:
        """
        Recursively uploads all files under `local_dir` to MinIO at `bucket_name/minio_prefix`.
        Returns the number of files uploaded.
        """

        if not self.client.bucket_exists(bucket_name):
            self.client.make_bucket(bucket_name)

        file_count = 0
        for root, dirs, files in os.walk(local_dir):
            for filename in files:
                local_path = os.path.join(root, filename)
                relative_path = os.path.relpath(local_path, local_dir)
                object_name = f"{minio_prefix}/{relative_path}".replace("\\", "/")

                print(f"Uploading {local_path} to {bucket_name}/{object_name}")
                self.client.fput_object(bucket_name, object_name, local_path)
                file_count += 1

        return file_count
