from pydantic import BaseModel
from enum import Enum

########################################################################
#
# Pydantic schemas for request/response
#
########################################################################


class Status(Enum):
    DONE = "DONE"


class GenericStatusResponse(BaseModel):
    message: str
    status: Status


#
# Miscellaneous
#
########################################################################


class PingResponse(BaseModel):
    message: str
