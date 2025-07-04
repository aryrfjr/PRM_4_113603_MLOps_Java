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

    #
    # File upload.
    #
    ########################################################################
    def upload_file(self, bucket_name: str, object_name: str, file_path: str):
        if not self.client.bucket_exists(bucket_name):
            self.client.make_bucket(bucket_name)

        return self.client.fput_object(bucket_name, object_name, file_path)

    #
    # Directory upload.
    #
    ########################################################################
    def upload_directory(
        self, local_dir: str, bucket_name: str, minio_prefix: str
    ) -> int:
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

    #
    # Download file.
    #
    ########################################################################
    def download_file(self, bucket_name: str, object_name: str, download_path: str):
        """
        Downloads a single file from MinIO.
        """
        os.makedirs(os.path.dirname(download_path), exist_ok=True)

        print(f"Downloading {bucket_name}/{object_name} to {download_path}")

        self.client.fget_object(bucket_name, object_name, download_path)

    #
    # Download files from a directory.
    #
    ########################################################################
    def download_prefix(
        self, bucket_name: str, minio_prefix: str, local_dir: str
    ) -> int:
        """
        Downloads all files under a given prefix (like a directory) from MinIO into `local_dir`.
        """

        os.makedirs(local_dir, exist_ok=True)

        objects = self.client.list_objects(
            bucket_name, prefix=minio_prefix, recursive=True
        )

        file_count = 0

        for obj in objects:

            rel_path = obj.object_name[len(minio_prefix) :].lstrip("/")

            local_path = os.path.join(local_dir, rel_path)

            os.makedirs(os.path.dirname(local_path), exist_ok=True)

            print(f"Downloading {obj.object_name} to {local_path}")

            self.client.fget_object(bucket_name, obj.object_name, local_path)

            file_count += 1

        return file_count
